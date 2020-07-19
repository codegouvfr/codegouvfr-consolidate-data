;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [cheshire.core :as json]
            [repos :as repos]
            [orgas :as orgas])
  (:gen-class))

(defn -main []
  (->> (repos/init)
       (sequence (repos/add-data))
       json/generate-string
       (spit "repos.json"))
  (->> (orgas/init)
       (sequence (orgas/add-data))
       json/generate-string
       (spit "orgas.json"))
  (println "Created codegouvfr json files"))
