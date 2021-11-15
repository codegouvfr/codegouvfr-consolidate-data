;; Copyright (c) 2020, 2021 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns deps
  (:require [jsonista.core :as json]
            [utils :as utils]
            [clojure.string :as s]
            [clojure.data.xml :as xml]
            [clojure.edn :as edn]
            [java-time :as t]))

(def dep-files
  {"PHP"        {:files ["composer.json"] :types ["composer"]}
   "Vue"        {:files ["package.json"] :types ["npm"]}
   "JavaScript" {:files ["package.json"] :types ["npm"]}
   "TypeScript" {:files ["package.json"] :types ["npm"]}
   "Python"     {:files ["setup.py" "requirements.txt"] :types ["pypi"]}
   "Ruby"       {:files ["Gemfile"] :types ["bundler"]}
   "Java"       {:files ["pom.xml"] :types ["maven"]}
   "Clojure"    {:files ["pom.xml" "deps.edn" "project.clj"] :types ["maven" "clojars"]}})

(def deps-init
  (let [deps (utils/get-contents "deps-all.json")]
    (->>  deps
          utils/json-parse-with-keywords
          (map #(dissoc % :r)))))

(def grouped-deps
  (atom (group-by (juxt :n :t) deps-init)))

(def deps (atom nil))

;; Utility function

(defn- check-module-of-type-is-known [module type]
  (when-let [res (-> (get @grouped-deps [module type])
                     first not-empty)]
    (when (utils/less-than-x-days-ago 28 (:u res)) res)))

;; Check against valid sources

(defn get-valid-npm [{:keys [n]}]
  (or
   (check-module-of-type-is-known n "npm")
   (do
     (println "Fetch info for npm module" n)
     (let [registry-url-fmt "https://registry.npmjs.org/-/v1/search?text=%s&size=1"]
       (when-let [res (utils/get-contents (format registry-url-fmt n))]
         (when (= (:status res) 200)
           (let [{:keys [description links]}
                 (-> (try (utils/json-parse-with-keywords res)
                          (catch Exception _ nil))
                     :objects first :package)]
             {:n n
              :t "npm"
              :d description
              :l (:npm links)})))))))

(defn get-valid-pypi [{:keys [n]}]
  (or
   (check-module-of-type-is-known n "pypi")
   (do
     (println "Fetch info for pypi module" n)
     (let [registry-url-fmt "https://pypi.org/pypi/%s/json"]
       (when-let [res (utils/get-contents (format registry-url-fmt n))]
         (when-let [{:keys [info]}
                    (try (utils/json-parse-with-keywords res)
                         (catch Exception _ nil))]
           {:n n
            :t "pypi"
            :d (:summary info)
            :l (:package_url info)}))))))

;; FIXME: Where to get a proper maven artifact description?
(defn get-valid-maven [{:keys [n]}]
  (or
   (check-module-of-type-is-known n "maven")
   (do
     (println "Fetch info for maven module" n)
     (let [[groupId artifactId] (drop 1 (re-find #"([^/]+)/([^/]+)" n))
           registry-url-fmt
           "https://search.maven.org/solrsearch/select?q=g:%%22%s%%22+AND+a:%%22%s%%22&core=gav&rows=1&wt=json"
           link-fmt
           "https://search.maven.org/classic/#search|ga|1|g:%%22%s%%22%%20AND%%20a:%%22%s%%22"]
       (when-let [res (utils/get-contents
                       (format registry-url-fmt groupId artifactId))]
         (when-let [tags (not-empty
                          (-> (try (utils/json-parse-with-keywords res)
                                   (catch Exception _ nil))
                              :response
                              :docs
                              first
                              :tags))]
           {:n n
            :t "maven"
            :d (s/join ", " (take 6 tags))
            :l (format link-fmt groupId artifactId)}))))))

(defn get-valid-clojars [{:keys [n]}]
  (or
   (check-module-of-type-is-known n "clojars")
   (do
     (println "Fetch info for clojars module" n)
     (let [registry-url-fmt "https://clojars.org/api/artifacts/%s"]
       (when-let [res (utils/get-contents (format registry-url-fmt n))]
         {:n n
          :t "clojars"
          :d (:d (try (utils/json-parse-with-keywords res) ;; FIXME?
                      (catch Exception _ nil)))
          :l (str "https://clojars.org/" n)})))))

(defn get-valid-bundler [{:keys [n]}]
  (or
   (check-module-of-type-is-known n "bundler")
   (do
     (println "Fetch info for bundler module" n)
     (let [registry-url-fmt "https://rubygems.org/api/v1/gems/%s.json"]
       (when-let [res (utils/get-contents (format registry-url-fmt n))]
         (let [{:keys [info project_uri]}
               (try (utils/json-parse-with-keywords res)
                    (catch Exception _ nil))]
           {:n n
            :t "bundler"
            :d info
            :l project_uri}))))))

(defn get-valid-composer [{:keys [n]}]
  (or
   (check-module-of-type-is-known n "composer")
   (do
     (println "Fetch info for composer module" n)
     (let [registry-url-fmt "https://packagist.org/packages/%s"]
       (when-let [res (utils/get-contents
                       (str (format registry-url-fmt n) ".json"))]
         {:n n
          :t "composer"
          :d (-> (try (utils/json-parse-with-keywords res)
                      (catch Exception _ nil))
                 :package
                 :description)
          :l (format registry-url-fmt n)})))))

;; Get dependencies info

(defn get-packagejson-deps [body]
  (let [parsed (json/read-value body)
        deps   (get parsed "dependencies")]
    (when (seq deps)
      {:npm (into [] (keys deps))})))

(defn get-composerjson-deps [body]
  (let [parsed (json/read-value body)
        deps   (get parsed "require")]
    (when (seq deps)
      {:composer (into [] (keys deps))})))

(defn get-setuppy-deps [body]
  (let [deps0 (last (re-find #"(?ms)install_requires=\[([^]]+)\]" body))]
    (when (seq deps0)
      (let [deps (map #(get % 1) (re-seq #"'([^>\n]+)(>=.+)?'" deps0))]
        (when (seq deps)
          {:pypi (into [] (map s/trim deps))})))))

(defn get-requirements-deps [body]
  (when (not-empty body)
    (let [deps (map last (re-seq #"(?m)^([^=]+)==.+" body))]
      (when (seq deps)
        {:pypi (into [] (map s/trim deps))}))))

(defn get-gemfile-deps [body]
  (let [deps (re-seq #"(?ms)^gem '([^']+)'" body)]
    (when (seq deps)
      {:bundler (into [] (map last deps))})))

(defn get-depsedn-deps [body]
  (let [deps (->> (map first (:deps (edn/read-string body)))
                  (map str)
                  (filter #(not (re-find #"^org\.clojure" %)))
                  (map symbol)
                  (map name))]
    (when deps {:clojars (into [] deps)})))

(defn get-projectclj-deps [body]
  (let [deps (->> (edn/read-string body)
                  (drop 3)
                  (apply hash-map)
                  :dependencies
                  (map first)
                  (filter #(not (re-find #"^org\.clojure" (name %))))
                  (map name))]
    (when deps {:clojars (into [] deps)})))

(defn get-pomxml-deps [body]
  (when-let [deps0 (try (not-empty
                         (filter #(= (name (:tag %)) "dependencies")
                                 (->> (:content (xml/parse-str body))
                                      (remove string?))))
                        (catch Exception _ nil))]
    (let [deps (->> deps0 first :content
                    (remove string?)
                    (map #(let [[g a] (remove string? (:content %))]
                            (str (first (:content g)) "/"
                                 (first (:content a)))))
                    (remove nil?)
                    flatten)]
      (when (seq deps)
        {:maven (into [] deps)}))))

;; Core function

(defn add-dependencies
  "Take a repository map and return the map completed with dependencies."
  [{:keys
    [repository_url organization_name is_archived
     name platform language deps_updated] :as repo}]
  (if (or (= language "")
          (= is_archived true)
          (when-let [d (not-empty deps_updated)]
            (utils/less-than-x-days-ago 14 d)))
    repo
    (let [baseurl    (re-find #"https?://[^/]+" repository_url)
          fmt-str    (if (= platform "GitHub")
                       "https://raw.githubusercontent.com/%s/%s/master/%s"
                       (str baseurl "/%s/%s/-/raw/master/%s"))
          dep-fnames (:files (get dep-files language))
          new-deps   (atom {})]
      (doseq [f dep-fnames]
        (when-let [body (utils/get-contents (format fmt-str organization_name name f))]
          (println "Fetching dependencies for" (format fmt-str organization_name name f))
          (let [reqs (condp = f
                       "package.json"
                       (get-packagejson-deps body)
                       "composer.json"
                       (get-composerjson-deps body)
                       "setup.py"
                       (get-setuppy-deps body)
                       "requirements.txt"
                       (get-requirements-deps body)
                       "Gemfile"
                       (get-gemfile-deps body)
                       "deps.edn"
                       (get-depsedn-deps body)
                       "project.clj"
                       (get-projectclj-deps body)
                       "pom.xml"
                       (get-pomxml-deps body))]
            (swap! new-deps #(merge-with into % reqs)))))
      (assoc repo
             :deps (utils/flatten-deps @new-deps)
             :deps_updated (str (t/instant))))))

;; Compute dep similarity

(defn jaccard-coefficient [a b]
  (/ (count (filter (into #{} b) a))
     (* 1.0 (count b))))

(defn jaccard-distance [a b]
  (- 1 (jaccard-coefficient a b)))

(defn get-jaccard-distance [m]
  (map #(hash-map
         (key %)
         (->> m
              (map (fn [[k v]]
                     (hash-map
                      k (jaccard-distance
                         (val %) v))))
              (apply merge)
              (sort-by val)
              reverse
              ;; (filter (fn [[r v]] (< 0.1 v)))
              (take 5)
              keys))
       m))

(defn- compute-similarity [deps-all repos lang]
  (let [deps0     (map #(select-keys % [:n :t]) deps-all)
        dep-types (into #{} (:types (get dep-files lang)))
        deps-lang (filter #(contains? dep-types (:t %)) deps0)]
    (->> (map (fn [{:keys [repository_url deps]}]
                (when (not-empty deps)
                  (hash-map repository_url
                            (->> (map #(if (contains? (into #{} deps) %)
                                         (hash-map (:n %) 1)
                                         (hash-map (:n %) 0))
                                      deps-lang)
                                 (apply merge)))))
              repos)
         (remove nil?)
         (apply merge))))

(defn spit-deps-repos-similarity [repos deps]
  (let [repos-by-lang
        (select-keys (group-by :language repos)
                     ["Python" "Java" "Clojure" "Ruby" "PHP"
                      "Javascript" "TypeScript" "Vue"])]
    (->> repos-by-lang
         (map (fn [[lang repos]]
                (hash-map
                 lang
                 (get-jaccard-distance
                  (compute-similarity deps repos lang)))))
         (map vals)
         flatten
         (apply merge)
         (filter #(not-empty (val %)))
         (into {})
         json/write-value-as-string
         (spit "deps-repos-sim.json"))
    (println "Updated deps-repos-sim.json")))
