(ns rental.views.landlord
  (:require
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
    [hiccup.form :refer [label form-to text-field password-field submit-button]]
;    [ring.util.response :as resp]
    [rental.views.layout :as layout]
  )
)

(defn home []
  (layout/common (h/html [:h1 "Logged-in landlord"]))
)

(defn register
  ([]
    (layout/common
      (h/html
        [:h1 "Landlord register"]
        [:table
         [:tr
          [:td {:class "left-td-label"} (label "firstname" "First name")]
          [:td {:class "right-td-field"} (text-field "firstname")] 
         ]
         [:tr
          [:td {:class "left-td-label"} (label "lastname" "Last name")]
          [:td {:class "right-td-field"} (text-field "lastname")] 
         ]
         [:tr
          [:td {:class "left-td-label"} (label "email" "Email")]
          [:td {:class "right-td-field"} (text-field "email")] 
         ]
         [:tr
          [:td {:class "left-td-label"} (label "password" "Password")]
          [:td {:class "right-td-field"} (password-field "password")]
         ]
         [:tr
           [:td {:colspan 2 :align "center"} (submit-button "Register")]
         ]
        ]
      )
    )
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
