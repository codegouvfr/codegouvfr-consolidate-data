;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns repos
  (:require  [cheshire.core :as json]
             [babashka.curl :as curl]
             [clojure.string :as s]
             [clojure.set :as set]))

(defonce repos-url
  "https://raw.githubusercontent.com/etalab/data-codes-sources-fr/master/data/repertoires/json/all.json")

(defonce emoji-json-url
  "https://raw.githubusercontent.com/amio/emoji.json/master/emoji.json")

;; Ignore these keywords
;; :software_heritage_url :software_heritage_exists :derniere_modification
;; :page_accueil :date_creation :plateforme
(def repos-mapping
  "Mapping from repositories keywords to local short versions."
  {
   :derniere_mise_a_jour   :u
   :description            :d
   :est_archive            :a?
   :est_fork               :f?
   :langage                :l
   :licence                :li
   :nom                    :n
   :nombre_forks           :f
   :nombre_issues_ouvertes :i
   :nombre_stars           :s
   :organisation_nom       :o
   :plateforme             :p
   :repertoire_url         :r
   :topics                 :t})

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

(defn emojis
  "A map of emojis with {:char \"\" :name \"\"}."
  []
  (->> (json/parse-string (:body
                           (try
                             (curl/get emoji-json-url)
                             (catch Exception e
                               (println (.getMessage e)))))
                          true)
       (map #(select-keys % [:char :name]))
       (map #(update % :name (fn [n] (str ":" (s/replace n " " "_") ":"))))))

(defn add-data
  "Relace keywords, add licenses and emojis."
  []
  (let [emojis (emojis)
        deps   (json/parse-string
                (try (slurp "deps-repos.json")
                     (catch Exception e
                       (println (.getMessage e)))))
        reuse  (json/parse-string
                (try (slurp "reuse.json")
                     (catch Exception e
                       (println (.getMessage e)))))]
    (comp
     ;; Remap keywords
     (map #(set/rename-keys
            (select-keys % (keys repos-mapping)) repos-mapping))
     ;; Add number of dependencies (aka [m]odules)
     (map #(if-let [d (not-empty (get deps (:r %)))]
             (assoc % :dp (count d))
             %))
     ;; Add number of reuse
     (map #(if-let [r (not-empty (get reuse (:r %)))]
             (assoc % :g (get r "reuse"))
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
                                  (s/replace x (:name e) (:char e))))))
                @desc)))))))

(defn init
  "Generate repos-raw.json from `repos-url` and output repos."
  []
  (when-let [repos (:body
                    (try (curl/get repos-url)
                         (catch Exception e
                           (println (.getMessage e)))))]
    (spit "repos-raw.json" repos)
    (json/parse-string repos true)))
