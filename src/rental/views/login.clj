(ns rental.views.login
  (:require
    [rental.views.layout :as layout :refer [common form-row]]
    [clojure.tools.logging :as log :refer [info]]
    [hiccup.core :refer [html]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label form-to text-field password-field submit-button]]
  )
)

(defn login-box []
  (log/info "login-box function called.")
  (form-to [ :post "/login" ]
    [:table
     (map (partial form-row ["left-td-label" "right-td-field"]) [
                    [text-field "username" "Username"]
                    [password-field "password" "Password"]
                   ]
     )
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
    (layout/common (html [:h1 "Enter your credentials:"] (login-box)))
  )
  ([username password]
    (log/info "Login post called")
    (layout/common (str "username=" username ", password=" password))
  )
)
