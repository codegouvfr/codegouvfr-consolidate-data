;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns repos
  (:require  [cheshire.core :as json]
             [clj-http.lite.client :as http]
             [clojure.string :as s]
             [clojure.set])
  (:gen-class))

(defonce http-get-params {:cookie-policy :standard})

(defonce repos-url
  "https://api-code.etalab.gouv.fr/api/repertoires/all")

(defonce emoji-json-url
  "https://raw.githubusercontent.com/amio/emoji.json/master/emoji.json")

(defn emojis
  "A map of emojis with {:char \"\" :name \"\"}."
  []
  (->> (json/parse-string (:body
                           (try
                             (http/get emoji-json-url http-get-params)
                             (catch Exception e (println "Can't reach emoji-json-url"))))
                          true)
       (map #(select-keys % [:char :name]))
       (map #(update % :name (fn [n] (str ":" (s/replace n " " "_") ":"))))))

;; Ignore these keywords
;; :software_heritage_url :software_heritage_exists :derniere_modification
;; :page_accueil :date_creation :plateforme
(def repos-mapping
  "Mapping from repositories keywords to local short versions."
  {:nom                    :n
   :description            :d
   :organisation_nom       :o
   :langage                :l
   :licence                :li
   :repertoire_url         :r
   :topics                 :t
   :derniere_mise_a_jour   :u
   :nombre_forks           :f
   :nombre_issues_ouvertes :i
   :nombre_stars           :s
   :est_archive            :a?
   :est_fork               :f?})

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
   "Creative Commons Attribution Share Alike 4.0 International" "Creative Commons Attribution Share Alike 4.0 International (CC-BY-NC-SA-4.0)"
   "BSD 2-Clause \"Simplified\" License"                        "BSD 2-Clause \"Simplified\" License (BSD-2-Clause)"
   "The Unlicense"                                              "The Unlicense (Unlicense)"
   "Do What The Fuck You Want To Public License"                "Do What The Fuck You Want To Public License (WTFPL)"
   "Creative Commons Attribution 4.0 International"             "Creative Commons Attribution 4.0 International (CC-BY-4.0)"})

(defn cleanup-repos []
  (let [emojis     (emojis)
        repos-deps (json/parse-string
                    (try (slurp "deps/repos-deps.json")
                         (catch Exception e nil))
                    true)]
    (comp
     (map #(clojure.set/rename-keys (select-keys % (keys repos-mapping)) repos-mapping))
     (map (fn [r] (assoc
                   r
                   :li (get licenses-mapping (:li r))
                   :dp (not (empty? (first (filter #(= (:n %) (:n r)) repos-deps))))
                   )))
     (map (fn [r] (update
                   r
                   :d
                   (fn [d]
                     (let [desc (atom d)]
                       (doseq [e emojis]
                         (swap! desc (fn [x]
                                       (when (string? x)
                                         (s/replace x (:name e) (:char e))))))
                       @desc))))))))

(defn update-repos
  "Generate repos.json from `repos-url`."
  []
  (when-let [repos-json
             (json/parse-string
              (:body (try (http/get repos-url http-get-params)
                          (catch Exception e
                            (println "Can't reach repos-url"))))
              true)]
    (spit "repos.json"
          (json/generate-string
           (sequence (cleanup-repos) repos-json)))
    (println "Updated repos.json")))

