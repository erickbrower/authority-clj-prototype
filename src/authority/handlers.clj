(ns authority.handlers
  (:require [ring.util.response :as ring-resp]
            [authority.db :as db]
            [noir.util.crypt :as crypt]))

(defn create-user [request]
  (let [inputs (:json-params request)
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

(defn list-users [request])

(defn show-user [request])

(defn update-user [request])

(defn delete-user [request])
