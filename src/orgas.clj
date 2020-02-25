;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns orgas
  (:require  [cheshire.core :as json]
             [semantic-csv.core :as semantic-csv]
             [clj-http.lite.client :as http]
             [ring.util.codec :as codec]
             [clojure.java.io :as io]
             [clojure.string :as s]
             [clojure.set]
             [hickory.core :as h]
             [hickory.select :as hs])
  (:gen-class))

(defonce orgas-url
  "https://api-code.etalab.gouv.fr/api/organisations/all")

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

;; FIXME: delete this
(defonce
  ^{:doc "The parsed output of retrieving orgas-url, updated by the fonction
  `update-orgas-json` and stored for further retrieval in
  `update-orgas` and `update-orgas-repos-deps`."}
  orgas-json
  (atom nil))

(defn update-orgas-json
  "Reset `orgas-json` from `orgas-url`."
  []
  (let [old-orgas-json @orgas-json
        result
        (try (:body (http/get orgas-url))
             (catch Exception e
               (do (println (str "Can't get groups: "
                                 (:cause (Throwable->map e))))
                   old-orgas-json)))]
    (reset! orgas-json (distinct (json/parse-string result true))))
  (println (str "updated @orgas-json (" (count @orgas-json) " organisations)")))

(defn update-orgas
  "Generate orgas.json from `orgas-json` and `annuaire-url`."
  []
  (when-let [annuaire (apply merge
                             (map #(let [{:keys [github lannuaire]} %]
                                     {(keyword github) lannuaire})
                                  (try (semantic-csv/slurp-csv annuaire-url)
                                       (catch Exception e
                                         (println
                                          "Can't reach annuaire-url")))))]
    (spit "orgas.json"
          (json/generate-string
           (map #(assoc %
                        :an ((keyword (:l %)) annuaire)
                        :dp (let [f (str "deps/orgas/" (:l %) ".json")]
                              (if (.exists (io/file f))
                                (not (empty? (json/parse-string (slurp f)))))))
                (map #(clojure.set/rename-keys % orgas-mapping)
                     @orgas-json))))
    (println (str "updated orgas.json"))))

