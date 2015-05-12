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
           (is (delete-database))
  )

  (log/info "Waiting 60 seconds for the database name to become available...")
  (. Thread sleep 60000)

  (testing "Testing create database..."
           (is (create-database))
  )

  (testing "Testing load-user..."
           (is (nil? (load-user "user-does-not-exist")))
           (let [ent (load-user "admin")]
             (log/info "ent =" ent)
             (is (= (:rental.schema/username ent) "admin"))
             (is (= (:rental.schema/usertype ent) :rental.auth/role-admin))
           )
  )
)
