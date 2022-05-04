;; Copyright (c) 2020, 2022 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [clojure.walk :as walk]
            [clojure.java.shell :as sh]
            [clojure.set :as set]
            [clojure.string :as string]
            [jsonista.core :as json]
            [utils :as utils]
            [stats :as stats]
            [charts :as charts]
            [rss :as rss]
            [deps :as deps]
            [java-time :as t]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [datalevin.core :as d]
            [clj-yaml.core :as yaml])
  (:gen-class))

;; Set logging

(timbre/set-config!
 {:level     :debug
  :output-fn (partial timbre/default-output-fn {:stacktrace-fonts {}})
  :appenders
  {:println (appenders/println-appender {:stream :auto})
   :spit    (appenders/spit-appender {:fname "log.txt"})}})

;; Configure and connect the db

(def schema
  {:repository_url
   {:db/valueType :db.type/string
    :db/unique    :db.unique/identity}
   :organization_url
   {:db/valueType :db.type/string
    :db/unique    :db.unique/identity}
   :dependency_url
   {:db/valueType :db.type/string
    :db/unique    :db.unique/identity}
   :tag_id
   {:db/valueType :db.type/string
    :db/unique    :db.unique/identity}
   :sill_id
   {:db/valueType :db.type/long
    :db/unique    :db.unique/identity}
   :dep_id
   {:db/valueType :db.type/keyword
    :db/unique    :db.unique/identity}
   :lib_id
   {:db/valueType :db.type/keyword
    :db/unique    :db.unique/identity}})

(def conn (d/get-conn ".db" schema))

(def db (d/db conn))

;;; Feed the database

