;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns orgas
  (:require  [cheshire.core :as json]
             [semantic-csv.core :as semantic-csv]
             [clj-http.lite.client :as http]
             [clojure.java.io :as io]
             [clojure.set])
  (:gen-class))

(defonce http-get-params {:cookie-policy :standard})

(defonce orgas-url
  "https://raw.githubusercontent.com/etalab/data-codes-sources-fr/master/data/organisations/csv/all.csv")

(defonce annuaire-url ;; returns a csv
  "https://www.data.gouv.fr/fr/datasets/r/ac26b864-6a3a-496b-8832-8cde436f5230")

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

(defn init-orgas
  "Generate orgas.json from `orgas-url` and `annuaire-url`."
  []
  (let [annuaire (apply merge
                        (map #(let [{:keys [github lannuaire]} %]
                                {(keyword github) lannuaire})
                             (try (semantic-csv/slurp-csv annuaire-url)
                                  (catch Exception e
                                    (println
                                     "ERROR: Can't reach annuaire-url")))))
        orgas    (map
                  #(clojure.set/rename-keys % orgas-mapping)
                  (try (semantic-csv/slurp-csv orgas-url)
                       (catch Exception e
                         (println "ERROR: Can't reach orgas-url"))))]
    (spit "orgas.json"
          (json/generate-string
           (map #(assoc % :an ((keyword (:l %)) annuaire)) orgas)))))

(defn update-orgas
  "Update orgas.json."
  []
  (spit "orgas.json"
        (json/generate-string
         (map #(assoc % :dp (json/parse-string
                             (try (slurp (str "deps/orgas/" (:l %) ".json"))
                                  (catch Exception e nil))))
              (json/parse-string (try (slurp "orgas.json")
                                      (catch Exception e nil))
                                 true))))
  (println (str "Updated orgas.json")))
