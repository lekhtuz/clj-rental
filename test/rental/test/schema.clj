(ns rental.test.schema
  (:use
    [clojure.test]
    [clojure.tools.logging :as log :refer [info]]
    [cemerick.friend.credentials :as creds]
    [rental.schema :refer [create-database delete-database load-user create-user]]
  )
  (:import
;    [java.util Thread]
  )
)

(defn- same-user? [original copy]
  (log/info "same-user?: original =" original)
  (log/info "same-user?: copy =" copy)
  (is (seq copy))
  (is (= (:username copy) (:username original)))
  (is (creds/bcrypt-verify (:password original) (:password copy)))
  (is (= (:usertype copy) (:usertype original)))
  (is (= (:firstname copy) (:firstname original)))
  (is (= (:lastname copy) (:lastname original)))
  (is (= (:address1 copy) (:address1 original)))
  (is (= (:address2 copy) (:address2 original)))
  (is (= (:city copy) (:city original)))
  (is (= (:state copy) (:state original)))
  (is (= (:zipcode copy) (:zipcode original)))
)

(deftest test-schema
  (testing "Testing delete database..."
           (is (delete-database))
  )

  (log/info "Waiting 60 seconds for the database name to become available...")
  (. Thread sleep 60000)

  (testing "Testing create database..."
           (is (create-database))
  )

  (testing "Testing load-user..."
           (is (nil? (load-user "user-does-not-exist")))
           (is (nil? (load-user nil)))

           (let [ent (load-user "admin")]
             (log/info "ent =" ent)
             (is (seq ent))
             (is (= (:username ent) "admin"))
             (is (= (:usertype ent) :rental.auth/role-admin))
             (is (= (:firstname ent) "Barak"))
             (is (= (:lastname ent) "Obama"))
             (is (= (:city ent) "Washington"))
             (is (= (:state ent) "DC"))
           )

           (let [ent (load-user "demo-admin")]
             (log/info "ent =" ent)
             (is (seq ent))
             (is (= (:username ent) "demo-admin"))
             (is (= (:usertype ent) :rental.auth/role-admin))
             (is (= (:firstname ent) "FirstDemo"))
             (is (= (:lastname ent) "LastDemo"))
             (is (nil? (:address1 ent)))
           )

           (let [ent (load-user "demo-landlord")]
             (log/info "ent =" ent)
             (is (seq ent))
             (is (= (:username ent) "demo-landlord"))
             (is (= (:usertype ent) :rental.auth/role-landlord))
             (is (= (:firstname ent) "FirstDemo"))
             (is (= (:lastname ent) "LastDemo"))
             (is (nil? (:address1 ent)))
           )

           (let [ent (load-user "demo-tenant")]
             (log/info "ent =" ent)
             (is (seq ent))
             (is (= (:username ent) "demo-tenant"))
             (is (= (:usertype ent) :rental.auth/role-tenant))
             (is (= (:firstname ent) "FirstDemo"))
             (is (= (:lastname ent) "LastDemo"))
             (is (nil? (:address1 ent)))
           )
  )

  (testing "Testing create-user..."
           (is (nil? (create-user nil)))

           (let [params {
                         :username "test-admin"
                         :password "password"
                         :usertype :rental.auth/role-admin
                         :firstname "Fadmin"
                         :lastname "Ladmin"
                         :email "test-admin@email.com"
                        }]
             (is (create-user params))
             (same-user? params (load-user (:username params)))
           )

           (let [params {
                         :username "test-landlord"
                         :password "password"
                         :usertype :rental.auth/role-landlord
                         :firstname "Flandlord"
                         :lastname "Llandlord"
                         :email "test-landlord@email.com"
                         :address1 "1 Main St"
                         :city "Franklin Lakes"
                         :state "NJ"
                         :zipcode "07417"
                        }]
             (is (create-user params))
             (same-user? params (load-user (:username params)))
           )

           (let [params {
                         :username "test-tenant"
                         :password "password"
                         :usertype :rental.auth/role-tenant
                         :firstname "Ftenant"
                         :lastname "Ltenant"
                         :email "test-tenant@email.com"
                         :address1 "1 Mountain View Dr"
                         :address2 "right hand side of the street"
                         :city "Woodland Park"
                         :state "NJ"
                         :zipcode "07424"
                        }]
             (is (create-user params))
             (same-user? params (load-user (:username params)))
           )
  )
)
