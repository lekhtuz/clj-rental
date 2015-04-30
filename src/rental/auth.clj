(ns rental.auth
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [rental.schema :as schema :refer [load-user]]
  )
)

(def all-roles (vals schema/usertype-role))

(defn authenticate [username]
  (log/info "authenticate: username =" username)
  (if (seq username)
    (if-let [ ent (load-user username) ]
      { :username username :password (:rental.schema/password ent) :rental.schema/db-entity ent :roles #{(:rental.schema/usertype ent)} }
    )
  )
)
