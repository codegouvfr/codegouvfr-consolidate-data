;; Copyright (c) 2020, 2022 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns utils
  (:require [jsonista.core :as json]
            [clojure.data.csv :as csv]
            [clojure.string :as string]
            [clojure.walk :as walk]
            [clojure.edn :as edn]
            [babashka.curl :as curl]
            [clj-yaml.core :as yaml]
            [java-time :as t]
            [hickory.core :as h]
            [hickory.select :as hs]
            [taoensso.timbre :as timbre]))

(defonce env-vars
  {:gh-user             (System/getenv "CODEGOUVFR_GITHUB_USER")
   :gh-token            (System/getenv "CODEGOUVFR_GITHUB_ACCESS_TOKEN")
   :thread-interval     (Integer. (System/getenv "CODEGOUVFR_GET_INTERVAL"))
   :updating-after-days (Integer. (System/getenv "CODEGOUVFR_DAYS_INTERVAL"))})

(defonce urls
  {:sources       "https://git.sr.ht/~etalab/codegouvfr-sources/blob/master/comptes-organismes-publics.yml"
   :sill          "https://sill.etalab.gouv.fr/api/sill.json"
   :libs          "https://code.gouv.fr/data/libraries/json/all.json"
   :repos         "https://code.gouv.fr/data/repositories/json/all.json"
   :orgas         "https://code.gouv.fr/data/organizations/json/all.json"
   :annuaire-cnll "https://annuaire.cnll.fr/api/prestataires-sill.json"
   :annuaire      "https://static.data.gouv.fr/resources/organisations-de-codegouvfr/20191011-110549/lannuaire.csv"
   :emoji-json    "https://raw.githubusercontent.com/amio/emoji.json/master/emoji.json"})

(defonce sources
  (try
    (-> (slurp (:sources urls))
        (yaml/parse-string :keywords false))
    (catch Exception e
      (timbre/error
       (str "Error while fetching the list of organizations")
       (.getMessage e)))))

