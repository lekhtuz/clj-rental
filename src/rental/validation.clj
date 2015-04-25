(ns rental.validation
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [clojure.string :as str]
  )
)

(def default-form-info {:class "error" :errors {}})

(defn add-error [form-info field text]
  (log/info "add-error: field =" field ", text =" text ", form-info =" form-info)
  (assoc-in form-info [:errors field] text)
)

(defn has-errors [form-info]
  (pos? (count (:errors form-info)))
)

(defn print-error [{ :keys [ class errors ]} field]
  [:span {:class class} "&nbsp;" (errors field)]
)

(defn reject-if-empty
  ([form-info field value]
    (reject-if-empty form-info field value (str "Field \"" (name field) "\" can not be blank"))
  )
  ([form-info field value message]
    (log/info "reject-if-empty: field =" field ", value =" value ", message =" message ", form-info =" form-info)
    (if (str/blank? value) (add-error form-info field message) form-info)
  )
)

(defn validate [form-info params]
  (log/info "validate: params =" params ", form-info =" form-info)
  (let [validators-map (:validators form-info)]
    (reduce-kv (fn [finfo field field-validators]
                 (log/info "validate: field =" field ", field-validators =" field-validators ", form-info =" finfo)
                 (reduce (fn [fi vfn] 
                           (log/info "validate: vfn =" vfn ", form-info =" fi)
                           (vfn fi field (params field))
                         ) finfo field-validators
                 ) 
               ) form-info validators-map
    )
  )
)
