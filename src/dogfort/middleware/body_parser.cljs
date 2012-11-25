(ns dogfort.middleware.body-parser
  (:use [cljs.node :only [log]])
  (:use-macros [redlobster.macros :only [let-realised]])
  (:require [redlobster.stream :as s]
            [dogfort.util.codec :as codec]))

(defn- merge-params [request params]
  (assoc request :params (merge (:params request {}) params)))

(defn- is-form-encoded? [request]
  (and
   (= (:request-method request) :post)
   (= (:content-type request) "application/x-www-form-urlencoded")))

(defn wrap-body-parser [handler]
  (fn [request]
    (if-not (is-form-encoded? request)
      (handler request)
      (let-realised
       [body (s/read-stream (:body request))]
       (let [form-params (codec/form-decode @body)]
         (handler (merge-params request form-params)))))))
