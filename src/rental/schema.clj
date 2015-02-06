(ns rental.schema
  (:require
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log :refer [info]]
            [datomic.api :as d]
  )
)

(def uri "datomic:dev://localhost:4334/rental")

(defn start []
  (log/info "Starting db...")
  (if (d/create-database uri)
    (log/info "database " uri " created.")
    (log/info "database " uri " already exists.")
  )
)

(defn stop []
  (log/info "Stoping db...")
)