(ns authority.service
    (:require [io.pedestal.service.http :as bootstrap]
              [io.pedestal.service.http.route :as route]
              [io.pedestal.service.http.route.definition :refer [defroutes]]
              [io.pedestal.service.http.body-params :as body-params]
              [authority.handlers :as handlers]))


(defroutes routes
  [[["/users" 
     ^:interceptors [(body-params/body-params) bootstrap/json-body]
     {:post handlers/create-user :get handlers/list-users}
     ["/:id" 
      ^:interceptors [handlers/load-user]
      {:get handlers/show-user 
       :put handlers/update-user 
       :delete handlers/delete-user}]]]])

;; Consumed by authority.server/create-server
;; See bootstrap/default-interceptors for additional options you can configure
(def service {:env :prod
              ;; You can bring your own non-default interceptors. Make
              ;; sure you include routing and set it up right for
              ;; dev-mode. If you do, many other keys for configuring
              ;; default interceptors will be ignored.
              ;; :bootstrap/interceptors []
              ::bootstrap/routes routes

              ;; Uncomment next line to enable CORS support, add
              ;; string(s) specifying scheme, host and port for
              ;; allowed source(s):
              ;;
              ;; "http://localhost:8080"
              ;;
              ;;::bootstrap/allowed-origins ["scheme://host:port"]

              ;; Root for resource interceptor that is available by default.
              ::bootstrap/resource-path "/public"

              ;; Either :jetty or :tomcat (see comments in project.clj
              ;; to enable Tomcat)
              ;;::bootstrap/host "localhost"
              ::bootstrap/type :jetty
              ::bootstrap/port 8080})
