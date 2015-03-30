(ns rental.views.login
  (:require
    [rental.validation :as validation]
    [rental.views.layout :as layout]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [hiccup.core :refer [html]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label form-to text-field password-field submit-button]]
  )
)

(defn login-box [errors]
  (log/info "login-box: errors =" errors)
  (form-to [ :post "/login" ]
    [:table
     (if-not (nil? (:form errors))
       [:tr
         [:td {:colspan 2 :align "center"} (validation/print-error errors :form)]
       ]
     )
     (map (partial layout/form-row ["left-td-label" "right-td-field"] errors)
          [
           [text-field "username" "Username"]
           [password-field "password" "Password"]
          ]
     )
     [:tr
       [:td {:colspan 2 :align "center"} (submit-button "Login")]
     ]
    ]
    [:br] (link-to "/lregister" "Landlord registration")
    [:br] (link-to "/forgotusername" "Forgot username")
    [:br] (link-to "/forgotpassword" "Forgot password")
  )
)

(defn login 
  ([]
    ; Display the login form
    (log/info "login: started")
    (layout/common (html [:h1 "Enter your credentials:"] (login-box validation/default-errors)))
  )
  ([username]
    ; Login failed
    (log/info "login: username =" username)
    (layout/common (login-box (if (str/blank? username)
                                (validation/add-error "username" "Username is blank")
                                (validation/add-error :form "Login failed. Please try again.")
                              )
                   )
    )
  )
)
