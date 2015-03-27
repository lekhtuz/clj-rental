(ns rental.geonames
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [clojure.data.json :as json]
    [clj-http.client :as client]
    [carica.core :as cc]
  )
)

; http://api.geonames.org/countryInfoJSON?country=US&username=lekhtuz
; http://api.geonames.org/childrenJSON?geonameId=6252001&username=lekhtuz

(def geonames-username (cc/config ::username))

(declare ^:dynamic *us-states-map*)
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
    (log/info "load-states status =" status)
    (log/info "load-states body =" body)
    (if (= status 200)
      (let [us-geoname-id ((((json/read-str body) "geonames") 0) "geonameId")] 
        (log/info "load-states us-geoname-id =" us-geoname-id)
        (let [{:keys [status body]} (client/get (cc/config ::states-url)
                                                {
                                                 :query-params {"geonameId" us-geoname-id "username" geonames-username}
                                                 :accept :json
                                                }
                                    )
              ]
          (log/info "load-states status =" status)
          (log/info "load-states body =" body)
          (if (= status 200)
            (let [us-states ((json/read-str body) "geonames")]
              (log/info "load-states us-states =" (count us-states))
              (log/info "load-states us-states[0] =" (us-states 0))
              (def ^:dynamic *us-states-map* (reduce #(assoc %1 (%2 "adminCode1") (%2 "toponymName")) { } us-states))
              (log/info "load-states *us-states-map* =" (count *us-states-map*) *us-states-map*)
              (def ^:dynamic *us-states-seq* (map #(vector (% "toponymName") (% "adminCode1")) us-states))
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
