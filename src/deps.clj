;; Copyright (c) 2020 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns deps
  (:require  [cheshire.core :as json]
             [clj-http.lite.client :as http]
             [clojure.string :as s]
             [clojure.set]
             [hickory.core :as h]
             [hickory.select :as hs])
  (:gen-class))

(defonce http-get-params {:cookie-policy :standard})
(defonce bys-url "http://localhost:3006/")

(defn get-deps
  "Scrap backyourstack to get dependencies of an organization."
  [orga]
  (if-let [deps (try (http/get
                      (str bys-url orga "/dependencies")
                      http-get-params)
                     (catch Exception e nil))]
    (let [out (-> deps
                  :body
                  h/parse
                  h/as-hickory
                  (as-> dps (hs/select (hs/id "__NEXT_DATA__") dps))
                  first
                  :content
                  first
                  (json/parse-string true)
                  :props
                  :pageProps)]
      (when-not (:error out) out))))

(defn extract-deps-repos
  [orga]
  (let [s-deps #(select-keys
                 (clojure.set/rename-keys
                  % {:type :t :name :n :core :c :dev :d})
                 [:t :n :c :d])]
    (comp
     (filter #(seq (:dependencies %)))
     (map #(select-keys % [:name :dependencies]))
     (map #(clojure.set/rename-keys % {:name :n :dependencies :d}))
     (map #(assoc % :d (map s-deps (:d %))))
     (map #(assoc % :g orga)))))

(defonce extract-orga-deps
  (comp
   (map #(apply dissoc % [:project :peer :engines]))
   (map (fn [r]
          (let [rs (:repos r)]
            (assoc r :repos (map #(dissoc % :id) rs)))))))

(defn merge-colls [a b]
  (if (and (coll? a) (coll? b)) (into a b) b))

(defn merge-colls-or-add [a b]
  (cond (and (coll? a) (coll? b))       (into a b)
        (and (integer? a) (integer? b)) (+ a b)
        :else
        b))

(defonce reduce-deps
  (comp
   (map #(apply (partial merge-with merge-colls) %))
   (map #(update-in % [:rs] count))))

(defn update-orgas-repos-deps
  "Generate deps/orgas/* and deps/repos-deps.json."
  []
  (let [orgas      (json/parse-string
                    (try (slurp "orgas.json")
                         (catch Exception e
                           (println "ERROR: No orgas.json file")))
                    true)
        repos      (json/parse-string
                    (try (slurp "repos.json")
                         (catch Exception e
                           (println "ERROR: No repos.json file")))
                    true)
        orgas-deps (atom nil)
        repos-deps (atom nil)]
    ;; Loop over GitHub orgas with a login and spit deps/orgas/
    (doseq [orga (map :l (filter #(= (:p %) "GitHub") orgas))]
      (if-let [data (get-deps orga)]
        (let [orga-deps  (sequence extract-orga-deps (:dependencies data))
              orga-repos (sequence (extract-deps-repos orga) (:repos data))]
          (swap! orgas-deps (partial apply conj) orga-deps)
          (reset! orgas-deps
                  (map (fn [[k v]]
                         (apply (partial merge-with merge-colls-or-add) v))
                       (group-by :name @orgas-deps)))
          (swap! repos-deps (partial apply conj) orga-repos)
          (spit (str "deps/orgas/" (s/lower-case orga) ".json")
                (json/generate-string orga-deps))
          ;; Group dependencies and spit deps-repos.json
          (spit (str "deps/deps-repos.json")
                (json/generate-string
                 (map (fn [dep]
                        (clojure.set/rename-keys
                         (assoc
                          dep :repos
                          (map (fn [r]
                                 (first (filter #(s/includes?
                                                  (:r %) (:full_name r)) repos)))
                               (:repos dep)))
                         {:type :t :name :n :repos :rs :core :c :dev :d}))
                      @orgas-deps)))
          ;; Spit the short version with no repositories
          (spit (str "deps/deps.json")
                (json/generate-string
                 (map (fn [d] (clojure.set/rename-keys
                               d {:type :t :name :n :core :c :dev :d}))
                      (map #(dissoc % :repos) @orgas-deps))))
          ;; All dependencies grouped by repos
          (spit (str "deps/repos-deps.json")
                (json/generate-string @repos-deps))
          (println (str "Updated orgas dependencies and "
                        (count @repos-deps) " repos dependencies")))))))

(defn update-deps
  "Generate deps/deps-total.json and deps/deps-top.json."
  []
  (let [deps (atom nil)]
    (doseq [repo (json/parse-string
                  (try (slurp "deps/repos-deps.json")
                       (catch Exception e nil))
                  true)
            :let [r-deps (:d repo)]]
      (doseq [d0   r-deps
              :let [d (apply dissoc d0 [:core :dev :peer :engines])]]
        (swap! deps conj (assoc d :rs (vector (dissoc repo :d))))))
    (reset!
     deps
     (reverse
      (sort-by :rs (sequence reduce-deps (vals (group-by :n @deps))))))
    (spit "deps/deps-total.json"
          (json/generate-string {:deps-total (count @deps)}))
    (spit "deps/deps-top.json"
          (json/generate-string (take 100 @deps)))
    (println
     (str "Updated deps-top and deps-total (" (count @deps) ")"))))
