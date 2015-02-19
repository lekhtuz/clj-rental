(ns rental.routes.home
  (:require [compojure.core :refer :all]
            [hiccup.core :as h]
            [hiccup.element :as h-e :refer [link-to]]
            [clojure.pprint :refer [pprint]]
            [clojure.tools.logging :as log :refer [info]]
            [rental.auth :as auth]
            [rental.schema :as schema]
            [rental.views.layout :as layout]
            [rental.views.login :refer [login-box]]
            [cemerick.friend :as friend]
            (cemerick.friend [workflows :as workflows]
                             [credentials :as creds])
  )
)

(defn home-anonymous []
  (layout/common (h/html [:h1 "Welcome to the web site"] (login-box)))
)

(defn home-authenticated [identity]
  (log/info "home-authenticated function called. :friend/identity = " identity)
  (log/info "home-authenticated function called. current-authentication = " (friend/current-authentication))
  (log/info "home-authenticated function called. friend/authorized? #{:rental.auth/role-admin} identity = " (friend/authorized? #{:rental.auth/role-admin} identity))
  (log/info "home-authenticated function called. friend/authorized? #{:rental.auth/role-user} identity = " (friend/authorized? #{:rental.auth/role-user} identity))
  (if (friend/authorized? #{:rental.auth/role-admin} identity)
    (layout/common (h/html [:h1 "Logged-in admin"]))
    (layout/common (h/html [:h1 "Logged-in user"]))
  )
)

(defn home [request]
  (log/info "Home function called. request = " (with-out-str (pprint request)))
  (log/info "Home function called. session = " (:session request))
  (log/info "Home function called. friend/anonymous? = " (friend/anonymous?))
  (if (friend/anonymous?)
    (home-anonymous)
    (home-authenticated (-> request :session :cemerick.friend/identity))
  )
)

(defn login []
  (log/info "login function called")
  (layout/common (h/html [:h1 "Enter your credentials:"] (login-box)))
)

(defn do-login [username password]
  (log/info "Login post called")
  (layout/common (str "username=" username ", password=" password))
)

(defroutes home-routes
  (GET "/" request (home request))
  (GET "/login" request (login))
  (POST "/login" [username password] (do-login username password))
  (context "/schema" []
    (GET "/" request (schema/maintenance))
    (GET "/create" request (schema/create-database))
    (GET "/delete" request (schema/delete-database))
  )
  (friend/logout (ANY "/logout" request (ring.util.response/redirect "/")))
)
