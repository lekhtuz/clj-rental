(ns rental.views.admin
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
    [rental.schema :as schema :refer [load-all-users role-description]]
    [rental.views.layout :as layout]
  )
)

(def date-formatter (java.text.SimpleDateFormat. "MM/d/y hh:mm:ss a"))

(defn home []
  (layout/common
    [:h1 "Administrator home page"]
    (h-e/link-to "/admin/newuser" "Create new user")
    [:br]
    [:br]
    (if-let [users (schema/load-all-users)]
      (do
        (log/info "admin-home: users = " users)
        (reduce
          #(conj %1 [:tr.bordered
                     [:td.bordered (h-e/link-to (str "/admin/userinfo/" (:username %2)) (:username %2))]
                     [:td.bordered (-> %2 :usertype schema/role-description)]
                     [:td.bordered (:first-name %2)]
                     [:td.bordered (:last-name %2)]
                     [:td.bordered (if-let [tt (:last-successful-login %2)] (.format date-formatter tt))]
                     [:td.bordered (if-let [tt (:last-failed-login %2)] (.format date-formatter tt))]
                    ]
          ) 
          [:table.bordered
           [:tr
            [:th.bordered "Username"]
            [:th.bordered "Role"]
            [:th.bordered "First name"]
            [:th.bordered "Last name"]
            [:th.bordered "Last login"]
            [:th.bordered "Last failed login"]
           ]
          ]
          (sort-by :username users)
        )
      )
      [:h1 "User list can not be loaded"]
    )
  )
)

(defn new-user []
  (layout/common
    [:h1 "Create new user"]
    [:br]
    [:br]
  )
)

(defn user-maintenance [username]
  (layout/common
    [:h1 "User maintenance"]
    [:br]
    [:br]
  )
)
