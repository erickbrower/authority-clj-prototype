(ns authority.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [authority.service :as service]))

(def service
  (::bootstrap/service-fn (bootstrap/create-servlet service/service)))

(deftest create-users-test
  (is (=
       (:status (response-for service 
                            :post 
                            "/users" 
                            :headers 
                            {"Content-Type" "application/json"} 
                            :body 
                            "{ \"username\": \"testuser\", \"password\": \"12345678\"}"))
       200)))
