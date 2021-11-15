;; Copyright (c) 2020, 2021 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [clojure.walk :as walk]
            [clojure.java.shell :as sh]
            [clojure.data.csv :as csv]
            [clojure.set :as set]
            [jsonista.core :as json]
            [utils :as utils]
            [repos :as repos]
            [orgas :as orgas]
            [reuses :as reuses]
            [rss :as rss]
            [deps :as deps]
            [java-time :as t]
            [clojure.string :as string]
            [clojure.java.io :as io])
  (:gen-class))

;; Utility

(defn- find-first-matching-module [{:keys [n t]}]
  (-> (get @deps/grouped-deps [n t])
      first
      (dissoc :r :u :d :l)))

(def deps-mapping
  "Mapping from dependencies short keyword names to long names."
  {:n :name
   :t :type
   :d :description
   :l :url
   :u :updated
   :r :repositories})

;; Core functions

(defn- spit-repos-deps
  "Add valid :deps to each repo and spit repos-deps.json."
  []
  (reset! repos/repos
          (map (fn [r]
                 (update
                  r :deps
                  #(map find-first-matching-module %)))
               @repos/repos))
  (spit "repos-deps.json" (json/write-value-as-string @repos/repos))
  (println "repos-deps.json: OK"))

(defn- validate-repos-deps
  "Update @deps/deps and @deps/grouped-deps with valid dependencies."
  [repos]
  (when-let [d (->> repos
                    (map :deps)
                    flatten
                    (group-by :t)
                    walk/keywordize-keys
                    not-empty)]
    (let [res       (atom {})
          timestamp (str (t/instant))]
      (doseq [[type modules] d]
        (->> (condp = type
               :npm      (map deps/get-valid-npm modules)
               :bundler  (map deps/get-valid-bundler modules)
               :maven    (map deps/get-valid-maven modules)
               :clojars  (map deps/get-valid-clojars modules)
               :composer (map deps/get-valid-composer modules)
               :pypi     (map deps/get-valid-pypi modules)
               nil)
             (remove nil?)
             (map #(assoc % :u timestamp))
             (swap! res concat)))
      (reset! deps/deps (distinct @res))
      (reset! deps/grouped-deps (group-by (juxt :n :t) @deps/deps))
      (println "Updated @deps with valid dependencies"))))

(defn- spit-deps-with-repos []
  (let [reps
        (map #(select-keys % [:deps :repository_url]) @repos/repos)
        deps-reps
        (map (fn [{:keys [n t] :as dep}]
               (->> (map :repository_url
                         (filter (fn [{:keys [deps]}]
                                   (not-empty
                                    (filter (fn [d] (and (= (:n d) n)
                                                         (= (:t d) t)))
                                            deps)))
                                 reps))
                    (assoc dep :r)))
             @deps/deps)
        ;; Limit dependency descriptions to 100 characters
        deps-reps (utils/limit-description :d deps-reps)]
    (spit "deps.json" (json/write-value-as-string (distinct deps-reps)))
    (println "deps.json: OK")))

(defn spit-csv [f ms]
  (with-open [writer (io/writer f)]
    (csv/write-csv writer (utils/maps-to-csv ms))))

(defn update-dependencies-all []
  (let [deps (->> "deps.json"
                  utils/get-contents
                  utils/json-parse-with-keywords
                  (map #(set/rename-keys
                         (select-keys % (keys deps-mapping)) deps-mapping)))]
    ;; Make sure the directories are existing
    (sh/sh "mkdir" "-p" "dependencies/json" "dependencies/csv")
    (spit "dependencies/json/all.json" (json/write-value-as-string deps))
    (spit-csv "dependencies/csv/all.csv" deps)))

(defn- spit-deps-repos [repos]
  (let [reps0 (group-by (juxt :name :organization_name) repos)
        reps  (reduce-kv (fn [m k v] (assoc m k (utils/get-all-deps v)))
                         {}
                         reps0)]
    (spit "deps-repos.json"
          (json/write-value-as-string reps))
    (println "deps-repos.json: OK")))

(defn- spit-deps-orgas [repos]
  (let [orgs1 (group-by (juxt :organization_name :platform) repos)
        orgs0 (reduce-kv (fn [m k v] (assoc m k (utils/get-all-deps v)))
                         {}
                         orgs1)]
    (spit "deps-orgas.json" (json/write-value-as-string orgs0))
    (println "deps-orgas.json: OK")))

(defn- spit-deps-total [deps]
  (spit "deps-total.json"
        (json/write-value-as-string
         {:deps-total (count deps)}))
  (println "deps-total.json: OK"))

(defn- spit-deps-top [deps]
  (spit "deps-top.json"
        (json/write-value-as-string
         (->> deps
              (sort-by #(count (:r %)))
              reverse
              (take 100))))
  (println "deps-top.json: OK"))

(defn -main []
  ;;
  (->> @repos/repos
       (sequence (repos/add-data))
       json/write-value-as-string
       (spit "repos.json"))
  (println "repos.json: OK")
  ;;
  (->> (orgas/init)
       (sequence (orgas/add-data))
       json/write-value-as-string
       (spit "orgas.json"))
  (println "orgas.json: OK")
  ;;
  ;; Read reuses.json, update information and spit back
  (println "Updating reuses.json")
  (reuses/spit-info @repos/repos)
  ;;
  ;; Updating @repos/repos with all dependencies
  (reset! repos/repos (map deps/add-dependencies @repos/repos))
  ;;
  ;; Update @deps/deps with valid dependencies
  (validate-repos-deps @repos/repos)
  ;;
  ;; Update @repos/repos with valid deps and spit repos-deps.json
  (spit-repos-deps)
  ;;
  ;; Update @deps by adding :repos and spit deps.json
  (spit-deps-with-repos)
  ;;
  ;; Update dependencies/{csv,json/all.{csv/json}
  (update-dependencies-all)
  ;;
  ;; Spit other json files
  (spit-deps-repos @repos/repos)
  (spit-deps-orgas @repos/repos)
  ;;
  (deps/spit-deps-repos-similarity @repos/repos @deps/deps)
  ;;
  (spit-deps-total @deps/deps)
  (spit-deps-top @deps/deps)
  ;;
  ;; Spit the latest.xml RSS feed
  (rss/make-feed)
  ;; Spit the top_licences.svg
  (do (sh/sh "vl2svg" (utils/generate-licenses-chart) "top_licenses.svg")
      (shutdown-agents)
      (println "top_licenses.svg: OK"))
  ;; Finish
  (println "Done adding or updating all json/xml/svg files"))
