(ns rental.views.admin
  (:require
    [clojure.string :as str :refer [join]]
    [clojure.tools.logging :as log :refer [info]]
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
    [rental.schema :as schema :refer [load-all-users role-description status-description]]
    [rental.views.layout :as layout]
  )
)

(def date-formatter (java.text.SimpleDateFormat. "MM/dd/y hh:mm:ss a"))

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
                     [:td.bordered (-> %2 :status schema/status-description)]
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
            [:th.bordered "Status"]
            [:th.bordered "Role"]
            [:th.bordered "First name"]
            [:th.bordered "Last name"]
            [:th.bordered "Last successful login"]
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
    (if-let [user (schema/load-user username)]
      [:table
       [:tr [:td.section-header {:colspan "2"} "General information"]]
       [:tr [:td.left-td-label "Username"][:td (:username user)] ]
       [:tr [:td.left-td-label "Userid"][:td (:id user)] ]
       [:tr [:td.left-td-label "Status"][:td (-> user :status schema/status-description)] ]
       [:tr [:td.left-td-label "Role"][:td (-> user :usertype schema/role-description)] ]
       [:tr [:td.left-td-label "First name"][:td (:first-name user)] ]
       [:tr [:td.left-td-label "Last name"][:td (:last-name user)] ]
       (if (:address1 user)
         [:tr [:td.left-td-label "Mailing address"][:td (str/join ", " (filter seq [(:address1 user) (:address2 user) (:city user) (:state user)])) " " (:zipcode user)] ]
       )
       [:tr [:td.left-td-label "Last successful login"][:td (if-let [tt (:last-successful-login user)] (.format date-formatter tt))] ]
       [:tr [:td.left-td-label "Last failed login"][:td (if-let [tt (:last-failed-login user)] (.format date-formatter tt))] ]
       [:tr [:td.section-header {:colspan "2"} "Login history"]]
       [:tr 
        [:td {:colspan "2"}
         (h-e/link-to (str "/admin/useredit/" (:username user)) "[&nbsp;Edit&nbsp;]")
         "&nbsp;"
         (h-e/link-to (str "/admin/userhistory/" (:username user)) "[&nbsp;History&nbsp;]")
         "&nbsp;"
         (h-e/link-to "/admin" "[&nbsp;Back to the user list&nbsp;]")
        ]
       ]
      ]
      [:h1 "User does not exist"]
    )
  )
)
