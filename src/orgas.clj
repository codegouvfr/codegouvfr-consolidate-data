;; Copyright (c) 2020, 2021 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns orgas
  (:require [jsonista.core :as json]
            [clojure.set :as set]
            [utils :as utils]))

(defonce urls
  {:orgas              "organizations/json/all.json"
   :orgas-remote       "https://code.gouv.fr/data/organizations/json/all.json"
   :annuaire           "https://static.data.gouv.fr/resources/organisations-de-codegouvfr/20191011-110549/lannuaire.csv"
   :orgas-floss-policy "https://git.sr.ht/~etalab/codegouvfr-sources/blob/master/comptes-organismes-avec-politique-de-publication-floss.csv"})

;; Ignore these keywords
;; :private :default_branch :language :id :checked :owner :full_name
(def orgas-mapping
  "Mapping from groups/organizations keywords to local short versions."
  {:description        :d
   :location           :a
   :email              :e
   :name               :n
   :platform           :p
   :website            :h
   :is_verified        :v?
   :login              :l
   :creation_date      :c
   :repositories_count :r
   :organization_url   :o
   :avatar_url         :au})

(defn get-floss-policy []
  (apply merge
         ;; Betware "organisation" here is in French, as it is in the
         ;; upstream source.
         (map #(let [{:keys [organisation url-politique-floss]} %]
                 {organisation url-politique-floss})
              (utils/csv-url-to-map (:orgas-floss-policy urls)))))

(defn get-annuaire []
  (apply merge
         (map #(let [{:keys [github lannuaire]} %]
                 {(keyword github) lannuaire})
              (utils/csv-url-to-map (:annuaire urls)))))

;; Core functions
(defn add-data []
  (let [floss-pol (get-floss-policy)
        annuaire  (get-annuaire)
        deps      (-> "deps-orgas.json" utils/get-contents json/read-value)]
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
  (when-let [orgas (or (utils/get-contents (:orgas urls))
                       (utils/get-contents (:orgas-remote urls)))]
    (utils/json-parse-with-keywords orgas)))
