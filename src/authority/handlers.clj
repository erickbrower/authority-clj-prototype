(ns authority.handlers
  (:require [ring.util.response :as ring-resp]
            [io.pedestal.service.interceptor :as interceptor :refer [definterceptorfn]]
            [authority.db :as db]
            [authority.validations :as vali]
            [noir.util.crypt :as crypt]))

(defn error-response [errors]
  (-> errors
      (ring-resp/response)
      (ring-resp/status 400)))

(defn create-user [req]
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

(defn list-users [req]
  (ring-resp/response (db/list-users)))

(defn show-user [req]
  (ring-resp/response (db/get-user (:uuid-id req))))

(defn update-user [req]
  (let [params (:json-params req)
        errors (vali/validate-update-user params)]
    (if-not errors
      (->> params
           (db/update-user (:uuid-id req))
           (ring-resp/response))
      (error-response errors))))

(defn delete-user [req]
  (ring-resp/response (db/delete-user (:uuid-id req))))

(definterceptorfn parse-uuid-id []
  (interceptor/on-request ::parse-uuid-id
                          (fn [request] 
                            (assoc 
                              request
                              :uuid-id
                              (java.util.UUID/fromString 
                                (get-in request [:path-params :id]))))))
