(ns authority.cache 
  (:require [immutant.cache :as cache]))

(def token-store (cache/create "tokens" :idle [30 :minutes] :ttl [1 :days]))

(defn available-tokens
  ([] (available-tokens 1))
  ([n] (cons (java.util.UUID/randomUUID) (lazy-seq (available-tokens (inc n))))))

(defn store-token [token cache-key]
  (cache/put token-store cache-key token))

(defn retrieve-token [cache-key]
  (cache-key token-store))
