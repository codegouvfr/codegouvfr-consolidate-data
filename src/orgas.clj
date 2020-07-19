;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns orgas
  (:require  [cheshire.core :as json]
             [clojure.data.csv :as csv]
             [babashka.curl :as curl]
             [clojure.set :as set]))

(defonce orgas-url
  "https://raw.githubusercontent.com/etalab/data-codes-sources-fr/master/data/organisations/json/all.json")

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

;; Utility functions

(defn- rows->maps [csv]
  (let [headers (map keyword (first csv))
        rows    (rest csv)]
    (map #(zipmap headers %) rows)))

(defn csv-url-to-map [url]
  (rows->maps (csv/read-csv (:body (curl/get url)))))

;; Core functions

(defn add-data []
  (let [annuaire (apply merge
                        (map #(let [{:keys [github lannuaire]} %]
                                {(keyword github) lannuaire})
                             (try (csv-url-to-map annuaire-url)
                                  (catch Exception e
                                    (println (.getMessage e))))))
        deps     (json/parse-string
                  (try (slurp "deps-orgas.json")
                       (catch Exception e
                         (println (.getMessage e)))))]
    (comp
     ;; Add information from `annuaire-url`.
     (map #(assoc % :an ((keyword (:l %)) annuaire)))
     ;; Remap keywords
     (map #(set/rename-keys % orgas-mapping))
     ;; Add orga deps number
     (map #(if-let [d (not-empty (get deps (str [(:n %) (:p %)])))]
             (assoc % :dp (count d))
             %)))))

(defn init
  "Generate orgas.json from `orgas-url`."
  []
  (when-let [orgas (:body (try (curl/get orgas-url)
                               (catch Exception e
                                 (println (.getMessage e)))))]
    (spit "orgas-raw.json" orgas)))
