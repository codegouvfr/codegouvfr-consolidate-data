;; Copyright (c) 2020-2023 DINUM, Bastien Guerry <bastien.guerry@code.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

(ns deps
  (:require [jsonista.core :as json]
            [utils :as utils]
            [clojure.data.xml :as xml]
            [clojure.edn :as edn]
            [java-time :as t]
            [clojure.string :as string]
            [toml.core :as toml]
            [taoensso.timbre :as timbre]))

(def dep-files
  {"PHP"        {:files ["composer.json"] :types ["composer"]}
   "Vue"        {:files ["package.json"] :types ["npm"]}
   "JavaScript" {:files ["package.json"] :types ["npm"]}
   "TypeScript" {:files ["package.json"] :types ["npm"]}
   "Python"     {:files ["setup.py" "requirements.txt"] :types ["pypi"]}
   "Ruby"       {:files ["Gemfile"] :types ["bundler"]}
   "Java"       {:files ["pom.xml"] :types ["maven"]}
   "Clojure"    {:files ["pom.xml" "deps.edn" "project.clj"] :types ["maven" "clojars"]}
   "Rust"       {:files ["Cargo.toml"] :types ["crate"]}})

;; FIXME: The server must download
;; https://static.crates.io/db-dump.tar.gz regularily and the
;; application should now the location of crates.csv.
(def crates
  (map #(select-keys % [:name :description :repository])
       (utils/csv-url-to-map "crates.csv")))

