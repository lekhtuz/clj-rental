(ns rental.validation
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [clojure.string :as str :refer [join]]
    [rental.schema :as schema :refer [load-user]]
  )
)

(def default-form-info {:class "error" :errors {}})

(defn add-error [form-info field error-message]
  (log/info "add-error: field =" field ", error-message =" error-message ", form-info =" form-info)
  (assoc-in form-info [:errors field] 
            (if-let [message-list (-> form-info :errors field)]
              (conj message-list error-message)
              [error-message]
            )
  )
)

(defn has-errors 
  ([form-info]
    (seq (:errors form-info)))
  ([form-info field]
    (seq (-> form-info :errors field)))
)

(defn print-error [{ :keys [ class errors ]} field]
  [:span {:class class} "&nbsp;" (str/join "<br>" (errors field))]
)

(defn reject-if-empty
  ([form-info field value]
    (reject-if-empty form-info field value (str "Field \"" (name field) "\" can not be blank"))
  )
  ([form-info field value message]
    (log/info "reject-if-empty: field =" field ", value =" value ", message =" message ", form-info =" form-info)
    (if (empty? value) (add-error form-info field message) form-info)
  )
)

(defn valid-pattern
  ([form-info field value pattern]
    (valid-pattern form-info field value (str "Field \"" (name field) "\" does not match the required pattern."))
  )
  ([form-info field value pattern message]
    (log/info "valid-pattern: field =" field ", value =" value ", message =" message ", form-info =" form-info)
    (if-not (re-matches pattern value) (add-error form-info field message) form-info)
  )
)

(defn valid-email
  ([form-info field value]
    (valid-email form-info field value (str "Field \"" (name field) "\" is not a valid email"))
  )
  ([form-info field value message]
    (log/info "valid-email: field =" field ", value =" value ", message =" message ", form-info =" form-info)
    (valid-pattern form-info field value #".*@.*" message)
  )
)

(defn valid-zipcode
  ([form-info field value]
    (valid-zipcode form-info field value (str "Field \"" (name field) "\" is not a valid zip code"))
  )
  ([form-info field value message]
    (log/info "valid-email: field =" field ", value =" value ", message =" message ", form-info =" form-info)
    (valid-pattern form-info field value #"\d{5}" message)
  )
)

(defn username-exists
  ([form-info field value]
    (username-exists form-info field value (str "Username is not available"))
  )
  ([form-info field value message]
    (log/info "username-exists: field =" field ", value =" value ", message =" message ", form-info =" form-info)
    (if (or (has-errors form-info field) (not (schema/load-user value))) form-info (add-error form-info field message))
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
