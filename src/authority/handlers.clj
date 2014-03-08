(ns authority.handlers
  (:require [ring.util.response :as ring-resp]
            [io.pedestal.service.log :as log]
            [io.pedestal.service.interceptor 
              :as interceptor 
              :refer [definterceptorfn defhandler interceptor]]
            [authority.db :as db]
            [authority.validations :as vali]
            [noir.util.crypt :as crypt]))

(defn error-response [errors]
  (-> {:errors errors}
      (ring-resp/response)
      (ring-resp/status 400)))

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

(defhandler create-token [req])
(defhandler show-user-token [req])

(defn try-load-user [context]
  (let [user-id (->> [:path-params :id]
                     (get-in (:request context))
                     (java.util.UUID/fromString))
        user (db/get-user user-id)]
    (if user
      (assoc context :user-resource user)
      (throw (ex-info "User does not exist!" {:user-id user-id})))))

(defn respond-not-found [context error]
  (assoc context :response (ring-resp/not-found "Not Found.")))

(definterceptorfn load-user []
  (interceptor :name 'load-user
               :enter try-load-user
               :error respond-not-found))
