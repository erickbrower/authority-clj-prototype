(ns authority.handlers
  (:require [ring.util.response :as ring-resp]
            [io.pedestal.service.interceptor 
              :as interceptor 
              :refer [definterceptorfn defhandler interceptor]]
            [authority.db :as db]
            [authority.cache :as cache]
            [authority.validations :as vali]
            [noir.util.crypt :as crypt]))

(defn error-response [errors]
  (-> {:errors errors}
      (ring-resp/response)
      (ring-resp/status 400)))

;;user handlers
(defhandler create-user [context]
  (let [params (:json-params context)
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

(defhandler list-users [context]
  (let [params (:json-params context)]
    (if (empty? params)
      (ring-resp/response (db/list-users))
      (ring-resp/response (db/list-users params)))))

(defhandler show-user [context]
  (ring-resp/response (:user-resource context)))

(defhandler update-user [context]
  (let [params (:json-params context)
        errors (vali/validate-update-user params)]
    (if-not errors
      (->> params
           (db/update-user (:id (:user-resource context)))
           (ring-resp/response))
      (error-response errors))))

(defhandler delete-user [context]
  (ring-resp/response (db/delete-user (:id (:user-resource context)))))

(defhandler create-session [context]
  (let [params (:json-params context)
        user (db/get-user-by-username (:username params))]
    (if (and user (crypt/compare (:password params) (:password_digest user)))
      (let [token (first (cache/available-tokens))]
        (cache/store-session-token token (keyword (:username user)))
        (ring-resp/response {:token token :user (dissoc user :password_digest)}))
      (error-response {:message "Unknown username or password."}))))

(defhandler show-session [context]
  (ring-resp/response {:token (:token context) }))

(defhandler delete-session [context])

(defn parse-uuid-param [param-key context]
  (->> [:path-params param-key]
       (get-in (:request context))
       (java.util.UUID/fromString)))

(defn try-load-user [context]
  (let [user-id (parse-uuid-param :id context)
        user (db/get-user user-id)]
    (if user
      (assoc context :user-resource user)
      (throw (ex-info "User does not exist!" {:user-id user-id})))))

(defn respond-not-found [context error]
  (assoc context :response (ring-resp/not-found "Not Found.")))

(defn try-load-session [context]
  (let [token (parse-uuid-param :token context)
        user-id (:id (:user-resource context))]
    (if (cache/session-exists? token user-id)
      (assoc context :token token)
      (throw (ex-info "Session does not exist!" {:token token })))))

(definterceptorfn load-user []
  (interceptor :name 'load-user
               :enter try-load-user
               :error respond-not-found))

(definterceptorfn load-session []
  (interceptor :name 'load-session
               :enter try-load-session
               :error respond-not-found))
