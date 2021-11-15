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

(defn executable-exists? [s]
  (if (re-matches
       (re-pattern (str "^" s ": /.+$"))
       (string/trim (:out (sh/sh "whereis" s))))
    true
    false))

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
        deps-reps (utils/limit-description deps-reps)]
    (spit "deps.json" (json/write-value-as-string (distinct deps-reps)))
    (println "Added or updated deps.json")))

(defn update-dependencies-all []
  (let [deps (->> "deps.json" utils/get-contents json/read-value
                  (map #(set/rename-keys
                         (select-keys % (keys deps-mapping)) deps-mapping)))]
    ;; Make sure the directories are existing
    (sh/sh "mkdir" "-p" "dependencies/{json,csv}")
    (spit "dependencies/json/all.json" (json/write-value-as-string deps))
    (with-open [writer (io/writer "dependencies/csv/all.csv")]
      (csv/write-csv
       writer
       (utils/maps-to-csv deps)))))

(defn- spit-deps-repos [repos]
  (let [reps0 (group-by (juxt :name :organization_name) repos)
        reps  (reduce-kv (fn [m k v] (assoc m k (utils/get-all-deps v)))
                         {}
                         reps0)]
    (spit "deps-repos.json"
          (json/write-value-as-string reps))
    (println "Added deps-repos.json")))

(defn- spit-deps-orgas [repos]
  (let [orgs1 (group-by (juxt :organization_name :platform) repos)
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
  (if (executable-exists? "vl2sg")
    (do (sh/sh "vl2svg" (utils/generate-licenses-chart) "top_licenses.svg")
        (shutdown-agents)
        (println "Generated top_licenses.svg"))
    (println "Can't find vl2svg, don't generate top_licenses.svg"))
  ;; Finish
  (println "Done creating/updating all json/xml/svg files"))


