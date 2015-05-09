(ns rental.geonames
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [clojure.data.json :as json]
    [clj-http.client :as client]
    [carica.core :as cc]
  )
  (:import
    [javax.servlet.http HttpServletResponse]
  )
)

(def geonames-username (cc/config ::username))

; Sorted map
(declare ^:dynamic *us-states-map*)

; Sorted sequence of ["New Jersey" "NJ"], suitable for HTML dropdowns/selects
(declare ^:dynamic *us-states-seq*)

(defn load-states [country]
  (log/info "load-states country =" country)
  (let [{:keys [status body]} (client/get (cc/config ::country-url)
                                          {
                                           :query-params {"country" country "username" geonames-username}
                                           :accept :json
                                          }
                              )
        ]
    (log/info "load-states: status =" status ", body =" body)
    (if (= status (HttpServletResponse/SC_OK))
      (let [us-geoname-id ((((json/read-str body) "geonames") 0) "geonameId")] 
        (log/info "load-states us-geoname-id =" us-geoname-id)
        (let [{:keys [status body]} (client/get (cc/config ::states-url)
                                                {
                                                 :query-params {"geonameId" us-geoname-id "username" geonames-username}
                                                 :accept :json
                                                }
                                    )
              ]
          (log/info "load-states: status =" status ", body =" (count body) " entries.")
          (if (= status (HttpServletResponse/SC_OK))
            (let [us-states ((json/read-str body) "geonames")]
              (log/info "load-states us-states =" (count us-states))
              (log/info "load-states us-states[0] =" (us-states 0))
              (def ^:dynamic *us-states-map* (reduce #(assoc %1 (%2 "adminCode1") (%2 "toponymName")) { } us-states))
              (log/info "load-states *us-states-map* =" (count *us-states-map*) *us-states-map*)
              (def ^:dynamic *us-states-seq* (sort-by last (map #(vector (% "toponymName") (% "adminCode1")) us-states)))
              (log/info "load-states *us-states-seq* =" (count *us-states-seq*) *us-states-seq*)
            )
          )
        )
      )
    )
  )
)

(defn get-states-map []
  (if (nil? *us-states-map*) (load-states "US"))
  *us-states-map*
)

(defn get-states-seq []
  (if (nil? *us-states-map*) (load-states "US"))
  *us-states-seq*
)

(defn start []
  (log/info "Loading geonames...")
  (load-states "US")
)
