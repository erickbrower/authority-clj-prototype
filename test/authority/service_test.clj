(ns authority.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [authority.service :as service]
            [korma.core :refer [exec-raw]]))

;; test helpers
(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(def all-tables
  (map #(:table_name %) 
       (exec-raw [(str "SELECT table_name "
                       "FROM information_schema.tables "
                       "WHERE table_schema='public' "
                       "AND table_type='BASE TABLE' "
                       "AND table_name <> 'ragtime_migrations'")] :results)))

(defn truncate-tables [f]
  (f)
  (doall 
    (map #(exec-raw [(str "TRUNCATE TABLE " %)]) all-tables)))

(defn build-json-request [verb url body]
  [service verb url :headers {"Content-Type" "application/json"} :body body])

(defn do-json-request [verb url body]
  (apply response-for (build-json-request verb url body)))

(defn create-user-request [body]
  (do-json-request :post "/users" body))

(def user-json
  "{\"username\": \"testuser\", \"password\": \"12345678\"}")


;; tests
(use-fixtures :each truncate-tables)

(deftest create-user
  (is 
    (= (:status (create-user-request user-json)) 200)))

(deftest show-user
  (let [user-id (:body (create-user-request user-json))]
    (is (= (:status (response-for service
                                  :get
                                  (str "/users/" user-id)
                                  :headers
                                  {"Content-Type" "application/json"}))))))
                                  
