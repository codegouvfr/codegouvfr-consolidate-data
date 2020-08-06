(ns utils
  (:require [jsonista.core :as json]
            [clojure.data.csv :as csv]
            [babashka.curl :as curl]))

(defn json-parse-with-keywords [s]
  (-> s
      (json/read-value
       (json/object-mapper {:decode-key-fn keyword}))))

(defn rows->maps [csv]
  (let [headers (map keyword (first csv))
        rows    (rest csv)]
    (map #(zipmap headers %) rows)))

(defn csv-url-to-map [url]
  (try
    (rows->maps (csv/read-csv (:body (curl/get url))))
    (catch Exception e
      (println (.getMessage e)))))

(defn get-body [url]
  (:body
   (try (curl/get url)
        (catch Exception e
          (println (.getMessage e))))))



