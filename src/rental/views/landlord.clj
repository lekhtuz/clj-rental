(ns rental.views.landlord
  (:require
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
    [hiccup.form :refer [label form-to text-field email-field password-field drop-down submit-button]]
;    [ring.util.response :as resp]
    [rental.views.layout :as layout]
    [rental.geonames :as geonames]
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
         (map (partial layout/form-row ["left-td-label" "right-td-field"]) [
                        [text-field "firstname" "First name"]
                        [text-field "lastname" "Last name"]
                        [email-field "email" "Email"]
                        [password-field "password" "Password"]
                        [text-field "address1" "Address"]
                        [text-field "address2" ""]
                       ]
         )
         [:tr
          [:td {:class "left-td-label"} (label "state" "State")]
          [:td {:class "right-td-field"} (drop-down "state" (geonames/get-states-seq))]
         ]
         [:tr
          [:td {:class "left-td-label"} (label "zipcode" "Zip Code")]
          [:td {:class "right-td-field"} (text-field "zipcode")]
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
