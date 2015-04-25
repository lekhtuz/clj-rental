(ns rental.views.landlord
  (:require
    [clojure.tools.logging :as log]
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
    [hiccup.form :refer [label form-to text-field email-field password-field drop-down submit-button]]
;    [ring.util.response :as resp]
    [rental.views.layout :as layout]
    [rental.geonames :as geonames]
    [rental.validation :as validation]
  )
)

(def registration-form-info
  (assoc validation/default-form-info :validators
    {
     :username [validation/reject-if-empty]
     :password [validation/reject-if-empty]
     :firstname [validation/reject-if-empty]
     :lastname [validation/reject-if-empty]
    }
  )
)

(def registration-template
  [
   [text-field :username "Username"]
   [password-field :password "Password"]
   [text-field :firstname "First name"]
   [text-field :lastname "Last name"]
   [email-field :email "Email"]
   [text-field :address1 "Address"]
   [text-field :address2 ""]
  ]
)

(def registration-column-classes ["left-td-label" "right-td-field"])

(defn home []
  (layout/common (h/html [:h1 "Logged-in landlord"]))
)

(defn register-view [form-info params]
  (log/info "register-view: form-info =" form-info ", params =" params)
  (layout/common
    (h/html
      [:h1
       (if (validation/has-errors form-info)
          "Correct errors and resubmit the form"
          "Landlord register"
       )
      ]
      (form-to [ :post "" ]
        [:table
         (map (partial layout/form-row registration-column-classes form-info) (layout/add-values-to-form-rows registration-template params))
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
)

(defn register [params]
  (log/info "register: params =" params)
  (let [form-info (validation/validate registration-form-info params)]
    (log/info "register: form-info =" form-info)
    (if (validation/has-errors form-info)
      (register-view form-info params)
      (layout/common 
        (h/html 
	         [:h1 "Landlord account created"]
	         [:br] (h-e/link-to "/login" "Log into your new account")
        )
	    )
    )
  )
)
