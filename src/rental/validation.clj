(ns rental.validation
  (:require
    [clojure.tools.logging :as log :refer [info]]
    [clojure.string :as str]
  )
)

(def default-errors {:class "error"})

(defn add-error 
  ([field text]
    (add-error default-errors field text)
  )
  ([errors field text]
    (assoc errors field text)
  )
)

(defn has-errors [errors]
  (> (count errors) (count default-errors))
)

(defn print-error [errors field]
  [:span {:class (errors :class)} "&nbsp;" (errors field)]
)

(defn reject-if-empty 
  ([field value message]
    (reject-if-empty default-errors field value message)
  )
  ([errors field value message]
    (if (str/blank? value) (add-error errors field message) errors)
  )
)
