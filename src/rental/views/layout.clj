(ns rental.views.layout
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [hiccup.element :refer [link-to]]
    [hiccup.form :refer [label]]
    [hiccup.page :refer [html5 include-css]]
    [cemerick.friend :as friend :refer [anonymous?]]
    [rental.validation :as validation]
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

(defn form-row 
  ([form-info [type-fn field info value]]
    (log/info "form-row: type-fn =" type-fn ", field =" field ", info =" info ", value =" value ", form-info =" form-info)
    [:tr
     [:td (label field info)]
     [:td (type-fn field value) (validation/print-error form-info field)]
    ]
  )
  ([colclasses form-info row]
    (log/info "form-row: form-info =" form-info ", colclasses =" colclasses ", row =" row)
    (let [[tr & tds] (form-row form-info row)]
      (log/info "form-row: tr=" tr ", tds=" tds)
      [tr (if (= tr :tr)
            (map #(if (map? (second %1))                               ; If <td> vector has an attribute map as a second element
                    (assoc-in %1 [1 :class] %2)                        ; insert class attribute into that map
                    (apply conj [(first %1)] {:class %2} (drop 1 %1))  ; otherwise insert a new attribute map between the first an the second element
                  ) tds colclasses
            )
            tds
          )
      ]
    )
  )
)

; rows - sequence of [type-fn :field info]
; for every row add (:field params) as the last element
(defn add-values-to-form-rows [rows params]
  (map-indexed #(conj %2 ((%2 1) params)) rows)
)
