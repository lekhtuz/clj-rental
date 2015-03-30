(ns rental.views.login
  (:require
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
         [:td {:colspan 2 :align "center"} (layout/print-error errors :form)]
       ]
     )
     (map (partial layout/form-row ["left-td-label" "right-td-field"] errors) [
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
    (layout/common (html [:h1 "Enter your credentials:"] (login-box layout/default-errors)))
  )
  ([username]
    ; Login failed
    (log/info "login: username =" username)
    (layout/common (login-box (if (str/blank? username)
                                (layout/add-error layout/default-errors "username" "Username is blank")
                                (layout/add-error layout/default-errors :form "Login failed. Please try again.")
                              )
                   )
    )
  )
)
