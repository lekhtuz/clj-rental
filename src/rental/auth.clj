(ns rental.auth
  (:require
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log :refer [info]]
            [cemerick.friend.credentials :as creds]
  )
)

; a dummy in-memory user "database"
(def users
             {"lekhtuz@gmail.com" {
                                   :username "lekhtuz@gmail.com"
                                   :password (creds/hash-bcrypt "dim1dim")
                                   :roles #{::role-admin}
                                  }
              "user" {:username "user"
                      :password (creds/hash-bcrypt "password")
                      :roles #{::role-user}
                     }
             }
)

(defn authenticate [username]
  (log/info "authenticate function called. username=" username)
  (users username)
)