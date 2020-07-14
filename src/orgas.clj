;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns orgas
  (:require  [cheshire.core :as json]
             [utils :as utils]
             [clojure.set]))

(defonce orgas-url
  "https://raw.githubusercontent.com/etalab/data-codes-sources-fr/master/data/organisations/csv/all.csv")

(defonce annuaire-url ;; returns a csv
  "https://static.data.gouv.fr/resources/organisations-de-codegouvfr/20191011-110549/lannuaire.csv")

;; Ignore these keywords
;; :private :default_branch :language :id :checked :owner :full_name
(def orgas-mapping
  "Mapping from groups/organizations keywords to local short versions."
  {:description        :d
   :adresse            :a
   :email              :e
   :nom                :n
   :plateforme         :p
   :site_web           :h
   :est_verifiee       :v?
   :login              :l
   :date_creation      :c
   :nombre_repertoires :r
   :organisation_url   :o
   :avatar_url         :au})

;; Core functions

(defn init-orgas
  "Generate orgas.json from `orgas-url` and `annuaire-url`."
  []
  (let [annuaire (apply merge
                        (map #(let [{:keys [github lannuaire]} %]
                                {(keyword github) lannuaire})
                             (try (utils/csv-url-to-map annuaire-url)
                                  (catch Exception e
                                    (println
                                     "ERROR: Cannot reach annuaire-url\n"
                                     (.getMessage e))))))
        orgas    (map
                  #(clojure.set/rename-keys % orgas-mapping)
                  (try (utils/csv-url-to-map orgas-url)
                       (catch Exception e
                         (println "ERROR: Cannot reach orgas-url\n"
                                  (.getMessage e)))))]
    (spit "orgas.json"
          (json/generate-string
           (map #(assoc % :an ((keyword (:l %)) annuaire)) orgas)))))

(defn update-orgas
  "Update orgas.json."
  []
  (spit "orgas.json"
        (json/generate-string
         (map #(assoc % :dp (json/parse-string
                             (let [path (str "deps/orgas/" (:l %) ".json")]
                               (try (slurp path)
                                    (catch Exception e
                                      (println "Cannot get" path "\n"
                                               (.getMessage e)))))))
              (json/parse-string (try (slurp "orgas.json")
                                      (catch Exception e
                                        (println "Cannot get orgas.json\n"
                                                 (.getMessage e))))
                                 true))))
  (println (str "Updated orgas.json")))
