(ns authority.service-test
  (:require [clojure.test :refer :all]
            [io.pedestal.service.test :refer :all]
            [io.pedestal.service.http :as bootstrap]
            [authority.service :as service]
            [korma.core :refer [exec-raw]]
            [cheshire.core :as json]))

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

(defn build-json-request 
  ([verb url body] [service verb url :headers {"Content-Type" "application/json"} :body body])
  ([verb url] [service verb url :headers {"Content-Type" "application/json"}]))

(defn do-json-request 
  ([verb url body] 
   (apply response-for (build-json-request verb url body)))
  ([verb url]
   (apply response-for (build-json-request verb url))))

(defn create-user-request [body]
  (do-json-request :post "/users" body))

(defn show-user-request [id]
  (do-json-request :get (str "/users/" id)))

(defn list-users-request []
  (do-json-request :get "/users"))

(defn test-users
  ([] (test-users 1))
  ([n] (cons (json/generate-string {:username (str "testuser" n) :password "12345678"})
             (lazy-seq (test-users (inc n))))))

;; tests
(use-fixtures :each truncate-tables)

(deftest create-user
  (is 
    (= (:status (create-user-request (first (test-users)))) 200)))

(deftest show-user
  (let [user-id (:body (create-user-request (first (test-users))))]
    (is (= (:status (show-user-request user-id))) 200)))

(deftest list-users
  (doall (map #(create-user-request %) (take 10 (test-users))))
  (let [resp (:body (list-users-request))
        users (json/parse-string resp)]
    (is (= (count users) 10))))
