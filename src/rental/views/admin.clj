(ns rental.views.admin
  (:require
    [hiccup.core :as h]
    [rental.views.layout :as layout]
  )
)

(defn home []
  (layout/common (h/html [:h1 "Logged-in admin"]))
)
