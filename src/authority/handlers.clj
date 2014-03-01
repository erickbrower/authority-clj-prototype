(ns authority.handlers
  (:require [ring.util.response :as ring-resp]
            [authority.db :as db]
            [noir.util.crypt :as crypt]
            [cheshire.core :as json]))

(defn create-user [req]
  (let [inputs (:json-params req)
        username (:username inputs)
        pass (:password inputs)]
    (if (and username pass)
      (let [salt (crypt/gen-salt)]
        (-> (db/create-user {:username username
                             :password_salt salt
                             :password_digest (crypt/encrypt salt pass)})
            (:id)
            (str)
            (ring-resp/response)))
      (ring-resp/status (ring-resp/response "boo.") 400))))

(defn list-users [req]
  (let [users (db/list-users)]
    (ring-resp/response (json/generate-string users))))

(defn show-user [req]
  (let [^String user-id (get-in req [:path-params :id])
        user (db/get-user user-id)]
    (ring-resp/response (json/generate-string user))))

(defn update-user [req]
  (let [^String user-id (get-in req [:path-params :id])
        user-attrs (:json-params req)
        result (db/update-user user-id user-attrs)]
    (ring-resp/response (json/generate-string result))))

(defn delete-user [req])