(defonce mappings
  {;; Mapping from libraries keywords to local short versions
   :libs     {:description                        :d
              :latest_stable_release_published_at :u
              :repo_url                           :l
              :name                               :n
              :platform                           :t
              :is_repo                            :r?
              :license                            :l}
   ;; Mapping from sill keywords to local short versions
   :sill     {:sill_id                          :id
              :name                             :n
              :license                          :l
              :function                         :f
              :versionMin                       :v
              :isFromFrenchPublicService        :fr
              :referencedSinceTime              :u
              :isPresentInSupportContract       :s
              :comptoirDuLibreSoftwareId        :cl
              :comptoirDuLibreSoftwareProviders :clp
              :annuaireCnllSoftwareProviders    :ac
              :wikidataDataLogoUrl              :i
              :useCaseUrls                      :c
              :workshopUrls                     :w
              :agentWorkstation                 :a
              :tags                             :t
              :dereferencing                    :d}
   ;; Mapping from papillon keywords to local short versions
   :papillon {:agencyName        :a
              :publicSector      :p
              :serviceName       :n
              :description       :d
              :serviceUrl        :l
              :softwareSillId    :i
              :comptoirDuLibreId :c}
   ;; Mapping from repositories keywords to local short versions
   :repos    {:last_update       :u
              :description       :d
              :is_archived       :a?
              :is_fork           :f?
              :is_esr            :e?
              :is_lib            :l?
              :is_contrib        :c?
              :is_publiccode     :p?
              :language          :l
              :license           :li
              :name              :n
              :forks_count       :f
              :stars_count       :s
              :platform          :p
              :organization_name :o
              :reuses            :re
              :repository_url    :r}
   ;; Mapping from libraries keywords to local short versions
   :deps     {:type         :t
              :name         :n
              :description  :d
              :repositories :r
              :updated      :u
              ;; FIXME: Unused yet?
              :repo_url     :ru
              :link         :l}
   ;; Mapping from groups/organizations keywords to local short versions
   :orgas    {:description        :d
              :location           :a
              :email              :e
              :name               :n
              :platform           :p
              :website            :h
              :is_verified        :v?
              :ministry           :m
              :annuaire           :an
              :floss_policy       :f
              :login              :l
              :creation_date      :c
              :repositories_count :r
              :organization_url   :o
              :avatar_url         :au}
   :licenses
   {"MIT License"                                                "MIT License (MIT)"
    "GNU Affero General Public License v3.0"                     "GNU Affero General Public License v3.0 (AGPL-3.0)"
    "GNU General Public License v3.0"                            "GNU General Public License v3.0 (GPL-3.0)"
    "GNU Lesser General Public License v2.1"                     "GNU Lesser General Public License v2.1 (LGPL-2.1)"
    "Apache License 2.0"                                         "Apache License 2.0 (Apache-2.0)"
    "GNU General Public License v2.0"                            "GNU General Public License v2.0 (GPL-2.0)"
    "GNU Lesser General Public License v3.0"                     "GNU Lesser General Public License v3.0 (LGPL-3.0)"
    "Mozilla Public License 2.0"                                 "Mozilla Public License 2.0 (MPL-2.0)"
    "Eclipse Public License 2.0"                                 "Eclipse Public License 2.0 (EPL-2.0)"
    "Eclipse Public License 1.0"                                 "Eclipse Public License 1.0 (EPL-1.0)"
    "BSD 3-Clause \"New\" or \"Revised\" License"                "BSD 3-Clause \"New\" or \"Revised\" License (BSD-3-Clause)"
    "European Union Public License 1.2"                          "European Union Public License 1.2 (EUPL-1.2)"
    "Creative Commons Attribution Share Alike 4.0 International" "Creative Commons Attribution Share Alike 4.0 International (CC-BY-SA-4.0)"
    "BSD 2-Clause \"Simplified\" License"                        "BSD 2-Clause \"Simplified\" License (BSD-2-Clause)"
    "The Unlicense"                                              "The Unlicense (Unlicense)"
    "Do What The Fuck You Want To Public License"                "Do What The Fuck You Want To Public License (WTFPL)"
    "Creative Commons Attribution 4.0 International"             "Creative Commons Attribution 4.0 International (CC-BY-4.0)"}
   :licenses-spdx
   {"Other"                                                      "Other"
    "MIT License"                                                "MIT"
    "GNU Affero General Public License v3.0"                     "AGPL-3.0"
    "GNU General Public License v3.0"                            "GPL-3.0"
    "GNU Lesser General Public License v2.1"                     "LGPL-2.1"
    "Apache License 2.0"                                         "Apache-2.0"
    "GNU General Public License v2.0"                            "GPL-2.0"
    "GNU Lesser General Public License v3.0"                     "LGPL-3.0"
    "Mozilla Public License 2.0"                                 "MPL-2.0"
    "Eclipse Public License 2.0"                                 "EPL-2.0"
    "Eclipse Public License 1.0"                                 "EPL-1.0"
    "BSD 3-Clause \"New\" or \"Revised\" License"                "BSD-3-Clause"
    "European Union Public License 1.2"                          "EUPL-1.2"
    "Creative Commons Attribution Share Alike 4.0 International" "CC-BY-SA-4.0"
    "BSD 2-Clause \"Simplified\" License"                        "BSD-2-Clause"
    "The Unlicense"                                              "Unlicense"
    "Do What The Fuck You Want To Public License"                "WTFPL"
    "Creative Commons Attribution 4.0 International"             "CC-BY-4.0"}})

(defn- mean
  ;; FIXME: Get this from a standard library?
  "Standard mean function."
  [xs] (float (/ (reduce + xs) (count xs))))

(defn median
  ;; FIXME: Get this from a standard library?
  "Standard mean function."
  [xs]
  (let [n   (count xs)
        mid (/ n 2)]
    (if (odd? n)
      (nth (sort xs) mid)
      (->> (sort xs)
           (drop (dec mid))
           (take 2)
           (mean)))))

