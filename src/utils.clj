;; Copyright (c) 2020, 2021 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns utils
  (:require [jsonista.core :as json]
            [clojure.data.csv :as csv]
            [clojure.data.json :as datajson]
            [clojure.walk :as walk]
            [babashka.curl :as curl]
            [java-time :as t]
            [clojure.java.io :as io]))

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
                    (println
                     (str "Error while getting contents for " s ":")
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

;; (defonce stats-url "https://api-code.etalab.gouv.fr/api/stats/general")
(defonce stats-url "stats.json")

(defonce licenses-spdx
  {"Other"                                                      "Other"
   "MIT License"                                                "MIT"
   "GNU Affero General Public License v3.0"                     "AGPL-3.0"
   "GNU General Public License v3.0"                            "GPL-3.0"
   "GNU Lesser General Public License v2.1"                     "LGPL-2.1"
   "Apache License 2.0"                                         "Apache-2.0"
   "GNU General Public License v2.0"                            "GPL-2.0"
   "GNU Lesser General Public License v3.0"                     "LGPL-3.0"
   "Mozilla Public License 2.0"                                 "MPL-2.0"
   "Eclipse Public License 2.0"                                 "EPL-2.0"
   "Eclipse Public License 1.0"                                 "EPL-1.0"
   "BSD 3-Clause \"New\" or \"Revised\" License"                "BSD-3-Clause"
   "European Union Public License 1.2"                          "EUPL-1.2"
   "Creative Commons Attribution Share Alike 4.0 International" "CC-BY-SA-4.0"
   "BSD 2-Clause \"Simplified\" License"                        "BSD-2-Clause"
   "The Unlicense"                                              "Unlicense"
   "Do What The Fuck You Want To Public License"                "WTFPL"
   "Creative Commons Attribution 4.0 International"             "CC-BY-4.0"})

(defn licenses-vega-data []
  (let [l0       (:top_licenses
                  (json-parse-with-keywords
                   (try (get-contents stats-url)
                        (catch Exception e
                          (println
                           (str "Cannot get stats\n"
                                (.getMessage e)))))))
        l1       (map #(zipmap [:License :Number] %)
                      (walk/stringify-keys
                       (dissoc l0 :Inconnue)))
        licenses (map #(assoc % :License (get licenses-spdx (:License %))) l1)]
    {:title    "Most used licenses"
     :data     {:values licenses}
     :encoding {:x     {:field "Number" :type "quantitative"
                        :axis  {:title "Number of repoitories"}}
                :y     {:field "License" :type "ordinal" :sort "-x"
                        :axis  {:title         false
                                :labelLimit    200
                                :offset        10
                                :maxExtent     100
                                :labelFontSize 15
                                :labelAlign    "right"}}
                :color {:field  "License"
                        :legend false
                        :type   "nominal"
                        :title  "Licenses"
                        :scale  {:scheme "tableau20"}}}
     :width    600
     :height   600
     :mark     {:type "bar" :tooltip {:content "data"}}}))

(defn temp-json-file
  "Convert `clj-vega-spec` to json and store it as tmp file."
  [clj-vega-spec]
  (let [tmp-file (java.io.File/createTempFile "vega." ".json")]
    (.deleteOnExit tmp-file)
    (with-open [file (io/writer tmp-file)]
      (datajson/write clj-vega-spec file))
    (.getAbsolutePath tmp-file)))

(defn generate-licenses-chart []
  (temp-json-file (licenses-vega-data)))
