(ns authority.cache 
  (:require [immutant.cache :as cache]))

(def token-store (cache/create "tokens" :ttl 30 :idle 30 :units :minutes))

(defn tokens
  ([] (tokens 1))
  ([n] (cons (java.util.UUID/randomUUID) (lazy-seq (tokens (inc n))))))

(def new-token
  (first (tokens)))
