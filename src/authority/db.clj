(ns authority.db
  (:require [environ.core :refer env])
  (:use korma.core
        [korma.db :only (defdb)]))

(def db-spec
  {:datasource
    (doto (new PGPoolingDataSource)
     (.setServerName   (env :db-host))
     (.setDatabaseName (env :db-name))
     (.setUser         (env :db-user))
     (.setPassword     (env :db-pass))
     (.setMaxConnections (env :db-max-conns)))})

(defdb db schema/db-spec)

(declare users)

(defentity users
  (entity-fields :id
                 :username
                 :created_at
                 :updated_at))


(defn create-user [user]
  (insert users
          (values user)))

(defn get-user [id]
  (first (select users
                 (where {:id id})
                 (limit 1))))

(defn get-user-by-login [username password])
