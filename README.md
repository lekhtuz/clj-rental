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


## Start datomic transactor:

cd /Users/lekhdm/java/datomic-pro-0.9.5130
bin/transactor ./config/samples/dev-transactor-template.properties

## Datomic connection test

https://groups.google.com/forum/#!topic/datomic/1WBgM84nKmc
Can not delete and immediately create database, name is not available for 1 minute.

(use '[clojure.edn :refer [read-string]])
(use '[carica.core :as cc])
(use '[datomic.api :as d])
(def uri "datomic:dev://localhost:4334/rental")
(def schema (read-string {:readers *data-readers*} (slurp (first (cc/resources "schema.edn")))))
(println schema)
(d/delete-database uri)
(d/create-database uri)
(def conn (d/connect uri))
@(d/transact (d/connect uri) schema)