(defn flatten-deps [m]
  (-> (fn [[k v]] (map #(assoc {} :type (name k) :library %) v))
      (map m)
      flatten))

;; Check whether libraries are known from various sources

(defn get-valid-npm [library]
  (timbre/info "Fetch info for npm module" library)
  (let [registry-url-fmt "https://registry.npmjs.org/-/v1/search?text=%s&size=1"]
    (when-let [res (utils/get-contents (format registry-url-fmt library))]
      (let [{:keys [description links]}
            (-> (try (utils/json-parse-with-keywords res)
                     (catch Exception _ nil))
                :objects first :package)]
        {:name        library
         :type        "npm"
         :description description
         :repo_url    (:repository links)
         :link        (:npm links)}))))

(defn get-valid-crate [library]
  (timbre/info "Fetch info for crate module" library)
  (when-let [lib (first (seq (filter #(= (:name %) library) crates)))]
    {:name        library
     :type        "crate"
     :description (:description lib)
     :repo_url    (:repository lib)
     :link        (str "https://crates.io/crates/" library)}))

(defn get-valid-pypi [library]
  (timbre/info "Fetch info for pypi module" library)
  (let [registry-url-fmt "https://pypi.org/pypi/%s/json"]
    (when-let [res (utils/get-contents (format registry-url-fmt library))]
      (when-let [{:keys [info]}
                 (try (utils/json-parse-with-keywords res)
                      (catch Exception _ nil))]
        {:name        library
         :type        "pypi"
         :repo_url    (:home_page info)
         :description (:summary info)
         :link        (:package_url info)}))))

;; FIXME: Where to get a proper maven artifact description?
(defn get-valid-maven [library]
  (timbre/info "Fetch info for maven module" library)
  (let [[groupId artifactId] (drop 1 (re-find #"([^/]+)/([^/]+)" library))
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
        {:name        library
         :type        "maven"
         :repo_url    "" ;; FIXME: Where to get this?
         :description (string/join ", " (take 6 tags))
         :link        (format link-fmt groupId artifactId)}))))

(defn get-valid-clojars [library]
  (timbre/info "Fetch info for clojars module" library)
  (let [registry-url-fmt "https://clojars.org/api/artifacts/%s"]
    (when-let [res (utils/get-contents (format registry-url-fmt library))]
      (let [res (try (utils/json-parse-with-keywords res) ;; FIXME?
                     (catch Exception _ nil))]
        {:name        library
         :type        "clojars"
         :repo_url    (:homepage res)
         :description (:description res)
         :link        (str "https://clojars.org/" library)}))))

(defn get-valid-bundler [library]
  (timbre/info "Fetch info for bundler module" library)
  (let [registry-url-fmt "https://rubygems.org/api/v1/gems/%s.json"]
    (when-let [res (utils/get-contents (format registry-url-fmt library))]
      (let [{:keys [info project_uri homepage_uri]}
            (try (utils/json-parse-with-keywords res)
                 (catch Exception _ nil))]
        {:name        library
         :type        "bundler"
         :description info
         :repo_url    homepage_uri
         :link        project_uri}))))

(defn get-valid-composer [library]
  (timbre/info "Fetch info for composer module" library)
  (let [registry-url-fmt "https://packagist.org/packages/%s"]
    (when-let [res (utils/get-contents
                    (str (format registry-url-fmt library) ".json"))]
      (let [res2 (try (utils/json-parse-with-keywords res)
                      (catch Exception _ nil))]
        {:name        library
         :type        "composer"
         :repo_url    (-> res2 :package :repository)
         :description (-> res2 :package :description)
         :link        (format registry-url-fmt library)}))))

;; Get dependencies information

(defn get-packagejson-deps [body]
  (when-let [deps (get (json/read-value body) "dependencies")]
    {:npm (into [] (keys deps))}))

(defn get-composerjson-deps [body]
  (when-let [deps (get (json/read-value body) "require")]
    {:composer (into [] (keys deps))}))

(defn get-setuppy-deps [body]
  (let [body (string/join
              (filter #(not (re-matches #"^#.+$" %))
                      (string/split body #"\n")))]
    (when-let [deps0 (last (re-find #"(?ms)install_requires=\[([^]]+)\]" body))]
      (when-let [deps (filter #(not-empty %)
                              (string/split
                               (string/replace deps0 #"\s+[<=>~]+\s+" "==")
                               #"\s"))]
        (when-let [deps (map #(last (re-find #"'?([^=<>~\[\]]+).*'?" %))
                             deps)]
          {:pypi (into [] (map string/trim deps))})))))

(defn get-requirements-deps [body]
  (when-let [deps0 (not-empty
                    (filter #(not (re-matches #"(^#.+$|^\s*git\+.+$)|^$" %))
                            (string/split body #"\n")))]
    (when-let [deps (->> (map #(last (re-find #"^([^=<>~\[\]]+).+$" %)) deps0)
                         (remove nil?))]
      {:pypi (into [] (map string/trim deps))})))

(defn get-cargo-deps [body]
  (when-let [deps (keys (get (toml/read body) "dependencies"))]
    {:crate (into [] (map string/trim deps))}))

(defn get-gemfile-deps [body]
  (when-let [deps (re-seq #"(?ms)^\s*gem '([^']+)'" body)]
    {:bundler (into [] (map last deps))}))

(defn get-depsedn-deps [body]
  (when-let [deps (->> (map first (:deps (edn/read-string body)))
                       (map str)
                       (filter #(not (re-find #"^org\.clojure" %)))
                       (map symbol)
                       (map name))]
    {:clojars (into [] deps)}))

(defn get-projectclj-deps [body]
  (when-let [deps (->> (edn/read-string body)
                       (drop 3)
                       (apply hash-map)
                       :dependencies
                       (map first)
                       (filter #(not (re-find #"^org\.clojure" (name %))))
                       (map name))]
    {:clojars (into [] deps)}))

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

;; Get dependencies for a repository

(defn get-dependencies
  [{:keys
    [repository_url organization_name is_archived
     name platform language default_branch dependencies]}]
  (if (or (= language "")
          (= is_archived true)
          (not (utils/needs-updating? (:updated dependencies))))
    {:updated   (str (t/instant))
     :libraries []}
    (let [baseurl        (re-find #"https?://[^/]+" repository_url)
          fmt-str        (if (= platform "GitHub")
                           "https://raw.githubusercontent.com/%s/%s/%s/%s"
                           (str baseurl "/%s/%s/-/raw/%s/%s"))
          dep-fnames     (:files (get dep-files language))
          default_branch (or default_branch "master")
          new-deps       (atom {})]
      (doseq [f dep-fnames]
        (when-let [body (utils/get-contents
                         (format fmt-str organization_name name default_branch f))]
          (timbre/info "Fetch dependencies for"
                       (format fmt-str organization_name name default_branch f))
          (try (let [reqs
                     (condp = f
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
                 (swap! new-deps #(merge-with into % reqs)))
               (catch Exception _ nil))))
      {:updated   (str (t/instant))
       :libraries (or (flatten-deps @new-deps) [])})))
