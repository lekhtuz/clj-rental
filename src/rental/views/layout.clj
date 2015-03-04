(ns rental.views.layout
  (:require
    [hiccup.page :refer [html5 include-css]]
    [hiccup.element :as h-e :refer [link-to]]
  )
)

(defn common [& body]
  (html5
    [:head
     [:title "Welcome to rental!"]
     (include-css "/css/screen.css")
    ]
    [:body
     [:table { :width "700px" }
       [:tr
        [:td { :align "left" } [:h1 "R E N T A L"]]
        [:td { :align "right" } "My Account" "&nbsp;|&nbsp;" (h-e/link-to "/logout" "Logout") ]
       ]
       [:tr
        [:td { :colspan "2" } body]
       ]
     ]
    ]
  )
)
