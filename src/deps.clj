;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns deps
  (:require  [cheshire.core :as json]
             [semantic-csv.core :as semantic-csv]
             [clj-http.lite.client :as http]
             [ring.util.codec :as codec]
             [clojure.java.io :as io]
             [clojure.string :as s]
             [clojure.set]
             [hickory.core :as h]
             [hickory.select :as hs])
  (:gen-class))

(defonce http-get-params {:cookie-policy :standard})

(defonce
  ^{:doc "A list of repositories dependencies, updated by the fonction
  `update-orgas-repos-deps` and stored for further retrieval in
  `update-deps`."}
  repos-deps
  (atom nil))

(defn get-deps
  "Scrap backyourstack to get dependencies of an organization."
  [orga]
  (if-let [deps (try (http/get
                      (str "https://backyourstack.com/" orga "/dependencies")
                      http-get-params)
                     (catch Exception e
                       (println (str "Can't get dependencies: "
                                     (:cause (Throwable->map e))))))]
    (-> deps
        :body
        h/parse
        h/as-hickory
        (as-> dps (hs/select (hs/child (hs/id :__NEXT_DATA__)) dps))
        first
        :content
        first
        (json/parse-string true)
        :props
        :pageProps)))

(defn extract-deps-repos
  [orga]
  (let [s-deps #(select-keys
                 (clojure.set/rename-keys % {:type :t :name :n})
                 [:t :n :core :dev])]
    (comp
     (filter #(not (empty? (:dependencies %))))
     (map #(select-keys % [:name :dependencies]))
     (map #(clojure.set/rename-keys % {:name :n :dependencies :d}))
     (map #(assoc % :d (map (fn [r] (s-deps r)) (:d %))))
     (map #(assoc % :g orga)))))

(defonce extract-orga-deps
  (comp
   (map #(apply dissoc % [:project :peer :engines]))
   (map (fn [r]
          (let [rs (:repos r)]
            (assoc r :repos (map #(dissoc % :id) rs)))))))

(defn merge-colls [a b]
  (if (and (coll? a) (coll? b)) (into a b) b))

(defonce reduce-deps
  (comp
   (map #(apply (partial merge-with merge-colls) %))
   (map #(assoc % :rs (count (:rs %))))))

(defn update-deps
  "Generate deps/deps*.json."
  []
  (let [deps (atom nil)]
    (doseq [repo @repos-deps :let [r-deps (:d repo)]]
      (doseq [d0   r-deps
              :let [d (apply dissoc d0 [:core :dev :peer :engines])]]
        (swap! deps conj (assoc d :rs (vector (dissoc repo :d))))))
    (reset! deps
            (reverse (sort-by :rs (sequence reduce-deps
                                            (vals (group-by :n @deps))))))
    (spit "deps/deps-total.json"
          (json/generate-string {:deps-total (count @deps)}))
    (spit "deps/deps-top.json"
          (json/generate-string (take 100 @deps)))
    (println (str "updated deps-top and deps-total ("
                  (count @deps) ")"))))