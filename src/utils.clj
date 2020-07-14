(ns utils
  (:require [clojure.data.csv :as csv]
            [babashka.curl :as curl]))

(defn- rows->maps [csv]
  (let [headers (map keyword (first csv))
        rows    (rest csv)]
    (map #(zipmap headers %) rows)))

(defn csv-url-to-map [url]
  (rows->maps (csv/read-csv (:body (curl/get url)))))
