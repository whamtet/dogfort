(ns dogfort.middleware.edn
  (:require
   [redlobster.promise]
   [redlobster.stream :as stream]
   [cljs.reader :refer [read-string]]
   )
  (:require-macros
   [redlobster.macros :refer [let-realised]])
  )

(defn- edn-request?
  [req]
  (if-let [type (get-in req [:headers "content-type"] "")]
    (not (empty? (re-find #"^application/(vnd.+)?edn" type)))))

(defn wrap-edn-params
  "If the request has the edn content-type, it will attempt to read
  the body as edn and then assoc it to the request under :edn-params
  and merged to :params.

  It may take an opts map to pass to clojure.edn/read-string"
  ([handler] (wrap-edn-params handler {}))
  ([handler opts]
   (fn [req]
     (if-let [body (and (edn-request? req) (:body req))]
       (let-realised
        [s (stream/read-stream body)]
        (let [
              edn-params (read-string @s)
              ]
          (handler (assoc req :edn-params edn-params :params (merge (:params req) edn-params)))))
       (handler req)))))
