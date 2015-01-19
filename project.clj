(defproject rental "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [
                 [org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.cemerick/friend "0.2.1"]
                 [compojure "1.3.1"]
                 [hiccup "1.0.5"]
                 [ring-server "0.3.1"]
                ]
  :plugins [[lein-ring "0.8.13"]]
  :ring {
         :handler rental.handler/app
         :init rental.handler/init
         :destroy rental.handler/destroy
         :nrepl {:start? true :port 3001}
        }
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]
    :open-browser? false, :stacktraces? true, :auto-reload? true
   }
  }
)
