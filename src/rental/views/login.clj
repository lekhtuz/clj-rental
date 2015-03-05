(ns rental.views.login
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label form-to text-field password-field submit-button]]
  )
)

(defn login-box []
  (log/info "login-box function called.")
  (form-to [ :post "/login" ]
    [:table
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
