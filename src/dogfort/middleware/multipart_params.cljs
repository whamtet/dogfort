(ns dogfort.middleware.multipart-params
  (:require [redlobster.stream :as stream]
            [redlobster.promise :as promise]
            )
  (:require-macros [dogfort.util.macros :refer [symzip]]
                   [redlobster.macros :refer [promise let-realised]]
                   ))

(def Busboy (js/require "busboy"))

(defn value-map [f m]
  (zipmap (keys m) (map f (vals m))))

(defn wrap-multipart-params [handler]
  (fn [{:keys [body request-method] :as req}]
    (if (= :post request-method)
      (try
        (let [
              busboy (Busboy. (clj->js {:headers (.-headers body)}))
              params (atom {})
              handler-promise (promise/promise)
              ]
          (.on busboy "file"
               (fn [fieldname file filename encoding mimetype]
                 (let-realised
                  [data (stream/read-binary-stream file)]
                  (let [data @data]
                    (swap! params assoc (keyword fieldname) (symzip data filename encoding mimetype))))))
          (.on busboy "field"
               (fn [fieldname val fieldname-truncated val-truncated encoding mimetype]
                 (swap! params assoc (keyword fieldname) val)))
          (.on busboy "finish"
               (fn []
                 (promise/realise
                  handler-promise
                  (handler
                   (assoc
                     (update-in req [:params] merge @params)
                     :multipart-params @params)))))
          (.pipe body busboy)
          handler-promise
          )
        (catch :default e
          (handler req)))
      (handler req))))
