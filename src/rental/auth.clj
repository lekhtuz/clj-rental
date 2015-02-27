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

; a dummy in-memory user "database"
(def users
  {
   "lekhtuz@gmail.com" {
                        :username "lekhtuz@gmail.com"
                        :password (creds/hash-bcrypt "dim1dim")
                        :roles #{::role-admin}
                       }
   "admin" {
            :username "admin"
            :password (creds/hash-bcrypt "password")
            :roles #{::role-admin}
           }
  }
)

(defn authenticate [username]
  (log/info "authenticate function called. username=" username)
  (let [
        id (ffirst (d/q '[:find ?e :in $ ?u :where [?e :rental.schema/username ?u]] (s/db) username))
        ent (d/entity (s/db) id)
      ]
      (log/info "id=" id)
      (log/info "ent=" ent)
      (log/info "keys ent=" (keys ent))
      { :username username :password (:rental.schema/password ent) :roles #{((:rental.schema/usertype ent) usertype-role)} }
  )
;  (users username)
)