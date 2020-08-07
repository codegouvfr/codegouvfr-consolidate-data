;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [clojure.walk :as walk]
            [jsonista.core :as json]
            [utils :as utils]
            [repos :as repos]
            [orgas :as orgas]
            [reuse :as reuse]
            [deps :as deps]
            [java-time :as t])
  (:gen-class))

(set! *warn-on-reflection* true)

;; Utility

(defn- find-first-matching-module [{:keys [n t]}]
  (-> (get @deps/grouped-deps [n t])
      first
      (dissoc :r :u :d :l)))

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
  (println "Added or updated repos-deps.json"))

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
  (let [reps (map #(select-keys % [:deps :repertoire_url]) @repos/repos)
        deps-reps
        (map (fn [{:keys [n t] :as dep}]
               (->> (map :repertoire_url
                         (filter (fn [{:keys [deps]}]
                                   (not-empty
                                    (filter (fn [d] (and (= (:n d) n)
                                                         (= (:t d) t)))
                                            deps)))
                                 reps))
                    (assoc dep :r)))
             @deps/deps)
        deps-reps-limited
        (->> deps-reps
             (filter #(> (count (:r %)) 1))
             distinct)]
    (reset! deps/deps deps-reps-limited)
    (spit "deps-all.json" (json/write-value-as-string (distinct deps-reps)))
    (spit "deps.json" (json/write-value-as-string deps-reps-limited))
    (println "Added or updated deps.json")))

(defn- spit-deps-repos [repos]
  (let [reps0 (group-by (juxt :nom :organisation_nom) repos)
        reps  (reduce-kv (fn [m k v] (assoc m k (utils/get-all-deps v)))
                         {}
                         reps0)]
    (spit "deps-repos.json"
          (json/write-value-as-string reps))
    (println "Added deps-repos.json")))

(defn- spit-deps-orgas [repos]
  (let [orgs1 (group-by (juxt :organisation_nom :plateforme) repos)
        orgs0 (reduce-kv (fn [m k v] (assoc m k (utils/get-all-deps v)))
                         {}
                         orgs1)]
    (spit "deps-orgas.json" (json/write-value-as-string orgs0))
    (println "Added deps-orgas.json")))

(defn- spit-deps-total [deps]
  (spit "deps-total.json"
        (json/write-value-as-string
         {:deps-total (count deps)}))
  (println "Added deps-total.json"))

(defn- spit-deps-top [deps]
  (spit "deps-top.json"
        (json/write-value-as-string
         (->> deps
              (sort-by #(count (:r %)))
              reverse
              (take 100))))
  (println "Added deps-top.json"))

(defn -main []
  ;;
  (println "Updating repos.json")
  (->> @repos/repos
       (sequence (repos/add-data))
       json/write-value-as-string
       (spit "repos.json"))
  ;;
  (println "Updating orgas.json")
  (->> (orgas/init)
       (sequence (orgas/add-data))
       json/write-value-as-string
       (spit "orgas.json"))
  ;;
  ;; Read reuse.json, update information and spit back
  (println "Updating reuse.json")
  (reuse/spit-info @repos/repos)
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
  ;; Spit other json files
  (spit-deps-repos @repos/repos)
  (spit-deps-orgas @repos/repos)
  (deps/spit-deps-repos-similarity @repos/repos @deps/deps)
  (spit-deps-total @deps/deps)
  (spit-deps-top @deps/deps)
  (println "Done creating/updating all json files"))

;; (-main)

