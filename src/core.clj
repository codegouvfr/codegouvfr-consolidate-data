;; Copyright (c) 2020-2023 DINUM, Bastien Guerry <bastien.guerry@code.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [clojure.walk :as walk]
            [clojure.java.shell :as sh]
            [clojure.set :as set]
            [clojure.string :as string]
            [clojure.instant :as instant]
            [jsonista.core :as json]
            [utils :as utils]
            [stats :as stats]
            [charts :as charts]
            [rss :as rss]
            [deps :as deps]
            [hut :as hut]
            [java-time :as t]
            [taoensso.timbre :as timbre]
            [taoensso.timbre.appenders.core :as appenders]
            [datalevin.core :as d])
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
  (doseq [d (->> (slurp data-url)
                 json/read-value
                 (map walk/keywordize-keys)
                 (map #(utils/replace-vals % nil "")))]
    (try (d/transact! conn [d])
         (catch Exception e (timbre/error (.getMessage e))))))

(defn- update-repos []
  (update-db (:repos utils/urls)))

(defn- update-orgas []
  (update-db (:orgas utils/urls)))

;; FIXME: See src/hut.clj header: this should really be done upstream
;; by https://git.sr.ht/~codegouvfr/codegouvfr-fetch-data.
(defn- update-hut  []
  (doseq [d (->> (hut/fetch)
                 (map #(utils/replace-vals % nil "")))]
    (try (d/transact! conn [d])
         (catch Exception e (timbre/error (.getMessage e))))))

(defn- get-sill []
  (->> (utils/get-contents-json-to-kwds (:sill utils/urls))
       (map #(set/rename-keys % {:id :sill_id}))))

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
  (try (->> (d/q `[:find ?e :where [?e ~kw]] db)
            (map first)
            (map #(d/pull db '[*] %)))
       (catch Exception e (timbre/error (.getMessage e)))))

(defn- get-repos [] (get-id :repository_url))
(defn- get-deps [] (get-id :dep_id))
(defn- get-orgas [] (get-id :organization_url))
(defn- get-tags [] (get-id :tag_id))
(defn- get-libs [] (get-id :lib_id))

(defn- get-dep [dep_id]
  (when-let [res (ffirst (d/q `[:find ?e :where [?e :dep_id ~dep_id]] db))]
    (try (d/pull db '[*] res)
         (catch Exception e (timbre/error (.getMessage e))))))

(defn- is-lib [repository_url]
  (-> (d/q `[:find ?e :where
             [?e :lib_id]
             [?e :repo_url ~repository_url]] db)
      seq nil? not))

(defn- is-esr [repo_orga_name]
  (let [mesri-string
        "Ministère de l'enseignement supérieur et de la recherche"]
    (->> (d/q `[:find ?e :where
                [?e :organization_url]
                [?e :login ~repo_orga_name]] db)
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
                      (get-orgas))]
    (timbre/info "Consolidate organizations data")
    (doseq [o orgas]
      (let [orga-url     (string/replace (:organization_url o) "/groups/" "/")
            orga-yaml    (->> (keys utils/sources)
                              (drop-while #(not (string/includes? orga-url %)))
                              first)
            o-y-sources  (get utils/sources orga-yaml)
            annuaire-url ((keyword (:login o)) annuaire)
            o-y-ministry (last (get o-y-sources "service_of"))
            o-y-floss    (last (get o-y-sources "floss_policy"))]
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
      (timbre/info (format "Update dependency %s (%s)" library type))
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
    (let [orga_name    (:organization_name repo)
          is_esr       (is-esr orga_name)
          ;; FIXME: Some values of :is_fork are "" upstream, fix them here
          is_fork      (true? (:is_fork repo))
          reuses       (utils/get-reuses repo)
          contributing (utils/get-contributing repo)
          publiccode   (utils/get-publiccode repo)
          dependencies (deps/get-dependencies repo)
          gh-api-baseurl
          (format "https://api.github.com/repos/%s/%s" orga_name (:name repo))
          api-str      (if (= (:platform repo) "GitHub")
                         (str gh-api-baseurl "/stats/contributors")
                         (str (re-find #"https?://[^/]+" (:repository_url repo))
                              (format "/api/v4/projects/%s/repository/contributors" (:id repo))))
          is_empty     (nil? (utils/get-contents api-str))]
      (try
        (d/transact! conn [(assoc repo
                                  :is_esr is_esr
                                  :is_fork is_fork
                                  :is_empty is_empty
                                  :reuses reuses
                                  :publiccode publiccode
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
  (doseq [{:keys [id name organization_name is_archived platform repository_url tags topics]
           :as   repo}
          (filter #(or (:is_contrib? (:contributing %))
                       (:is_publiccode? (:publiccode %))
                       (is-lib (:repository_url %)))
                  (get-repos))]
    (if-not (and id
                 (not is_archived)
                 ;; FIXME: TODO: implement getting tags for SourceHut
                 (some #{"GitHub" "GitLab"} (list platform))
                 (utils/needs-updating? (:updated tags)))
      (timbre/info "Skip updating tags for" repository_url)
      (let  [gh-api-baseurl
             (format "https://api.github.com/repos/%s/%s" organization_name name)
             api-str (if (= platform "GitHub")
                       (str gh-api-baseurl "/tags")
                       (str (re-find #"https?://[^/]+" repository_url)
                            (format "/api/v4/projects/%s/repository/tags" id)))
             tags    (utils/get-contents-json-to-kwds api-str)]
        (timbre/info "Update tags for" repository_url)
        (try
          (doseq [t tags]
            (if-not (= platform "GitHub")
              (let [c (:commit t)]
                (d/transact! conn [{:tag_id     (:id c)
                                    :repo_name  name
                                    :repository repository_url
                                    :topics     (string/split topics #",")
                                    :name       (:name t)
                                    :committer  (:committer_name c)
                                    :date       (:committed_date c)
                                    :title      (:title c)
                                    :url        (:web_url c)}]))
              (let [commit (utils/get-contents-json-to-kwds (:url (:commit t)))]
                (d/transact! conn [{:tag_id     (:sha (:commit t))
                                    :repo_name  name
                                    :repository repository_url
                                    :topics     (string/split topics #",")
                                    :name       (:name t)
                                    :committer  (:name (:committer (:commit commit)))
                                    :date       (:date (:committer (:commit commit)))
                                    :title      (:message (:commit commit))
                                    :url        (:html_url commit)}]))))
          (catch Exception e (timbre/error (.getMessage e))))
        (d/transact! conn [(assoc repo :tags {:updated (str (t/instant))})])))))

;;; Prepare data for json generation

(def prepare-repos
  (let [repo-mapping (:repos utils/mappings)]
    (comp
     ;; Remove empty repos
     (remove #(:is_empty %))
     ;; Add is_lib if repo is also listed in libraries
     (map #(assoc % :is_lib (is-lib (:repository_url %))))
     ;; Add contributing
     (map #(assoc % :is_contrib (:is_contrib? (:contributing %))))
     ;; Add publiccode
     (map #(assoc % :is_publiccode (:is_publiccode? (:publiccode %))))
     ;; Add the number of reuses
     (map #(assoc % :reuses (:number (:reuses %))))
     ;; Rename keywords
     (map #(set/rename-keys (select-keys % (keys repo-mapping)) repo-mapping))
     ;; Add the number of dependencies
     (map #(assoc % :dp (count (:libraries (:dependencies %)))))
     ;; Remap licenses
     (map #(assoc % :li (get (:licenses utils/mappings) (:li %))))
     ;; Limit description - hardcode it to 200
     (map #(update % :d (fn [d] (if d (subs d 0 (min (count d) 200)) ""))))
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
  (let [annuaire-cnll (utils/get-contents-json-to-kwds (:annuaire-cnll utils/urls))
        sill-mapping  (:sill utils/mappings)]
    (comp
     ;; Convert tags
     (map #(update % :tags (fn [t] (string/join " " t))))
     ;; Convert timestamps
     (map #(update % :referencedSinceTime (fn [t] (str (t/instant (java.util.Date. t))))))
     ;; Add comptoirDuLibreSoftware id
     (map #(assoc % :comptoirDuLibreSoftwareId (:id (:comptoirDuLibreSoftware %))))
     ;; Add wikidataDataLogoUrl
     (map #(assoc % :wikidataDataLogoUrl (:logoUrl (:wikidataData %))))
     ;; Add annuaireCnllSoftwareProviders
     (map #(assoc % :annuaireCnllSoftwareProviders
                  (map (fn [e] (select-keys e [:nom :url]))
                       (:prestataires
                        (first (filter (fn [e] (= (:sill_id e) (:sill_id %)))
                                       annuaire-cnll))))))
     ;; Add comptoirDuLibreSoftwareProviders
     (map #(assoc % :comptoirDuLibreSoftwareProviders
                  (map (fn [e] (select-keys e [:name :url]))
                       (:providers (:comptoirDuLibreSoftware %)))))
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

(defn- generate-json [{:keys [t d]}]
  (spit (str t  ".json")
        (json/write-value-as-string
         (sequence (condp = t
                     "repos" prepare-repos
                     "orgas" prepare-orgas
                     "deps"  prepare-deps
                     "libs"  prepare-libs
                     "sill"  prepare-sill
                     "tags"  (map #(dissoc % :db/id)))
                   d))))

;;; Main functions

(defn- init-db []
  ;; Initiatize data from upstream resources
  (update-repos)
  (update-orgas)
  ;; ;; Fetch SourceHut data (see sr/hut.clj)
  (update-hut)
  (update-libs))

(defn- consolidate-data []
  ;; Consolidate data in the local db
  (consolidate-repos)
  (consolidate-orgas)
  (consolidate-tags))

(defn- generate-charts [repos]
  (try (sh/sh (str (System/getenv "NODEJS_BIN_HOME") "vl2svg")
              (charts/generate-licenses-chart
               (stats/top-licenses repos))
              "top_licenses.svg")
       (timbre/info "Successfully generated top_licenses.svg")
       (shutdown-agents)
       (catch Exception e (timbre/error (.getMessage e)))))

(defn- locale-date-from-time [& [time]]
  (.format (java.text.SimpleDateFormat. "dd/MM/yyyy")
           (if time (java.util.Date. time)
               (java.util.Date.))))

(defn- generate-sill-files []
  ;; Export as .tsv
  (spit "sill.tsv" "Logiciel\tVersion\tLicence\tAjouté\n")
  (doseq [{:keys [name versionMin license referencedSinceTime]}
          (filter #(nil? (:dereferencing %)) (get-sill))]
    (spit "sill.tsv"
          (str (string/join "\t" [name versionMin license
                                  (locale-date-from-time referencedSinceTime)])
               "\n")
          :append true))
  ;; Export as .org
  (spit "sill.org"
        (str "#+title: Socle interministériel de logiciels libres\n"
             "#+author: Les référents SILL ministériels et DINUM\n"
             "#+date: " (locale-date-from-time) "\n\n"
             "* Logiciels libres recommandés\n\n"
             "| Nom | Version | Licence | Ajouté |\n"
             "|-----|---------|---------|--------|\n"))
  (doseq [{:keys [name versionMin license referencedSinceTime]}
          (filter #(nil? (:dereferencing %)) (get-sill))]
    (spit
     "sill.org"
     (format
      "|%s|\n"
      (string/join " | " [name versionMin license
                          (locale-date-from-time referencedSinceTime)]))
     :append true))
  (spit "sill.org"
        (str "* Logiciels qui ne sont plus recommandés\n\n"
             "| Nom | Ajouté | Dernière version | Raison | Retiré |\n"
             "|-----|--------|------------------|--------|--------|\n")
        :append true)
  (doseq [{:keys [name versionMin referencedSinceTime dereferencing]}
          (filter :dereferencing (get-sill))]
    (spit
     "sill.org"
     (format
      "|%s|\n"
      (string/join " | " [name (locale-date-from-time referencedSinceTime)
                          versionMin (:reason dereferencing)
                          (locale-date-from-time (:time dereferencing))]))
     :append true))
  (try (sh/sh "pandoc" "sill.org" "-o" "sill.md")
       ;; See utils/ for sill-header.sty
       (sh/sh "pandoc" "-H" "sill-header.sty" "sill.org" "-o" "sill.pdf")
       ;; See utils/ for remove-header-attr.lua
       (sh/sh "pandoc" "--lua-filter=remove-header-attr.lua" "sill.org" "-o" "sill.org")
       (timbre/info "Successfully generated SILL files in .org, .md and .pdf")
       (catch Exception e (timbre/error (.getMessage e)))))

(defn -main []
  (init-db)
  (consolidate-data)
  ;; Prepare data output
  (let [repos (get-repos)
        orgas (get-orgas)
        libs  (get-libs)
        deps  (get-deps)
        tags  (->> (get-tags)
                   (sort-by #(instant/read-instant-date (:date %)))
                   reverse
                   (take 100))
        sill  (get-sill)]
    ;; Generate json output
    (doseq [[t d] [["repos" repos]
                   ["orgas" orgas]
                   ["libs" libs]
                   ["deps" deps]
                   ["sill" sill]
                   ["tags" tags]]]
      (generate-json {:t t :d d}))
    ;; Generate sill in .org .md and .pdf formats
    (generate-sill-files)
    ;; Generate stats
    (stats/generate-stats-json repos orgas libs deps sill)
    ;; Generate RSS feeds
    (rss/latest-repositories repos)
    (rss/latest-organizations orgas)
    (rss/latest-dependencies deps)
    (rss/latest-libraries libs)
    (rss/latest-sill sill)
    (rss/latest-tags tags)
    ;; Spit the top_licences.svg
    (generate-charts repos))
  )

;; (-main)
