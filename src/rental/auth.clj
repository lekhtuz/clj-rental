(ns rental.auth
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [cemerick.friend.credentials :as creds]
    [datomic.api :as d :refer [q]]
    [rental.schema :as s :refer [db]]
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

(defn authenticate [username]
  (log/info "authenticate: username =" username)
  (let [
        id (ffirst (d/q '[:find ?e :in $ ?u :where [?e :rental.schema/username ?u]] (s/db) username))
        ent (d/entity (s/db) id)
      ]
      (log/info "authenticate: id =" id ", ent =" ent "(keys ent) =" (keys ent))
      (if-not (nil? ent)
        { :username username :password (:rental.schema/password ent) :rental.schema/db-entity ent :roles #{((:rental.schema/usertype ent) usertype-role)} }
      )
  )
)
