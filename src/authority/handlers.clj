(ns authority.handlers
  (:require [ring.util.response :as ring-resp]
            [io.pedestal.service.interceptor 
              :as interceptor 
              :refer [definterceptorfn defhandler interceptor]]
            [authority.db :as db]
            [authority.cache :as cache]
            [authority.validations :as vali]
            [noir.util.crypt :as crypt])
  (:use [clojure.tools.logging :only (info)]))

(defn error-response [errors]
  (-> {:errors errors}
      (ring-resp/response)
      (ring-resp/status 400)))

;;user handlers
(defhandler create-user [req]
  (let [params (:json-params req)
        errors (vali/validate-create-user params)]
    (if-not errors
      (let [salt (crypt/gen-salt)]
        (->> (:password params)
             (crypt/encrypt salt)
             (hash-map :username (:username params)
                       :password_salt salt
                       :password_digest)
             (db/create-user)
             (ring-resp/response)))
      (error-response errors))))

(defhandler list-users [req]
  (let [params (:json-params req)]
    (if (empty? params)
      (ring-resp/response (db/list-users))
      (ring-resp/response (db/list-users params)))))

(defhandler show-user [req]
  (ring-resp/response (:user-resource req)))

(defhandler update-user [req]
  (let [params (:json-params req)
        errors (vali/validate-update-user params)]
    (if-not errors
      (->> params
           (db/update-user (:id (:user-resource req)))
           (ring-resp/response))
      (error-response errors))))

(defhandler delete-user [req]
  (ring-resp/response (db/delete-user (:id (:user-resource req)))))


;;session handlers
(defhandler create-session [req]
  (let [params (:json-params req)
        user (:user-resource req)]
    (if (crypt/compare (:password params) (:password_digest user))
      (let [token (first (cache/available-tokens))]
        (cache/store-session-token token (:id user))
        (ring-resp/response {:token token :user (dissoc user :password_digest)}))
      (error-response {:username "Uknown username or password" 
                       :password "Uknown username or password"}))))

(defhandler show-session [req]
  (ring-resp/response {:token (:token req)}))

(defhandler delete-session [req])

;;interceptors
(defn try-load-user [context]
  (let [user-id (->> [:path-params :id]
                     (get-in (:request context))
                     (java.util.UUID/fromString))
        user (db/get-user user-id)]
    (if user
      (assoc-in context [:request :user-resource] user)
      (throw (ex-info "User does not exist!" {:user-id user-id})))))

(defn respond-not-found [context error]
  (assoc context :response (ring-resp/not-found "Not Found.")))

(defn try-load-session [context]
  (let [token (->> [:path-params :token]
                   (get-in (:request context))
                   (java.util.UUID/fromString))
        user-id (get-in (:request context) [:user-resource :id])]
    (if (cache/session-exists? token user-id)
      (assoc-in context [:request :token] token)
      (throw (ex-info "Session does not exist!" {:token token})))))

(definterceptorfn load-user []
  (interceptor :name 'load-user
               :enter try-load-user
               :error respond-not-found))

(definterceptorfn load-session []
  (interceptor :name 'load-session
               :enter try-load-session
               :error respond-not-found))