(defn replace-vals [m v r]
  (walk/postwalk #(if (= % v) r %) m))

(def user-agent
  {:raw-args ["--connect-timeout" "10"]
   :headers  {"User-Agent" "https://code.gouv.fr bot (logiciels-libres@data.gouv.fr)"}})

(def default-parameters user-agent)

(def gh-parameters
  (merge user-agent {:basic-auth [(:gh-user env-vars) (:gh-token env-vars)]}))

(defn needs-updating? [date-str]
  (let [delay (:updating-after-days env-vars)]
    (if-not (string? date-str)
      true
      (t/before?
       (t/minus (t/instant date-str) (t/days (rand-int delay)))
       (t/minus (t/instant) (t/days delay))))))

(defn get-contents [s]
  (Thread/sleep (:thread-interval env-vars))
  (let [url?    (re-find #"https://" s)
        gh-api? (and url? (re-find #"https://api.github.com" s))
        res     (try (apply
                      (cond
                        gh-api? #(curl/get % gh-parameters)
                        url?    #(curl/get % default-parameters)
                        :else   slurp) [s])
                     (catch Exception e
                       (timbre/error
                        (str "Error while getting contents for " s ":")
                        (.getMessage e))))]
    (if (and url? (= (:status res) 200))
      (:body res)
      res)))

(defn- rows->maps [csv]
  (let [headers (map keyword (first csv))
        rows    (rest csv)]
    (map #(zipmap headers %) rows)))

(defn csv-url-to-map [url]
  (try
    (rows->maps (csv/read-csv (get-contents url)))
    (catch Exception e
      (timbre/error (.getMessage e)))))

(defn json-parse-with-keywords [s]
  (-> s
      (json/read-value
       (json/object-mapper {:decode-key-fn keyword}))))

(defn get-contents-json-to-kwds [s]
  (json-parse-with-keywords (get-contents s)))

(defonce emojis
  (->> (:emoji-json urls)
       get-contents
       json-parse-with-keywords
       (map #(select-keys % [:char :name]))
       (map #(update % :name
                     (fn [n] (str ":" (string/replace n " " "_") ":"))))))

;;; Main functions to update repos

(defn get-contributing
  [{:keys [platform organization_name name repository_url default_branch contributing]}]
  (timbre/info "Check CONTRIBUTING.md for" repository_url)
  (if-not (needs-updating? (:updated contributing))
    contributing
    (let  [path        (str (or default_branch "master") "/CONTRIBUTING.md")
           url         (condp = platform
                         "GitHub"    (format "https://raw.githubusercontent.com/%s/%s/%s"
                                             organization_name name path)
                         "SourceHut" (str repository_url "/blob/" path)
                         "GitLab"    (str repository_url "/-/raw/" path))
           contents    (get-contents url)
           ;; FIXME: Hack to circumvent cases when GitLab returns the Sign in page:
           contents-ok (and contents (not (re-matches #"<!DOCTYPE html>" contents)))]
      {:is_contrib? (if contents-ok (boolean (seq contents)) false)
       :updated     (str (t/instant))})))

(defn get-publiccode
  [{:keys [platform organization_name name repository_url default_branch publiccode]}]
  (timbre/info "Check publiccode.yml for" repository_url)
  (if-not (needs-updating? (:updated publiccode))
    publiccode
    (let  [path        (str (or default_branch "master") "/publiccode.yml")
           url         (condp = platform
                         "GitHub"    (format "https://raw.githubusercontent.com/%s/%s/%s"
                                             organization_name name path)
                         "SourceHut" (str repository_url "/blob/" path)
                         "GitLab"    (str repository_url "/-/raw/" path))
           contents    (get-contents url)
           ;; FIXME: Hack to circumvent cases when GitLab returns the Sign in page:
           contents-ok (and contents (not (re-matches #"<!DOCTYPE html>" contents)))]
      {:is_publiccode? (if contents-ok (boolean (seq contents)) false)
       :updated        (str (t/instant))})))

(defn get-reuses
  "Return a hash-map with reuse information"
  [{:keys [platform repository_url reuses]}]
  (if-not (needs-updating? (:updated reuses))
    reuses
    (let [updated        (str (t/instant))
          default_reuses {:number 0 :updated updated}]
      (if-not (= platform "GitHub")
        ;; Don't try to fetch reuses for GitLab and SourceHut
        default_reuses
        (do
          (timbre/info "Getting dependents for" repository_url)
          (if-let [repo-github-html
                   (get-contents (str repository_url "/network/dependents"))]
            (let [btn-links (-> repo-github-html
                                h/parse
                                h/as-hickory
                                (as-> d (hs/select (hs/class "btn-link") d)))
                  nb-reps   (or (try (re-find #"\d+" (last (:content (nth btn-links 1))))
                                     (catch Exception _ "0")) "0")
                  nb-pkgs   (or (try (re-find #"\d+" (last (:content (nth btn-links 2))))
                                     (catch Exception _ "0")) "0")]
              {:number  (+ (edn/read-string nb-reps)
                           (edn/read-string nb-pkgs))
               :updated updated})
            default_reuses))))))
