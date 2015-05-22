(ns rental.auth
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [cemerick.friend.workflows :as workflows]
    [rental.schema :as schema :refer [load-user update-last-failed-login]]
    [rental.views.login :as login :refer [record-failed-login]]
  )
)

(def all-roles (vals schema/usertype-role))

(defn authenticate [username]
  (log/info "authenticate: username =" username)
  (if-let [ user (schema/load-user username) ]
    { :username username :password (:password user) :current-user user :roles #{(:usertype user)} }
  )
)

(defn login-failure-handler [request]
  (log/info "login-failure-handler: request =" (with-out-str (pprint request)))
  (login/record-failed-login (-> request :params :username))
  (workflows/interactive-login-redirect request)
)
 