(ns authority.handlers
  (:require [ring.util.response :as ring-resp]
            [io.pedestal.service.http :as srv]
            [io.pedestal.service.interceptor :as interceptor :refer [definterceptorfn]]
            [authority.db :as db]
            [noir.util.crypt :as crypt]
            [cheshire.core :as json]))

(defn create-user [req]
  (let [params (:json-params req)
        username (:username params)
        pass (:password params)]
    (if (and username pass)
      (let [salt (crypt/gen-salt)]
        (-> (db/create-user {:username username
                             :password_salt salt
                             :password_digest (crypt/encrypt salt pass)})
            (ring-resp/response)))
      (ring-resp/status (ring-resp/response {:error "SNAP!"}) 400))))

(defn list-users [req]
  (ring-resp/response (db/list-users)))

(defn show-user [req]
  (ring-resp/response (db/get-user (:uuid-id req))))

(defn update-user [req]
  (->> (:json-params req)
       (db/update-user (:uuid-id req))
       (ring-resp/response)))

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

