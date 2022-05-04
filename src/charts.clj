;; Copyright (c) 2020, 2022 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns charts
  (:require [utils :as utils]
            [clojure.walk :as walk]
            [clojure.java.io :as io]
            [clojure.data.json :as datajson]))

(defn licenses-vega-data
  "Produce vega data from license stats."
  []
  (let [l0       (:top_licenses (utils/get-contents-json-to-kwds
                                 (:stats utils/urls)))
        l1       (map #(zipmap [:License :Number] %)
                      (walk/stringify-keys
                       (dissoc l0 :Inconnue)))
        licenses (map #(assoc % :License
                              (get (:licenses-spdx utils/mappings)
                                   (:License %))) l1)]
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
