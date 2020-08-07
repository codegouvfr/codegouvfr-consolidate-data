(ns reuse
  (:require [jsonista.core :as json]
            [clojure.walk :as walk]
            [java-time :as t]
            [clojure.edn :as edn]
            [utils :as utils]
            [hickory.core :as h]
            [hickory.select :as hs]))

(defn- get-reuse
  "Return a hash-map with reuse information"
  [repertoire_url]
  (when-let [repo-github-html
             (utils/get-contents (str repertoire_url "/network/dependents"))]
    (println "Getting dependents for" repertoire_url)
    (let [updated   (str (t/instant))
          btn-links (-> repo-github-html
                        h/parse
                        h/as-hickory
                        (as-> d (hs/select (hs/class "btn-link") d)))
          nb-reps   (or (try (re-find #"\d+" (last (:content (nth btn-links 1))))
                             (catch Exception _ "0")) "0")
          nb-pkgs   (or (try (re-find #"\d+" (last (:content (nth btn-links 2))))
                             (catch Exception _ "0")) "0")]
      (hash-map
       repertoire_url
       {:u updated
        :r (+ (edn/read-string nb-reps)
              (edn/read-string nb-pkgs))}))))

(defn- add-reuse
  "Return a hash-map entry with the repo URL and the reuse information."
  [{:keys [repertoire_url]} reused]
  (if-let [{:keys [u] :as entry}
           (walk/keywordize-keys
            (get reused repertoire_url))]
    (if (utils/less-than-x-days-ago 14 u)
      (hash-map repertoire_url entry)
      (get-reuse repertoire_url))
    (get-reuse repertoire_url)))

(defn spit-info
  "Generate reuse.json with GitHub reused-by information."
  [repos]
  (let [reused
        (when-let [res (utils/get-contents "reuse.json")]
          (json/read-value res))]
    (->> repos
         (filter #(= (:plateforme %) "GitHub"))
         (map #(add-reuse % reused))
         (apply merge)
         json/write-value-as-string
         (spit "reuse.json")))
  (println "Added reuse information and stored it in reuse.json"))
