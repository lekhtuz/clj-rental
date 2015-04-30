(ns rental.views.landlord
  (:require
    [clojure.tools.logging :as log]
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
    [hiccup.form :refer [label form-to text-field email-field password-field drop-down submit-button]]
;    [ring.util.response :as resp]
    [rental.views.layout :as layout]
    [rental.geonames :as geonames]
    [rental.schema :as schema]
    [rental.validation :as validation]
  )
)

(def registration-form-info
  (assoc validation/default-form-info :validators
    {
     :username [validation/reject-if-empty validation/username-exists]
     :password [validation/reject-if-empty]
     :firstname [validation/reject-if-empty]
     :lastname [validation/reject-if-empty]
     :email [validation/valid-email]
     :address1 [validation/reject-if-empty]
     :city [validation/reject-if-empty]
     :zipcode [validation/valid-zipcode]
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
   [text-field :city "City"]
   [#(drop-down %1 (geonames/get-states-seq) %2) :state "State"]
   [text-field :zipcode "Zip code"]
  ]
)

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
         (if (validation/has-errors form-info :form)
           [:tr
             [:td {:colspan 2 :align "center"} (validation/print-error form-info :form)]
           ]
         )
         (map (partial layout/form-row layout/form-column-classes form-info) (layout/add-values-to-form-rows registration-template params))
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
      (do
        (let [dbresult (schema/create-user (assoc params :usertype :rental.auth/role-landlord))]
          (log/info "register: dbresult =" dbresult)
          (if (:result dbresult)
            (layout/common 
              (h/html 
	               [:h1 "Landlord account created"]
	               [:br] (h-e/link-to "/login" "Log into your new account")
              )
            )
            (register-view (validation/add-error form-info :form "Database error occured while saving user") params)
          )
        )
	    )
    )
  )
)
