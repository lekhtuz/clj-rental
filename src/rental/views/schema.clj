(ns rental.views.schema
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [carica.core :as cc]
    [rental.views.layout :as layout]
    [rental.schema :as schema]
    [hiccup.element :as h-e :refer [link-to unordered-list]]
  )
)

(def menu
  [
   (h-e/link-to "/schema/create" "Create database")
   (h-e/link-to "/schema/delete" "Delete database")
  ]
)

(defn maintenance []
 (log/info "Schema maintenance")
 (layout/common 
   [:h1 "Schema maintenance"] 
   [:br]
   "URI=" schema/uri
   [:br]
   "Schema file=" (cc/config :rental.schema/schema)
   [:br]
   (h-e/unordered-list menu)
 )
)

(defn create-database []
  (schema/create-database)
  (ring.util.response/redirect "/schema")
)

(defn delete-database []
  (schema/delete-database)
  (ring.util.response/redirect "/schema")
)

