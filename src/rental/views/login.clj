(ns rental.views.login
  (:require
    [rental.views.layout :refer [common form-row]]
    [clojure.tools.logging :as log :refer [info]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label form-to text-field password-field submit-button]]
  )
)

(defn login-box []
  (log/info "login-box function called.")
  (form-to [ :post "/login" ]
    [:table
     (map form-row [
                    [text-field "username" "Username"]
                    [password-field "password" "Password"]
                   ]
     )
     [:tr
       [:td {:class "left-td-label"} (label "username" "Username") ]
       [:td {:class "right-td-field"} (text-field "username")] 
     ]
     [:tr
       [:td {:class "left-td-label"} (label "password" "Password") ]
       [:td {:class "right-td-field"} (password-field "password")]
     ]
     [:tr
       [:td {:colspan 2 :align "center"} (submit-button "Login")]
     ]
    ]
    [:br] (link-to "/lregister" "Landlord signup")
    [:br] (link-to "/forgotusername" "Forgot username")
    [:br] (link-to "/forgotpassword" "Forgot password")
  )
)

(defn login 
  ([]
    (log/info "login function called")
    (layout/common (h/html [:h1 "Enter your credentials:"] (login-box)))
  )
  ([username password]
    (log/info "Login post called")
    (layout/common (str "username=" username ", password=" password))
  )
)
