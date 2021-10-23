;; Copyright (c) 2020, 2021 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns utils
  (:require [jsonista.core :as json]
            [clojure.data.csv :as csv]
            [babashka.curl :as curl]
            [java-time :as t]))

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

(defn get-contents [s]
  (let [url? (re-find #"https://" s)
        res  (try (apply (if url? curl/get slurp) [s])
                  (catch Exception e
                    (println (str "Error while getting contents for "
                                  s ":")
                             (.getMessage e))))]
    (if (and url? (= (:status res) 200))
      (:body res)
      res)))

(defn less-than-x-days-ago [^Integer days ^String date-str]
  (try
    (t/before? (t/minus (t/instant) (t/days days))
               (t/instant date-str))
    (catch Exception e
      (println (.getMessage e)))))

(defn flatten-deps [m]
  (-> (fn [[k v]] (map #(assoc {} :t (name k) :n %) v))
      (map m)
      flatten))

(defn get-all-deps [m]
  (->> m
       (map :deps)
       flatten
       (map #(dissoc % :u :d :l))
       distinct))
