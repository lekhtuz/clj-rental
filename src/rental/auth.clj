(ns rental.auth
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [cemerick.friend.credentials :as creds]
    [datomic.api :as d :refer [q]]
    [rental.schema :as schema :refer [load-user]]
  )
)

; Mapping between schema usertype and application roles
(def usertype-role
  {
   :rental.schema.usertype/admin ::role-admin
   :rental.schema.usertype/landlord ::role-landlord
   :rental.schema.usertype/tenant ::role-tenant
  }
)

(def all-roles (vals usertype-role))

(defn authenticate [username]
  (log/info "authenticate: username =" username)
  (if-let [ ent (schema/load-user username) ]
    { :username username :password (:rental.schema/password ent) :rental.schema/db-entity ent :roles #{((:rental.schema/usertype ent) usertype-role)} }
  )
)
