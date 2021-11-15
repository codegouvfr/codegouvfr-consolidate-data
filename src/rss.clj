(ns rss
  (:require [utils :as utils]
            [clj-rss.core :as rss]
            [java-time :as t]))

(defonce urls
  {:repos        "repositories/json/all.json"
   :repos-remote "https://code.gouv.fr/data/repositories/json/all.json"})

(defn latest-repositories []
  (->> (or (utils/get-contents (:repos urls))
           (utils/get-contents (:repos-remote urls)))
       utils/json-parse-with-keywords
       (sort-by #(t/instant (:creation_date %)))
       reverse
       (take 1)))

(defn make-feed
  "Generate a RSS feed from `lastest-repositories`."
  []
  (->>
   (rss/channel-xml
    {:title       "code.gouv.fr - Nouveaux dépôts - New repositories"
     :link        "https://code.etalab.gouv.fr/latest.xml"
     :description "code.gouv.fr - Nouveaux dépôts - New repositories"}
    (map (fn [item]
           {:title       (:name item)
            :link        (:repository_url item)
            :description (:description item)
            :author      (:organization_name item)
            :pubDate     (t/instant (:last_update item))})
         (latest-repositories)))
   (spit "latest.xml"))
  (println "Updated latest.xml"))
