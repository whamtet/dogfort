; From https://github.com/eduardoejp/snout/blob/master/src/snout/core.cljs

(ns dogfort.middleware.routes
  (:use-macros [dogfort.middleware.routes-macros :only [compile-route]]
               [redlobster.macros :only [promise]])
  (:use [cljs.node :only [log]])
  (:require [redlobster.promise :as p]
            [dogfort.util.response :as response]
            [dogfort.util.codec :as codec]))

(defn- route-match
  "Matches the URL to the matcher and (if they coincide) returns a set of
route bindings."
  [url matcher]
  (loop [[m & matcher] matcher
         url url
         res {}]
    (if (empty? url)
      (if (empty? matcher) res)
      (if (= "*" m)
        (assoc res :* url)
        (let [r (.substring (re-find #"/[^/]*" url) 1)
              url (.substring url (inc (count r)))]
          (cond
           (keyword? m) (recur matcher url (assoc res m (codec/url-decode r)))
           (= m (str "/" r)) (recur matcher url res)))))))

(defn- merge-params [request params]
  (assoc request :params (merge (:params request {}) params)))

(defn eval-route [request method matcher handler]
  (when (= (:request-method request) method)
    (when-let [matches (route-match (:uri request) matcher)]
      (handler (merge-params request matches)))))

(defn routing [request & handlers]
  (let [response (or (some #(% request) handlers)
                     (p/promise (response/default-response 404)))]
    (cond (p/promise? response) response
          (map? response) (p/promise response)
          :else (p/promise (response/bare-response 200 response)))))

(defn routes [& handlers]
  #(apply routing % handlers))
