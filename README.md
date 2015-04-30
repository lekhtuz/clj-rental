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

(use '[clojure.edn :refer [read-string]])
(use '[carica.core :as cc])
(use '[cemerick.friend.credentials :as creds])
(use '[datomic.api :as d])

(def uri "datomic:dev://localhost:4334/rental")
(def schema-tx (read-string {:readers *data-readers*} (slurp (first (cc/resources "schema.edn")))))
(def setup-data-tx (read-string {:readers *data-readers*} (slurp (first (cc/resources "setup-data.edn")))))
(def setup-data-encrypted-passwords-tx (map #(assoc % :rental.schema/password (creds/hash-bcrypt (:rental.schema/password %))) setup-data-tx))

(d/delete-database uri)
(d/create-database uri)
; wait 1 minute
(def conn (d/connect uri))
@(d/transact conn schema-tx)
@(d/transact conn setup-data-encrypted-passwords-tx)

(def results (q '[:find ?e :where [?e :rental.schema/password "password"]] (db conn)))
; returns entities with password "password"
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
; convert to real map, id fields are missung. nested structures require it's own convertion.

(def results (q '[:find ?e :where [_ :rental.schema/username ?e]] (db conn)))
; returns all usernames

(println results)
#<HashSet [[demo-user], [admin], [demo-landlord], [demo-admin]]>

(def results (q '[:find ?e :where [_ :rental.schema/password ?e]] (db conn)))
; returns all passwords. note that distinct is automatically applied
(println results)
#<HashSet [[password]]>

