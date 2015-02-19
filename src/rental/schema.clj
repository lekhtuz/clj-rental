(ns rental.schema
  (:require 
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [datomic.api :as d]
    [carica.core :as cc]
    [hiccup.page :as h :refer [html5]]
    [hiccup.element :as h-e :refer [link-to unordered-list]]
    [rental.views.layout :as layout]
  )
)

(def *uri* (cc/config :db-url))

(def menu
  [
   (h-e/link-to "/schema/create" "Create database")
   (h-e/link-to "/schema/delete" "Delete database")
  ]
)

(defn start []
  (log/info "Starting db " *uri* "...")
)

(defn stop []
  (log/info "Stoping db" *uri* "...")
)

(defn maintenance []
 (log/info "Schema maintenance")
 (layout/common 
   [:h1 "Schema maintenance"] 
   [:br]
   "URI=" *uri*
   [:br]
   "Schema file=" (cc/config :db-schema)
   [:br]
   (h-e/unordered-list menu)
 )
)

(defn create-database []
  (if (d/create-database *uri*)
    (log/info "database" *uri* "created.")
    (log/info "database" *uri* "already exists.")
  )
  (ring.util.response/redirect "/schema")
)

(defn delete-database []
  (if (d/delete-database *uri*)
    (log/info "database" *uri* "deleted.")
    (log/info "database" *uri* "was not deleted.")
  )
  (ring.util.response/redirect "/schema")
)
