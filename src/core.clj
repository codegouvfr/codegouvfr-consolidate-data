;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns core
  (:require [repos :as repos]
            [orgas :as orgas]
            [deps :as deps]
            [cheshire.core :as json]
            [clojure.string :as s])
  (:gen-class))

(defn update-orgas-repos-deps
  "Generate deps/orgas/* and deps/repos-deps.json.
  Also reset the `repos-deps` atom."
  []
  (reset! deps/repos-deps nil)
  (let [gh-orgas (map :login (filter #(= (:plateforme %) "GitHub") @orgas/orgas-json))]
    (doseq [orga gh-orgas]
      (if-let [data (deps/get-deps orga)]
        (let [orga-deps  (sequence deps/extract-orga-deps (:dependencies data))
              orga-repos (sequence (deps/extract-deps-repos orga) (:repos data))]
          (spit (str "deps/orgas/" (s/lower-case orga) ".json")
                (json/generate-string orga-deps))
          (swap! deps/repos-deps (partial apply conj) orga-repos))))
    (spit (str "deps/repos-deps.json")
          (json/generate-string @deps/repos-deps))
    (println (str "updated orgas dependencies and "
                  (count @deps/repos-deps) " repos dependencies"))))

(defn -main [& args]
  (orgas/update-orgas-json)
  (update-orgas-repos-deps)
  (deps/update-deps)
  (repos/update-repos)
  (orgas/update-orgas)
  (println "Created codegouvfr json files"))

;; (-main)

