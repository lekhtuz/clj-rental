# rental

FIXME

## Prerequisites

You will need [Leiningen][1] 1.7.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running

To start a web server for the application, run:

    lein ring server

Create uberwar:
lein ring uberwar

Remove all attributes from web.xml <webapp> tag before copying to resin/webapps folder

Connect to repl:
lein repl :connect localhost:3001

## License

Copyright © 2015 FIXME

## Git commands

git init
git add --all *
git commit -m "first commit"
git remote add -m dev origin https://github.com/lekhtuz/rental.git
git branch --set-upstream-to=origin/dev master
git push origin HEAD:dev


## Datomic commands:

cd /Users/lekhdm/java/datomic-pro-0.9.5130
bin/transactor ./config/samples/dev-transactor-template.properties
bin/console -p 8081 dev datomic:dev://localhost:4334

## Datomic connection test

https://groups.google.com/forum/#!topic/datomic/1WBgM84nKmc
Can not delete and immediately create database, name is not available for 1 minute.

d/transact returns a future map with the following fields:
:db-before Datomic.db.DB
:db-after Datomic.db.DB
:tx-data List<datomic.db.Datum>
:tempids { tempdbid realdbid... }

(use '[clojure.edn :refer [read-string]])
(use '[carica.core :as cc])
(use '[cemerick.friend.credentials :as creds])
(use '[datomic.api :as d])

(def schema-tx (read-string {:readers *data-readers*} (slurp (first (cc/resources "schema.edn")))))
(def setup-data-tx (read-string {:readers *data-readers*} (slurp (first (cc/resources "setup-data.edn")))))
(def setup-data-encrypted-passwords-tx (map #(assoc % :rental.schema/password (creds/hash-bcrypt (:rental.schema/password %))) setup-data-tx))

(def uri ((cc/config :rental.schema/db-key) (cc/config :rental.schema/db-url)))
(d/delete-database uri)
(d/create-database uri)
; wait 1 minute
(def conn (d/connect uri))
(def rdb (d/db conn))
(def hdb (d/history rdb))
@(d/transact conn schema-tx)
@(d/transact conn setup-data-encrypted-passwords-tx)

(def results (q '[:find ?e :where [?e :rental.schema/password (creds/hash-bcrypt "password")]] rdb))
; returns entities with password "password" or #{} - empty set
(println results)
; returns Set of Vectors #<HashSet [[17592186045422], [17592186045421], [17592186045424], [17592186045423]]>

(println (count results))

(def id (ffirst results))
; returns the first id - 17592186045422

(def ent (-> conn db (d/entity id)))
; returns the entity, associated with the given id
(type ent)
; datomic.query.EntityMap - according to some articles, it's not a real map
(pprint (keys ent))
; keys associated with entity's attributes (:rental.schema/usertype :rental.schema/password :rental.schema/username)
(println (:rental.schema/username ent))
; prints the value of the attribute
(d/touch ent)
; generate all lazy attributes
(into {} (d/touch ent))
; convert to real map, id fields are missing. nested structures require it's own conversion.

(def results (q '[:find ?e :where [_ :rental.schema/username ?e]] rdb))
; returns all usernames

(println results)
#<HashSet [[demo-user], [admin], [demo-landlord], [demo-admin]]>

(def results (q '[:find ?e :where [?e :rental.schema/username _]] rdb))
; returns all user :db/id

(println results)
#<HashSet 

(def results (q '[:find ?tx ?v ?op :in $ ?e ?attr :where [?e ?attr ?v ?tx ?op]] hdb 17592186045423 :rental.schema/last-successful-login))
; returns history of changes for the entity/attribute. ?op can be replaced with true, since we are not interested in retractions
; the first value is the transaction id. It is an entity with only 2 attributed - :db/id and :db/txInstant

(println results)
#<HashSet [[13194139534343 #inst "2015-06-05T21:13:53.422-00:00" true], [13194139534344 #inst "2015-06-05T21:34:04.822-00:00" true], [13194139534344 #inst "2015-06-05T21:13:53.422-00:00" false]]>

(def results (d/q '[:find ?tx ?a :in $ ?e :where [?e ?a _ ?tx]] hdb 17592186045423))
; Finds all tuples of the tx and the actual attribute that changed for a specific entity.
; The first value is the transaction id. It is an entity with only 2 attributed - :db/id and :db/txInstant
; The second value is the numeric representation of the attribute (probably)

(println results)
#{[13194139534318 64] [13194139534318 63] [13194139534318 66] [13194139534318 65] [13194139534318 67] [13194139534318 72] [13194139534318 71] [13194139534344 68] [13194139534318 73] [13194139534343 68]}

(def results (q '[:find ?e :where [_ :rental.schema/password ?e]] rdb))
; returns all passwords. note that distinct is automatically applied
(println results)
#<HashSet [[password]]>

(d/get-database-names uri)
; returns sequence with all databases. this requires * instead of database name in url: 

