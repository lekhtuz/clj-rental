(ns rental.schema
  (:require
    [clojure.edn :refer [read-string] :rename { read-string edn-read-string }]
    [clojure.pprint :refer [pprint]]
    [clojure.set :as set :refer [map-invert]]
    [clojure.tools.logging :as log :refer [info]]
    [datomic.api :as d]
    [carica.core :as cc]
    [cemerick.friend.credentials :as creds]
  )
  (:import
    [java.util Date]
    [java.util.concurrent ExecutionException]
  )
)

;(def insert-admin '({:db/id #db/id[:db.part/user] :rental.schema/usertype :rental.schema.usertype/admin :rental.schema/username "admin" :rental.schema/password "password"}))

(def uri (cc/config ::db-url))

; Mapping between application roles and schema usertype
(def role-usertype
  {
   :rental.auth/role-admin    :rental.schema.usertype/admin
   :rental.auth/role-landlord :rental.schema.usertype/landlord
   :rental.auth/role-tenant   :rental.schema.usertype/tenant
  }
)

(def usertype-role (set/map-invert role-usertype))

; Retrieve connection every time it is needed. It is cached internally, so it's cheap.
(defn conn []
  (log/info "Connection request received. Calling d/connect...")
  (let [c (d/connect uri)]
    (log/info "Connection retrieved" c)
    c
  )
)

(defn db []
  (log/info "Database request received. Calling d/db...")
  (let [d (d/db (conn))]
    (log/info "Database retrieved" d)
    d
  )
)

; Attribute map added as per https://groups.google.com/forum/#!topic/datomic/aPtVB1ntqIQ
; Otherwise clojure.edn/read-string throws "No reader function for tag db/id" message
; Regular read-string works just fine witout attributes
(def schema-tx (edn-read-string {:readers *data-readers*} (slurp (first (cc/resources (cc/config ::schema))))))
(def setup-data-tx (edn-read-string {:readers *data-readers*} (slurp (first (cc/resources (cc/config ::setup-data))))))
(def setup-data-encrypted-passwords-tx (map #(assoc % ::password (creds/hash-bcrypt (::password %))) setup-data-tx))

(defn start []
  (log/info "Starting db " uri "...")
)

(defn stop []
  (log/info "Stopping db" uri "...")
)

(defn create-database []
  (if (d/create-database uri)
    (do
      (log/info "database" uri "created.")
      (log/info "Adding schema:" schema-tx)
      (log/info "Connection:" (conn))
      @(d/transact (conn) schema-tx)
      (log/info "Schema added.")
      @(d/transact (conn) setup-data-encrypted-passwords-tx)
      (log/info "Setup data added.")
    )
    (log/info "database" uri "already exists.")
  )
)

(defn delete-database []
  (if (d/delete-database uri)
    (log/info "database" uri "deleted.")
    (log/info "database" uri "was not deleted.")
  )
)

(defn load-user [username]
  (log/info "load-user: username =" username)
  (let [
        id (ffirst (d/q '[:find ?e :in $ ?u :where [?e ::username ?u]] (db) username))
        ent (d/entity (db) id)
      ]
      (log/info "load-user: (class ent) =" (class ent) ", id =" id ", ent =" ent ", (keys ent) =" (keys ent))
      (if-not (nil? ent) 
        (let [
              address (::mailing_address ent)
              entity-as-map (into {} (d/touch ent))
              entity-as-map1 (assoc entity-as-map ::usertype ((::usertype ent) usertype-role) :db/id (:db/id ent) ::mailing_address (into {} (d/touch address)))
              entity-as-map2 (assoc-in entity-as-map1 [::mailing_address :db/id] (:db/id address))
             ]
          (log/info "load-user: (class (d/touch ent)) =" (class (d/touch ent)) ", (d/touch ent) =" (d/touch ent))
          (log/info "load-user: entity-as-map =" entity-as-map)
          (log/info "load-user: entity-as-map1 =" entity-as-map1)
          (log/info "load-user: entity-as-map2 =" entity-as-map2)
          entity-as-map2
        )
      )
  )
)

(defn create-user [params]
  (log/info "create-user: params =" params)
  (try
    (let [future @(d/transact (conn) [
                                      {
                                       :db/id #db/id[:db.part/user]
                                       ::usertype (-> params :usertype role-usertype)
                                       ::username (:username params)
                                       ::email (:email params)
                                       ::password (creds/hash-bcrypt (:password params))
                                       ::first_name (:firstname params)
                                       ::last_name (:lastname params)
                                       ::mailing_address {
                                                          :rental.schema.address/address1 (:address1 params)
                                                          :rental.schema.address/address2 (:address2 params)
                                                          :rental.schema.address/city (:city params)
                                                          :rental.schema.address/state (:state params)
                                                          :rental.schema.address/zipcode (:zipcode params)
                                                          }
                                       }
                                     ]
                 )]
      (log/info "create-user: user created. future =" future)
      { :result true, :message "User successfully created." :future future }
    )
    (catch ExecutionException e (do
                                  (log/info "create-user: error creating the user " (with-out-str (.printStackTrace e))) 
                                  { :result false, :message (.getMessage e), :exception e }
                                )
    )
  )
)

(defn update-last-successful-login [id]
  (log/info "update-last-successful-login: id =" id)
  @(d/transact (conn) [{:db/id id ::last-successful-login (java.util.Date.)}])
  (log/info "update-last-successful-login: updated")
)
