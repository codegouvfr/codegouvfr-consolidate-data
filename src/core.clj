;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [jsonista.core :as json]
            [repos :as repos]
            [orgas :as orgas])
  (:gen-class))

(defn -main []
  (->> (repos/init)
       (sequence (repos/add-data))
       json/write-value-as-string
       (spit "repos.json"))
  (->> (orgas/init)
       (sequence (orgas/add-data))
       json/write-value-as-string
       (spit "orgas.json"))
  (println "Created codegouvfr json files"))
