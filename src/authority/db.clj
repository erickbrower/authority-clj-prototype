(ns authority.db
  (:require [environ.core :refer [env]])
  (:use korma.core
        [korma.db :only (defdb)]))

(def db-spec
  {:subprotocol (env :db-subprotocol)
   :subname (str "//" (env :db-host) "/" (env :db-name))
   :user (env :db-user)
   :password (env :db-pass)})

(defdb db db-spec)

(declare users)

(defentity users
  (entity-fields :id
                 :username
                 :password_digest
                 :password_salt
                 :created_at
                 :updated_at))


(defn create-user [user]
  (insert users
          (values user)))

(defn get-user [id]
  (first (select users
                 (where {:id id})
                 (limit 1))))

(defn get-user-by-username [username]
  (first (select users
                 (where {:username username}))))

(defn get-user-by-login [username password])
