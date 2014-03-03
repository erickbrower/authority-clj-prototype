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

(defn update-user-request [id user]
  (do-json-request :put (str "/users/" id) (json/generate-string user)))

(defn delete-user-request [id]
  (do-json-request :delete (str "/users/" id)))

(defn gen-random-str [length]
  (->> #(rand-nth "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789")
       (repeatedly)
       (take length)
       (apply str)))

(defn test-users
  ([] (test-users 1))
  ([n] (cons (json/generate-string {:username (str "testuser" n) :password "12345678"})
             (lazy-seq (test-users (inc n))))))

(def user-with-short-username
  (json/generate-string {:username "a" :password "1234567"}))

(def user-with-long-username
  (json/generate-string {:username (gen-random-str 31) :password "12345678"}))

(def user-with-short-password
  (json/generate-string {:username "testuser" :password "1234567"}))

(def user-with-long-password
  (json/generate-string {:username "testuser" :password (gen-random-str 101)}))

(defn setup-user []
  (let [resp-body (:body (create-user-request (first (test-users))))]
    (:id (json/parse-string resp-body true))))


;; tests
(use-fixtures :each truncate-tables)

(deftest create-user
  (is (= (:status (create-user-request (first (test-users)))) 200)))

(deftest create-user-with-short-username
  (is (= (:status (create-user-request user-with-short-username) 400))))

(deftest create-user-with-long-username
  (is (= (:status (create-user-request user-with-long-username) 400))))

(deftest create-user-with-short-password
  (is (= (:status (create-user-request user-with-short-password) 400))))

(deftest create-user-with-long-password
  (is (= (:status (create-user-request user-with-long-password) 400))))

(deftest show-user
  (let [user-id (setup-user)]
    (is (= (:status (show-user-request user-id))) 200)))

(deftest list-users
  (doall (map #(create-user-request %) (take 10 (test-users))))
  (let [resp (:body (list-users-request))
        users (json/parse-string resp)]
    (is (= (count users) 10))))

(deftest update-user
  (let [user-id (setup-user)]
    (is (= (:status (update-user-request user-id {:username "bob12345"})) 200))))

(deftest update-user-with-short-username
  (let [user-id (setup-user)]
    (is (= (:status (update-user-request user-id {:username "bob"})) 400))))

(deftest update-user-with-long-username
  (let [user-id (setup-user)]
    (is (= (:status (update-user-request user-id {:username (gen-random-str 31)})) 400))))

(deftest update-user-with-short-password
  (let [user-id (setup-user)]
    (is (= (:status (update-user-request user-id {:password "1"})) 400))))

(deftest update-user-with-long-password
  (let [user-id (setup-user)]
    (is (= (:status (update-user-request user-id {:username (gen-random-str 101)})) 400))))

(deftest delete-user
  (let [user-id (setup-user)]
    (is (= (:status (delete-user-request user-id)) 200))))
