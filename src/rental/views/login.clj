(ns rental.views.login
  (:require
    [rental.validation :as validation]
    [rental.schema :as schema]
    [rental.views.layout :as layout]
    [clojure.tools.logging :as log]
    [hiccup.core :refer [html]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label form-to text-field password-field submit-button]]
  )
)

(def login-template
  [
   [text-field :username "Username"]
   [password-field :password "Password"]
  ]
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
     (if-not (nil? (-> form-info :errors :form))
       [:tr
         [:td {:colspan 2 :align "center"} (validation/print-error form-info :form)]
       ]
     )
     (map (partial layout/form-row layout/form-column-classes form-info) login-template)
     [:tr
       [:td {:colspan 2 :align "center"} (submit-button "Login")]
     ]
    ]
    [:br] (link-to "/lregister" "Landlord registration")
    [:br] (link-to "/forgotusername" "Forgot username")
    [:br] (link-to "/forgotpassword" "Forgot password")
  )
)

(defn login-view
  ([form-info]
    ; Display the login form
    (log/info "login: started")
    (layout/common (html [:h1 "Enter your credentials:"] (login-box form-info)))
  )
  ([form-info username]
    ; Login failed
    (log/info "login: username =" username)
    (layout/common (login-box (if (empty? username)
                                (validation/add-error form-info :username "Username is blank")
                                (validation/add-error form-info :form "Login failed. Please try again.")
                              )
                   )
    )
  )
)
