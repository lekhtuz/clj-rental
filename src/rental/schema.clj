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
(def query-attribute-history '[:find ?v in $ ?e ?attr :where [?e ?attr ?v _ true]])

; Print all arguments and return the last one, useful inside -> and ->>
(defn- spy [& args]
  (apply prn args)
  (last args)
)

; Retrieve connection every time it is needed. It is cached internally, so it's cheap.
(defn conn []
  (log/info "conn: Connection request received. Calling d/connect...")
  (spy "conn: Connection retrieved" (d/connect uri))
)

; This is the regular database
(defn rdb []
  (log/info "db: Database request received. Calling d/db...")
  (spy "db: Database retrieved" (d/db (conn)))
)

; This is the history database
(defn hdb []
  (log/info "hdb: History database request received. Calling d/history...")
  (spy "db: Database retrieved" (d/history rdb))
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
      (log/info "transact-data: error transacting data " (with-out-str (.printStackTrace e))) 
      { :result false, :message (.getMessage e), :exception e }
    )
  )
)

; Function returns true, if database was created, schema and setup data successfuly transacted, false if exception was thrown
(defn create-rental-database []
  (log/info "create-database: uri =" uri)
  (try
    (if (d/create-database uri)
      (do
        (log/info "create-database: database" uri "created. connection =" (conn) ". Adding schema =" schema-tx)
        (->> schema-tx
          transact-data
          (log/info "Schema added. transaction-result =")
        )
        (->> setup-data-encrypted-passwords-tx
          transact-data
          (log/info "Setup data added. transaction-result =")
        )
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

; Run query on the reqular database
(defn run-query 
  ([query]
    (log/info "run-query: query =" query)
    (spy "run-query: result =" (d/q query (rdb)))
  )
  ([query param]
    (log/info "run-query: query =" query ", param =" param)
    (spy "run-query: result =" (d/q query (rdb) param))
  )
)

; Run query on the history database
(defn run-history-query 
  ([query]
    (log/info "run-history-query: query =" query)
    (spy "run-history-query: result =" (d/q query (hdb)))
  )
  ([query param1 param2]
    (log/info "run-history-query: query =" query ", param1 =" param1 ", param2 =" param2)
    (spy "run-history-query: result =" (d/q query (hdb) param1 param2))
  )
)

; Load attribute history
(defn load-attribute-history [entity-id attribute-id]
  (log/info "load-attribute-history: entity-id =" entity-id ", attribute-id =" attribute-id)
  (reduce #(conj %1 [(:db/txInstant %2) (%2 1)]) (run-history-query entity-id attribute-id))
)

; Check if the passed object is a Datomic entity
(defn datomic-entity? [o]
  (instance? datomic.Entity o)
)

; Convert a Datomic entity to a map, where keys correspond to the database attribute keys
(declare convert-ids-to-maps) ; need this forward declaration due to recursive calls
(defn convert-entity-to-map [db ent]
  (log/info "convert-entity-to-map: ent =" ent ", (keys ent) =" (keys ent))
  (reduce
    #(assoc %1 %2
            (let [value (%2 ent)]
              (if (set? value)
                (if (every? datomic-entity? value) (convert-ids-to-maps db value) value)
                (if (datomic-entity? value) (convert-entity-to-map db value) value)
              )
            )
     )
    {:db/id (:db/id ent)} (keys ent)
  )
)

; Convert set of single element vectors #{ [id1] [id2]...} as returned by run-query to a set/vector/list of maps, where keys correspond to the database attribute keys
; Further single element conversion is performed by the optional function parameter
(defn convert-ids-to-maps 
  ([db ids]
    (convert-ids-to-maps db ids (fn [param] param))
  )
  ([db ids f]
    (log/info "convert-ids-to-maps: ids =" ids ", f =" f)
    (reduce
      #(conj %1 (f (convert-entity-to-map db (d/entity db (first %2)))))
      #{} ids) ; change #{} to [] to return vector of maps, to '() to return list of maps
  )
)

(defn convert-map-to-user [db m]
  (log/info "convert-map-to-user: m =" (with-out-str (pprint m)))
  ; The only reason I test m for nil is because of :usertype and :status. Otherwise an empty map is beautifully generated despite of m being nil.
  (if (seq m)
    (->
      (if-let [last-successful-login (::login-attempts m)]
        { :login-attempts (convert-ids-to-maps db (::login-attempts m))}
      )
      (merge ; merges all maps into one. nil maps are conveniently ignored.
        (if-let [last-failed-login (::last-failed-login m)]
          { :last-failed-login last-failed-login }
        )
        {
         :status (-> m ::status dbstatus-status)
         :usertype (-> m ::usertype usertype-role)
        }
        (if-let [address (::mailing-address m)]
          (set/rename-keys (convert-entity-to-map db address) dbaddress-address)
        )
        (set/rename-keys m { :db/id :id, ::username :username, ::email :email, ::password :password, ::first-name :first-name, ::last-name :last-name })
      )
      (dissoc ; cleanup to remove db versions of the attribute keys
        ::usertype ::mailing-address ::last-successful-login ::last-failed-login
      )
    )
  )
)

(defn convert-ids-to-users [db ids]
  (log/info "convert-ids-to-users: ids =" ids)
  (convert-ids-to-maps db ids convert-map-to-user)
)

(defn load-user [username]
  (log/info "load-user: username =" username)
  (if (seq username)
    (->> username
      (run-query query-load-user)
      (convert-ids-to-maps (rdb))
      first
      (convert-map-to-user (rdb))
    )
  )
)

(defn load-all-users []
  (log/info "load-all-users: started")
  (convert-ids-to-users (rdb) (run-query query-load-all-users))
)

(defn load-login-history [] 1
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

(defn update-last-successful-login [id ip-address]
  (log/info "update-last-successful-login: id =" id)
  (transact-data [{:db/id id ::login-attempts
                   {
                    :rental.schema.login-attempt/timestamp (java.util.Date.)
                    :rental.schema.login-attempt/result :rental.schema.login-attempt.result/success
;                    :rental.schema.login-attempt/ip-address ip-address
                   }
                  }
                 ]
  )
  (log/info "update-last-successful-login: updated")
)

(defn update-last-failed-login [username ip-address]
  (log/info "update-last-failed-login: username =" username)
  (if-let [user (load-user username)]
    (transact-data [{:db/id (:id user) ::login-attempts
                     {
                      :rental.schema.login-attempt/timestamp (java.util.Date.)
                      :rental.schema.login-attempt/result :rental.schema.login-attempt.result/failure
;                      :rental.schema.login-attempt/ip-address ip-address
                     }
                    }
                   ]
    )
  )
)
