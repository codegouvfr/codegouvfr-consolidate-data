;; Copyright (c) 2022 DINUM, Bastien Guerry <bastien.guerry@data.gouv.fr>
;; SPDX-License-Identifier: EPL-2.0
;; License-Filename: LICENSE

;; https://git.sr.ht/~etalab/codegouvfr-fetch-data should be the place
;; to fetch organizations and repositories from SourceHut. If you want
;; to contribute a patch against codegouvfr-fetch-data, please do!

(ns hut
  (:require [utils :as utils]
            [babashka.curl :as curl]
            [taoensso.timbre :as timbre]))

(def hut-token (System/getenv "CODEGOUVFR_HUT_TOKEN"))

(defn- hut-parameters [d]
  (update (update utils/user-agent :raw-args merge
                  "--oauth2-bearer" hut-token
                  "--data"          d)
          :headers conj {"content-type" "application/json"}))

(defn- query-hut-api [subdomain q]
  (Thread/sleep 1000)
  (let [res (try (curl/get (str "https://" subdomain ".sr.ht/query")
                           (hut-parameters q))
                 (catch Exception e
                   (timbre/error
                    (format "Error while querying Hut (%s) for %s" subdomain q)
                    (.getMessage e))))]
    (when (= (:status res) 200)
      (:body res))))

(defn- hut-orga-query [orga]
  (format "{\"query\" : \"{ userByName (username: \\\"%s\\\") { id email created url location bio } }\"}" orga))

(defn- hut-repo-query [orga]
  (format "{\"query\" : \"{ user (username: \\\"%s\\\") { repositories { results { id name created updated description visibility HEAD { name } } } } }\"}" orga))

(defn- get-hut-orgas []
  (->> utils/sources
       (filter (fn [[k _]] (re-find #"https://sr.ht.*" k)))))

(defn fetch []
  (let [orgas-data (atom ())
        repos-data (atom ())]
    (doseq [[orga-url orga-source-data] (get-hut-orgas)]
      (let [orga-name    (peek (re-find #"^.*~(.*)$" orga-url))
            floss_policy (last (get orga-source-data "floss_policy"))
            service_of   (last (get orga-source-data "service_of"))
            r-data       (->> (utils/json-parse-with-keywords
                               (query-hut-api "git" (hut-repo-query orga-name)))
                              :data :user :repositories :results
                              (filter #(= (:visibility %) "PUBLIC")))
            o-data       (->> (utils/json-parse-with-keywords
                               (query-hut-api "meta" (hut-orga-query orga-name)))
                              :data :userByName)
            orga-data    {:email              (or (:email o-data) "")
                          :description        (or (:bio o-data) "")
                          :name               orga-name
                          :floss_policy       (or floss_policy "")
                          :ministry           (or service_of "")
                          :is_verified        true
                          :organization_url   orga-url
                          :login              orga-name
                          :creation_date      (:created o-data)
                          :website            (or (:url o-data) "")
                          :location           (:location o-data)
                          :platform           "SourceHut"
                          :repositories_count (count r-data)
                          :avatar_url         ""}]
        (swap! orgas-data conj orga-data)
        ;; For each orga, get data for its repositories
        (doseq [r r-data]
          (let [r-d {:description              (or (:description r) "")
                     :open_issues_count        0
                     :last_modification        ""
                     :forks_count              0
                     :stars_count              0
                     :software_heritage_url    ""
                     :software_heritage_exists ""
                     :license                  ""
                     :organization_name        orga-name
                     :homepage                 ""
                     :name                     (:name r)
                     :repository_url           (str "https://git.sr.ht/~"
                                                    orga-name "/" (:name r))
                     :is_archived              false
                     :is_fork                  false
                     :default_branch
                     (last (re-find #"^.+/([^/]+)$" (:name (:HEAD r))))
                     :creation_date            (:created r)
                     :topics                   ""
                     :language                 ""
                     :last_update              (:updated r)
                     :platform                 "SourceHut"}]
            (swap! repos-data conj r-d)))))
    (when-let [res (concat @orgas-data @repos-data)]
      (timbre/info (format "Fetched %s organizations and %s repositories from SourceHut"
                           (count @orgas-data) (count @repos-data)))
      res)))
