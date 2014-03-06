(ns authority.validations
  (:require [bouncer [core :as b] [validators :as v]]))

(def user-username-pattern
  #"^[A-Za-z0-9_]{8,30}$")

(def user-password-length
  (range 8 100))

(def user-password-pattern
  #"^[A-Za-z0-9_#^@$%&*!]{8,100}$")

(def user-creation-rules
  {:username [v/required [v/matches user-username-pattern]]
   :password [v/required [v/matches user-password-pattern]]})

(def user-update-rules
  {:username [[v/matches user-username-pattern :pre (comp not nil? :username)]]
   :password [[v/matches user-password-pattern :pre (comp not nil? :password)]]})

(defn validate-with-rules [user rules]
  (first (b/validate user rules)))

(defn validate-create-user [user]
  (validate-with-rules user user-creation-rules))

(defn validate-update-user [user]
  (validate-with-rules user user-update-rules))
