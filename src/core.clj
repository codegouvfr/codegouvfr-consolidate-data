;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [repos :as repos]
            [orgas :as orgas]
            ;; [deps :as deps]
            )
  (:gen-class))

(defn -main []
  (orgas/init)
  (repos/init)
  (repos/add-reuse-info)
  ;; (deps/update-orgas-repos-deps)
  ;; (deps/update-deps)
  ;; (repos/update-repos)
  ;; (orgas/update-orgas)
  (println "Created codegouvfr json files"))
