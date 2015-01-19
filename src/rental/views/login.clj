(ns rental.views.login
  (:require [hiccup.page]
            [hiccup.element :as h-e :refer [link-to]]
  )
)

(defn login-box []
  [:form {:method "POST" :action "login" :class ""}
    [:table
      [:tr
        [:td { :class "left-td-label" } "Username" ]
        [:td { :class "right-td-field" } [:input {:type "text" :name "username"}] ]
      ]
      [:tr
        [:td { :class "left-td-label" } "Password" ]
        [:td { :class "right-td-field" } [:input {:type "password" :name "password"}] ]
      ]
      [:tr
        [:td {:colspan 2 :align "center"} [:input {:type "submit" :value "Login"}]]
      ]
    ]
    [:br] (h-e/link-to "/signup" "Landlord signup")
    [:br] (h-e/link-to "/forgotusername" "Forgot username")
    [:br] (h-e/link-to "/forgotpassword" "Forgot password")
  ]
)
