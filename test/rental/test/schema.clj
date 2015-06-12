(ns rental.test.schema
  (:use
    [clojure.test]
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [cemerick.friend.credentials :as creds]
    [clj-time.local :as l :refer [to-local-date-time]]
    [rental.schema :refer [create-rental-database delete-rental-database load-user create-user update-last-successful-login update-last-failed-login load-all-users]]
  )
  (:import
;    [java.util Thread]
  )
)

(def create-user-test-cases
  [
   { :username "test-admin",    :status :active, :password "password", :usertype :rental.auth/role-admin,    :firstname "Fadmin",    :lastname "Ladmin",    :email "test-admin@email.com" }
   { :username "test-landlord", :status :active, :password "password", :usertype :rental.auth/role-landlord, :firstname "Flandlord", :lastname "Llandlord", :email "test-landlord@email.com", :address1 "1 Main St", :city "Franklin Lakes", :state "NJ", :zipcode "07417" }
   { :username "test-tenant",   :status :active, :password "password", :usertype :rental.auth/role-tenant,   :firstname "Ftenant",   :lastname "Ltenant",   :email "test-tenant@email.com",   :address1 "1 Mountain View Dr", :address2 "right hand side of the street", :city "Woodland Park", :state "NJ", :zipcode "07424" }
   { :username "test-inactive-tenant", :status :inactive, :password "password", :usertype :rental.auth/role-tenant, :firstname "Finactivetenant", :lastname "Ltenant", :email "test-inactive-tenant@email.com",   :address1 "1 Mountain View Dr", :address2 "right hand side of the street", :city "Woodland Park", :state "NJ", :zipcode "07424" }
  ]
)

(defn- same-user? [original copy]
  (log/info "same-user?: original =" original)
  (log/info "same-user?: copy =" copy)
  (is (seq copy))
  (is (= (:username copy) (:username original)))
  (is (creds/bcrypt-verify (:password original) (:password copy)))
  (is (= (:usertype copy) (:usertype original)))
  (is (= (:first-name copy) (:firstname original)))
  (is (= (:last-name copy) (:lastname original)))
  (is (= (:address1 copy) (:address1 original)))
  (is (= (:address2 copy) (:address2 original)))
  (is (= (:city copy) (:city original)))
  (is (= (:state copy) (:state original)))
  (is (= (:zipcode copy) (:zipcode original)))
)

(deftest test-schema
  (testing "Testing delete database..."
           (is (delete-rental-database))
  )

  (log/info "Waiting 60 seconds for the database name to become available again...")
  (Thread/sleep 60000)

  (testing "Testing create database..."
           (is (create-rental-database))
  )

  (testing "Testing load-user..."
           (is (empty? (load-user "user-does-not-exist")))
           (is (empty? (load-user nil)))

           (let [ent (load-user "admin")]
             (log/info "ent =" (with-out-str (pprint ent)))
             (is (seq ent))
             (is (= (:username ent) "admin"))
             (is (= (:usertype ent) :rental.auth/role-admin))
             (is (= (:first-name ent) "Barak"))
             (is (= (:last-name ent) "Obama"))
             (is (= (:city ent) "Washington"))
             (is (= (:state ent) "DC"))
           )

           (let [ent (load-user "demo-admin")]
             (log/info "ent =" (with-out-str (pprint ent)))
             (is (seq ent))
             (is (= (:username ent) "demo-admin"))
             (is (= (:usertype ent) :rental.auth/role-admin))
             (is (= (:first-name ent) "FirstDemo"))
             (is (= (:last-name ent) "LastDemo"))
             (is (nil? (:address1 ent)))
           )

           (let [ent (load-user "demo-landlord")]
             (log/info "ent =" (with-out-str (pprint ent)))
             (is (seq ent))
             (is (= (:username ent) "demo-landlord"))
             (is (= (:usertype ent) :rental.auth/role-landlord))
             (is (= (:first-name ent) "FirstDemo"))
             (is (= (:last-name ent) "LastDemo"))
             (is (nil? (:address1 ent)))
           )

           (let [ent (load-user "demo-tenant")]
             (log/info "ent =" (with-out-str (pprint ent)))
             (is (seq ent))
             (is (= (:username ent) "demo-tenant"))
             (is (= (:usertype ent) :rental.auth/role-tenant))
             (is (= (:first-name ent) "FirstDemo"))
             (is (= (:last-name ent) "LastDemo"))
             (is (nil? (:address1 ent)))
           )
  )

  (testing "Testing create-user..."
           (is (nil? (create-user nil)))

           (doseq [test-user create-user-test-cases]
             (log/info "Test user - " test-user)
             (is (create-user test-user))
             (when-let [retrieved-user (load-user (:username test-user))]
               (same-user? test-user retrieved-user)
               (update-last-successful-login (:id retrieved-user))
               (update-last-failed-login (:username retrieved-user))
                 
               (let [{:keys [last-successful-login last-failed-login]} (load-user (:username test-user))]
                 (log/info "last-successful-login =" (l/to-local-date-time last-successful-login))
                 (is (not (nil? last-successful-login)))
                 (log/info "last-failed-login =" (l/to-local-date-time last-failed-login))
                 (is (not (nil? last-failed-login)))
               )
             )
           )
  )

  (testing "Testing load all users..."
           (let [users (load-all-users)]
             (log/info "users =" (with-out-str (pprint users)))
           )
  )
)
