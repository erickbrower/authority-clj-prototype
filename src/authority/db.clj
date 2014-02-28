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

(defn db-cast [x as]
  (raw (format "CAST(%s AS %s)" (name x) (name as))))

(defn create-user [user]
  (insert users
          (values user)))

;;TODO do this without exec-raw
(defn get-user [id]
  (exec-raw [(str "SELECT * FROM users "
                  "WHERE users.id = ?::uuid") [id]] :results))

(defn get-user-by-username [username]
  (first (select users
                 (where {:username username}))))

(defn list-users
  ([] (select users
              (order :username))))
