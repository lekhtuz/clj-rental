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

(def uri ((cc/config ::db-key) (cc/config ::db-url)))

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
  (log/info "conn: Connection request received. Calling d/connect...")
  (let [c (d/connect uri)]
    (log/info "conn: Connection retrieved" c)
    c
  )
)

(defn db []
  (log/info "db: Database request received. Calling d/db...")
  (let [d (d/db (conn))]
    (log/info "db: Database retrieved" d)
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

(defn transact-data [data]
  (log/info "transact-data: datae =" data)
  (try
    (let [future @(d/transact (conn) data)]
      (log/info "transact-data: future =" future)
      { :result true, :message "Data successfully transacted." :future future }
    )
    (catch ExecutionException e 
      (do
        (log/info "transact-data: error transacting data " (with-out-str (.printStackTrace e))) 
        { :result false, :message (.getMessage e), :exception e }
      )
    )
  )
)

(defn create-database []
  (log/info "create-database: uri =" uri)
  (try
    (if (d/create-database uri)
      (do
        (log/info "create-database: database" uri "created. Adding schema:" schema-tx)
        (log/info "Connection:" (conn))
        (transact-data schema-tx)
        (log/info "Schema added.")
        (transact-data setup-data-encrypted-passwords-tx)
        (log/info "Setup data added.")
        true
      )
      (log/info "database" uri "already exists.")
    )
    (catch ExecutionException e
      (log/info "create-database: error creating database " (with-out-str (.printStackTrace e)))
    )
  )
)

; Function returns true, if database was deleted or did not exist, false if exception was thrown
(defn delete-database []
  (log/info "delete-database: uri =" uri)
  (try
    (if (d/delete-database uri)
      (log/info "database deleted.")
      (log/info "database was not deleted.")
    )
    true
    (catch ExecutionException e
      (log/info "delete-database: error deleting database " (with-out-str (.printStackTrace e)))
    )
  )
)

(defn load-user [username]
  (log/info "load-user: username =" username)
  (if (seq username)
    (let [
          id (ffirst (d/q '[:find ?e :in $ ?u :where [?e ::username ?u]] (db) username))
          ent (d/entity (db) id)
        ]
        (log/info "load-user: (class ent) =" (class ent) ", id =" id ", ent =" ent ", (keys ent) =" (keys ent))
        (if-not (nil? ent)
          (merge {
                  :id (:db/id ent)
                  :usertype (-> ent ::usertype  usertype-role)
                  :username (::username ent)
                  :email (::email ent)
                  :password (::password ent)
                  :firstname (::first-name ent)
                  :lastname (::last-name ent)
                 }
                 (let [ address (::mailing-address ent) ]
                   (log/info "load-user: address =" address)
                   (if-not (nil? address)
                     (let [touched-address (d/touch address)]
                       {
                        :address-id (:db/id address)
                        :address1 (:rental.schema.address/address1 address)
                        :address2 (:rental.schema.address/address2 address)
                        :city (:rental.schema.address/city address)
                        :state (:rental.schema.address/state address)
                        :zipcode (:rental.schema.address/zipcode address)
                       }
                     )
                   )
                 )
          )
        )
    )
  )
)

(defn create-user [params]
  (log/info "create-user: params =" params)
  (let [{ :keys [ result ] :as transact-result } 
        (transact-data
          [
           {
            :db/id #db/id[:db.part/user]
            ::usertype (-> params :usertype role-usertype)
            ::username (:username params)
            ::email (:email params)
            ::password (creds/hash-bcrypt (:password params))
            ::first-name (:firstname params)
            ::last-name (:lastname params)
            ::mailing-address {
                               :rental.schema.address/address1 (:address1 params)
                               :rental.schema.address/address2 (:address2 params)
                               :rental.schema.address/city (:city params)
                               :rental.schema.address/state (:state params)
                               :rental.schema.address/zipcode (:zipcode params)
                              }
            }
          ]
        )
       ]
    (log/info "create-user: data transacted. transact-result =" transact-result)
    result
  )
)

(defn update-last-successful-login [id]
  (log/info "update-last-successful-login: id =" id)
  (transact-data [{:db/id id ::last-successful-login (java.util.Date.)}])
  (log/info "update-last-successful-login: updated")
)
