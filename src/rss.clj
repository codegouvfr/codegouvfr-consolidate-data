;; Copyright (c) 2020-2023 DINUM, Bastien Guerry <bastien.guerry@code.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns rss
  (:require [clj-rss.core :as rss]
            [java-time :as t]
            [taoensso.timbre :as timbre]))

;; FIXME: A lot of redundant code below.
(defn latest-repositories [repos]
  (let [repos (->> repos
                   (sort-by #(t/instant (:creation_date %)))
                   reverse
                   (take 20))]
    (->>
     (rss/channel-xml
      {:title       "code.gouv.fr - Nouveaux dépôts - New repositories"
       :link        "https://code.etalab.gouv.fr/data/latest.xml"
       :description "code.gouv.fr - Nouveaux dépôts - New repositories"}
      (map (fn [item]
             (let [link (:repository_url item)]
               {:title       (str "Nouveau dépôt : " (:name item))
                :link        link
                :guid        link
                :description (:description item)
                :author      (:organization_name item)
                :pubDate     (t/instant (:creation_date item))}))
           repos))
     (spit "latest.xml"))
    (timbre/info "Updated latest.xml")))

(defn latest-organizations [orgas]
  (let [orgas (->> orgas
                   (filter #(not-empty (:creation_date %)))
                   (sort-by #(t/instant (:creation_date %)))
                   reverse
                   (take 20))]
    (->>
     (rss/channel-xml
      {:title       "code.gouv.fr - Nouvelles organisations - New Organizations"
       :link        "https://code.etalab.gouv.fr/data/latest-organizations.xml"
       :description "code.gouv.fr - Nouvelles organisations - New Organizations"}
      (map (fn [item]
             (let [link (:organization_url item)]
               {:title       (str "Nouvelle organisation : " (:name item))
                :link        link
                :guid        link
                :description (:description item)
                :author      (:name item)
                :pubDate     (t/instant (:creation_date item))}))
           orgas))
     (spit "latest-organizations.xml"))
    (timbre/info "Updated latest-organizations.xml")))

(defn latest-dependencies [dependencies]
  (let [dependencies (->> dependencies
                          (sort-by #(t/instant (:updated %)))
                          reverse
                          (take 20))]
    (->>
     (rss/channel-xml
      {:title       "code.gouv.fr - Nouvelles dépendances - New dependencies"
       :link        "https://code.etalab.gouv.fr/data/latest-dependencies.xml"
       :description "code.gouv.fr - Nouvelles dépendances - New dependencies"}
      (map (fn [item]
             (let [link (:link item)]
               {:title       (str "Nouvelle dépendance identifiée :" (name (:dep_id item)))
                :link        link
                :guid        link
                :description (:description item)
                :author      (name (:dep_id item))
                :pubDate     (t/instant (:updated item))}))
           dependencies))
     (spit "latest-dependencies.xml"))
    (timbre/info "Updated latest-dependencies.xml")))

(defn latest-libraries [libraries]
  (let [libraries
        (->> (filter #(not= (:latest_stable_release_published_at %) "None")
                     libraries)
             (sort-by #(t/instant (:latest_stable_release_published_at %)))
             reverse
             (take 20))]
    (->>
     (rss/channel-xml
      {:title       "code.gouv.fr - Dernières bibliothèques - Latest libraries"
       :link        "https://code.etalab.gouv.fr/data/latest-libraries.xml"
       :description "code.gouv.fr - Dernières bibliothèques - Latest libraries"}
      (map (fn [item]
             (let [link (:repo_url item)]
               {:title       (str "Nouvelle bibliothèque : " (name (:lib_id item)))
                :link        link
                :guid        link
                :description (:description item)
                :author      (name (:lib_id item))
                :pubDate     (t/instant ;; (:latest_stable_release_published_at item)
                              )}))
           libraries))
     (spit "latest-libraries.xml"))
    (timbre/info "Updated latest-libraries.xml")))

(defn latest-tags [tags]
  (let [tags (->> tags
                  ;; Only take tags with a correct timestamp
                  (filter #(try (t/instant (:date %)) (catch Exception _ nil)))
                  (sort-by #(t/instant (:date %)))
                  reverse
                  (take 50))]
    (->>
     (rss/channel-xml
      {:title       "code.gouv.fr - Nouveaux tags - New tags"
       :link        "https://code.etalab.gouv.fr/data/latest-tags.xml"
       :description "code.gouv.fr - Nouveaux tags - New tags"}
      (map (fn [item]
             (let [link (:url item)]
               {:title       (str "Nouvelle version : " (:repo_name item) " - " (:name item))
                :link        link
                :guid        link
                :description (:title item)
                :author      (:committer item)
                :pubDate     (t/instant (:date item))}))
           tags))
     (spit "latest-tags.xml"))
    (timbre/info "Updated latest-tags.xml")))

(defn latest-sill [sill]
  (let [sill (->> sill
                  (sort-by #(t/instant (java.util.Date. (:referencedSinceTime %))))
                  reverse
                  (take 10))]
    (->>
     (rss/channel-xml
      {:title       "code.gouv.fr - Nouveaux logiciels libres au SILL - New SILL entries"
       :link        "https://code.gouv.fr/data/latest-sill.xml"
       :description "code.gouv.fr - Nouveaux logiciels libres au SILL - New SILL entries"}
      (map (fn [item]
             (let [link (str "https://sill.code.gouv.fr/software?name=" (:name item))]
               {:title       (str "Nouveau logiciel au SILL : " (:name item))
                :link        link
                :guid        link
                :description (:function item)
                :pubDate     (t/instant (java.util.Date. (:referencedSinceTime item)))}))
           sill))
     (spit "latest-sill.xml"))
    (timbre/info "Updated latest-sill.xml")))
