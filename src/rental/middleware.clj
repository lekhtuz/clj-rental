(ns rental.middleware
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
  )
)

(defn wrap-log-request-response
  [handler description]
  (fn [request]
    (log/info "wrap-log-request-response: --" description "-- request =" (with-out-str (pprint request)))
    (let 
      [
        start-time (System/nanoTime)
        response (handler request)
        end-time (System/nanoTime)
        duration (/ (double (- end-time start-time)) 1000000) ; in milliseconds
      ]
      (log/info "wrap-log-request-response: --" description "-- duration=" duration "ms, response =" (with-out-str (pprint response)))
      response
    )
  )
)
