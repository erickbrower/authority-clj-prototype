(ns authority.db
  (:require [environ.core :refer [env]]
            [io.pedestal.service.log :as log])
  (:use korma.core
        [korma.db :only (defdb)]))

(def db-conf
  {:subprotocol (env :db-subprotocol)
   :subname (str "//" (env :db-host) "/" (env :db-name))
   :user (env :db-user)
   :password (env :db-pass)})

(defdb db db-conf)

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
    (first (select users 
                   (where {:id id}))))

(defn get-user-by-username [username]
  (first (select users
                 (where {:username username}))))

(defn list-users-by-page
  ([] (list-users-by-page 25 0)) 
  ([lim oset] 
   (sql-only (-> (select users)
       (order :created_at :ASC)
       (limit lim)
       (offset oset)))))

(defn list-users
  ([] (list-users-by-page))
  ([args] (-> (list-users-by-page)
              (where args)
              (select)))
  ([args lim oset] (-> (list-users-by-page lim oset)
                       (where args)
                       (select))))


(defn update-user [id user]
  (let [attrs (merge user {:updated_at (sqlfn now)})]
    (update users 
            (set-fields attrs) 
            (where {:id id}))))

(defn delete-user [id]
  (delete users
          (where {:id id})))
