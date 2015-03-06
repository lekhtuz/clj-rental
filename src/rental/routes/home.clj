(ns rental.routes.home
  (:require 
    [compojure.core :refer :all]
    [hiccup.core :as h]
    [hiccup.element :as h-e :refer [link-to]]
    [clojure.pprint :refer [pprint]]
    [clojure.tools.logging :as log :refer [info]]
    [rental.auth :as auth]
    [rental.schema :as schema]
    [rental.views.layout :as layout]
    [rental.views.admin :as admin]
    [rental.views.tenant :as tenant]
    [rental.views.landlord :as landlord]
    [rental.views.login :as login :refer [login-box login]]
    [cemerick.friend :as friend]
    [ring.util.response :as resp]
    (cemerick.friend [workflows :as workflows]
                     [credentials :as creds])
  )
)

(defn home-anonymous []
  (log/info "home-anonymous function called.")
  (layout/common (h/html [:h1 "Welcome to the web site"] (login/login-box)))
)

(defn home-authenticated [identity]
  (log/info "home-authenticated function called. :friend/identity = " identity)
  (log/info "home-authenticated function called. current-authentication = " (friend/current-authentication))
  (log/info "home-authenticated function called. friend/authorized? #{:rental.auth/role-admin} identity = " (friend/authorized? #{:rental.auth/role-admin} identity))
  (log/info "home-authenticated function called. friend/authorized? #{:rental.auth/role-landlord} identity = " (friend/authorized? #{:rental.auth/role-landlord} identity))
  (log/info "home-authenticated function called. friend/authorized? #{:rental.auth/role-tenant} identity = " (friend/authorized? #{:rental.auth/role-tenant} identity))
  (condp friend/authorized? identity
    #{:rental.auth/role-admin} (resp/redirect "/admin")
    #{:rental.auth/role-landlord} (resp/redirect "/landlord")
    #{:rental.auth/role-tenant} (resp/redirect "/tenant")
  )
)

(defn home [request]
  (log/info "Home function called. friend/anonymous? = " (friend/anonymous?))
  (if (friend/anonymous?)
    (home-anonymous)
    (home-authenticated (-> request :session :cemerick.friend/identity))
  )
)

(defroutes admin-routes
    (GET "/" request (admin/home))
)

(defroutes landlord-routes
    (GET "/" request (landlord/home))
)

(defroutes tenant-routes
    (GET "/" request (tenant/home))
)

(defroutes home-routes
  (GET "/" request (home request))
  (GET "/login" request (login/login))
  (POST "/login" [username password] (login/login username password))
  (GET "/lregister" request (landlord/register))
  (POST "/lregister" [username password firstname lastname] (landlord/register username password firstname lastname))
  (context "/admin" request (friend/wrap-authorize admin-routes #{:rental.auth/role-admin}))
  (context "/landlord" request (friend/wrap-authorize landlord-routes #{:rental.auth/role-landlord}))
  (context "/tenant" request (friend/wrap-authorize tenant-routes #{:rental.auth/role-tenant}))
  (context "/schema" []
    (GET "/" request (schema/maintenance))
    (GET "/create" request (schema/create-database))
    (GET "/delete" request (schema/delete-database))
  )
  (friend/logout (ANY "/logout" request (resp/redirect "/")))
)
