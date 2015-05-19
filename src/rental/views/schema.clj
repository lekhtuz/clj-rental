(ns rental.views.schema
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [carica.core :as cc :refer [config]]
    [ring.util.response :as response :refer [redirect]]
    [rental.views.layout :as layout :refer [common]]
    [rental.schema :as schema :refer [create-rental-database delete-rental-database]]
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
  (schema/create-rental-database)
  (response/redirect "/schema")
)

(defn delete-database []
  (schema/delete-rental-database)
  (response/redirect "/schema")
)

