;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [repos :as repos]
            [orgas :as orgas])
  (:gen-class))

(defn -main [& args]
  (repos/update-repos)
  (orgas/update-orgas-json)
  (orgas/update-orgas)
  (println "Created codegouvfr json files"))
