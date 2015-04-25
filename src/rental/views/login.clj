(ns rental.views.login
  (:require
    [rental.validation :as validation]
    [rental.schema :as schema]
    [rental.views.layout :as layout]
    [clojure.string :as str]
    [clojure.tools.logging :as log]
    [hiccup.core :refer [html]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label form-to text-field password-field submit-button]]
  )
)

(defn record-successful-login [identity]
  (log/info "record-successful-login: started. identity =" identity)
  (schema/update-last-successful-login (:db/id (:rental.schema/db-entity identity)))
)

(defn record-failed-login [username]
)

(defn login-box [form-info]
  (log/info "login-box: form-info =" form-info)
  (form-to [ :post "/login" ]
    [:table
     (if-not (nil? (:form form-info))
       [:tr
         [:td {:colspan 2 :align "center"} (validation/print-error form-info :form)]
       ]
     )
     (map (partial layout/form-row ["left-td-label" "right-td-field"] form-info)
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
    (layout/common (html [:h1 "Enter your credentials:"] (login-box validation/default-form-info)))
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
