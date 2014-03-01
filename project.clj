(defproject authority "0.0.1-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [io.pedestal/pedestal.service "0.2.2"]
                 [io.pedestal/pedestal.service-tools "0.2.2"]
                 [environ "0.4.0"]
                 [ragtime/ragtime.sql.files "0.3.4"]
                 [korma "0.3.0-RC6"] 
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 [lib-noir "0.8.1"]
                 [bouncer "0.3.1-beta1"]
                 [cheshire "5.3.1"]]
  :ragtime {:migrations ragtime.sql.files/migrations
            :database ((keyword (or (System/getenv "CLJ_ENV") "dev"))
                       {:dev "jdbc:postgresql://localhost/authority_dev?user=postgres"
                        :test "jdbc:postgresql://localhost/authority_test?user=postgres"})}
  :profiles
  {:dev 
   {:dependencies [[io.pedestal/pedestal.jetty "0.2.2"]]
    :env {:dev true
          :db-host "localhost"
          :db-subprotocol "postgresql"
          :db-port "5432"
          :db-name "authority_dev"
          :db-user "postgres"
          :db-pass ""
          :db-max-conns 20}}
   :test 
   {:dependencies [[io.pedestal/pedestal.tomcat "0.2.2"]]
    :env {:test true
          :db-host "localhost"
          :db-subprotocol "postgresql"
          :db-port "5432"
          :db-name "authority_test"
          :db-user "postgres"
          :db-pass ""
          :db-max-conns 20}}
   :production 
   {:env {:production true}}}
  :plugins 
  [[lein-environ "0.4.0"][ragtime/ragtime.lein "0.3.4"]]
  :min-lein-version "2.0.0"
  :resource-paths ["config", "resources"]
  :aliases {"run-dev" ["trampoline" "run" "-m" "authority.server/run-dev"]}
  :repl-options  {:init-ns user
                  :init (try
                          (use 'io.pedestal.service-tools.dev)
                          (require 'authority.service)
                          ;; Nasty trick to get around being unable to reference non-clojure.core symbols in :init
                          (eval '(init authority.service/service #'authority.service/routes))
                          (catch Throwable t
                            (println "ERROR: There was a problem loading io.pedestal.service-tools.dev")
                            (clojure.stacktrace/print-stack-trace t)
                            (println)))
                  :welcome (println "Welcome to pedestal-service! Run (tools-help) to see a list of useful functions.")}
  :main ^{:skip-aot true} authority.server)
