;; Copyright (c) 2022 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns stats
  (:require [clojure.set :as set]
            [clojure.string :as string]
            [utils :as utils]
            [jsonista.core :as json]))

(defn- top-orgas-by-repos
  "Take the first 10 organizations with the highest repos count."
  [orgas]
  (->> orgas
       (sort-by :repositories_count)
       reverse
       (take 10)
       (map #(select-keys % [:login :platform :repositories_count]))
       (map (fn [{:keys [login platform repositories_count]}]
              [(str login " (" platform ")") repositories_count]))))

(defn- top-orgas-by-stars
  "Take the first 10 organizations with the highest stars count."
  [repos]
  (->> repos
       (group-by :organization_name)
       (map (fn [[k v]] {:orga  k
                         :stars (reduce + (map :stars_count v))}))
       (sort-by :stars)
       reverse
       (take 10)
       (map (fn [{:keys [orga stars]}] [orga stars]))))

(defn- top-licenses
  "Return the 10 most used licenses in all repositories."
  [repos]
  (->> repos
       (filter #(not-empty (:license %)))
       (group-by :license)
       (map (fn [[k v]] {:license k :repos_cnt (count v)}))
       (sort-by :repos_cnt)
       reverse
       (take 10)
       (map (fn [{:keys [license repos_cnt]}] [license repos_cnt]))))

(defn- top-languages
  "Return the 10 most used languages in all repositories."
  [repos]
  (->> repos
       (filter #(not-empty (:language %)))
       (group-by :language)
       (map (fn [[k v]] {:language k :repos_cnt (count v)}))
       (sort-by :repos_cnt)
       reverse
       (take 10)
       (map (fn [{:keys [language repos_cnt]}] [language repos_cnt]))))

(defn- platforms-cnt
  "Return the number of GitHub/GitLab hosted repositories."
  [repos]
  {:GitHub (count (filter #(= (:platform %) "GitHub") repos))
   :GitLab (count (filter #(= (:platform %) "GitLab") repos))})

(defn- top-topics
  "Return the 10 most frequent topics in all repositories."
  [repos]
  (->> repos
       (map :topics)
       (keep not-empty)
       (map #(string/split % #","))
       flatten
       (group-by identity)
       (sort-by #(count (val %)))
       (map (fn [[k v]] [k (count v)]))
       reverse
       (take 10)))

(defn- mean_repos_by_orga
  "Return the average number of repositories per organization."
  [orgas]
  (float (/ (reduce + (map :repositories_count orgas))
            (count orgas))))

(defn- median_repos_by_orga
  "Return the median number of repositories per organization."
  [orgas]
  (int (utils/median (map :repositories_count orgas))))

(defn generate-stats-json [repos orgas libs deps sill]
  (let [stats {:repos_cnt         (count repos)
               :orgas_cnt         (count orgas)
               :libs_cnt          (count libs)
               :deps_cnt          (count deps)
               :sill_cnt          (count sill)
               :median_repos_cnt  (median_repos_by_orga orgas)
               :avg_repos_cnt     (mean_repos_by_orga orgas)
               :top_orgs_by_repos (top-orgas-by-repos orgas)
               :top_orgs_by_stars (top-orgas-by-stars repos)
               :top_licenses      (top-licenses repos)
               :top_languages     (top-languages repos)
               :top_topics        (top-topics repos)
               :platforms         (platforms-cnt repos)}]
    (spit "stats.json" (json/write-value-as-string stats))))
