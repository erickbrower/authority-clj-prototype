(ns authority.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [authority.service :as service]
            [korma.core :refer [exec-raw]]))

(def all-tables
  (map #(:table_name %) 
       (exec-raw [(str "SELECT table_name "
                       "FROM information_schema.tables "
                       "WHERE table_schema='public' "
                       "AND table_type='BASE TABLE'")] :results)))

(defn truncate-tables [f]
  (f)
  (doall (map #(exec-raw [(str "TRUNCATE TABLE " %)]) all-tables)))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(use-fixtures :each truncate-tables)

(deftest create-users-test
  (is 
    (= 
      (:status (response-for service 
                             :post 
                             "/users" 
                             :headers
                             {"Content-Type" "application/json"}
                             :body 
                             "{\"username\": \"testuser\", \"password\": \"12345678\"}")) 
      200)))