(defn- update-db [data-url]
  (let [d0   (slurp data-url)
        d1   (->> d0 json/read-value
                  (map walk/keywordize-keys))
        data (map #(utils/replace-vals % nil "") d1)]
    (doseq [d
            ;; Testing
            ;; (take 20 (shuffle data))
            data
            ]
      (try (d/transact! conn [d])
           (catch Exception e (timbre/error (.getMessage e)))))))

(defn- update-repos []
  (update-db (:repos utils/urls)))

(defn- update-orgas []
  (update-db (:orgas utils/urls)))

(defn- update-sill []
  (let [sill (:catalog (utils/get-contents-json-to-kwds (:sill utils/urls)))
        sill (map #(set/rename-keys % {:id :sill_id}) sill)]
    (doseq [s sill]
      (try (d/transact! conn [s])
           (catch Exception e (timbre/error (.getMessage e)))))))

(defn- update-libs []
  (let [libs (utils/get-contents-json-to-kwds (:libs utils/urls))
        ;; Remove possible nil values from source data
        libs (map #(into {} (filter second %)) libs)
        libs (map #(set/rename-keys % {:repository_url :repo_url}) libs)
        libs (map #(assoc % :lib_id
                          (keyword (string/lower-case
                                    (str (:platform %) "/" (:name %))))) libs)]
    (doseq [l libs]
      (try (d/transact! conn [l])
           (catch Exception e (timbre/error (.getMessage e)))))))

;;; Get data

(defn- get-annuaire []
  (apply merge
         (map #(let [{:keys [github lannuaire]} %]
                 {(keyword github) lannuaire})
              (utils/csv-url-to-map (:annuaire utils/urls)))))

(defn- get-id [kw]
  (->> (d/q `[:find ?e :where [?e ~kw _]] db)
       (map first)
       (map #(d/pull db '[*] %))))

(defn- get-repos [] (get-id :repository_url))
(defn- get-deps [] (get-id :dep_id))
(defn- get-orgas [] (get-id :organization_url))
(defn- get-tags [] (get-id :tag_id))
(defn- get-sill [] (get-id :sill_id))
(defn- get-libs [] (get-id :lib_id))

(defn- get-dep [dep_id]
  (when-let [res (ffirst (d/q `[:find ?e :where [?e :dep_id ~dep_id]] db))]
    (d/pull db '[*] res)))

(defn- is-lib [repository_url]
  (-> (d/q `[:find ?e :where
             [?e :lib_id _]
             [?e :repo_url ~repository_url]] db)
      seq nil? not))

(defn- is-esr [repo_orga_name]
  (let [mesri-string
        "Ministère de l'enseignement supérieur, de la recherche et de l'innovation"]
    (->> (d/q `[:find ?e :where
                [?e :organization_url _]
                [?e :name ~repo_orga_name]] db)
         (map first)
         (map #(d/entity db %))
         (filter #(= (:ministry %) mesri-string))
         not-empty nil? not)))

(defn- is-repo [repo_url]
  (-> (d/q `[:find ?e :where [?e :repository_url ~repo_url]] db)
      seq nil? not))

;;; Consolidate data

(defn- consolidate-orgas []
  (let [annuaire (get-annuaire)
        orgas    (map #(update
                        ;; Set empty dates to UNIX epoch
                        % :creation_date
                        (fn [c] (if (not-empty c) c "1970-01-01T00:00:00Z")))
                      (get-orgas))
        orgas-yaml
        (yaml/parse-string (slurp (:sources utils/urls)) :keywords false)]
    (timbre/info "Consolidate organizations data")
    (doseq [o orgas]
      (let [orga-yaml    (first (filter #(string/includes?
                                          (:organization_url o) %)
                                        (keys orgas-yaml)))
            annuaire-url ((keyword (:login o)) annuaire)
            o-y-ministry (last (get (get orgas-yaml orga-yaml) "service_of"))
            o-y-floss    (last (get (get orgas-yaml orga-yaml) "floss_policy"))]
        (try
          (d/transact! conn [(assoc o
                                    :annuaire (or annuaire-url "")
                                    :ministry (or o-y-ministry "")
                                    :floss_policy (or o-y-floss ""))])
          (catch Exception e (timbre/error (.getMessage e))))))))

(defn- consolidate-deps [dependencies repository_url]
  (doseq [{:keys [type library]} (:libraries dependencies)]
    (when-let [valid-library
               (condp = type
                 "npm"      (deps/get-valid-npm library)
                 "bundler"  (deps/get-valid-bundler library)
                 "maven"    (deps/get-valid-maven library)
                 "clojars"  (deps/get-valid-clojars library)
                 "composer" (deps/get-valid-composer library)
                 "pypi"     (deps/get-valid-pypi library)
                 "crate"    (deps/get-valid-crate library)
                 nil)]
      (timbre/info (format "Updating dependency %s (%s)" library type))
      (let [dep_id        (keyword (str type "/" library))
            library-repos (:repositories (get-dep dep_id))]
        (try
          (d/transact!
           conn
           [(merge {:dep_id       dep_id
                    :repositories (conj library-repos repository_url)
                    :updated      (str (t/instant))}
                   (select-keys valid-library [:description :link]))])
          (catch Exception e (timbre/error (.getMessage e))))))))

(defn- consolidate-repos []
  (doseq [repo (get-repos)]
    (let [is_esr       (is-esr (:organization_name repo))
          ;; FIXME: Some values of :is_fork are "" upstream, fix them here
          is_fork      (true? (:is_fork repo))
          reuses       (utils/get-reuses repo)
          contributing (utils/get-contributing repo)
          dependencies (deps/get-dependencies repo)]
      (try
        (d/transact! conn [(assoc repo
                                  :is_esr is_esr
                                  :is_fork is_fork
                                  :reuses reuses
                                  :contributing contributing
                                  :dependencies dependencies)])
        (catch Exception e (timbre/error (.getMessage e))))
      (consolidate-deps dependencies (:repository_url repo)))))

;; |           | GitHub                           | GitLab                |
;; |-----------+----------------------------------+-----------------------|
;; | Name      | name                             | name                  |
;; | Sha       | commit>sha                       | id                    |
;; | Committer | commit=>commit>committer>name    | commit>committer_name |
;; | Date      | commit=>commit>committer>date    | commit>committed_date |
;; | Title     | commit=>commit>committer>message | commit>title          |
;; | Web URL   | commit=>html_url                 | commit>web_url        |
(defn- consolidate-tags []
  (doseq [{:keys [id name organization_name platform repository_url tags topics]
           :as   repo} (get-repos)]
    (when  (and id (utils/needs-updating? (:updated tags)))
      (let  [gh-api-baseurl
             (format "https://api.github.com/repos/%s/%s" organization_name name)
             api-str (if (= platform "GitHub")
                       (str gh-api-baseurl "/tags")
                       (str (re-find #"https?://[^/]+" repository_url)
                            (format "/api/v4/projects/%s/repository/tags" id)))]
        (when-let [tags (utils/get-contents-json-to-kwds api-str)]
          (timbre/info "Updating tags for" repository_url)
          (doseq [t tags]
            (if-not (= platform "GitHub")
              (d/transact! conn [{:tag_id     (:id (:commit t))
                                  :repository repository_url
                                  :topics     (string/split topics #",")
                                  :name       (:name t)
                                  :committer  (:committer_name (:commit t))
                                  :date       (:committed_date (:commit t))
                                  :title      (:title (:commit t))
                                  :url        (:web_url (:commit t))}])
              (let [commit (utils/get-contents-json-to-kwds (:url (:commit t)))]
                (d/transact! conn [{:tag_id     (:sha (:commit t))
                                    :repository repository_url
                                    :topics     (string/split topics #",")
                                    :name       (:name t)
                                    :committer  (:name (:committer (:commit commit)))
                                    :date       (:date (:committer (:commit commit)))
                                    :title      (:message (:commit commit))
                                    :url        (:html_url commit)}]))))
          (d/transact! conn [(assoc repo :tags {:updated (str (t/instant))})]))))))

;;; Prepare data for json generation

(def prepare-repos
  (let [repo-mapping (:repos utils/mappings)]
    (comp
     ;; Add is_lib if repo is also listed in libraries
     (map #(assoc % :is_lib (is-lib (:repository_url %))))
     ;; Add the number of reuses
     (map #(assoc % :reuses (:number (:reuses %))))
     ;; Rename keywords
     (map #(set/rename-keys (select-keys % (keys repo-mapping)) repo-mapping))
     ;; Add the number of dependencies
     (map #(assoc % :dp (count (:libraries (:dependencies %)))))
     ;; Remap licenses
     (map #(assoc % :li (get (:licenses utils/mappings) (:li %))))
     ;; Limit description
     (map #(update
            % :d (fn [d] (if-not d "" (subs d 0 (min (max 0 (dec (count d)))
                                                     utils/max-description-length))))))
     ;; Replace emojis
     (map #(update
            %
            :d
            (fn [d]
              (let [desc (atom d)]
                (doseq [e utils/emojis]
                  (swap! desc (fn [x]
                                (when (string? x)
                                  (string/replace x (:name e) (:char e))))))
                @desc)))))))

(def prepare-deps
  (let [deps-mapping (:deps utils/mappings)]
    (comp
     ;; Set type and name
     (map #(let [id (:dep_id %)]
             (assoc % :type (namespace id) :name (name id))))
     ;; Remove keywords
     (map #(set/rename-keys (select-keys % (keys deps-mapping)) deps-mapping)))))

(def prepare-orgas
  (comp
   ;; Remap keywords
   (map #(set/rename-keys % (:orgas utils/mappings)))
   ;; Remove db/id keyword
   (map #(dissoc % :db/id))
   ;; Only keep organizations with repositories
   (filter #(pos? (:r %)))))

(def prepare-sill
  (let [sill-mapping (:sill utils/mappings)]
    (comp
     ;; Convert timestamps
     (map #(update % :referencedSinceTime (fn [t] (str (t/instant (java.util.Date. t))))))
     ;; Add comptoirDuLibreSoftware id
     (map #(assoc % :comptoirDuLibreSoftwareId (:id (:comptoirDuLibreSoftware %))))
     ;; Remap keywords
     (map #(set/rename-keys (select-keys % (keys sill-mapping)) sill-mapping)))))

(def prepare-libs
  (let [libs-mapping (:libs utils/mappings)]
    (comp
     ;; Add is_repo is the dependency is also published as a fr repo
     (map #(assoc % :is_repo (is-repo (:repo_url %))))
     ;; Remap keywords
     (map #(set/rename-keys (select-keys % (keys libs-mapping)) libs-mapping)))))

;;; Functions to generate the json files

(defn- generate-repos-json [repos]
  (spit "repos.json"
        (json/write-value-as-string
         (sequence prepare-repos repos))))

(defn- generate-orgas-json [orgas]
  (spit "orgas.json"
        (json/write-value-as-string
         (sequence prepare-orgas orgas))))

(defn- generate-deps-json [deps]
  (spit "deps.json"
        (json/write-value-as-string
         (sequence prepare-deps deps))))

(defn- generate-libs-json [libs]
  (spit "libs.json"
        (json/write-value-as-string
         (sequence prepare-libs libs))))

(defn- generate-sill-json [sill]
  (spit "sill.json"
        (json/write-value-as-string
         (sequence prepare-sill sill))))

;;; Main function

(defn -main []
  ;; Initiatize data from upstream resources
  (update-repos)
  (update-orgas)
  (update-sill)
  (update-libs)
  ;; Consolidate data in the local db
  (consolidate-repos)
  (consolidate-orgas)
  (consolidate-tags)
  ;; Prepare data output
  (let [repos (get-repos)
        orgas (get-orgas)
        libs  (get-libs)
        deps  (get-deps)
        tags  (get-tags)
        sill  (get-sill)]
    ;; Generate json output
    (generate-sill-json sill)
    (generate-repos-json repos)
    (generate-orgas-json orgas)
    (generate-deps-json deps)
    (generate-libs-json libs)
    ;; Generate stats
    (stats/generate-stats-json repos orgas libs deps sill)
    ;; Generate RSS feeds
    (rss/latest-repositories repos)
    (rss/latest-organizations orgas)
    (rss/latest-dependencies deps)
    (rss/latest-libraries libs)
    (rss/latest-sill sill)
    (rss/latest-tags tags))
  ;; Spit the top_licences.svg
  (sh/sh "vl2svg" (charts/generate-licenses-chart) "top_licenses.svg")
  (timbre/info "Generated top_licenses.svg")
  (timbre/info "All data were produced!")
  (shutdown-agents))
