;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [cheshire.core :as json]
            [repos :as repos]
            [orgas :as orgas])
  (:gen-class))

(defn -main []
  (let [repos (repos/init)
        orgas (orgas/init)]
    (spit "repos.json"
          (json/generate-string (sequence (repos/add-data) repos)))
    (spit "orgas.json"
          (json/generate-string (sequence (orgas/add-data) orgas))))
  (println "Created codegouvfr json files"))
