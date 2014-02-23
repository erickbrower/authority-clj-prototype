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
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 ;; Remove this line and uncomment the next line to
                 ;; use Tomcat instead of Jetty:
                 [io.pedestal/pedestal.jetty "0.2.2"]
                 ;; [io.pedestal/pedestal.tomcat "0.2.2"]
                 ]
  :ragtime {:migrations ragtime.sql.files/migrations
            :database ((keyword (or (System/getenv "LEIN_ENV") "dev"))
                       {:dev "jdbc:postgresql://localhost/authority_dev?user=postgres"
                        :test "jdbc:postgresql://localhost/authority_test?user=postgres"})}
  :profiles
  {:dev 
   {:env {:dev true
          :db-host "localhost"
          :db-subprotocol "postgresql"
          :db-port "5432"
          :db-name "authority_dev"
          :db-user "postgres"
          :db-pass ""}}
   :test 
   {:env {:test true
          :db-host "localhost"
          :db-subprotocol "postgresql"
          :db-port "5432"
          :db-name "authority_test"
          :db-user "postgres"
          :db-pass ""}}
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
