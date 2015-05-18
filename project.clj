(defproject rental "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :min-lein-version "2.0.0"
  
  :repositories {"my.datomic.com" {:url "https://my.datomic.com/repo"
                                 :username ~(System/getenv "DATOMIC_EMAIL")
                                 :password ~(System/getenv "DATOMIC_KEY")}}

  :dependencies [
                 [clj-http "1.1.0"]
                 [clj-time "0.9.0"]
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.cemerick/friend "0.2.1"]
                 [compojure "1.3.1"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                 [com.datomic/datomic-pro "0.9.5153" :exclusions [joda-time org.slf4j/slf4j-nop org.slf4j/slf4j-log4j12]]
                 [ch.qos.logback/logback-classic "1.1.2"]
                 [sonian/carica "1.1.0" :exclusions [cheshire]]
                 [org.postgresql/postgresql "9.4-1201-jdbc41"] ; not sure if this is needed
                ]

  :jvm-opts ^:replace ["-Xmx1g" "-server"]

  :plugins [[lein-ring "0.8.13"]]

  :ring {
         :handler rental.handler/app
         :init rental.handler/init
         :destroy rental.handler/destroy
         :nrepl {:start? true :port 3001}
        }
  :profiles {
             :uberjar {:aot :all}
             :production {
                          :ring {
                                 :open-browser? false
                                 :stacktraces? false
                                 :auto-reload? false
                                }
                         }
             :dev {
                   :dependencies [
                                  [ring-mock "0.1.5"]
                                  [ring/ring-devel "1.3.1"]
                                 ]
                   :ring {
                          :open-browser? false
                          :stacktraces? true
                          :auto-reload? true
                         }
                   }
             }
)
