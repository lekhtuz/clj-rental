(ns rental.views.landlord
  (:require
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
;    [ring.util.response :as resp]
    [rental.views.layout :as layout]
  )
)

(defn home []
  (layout/common (h/html [:h1 "Logged-in landlord"]))
)

(defn register
  ([]
    (layout/common (h/html [:h1 "Landlord register"]))
  )
  ([username password firstname lastname]
    (
      (layout/common 
        (h/html 
          [:h1 "Landlord account created"]
          [:br] (h-e/link-to "/login" "Log into your new account")
        )
      )
    )
  )
)
