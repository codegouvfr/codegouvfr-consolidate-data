;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns orgas
  (:require [jsonista.core :as json]
            [clojure.set :as set]
            [utils :as utils]))

(defonce urls
  {:orgas
   "https://raw.githubusercontent.com/etalab/data-codes-sources-fr/master/data/organisations/json/all.json"
   ;; Next url return a csv
   :annuaire
   "https://static.data.gouv.fr/resources/organisations-de-codegouvfr/20191011-110549/lannuaire.csv"
   :orgas-floss-policy
   "https://raw.githubusercontent.com/DISIC/politique-de-contribution-open-source/master/comptes-organismes-avec-politique-de-publication-floss.csv"})

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

(defn add-data []
  (let [floss-pol (apply merge
                         (map #(let [{:keys [organisation url-politique-floss]} %]
                                 {organisation url-politique-floss})
                              (utils/csv-url-to-map (:orgas-floss-policy urls))))
        annuaire  (apply merge
                         (map #(let [{:keys [github lannuaire]} %]
                                 {(keyword github) lannuaire})
                              (utils/csv-url-to-map (:annuaire urls))))
        deps      (json/read-value
                   (try (slurp "deps-orgas.json")
                        (catch Exception e
                          (println (.getMessage e)))))]
    (comp
     ;; Remap keywords
     (map #(set/rename-keys % orgas-mapping))
     ;; Only keep organizations with repositories
     (filter #(pos? (:r %)))
     ;; Add information from orgas-floss-policy
     (map #(assoc % :fp (get floss-pol (:o %))))
     ;; Add information from annuaire
     (map #(assoc % :an ((keyword (:l %)) annuaire)))
     ;; Add orga deps number
     (map #(if-let [d (not-empty (get deps (str [(:l %) (:p %)])))]
             (assoc % :dp (count d))
             %)))))

(defn init
  "Generate orgas.json."
  []
  (when-let [orgas (utils/get-body (:orgas urls))]
    (spit "orgas-raw.json" orgas)
    (utils/json-parse-with-keywords orgas)))
