(ns rss
  (:require [utils :as utils]
            [clojure.instant :as instant]
            [clj-rss.core :as rss]))

(defonce ^{:doc "The URL for the latest repositories."}
  latest-repositories-url
  "https://api-code.etalab.gouv.fr/api/stats/last_repositories")

(defn latest-repositories []
  (utils/json-parse-with-keywords
   (utils/get-contents latest-repositories-url)))

(defn make-feed
  "Generate a RSS feed from `lastest-repositories`."
  []
  (->>
   (rss/channel-xml
    {:title       "code.gouv.fr - derniers dépôts"
     :link        "https://code.etalab.gouv.fr/latest.xml"
     :description "Derniers dépôts ajoutés"}
    (map (fn [item]
           {:title       (:name item)
            :link        (:repository_url item)
            :description (:description item)
            :author      (:organization_name item)
            :pubDate     (instant/read-instant-date
                          (:last_update item))})
         (latest-repositories)))
   (spit "latest.xml"))
  (println "Updated latest.xml"))
