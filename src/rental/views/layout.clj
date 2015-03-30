(ns rental.views.layout
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label]]
    [hiccup.page :refer [html5 include-css]]
    [cemerick.friend :as friend :refer [anonymous?]]
  )
)

(def default-errors {:class "error"})

(defn add-error [errors field text]
  (assoc errors field text)
)

(defn has-errors [errors]
  (> (count errors) (count default-errors))
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
        [:td { :align "left" } [:h1 "R&nbsp;E&nbsp;N&nbsp;T&nbsp;A&nbsp;L"]]
        [:td { :align "right" } (if-not (friend/anonymous?) [:span "My Account" "&nbsp;|&nbsp;" (link-to "/logout" "Logout")]) ]
       ]
       [:tr
        [:td { :colspan "2" } body]
       ]
     ]
    ]
  )
)

(defn print-error [errors field]
  [:span {:class (errors :class)} (errors field)]
)

(defn form-row 
  ([errors [type name info value]]
    (log/info "form-row: type =" type ", name =" name ", info =" info ", value =" value ", errors =" errors)
    [:tr
     [:td (label name info)]
     [:td (type name value) (print-error errors name)]
    ]
  )
  ([colclasses errors row]
    (log/info "form-row: errors =" errors ", colclasses =" colclasses ", row =" row)
    (let [[tr & tds] (form-row errors row)]
      (log/info "form-row: tr=" tr ", tds=" tds)
      [tr (if (= tr :tr)
            (map #(if (map? (second %1))
                         (assoc (assoc (second %1) :class %2) 1 %1)
                         (apply conj [(first %1)] {:class %2} (drop 1 %1))
                       ) tds colclasses
            )
            tds
          )
      ]
    )
  )
)

