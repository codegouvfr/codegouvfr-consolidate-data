;; Copyright (c) 2020, 2021 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns repos
  (:require [jsonista.core :as json]
            [clojure.string :as string]
            [clojure.set :as set]
            [utils :as utils]))

(defonce urls
  {:repos        "repositories/json/all.json"
   :repos-remote "https://code.gouv.fr/data/repositories/json/all.json"
   :emoji-json   "https://raw.githubusercontent.com/amio/emoji.json/master/emoji.json"
   :orgas-esr    "https://git.sr.ht/~etalab/codegouvfr-sources/blob/master/comptes-organismes-publics-esr"})

;; Ignore these keywords
;; :software_heritage_url :software_heritage_exists :last_update
;; :homepage :date_creation :platform
(def repos-mapping
  "Mapping from repositories keywords to local short versions."
  {
   :last_update       :u
   :description       :d
   :is_archived       :a?
   :is_fork           :f?
   :language          :l
   :license           :li
   :name              :n
   :forks_count       :f
   :open_issues_count :i
   :stars_count       :s
   :organization_name :o
   :platform          :p
   :repository_url    :r
   :topics            :t})

(defonce
  ^{:doc "Mapping from GitHub license strings to the their license+SDPX short
  identifier version."}
  licenses-mapping
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
   "Creative Commons Attribution 4.0 International"             "Creative Commons Attribution 4.0 International (CC-BY-4.0)"})

(defn get-emojis
  "A map of emojis with {:char \"\" :name \"\"}."
  []
  (->> (:emoji-json urls)
       utils/get-contents
       utils/json-parse-with-keywords
       (map #(select-keys % [:char :name]))
       (map #(update % :name
                     (fn [n] (str ":" (string/replace n " " "_") ":"))))))

(defn get-esr-orgas []
  (->>  (:orgas-esr urls)
        utils/get-contents
        string/split-lines
        (map #(string/replace % #"^.+/([^/]+)$" "$1"))
        (into #{})))

(defn add-data
  "Relace keywords, add licenses and emojis."
  []
  (let [emojis    (get-emojis)
        esr-orgas (get-esr-orgas)
        deps      (-> "deps-repos.json" utils/get-contents json/read-value)
        reuses    (-> "reuses.json" utils/get-contents json/read-value)]
    (comp
     ;; Remap keywords
     (map #(set/rename-keys
            (select-keys % (keys repos-mapping)) repos-mapping))
     ;; Add number of dependencies (aka [m]odules)
     (map #(if-let [d (not-empty (get deps (str [(:n %) (:o %)])))]
             (assoc % :dp (count d))
             %))
     ;; Add information from orgas-esr
     (map #(assoc % :e (contains? esr-orgas (:o %))))
     ;; Add number of reuses
     (map #(if-let [r (not-empty (get reuses (:r %)))]
             (assoc % :g (get r "r"))
             %))
     ;; Remap licenses
     (map #(assoc % :li (get licenses-mapping (:li %))))
     ;; Replace emojis
     (map #(update
            %
            :d
            (fn [d]
              (let [desc (atom d)]
                (doseq [e emojis]
                  (swap! desc (fn [x]
                                (when (string? x)
                                  (string/replace x (:name e) (:char e))))))
                @desc)))))))

;; Initialize repos-deps
(def repos-deps
  (group-by (juxt :name :organization_name)
            (utils/json-parse-with-keywords
             (utils/get-contents "repos-deps.json"))))

(defn find-repo-deps [{:keys [name organization_name]}]
  (-> (get repos-deps [name organization_name])
      first
      (select-keys [:deps_updated :deps])))

;; Initialize repos atom by reusing :deps_updated and :deps from
;; repos-deps.json when available
(def repos
  (let [repos (or (utils/get-contents (:repos urls))
                  (utils/get-contents (:repos-remote urls)))]
    (->> (map
          #(merge % (find-repo-deps %))
          (->> repos
               utils/json-parse-with-keywords
               utils/limit-description))
         ;; (take 100) ;; DEBUG
         atom)))
