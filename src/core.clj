;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [repos :as repos])
  (:gen-class))

(defn -main [& args]
  (repos/update-repos)
  (println "Created codegouvfr json files"))
