(ns rental.handler
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [cemerick.friend :as friend]
    (cemerick.friend [workflows :as workflows]
                     [credentials :as creds])
    [compojure.core :refer [defroutes]]
    [compojure.handler :as handler]
    [compojure.route :as route]
    [ring.util.response :as resp]
    [ring.middleware.resource :refer [wrap-resource]]
    [ring.middleware.file-info :refer [wrap-file-info]]
    [hiccup.page :as h]
    [hiccup.element :as e]
    [hiccup.middleware :refer [wrap-base-url]]
    [rental.routes.home :as home :refer [home-routes]]
    [rental.auth :as auth :refer [authenticate]]
    [rental.schema :as schema]
    [rental.geonames :as geonames]
    [rental.middleware :refer [wrap-log-request-response]]
  )
  (:import
    [javax.servlet.http HttpServletResponse]
  )
)

(defn init []
  (log/info "rental is starting")
  (schema/start)
  (geonames/start)
)

(defn destroy []
  (log/info "rental is shutting down")
  (schema/stop)
)

(defroutes app-routes
  (route/resources "/")
  (route/not-found "The page you are trying to access does not exist.")
)

(defroutes myroutes home-routes app-routes)

; This is referenced from :ring:handler in project.clj and called from autogenerated rental.servlet
; middleware handlers are executed from bottom to top
(def app
  (->
    (handler/site
      (friend/authenticate myroutes 
                           {
                            :allow-anon? true
                            :login-url "/login"
                            :default-landing-uri "/landing"
                            :unauthorized-handler 
                              #(-> (h/html5 [:h2 "You do not have sufficient privileges to access " (:uri %)])
                                          resp/response
                                          (resp/status HttpServletResponse/SC_UNAUTHORIZED))
                            :credential-fn #(creds/bcrypt-credential-fn auth/authenticate %)
                            :workflows [(workflows/interactive-form)]}))
    (wrap-log-request-response "after wrap-base-url")
    (wrap-base-url)
    (wrap-log-request-response "before wrap-base-url")
  )
)