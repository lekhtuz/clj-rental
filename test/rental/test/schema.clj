(ns rental.test.schema
  (:use
    [clojure.test]
    [clojure.tools.logging :as log :refer [info]]
    [rental.schema :refer [create-database delete-database load-user]]
  )
  (:import
;    [java.util Thread]
  )
)

(deftest test-schema
  (testing "Testing delete database..."
;           (is (delete-database))
  )

  (log/info "Waiting 60 seconds for the database name to become available...")
;  (. Thread sleep 60000)

  (testing "Testing create database..."
;           (is (create-database))
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
  )
)
