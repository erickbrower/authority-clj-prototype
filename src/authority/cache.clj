(ns authority.cache 
  (:require [immutant.cache :refer :all]))

(defn available-tokens
  ([] (available-tokens 1))
  ([n] (cons (java.util.UUID/randomUUID) (lazy-seq (available-tokens (inc n))))))

(def token-store (create "sessions" :idle [30 :minutes] :ttl [1 :days]))

(defn create-cache-key [token user-id]
  (keyword (str token user-id)))

(defn store-session-token [token user-id]
  (let [cache-key (create-cache-key token user-id)]
    (put token-store cache-key 1)))

(defn session-exists? [token user-id]
  (let [cache-key (create-cache-key token user-id)]
    (contains? token-store cache-key)))

(defn delete-session-token [token user-id]
  (let [cache-key (create-cache-key token user-id)]
    (delete token-store cache-key)))
