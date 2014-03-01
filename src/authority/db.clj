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
  (let [attrs (merge user {:updated_at (sqlfn now)})]
    (insert users
            (values attrs))))

(defn get-user [id]
  (let [uuid (java.util.UUID/fromString id)]
    (first (select users 
                   (where {:id uuid})))))

(defn get-user-by-username [username]
  (first (select users
                 (where {:username username}))))

(defn list-users
  ([] (select users
              (order :username))))

(defn update-user [id user]
  (let [uuid (java.util.UUID/fromString id)
        attrs (merge user {:updated_at (sqlfn now)})]
    (update users 
            (set-fields attrs) 
            (where {:id uuid}))))
