(ns rental.schema
  (:require
    [clojure.edn :refer [read-string] :rename { read-string edn-read-string }]
    [clojure.pprint :refer [pprint]]
    [clojure.set :as set :refer [map-invert rename-keys]]
    [clojure.tools.logging :as log :refer [info]]
    [datomic.api :as d :refer [q connect transact create-database delete-database entity]]
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

(def role-description
  {
   :rental.auth/role-admin    "Administrator"
   :rental.auth/role-landlord "Landlord"
   :rental.auth/role-tenant   "Tenant"
  }
)

; Mapping between application account status and schema account status
(def status-dbstatus
  {
   :active   :rental.schema.status/active
   :inactive :rental.schema.status/inactive
  }
)

(def dbstatus-status (set/map-invert status-dbstatus))

(def status-description
  {
   :active   "Active"
   :inactive "Inactive"
  }
)
; Mapping between application and database address keys
(def address-dbaddress
  { 
   :address-id :db/id
   :address1   :rental.schema.address/address1
   :address2   :rental.schema.address/address2
   :city       :rental.schema.address/city
   :state      :rental.schema.address/state
   :zipcode    :rental.schema.address/zipcode
  }
)

(def dbaddress-address (set/map-invert address-dbaddress))

; Queries defined here
(def query-load-user '[:find ?e :in $ ?u :where [?e ::username ?u]])
(def query-load-all-users '[:find ?e :in $ :where [?e ::username _]])

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
  (log/info "transact-data: data =" data)
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

(defn create-rental-database []
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
(defn delete-rental-database []
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

(defn run-query 
  ([query]
    (log/info "run-query: query =" query)
    (d/q query (db))
  )
  ([query param]
    (log/info "run-query: query =" query ", param =" param)
    (d/q query (db) param)
  )
)

; Convert a Datomic entity to a map, where keys correspond to the database attribute keys
(defn convert-entity-to-map [ent]
  (log/info "convert-entity-to-map: ent =" ent ", (keys ent) =" (keys ent))
  (reduce
    #(assoc %1 %2 (%2 ent))
    {:db/id (:db/id ent)} (keys ent)
  )
)

; Convert set of single element vectors #{ [id1] [id2]...} as returned by run-query to a set/vector/list of maps, where keys correspond to the database attribute keys
; Further single element conversion is performed by the optional function parameter
(defn convert-ids-to-maps 
  ([ids]
    (convert-ids-to-maps ids (fn [param] param))
  )
  ([ids f]
    (log/info "convert-ids-to-maps: ids =" ids ", f =" f)
    (reduce
      #(conj %1 (f (convert-entity-to-map (d/entity (db) (first %2)))))
      #{} ids) ; change #{} to [] to return vector of maps, to '() to return list of maps
  )
)

(defn convert-map-to-user [m]
  (log/info "convert-map-to-user: m =" m)
  ; The only reason I test m for nil is because of :usertype and :status. Otherwise an empty map is beautifully generated despite of m being nil.
  (if (seq m)
    (dissoc ; cleanup to remove db versions of the attribute keys
      (merge
        (if-let [last-successful-login (::last-successful-login m)]
          { :last-successful-login last-successful-login }
        )
        (if-let [last-failed-login (::last-failed-login m)]
          { :last-failed-login last-failed-login }
        )
        {
         :status (-> m ::status dbstatus-status)
         :usertype (-> m ::usertype usertype-role)
        }
        (if-let [ address (::mailing-address m) ]
          (set/rename-keys (convert-entity-to-map address) dbaddress-address)
        )
        (set/rename-keys m { :db/id :id, ::username :username, ::email :email, ::password :password, ::first-name :first-name, ::last-name :last-name })
      )
      ::usertype ::mailing-address ::last-successful-login ::last-failed-login
    )
  )
)

(defn convert-ids-to-users [ids]
  (log/info "convert-ids-to-users: ids =" ids)
  (convert-ids-to-maps ids convert-map-to-user)
)

(defn load-user [username]
  (log/info "load-user: username =" username)
  (if (seq username)
    (convert-map-to-user (first (convert-ids-to-maps (run-query query-load-user username))))
  )
)

(defn load-all-users []
  (log/info "load-all-users: started")
  (convert-ids-to-users (run-query query-load-all-users))
)

(defn create-user [params]
  (log/info "create-user: params =" params)
  (if (seq params)
    (let [{ :keys [ result ] :as transact-result } 
          (transact-data
            [
             (merge-with
               merge 
               {
                :db/id #db/id[:db.part/user]
                ::status (-> params :status status-dbstatus)
                ::usertype (-> params :usertype role-usertype)
                ::username (:username params)
                ::email (:email params)
                ::password (creds/hash-bcrypt (:password params))
                ::first-name (:firstname params)
                ::last-name (:lastname params)
               }
               (if (seq (:address1 params)) 
                 { ::mailing-address (set/rename-keys (select-keys params (keys address-dbaddress)) address-dbaddress) }
               )
             )
            ]
          )
         ]
      (log/info "create-user: data transacted. transact-result =" transact-result)
      result
    )
  )
)

(defn update-last-successful-login [id]
  (log/info "update-last-successful-login: id =" id)
  (transact-data [{:db/id id ::last-successful-login (java.util.Date.)}])
  (log/info "update-last-successful-login: updated")
)

(defn update-last-failed-login [username]
  (log/info "update-last-failed-login: username =" username)
  (if-let [user (load-user username)]
    (transact-data [{:db/id (:id user) ::last-failed-login (java.util.Date.)}])
  )
)
