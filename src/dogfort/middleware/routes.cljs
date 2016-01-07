; From https://github.com/eduardoejp/snout/blob/master/src/snout/core.cljs

(ns dogfort.middleware.routes
  (:use-macros [dogfort.middleware.routes-macros :only [compile-route ANY]]
               [redlobster.macros :only [promise]])
  (:use [cljs.node :only [log]])
  (:require [redlobster.promise :as p]
            [dogfort.util.response :as response]
            [dogfort.util.codec :as codec]
            ))

(defn- route-match
  "Matches the URL to the matcher and (if they coincide) returns a set of
  route bindings."
  [url matcher]
  (let [
        url (rest (.split url "/"))
        ]
    (loop [[m & matcher] matcher
           [u & url] url
           res {}]
      (cond
       (not (or m u)) res
       (not (and m u)) nil
       (= "*" m) (assoc res :* (apply str (interpose "/" (list* u url))))
       (.startsWith m ":")
       (recur matcher url (assoc res
                            (keyword (.substring m 1))
                            (codec/url-decode u)))
       (= m u)
       (recur matcher url res)))))

(defn- merge-params [request params]
  (assoc request :params (merge (:params request {}) params)))

(defn eval-route [request method matcher handler]
  (when (or (not method) (= (:request-method request) method))
    (when-let [matches (route-match (:uri request) matcher)]
      (handler (merge-params request matches)))))

(defn routing [request & handlers]
  (let [response (some #(% request) handlers)]
    (cond (p/promise? response) response
          (map? response) (p/promise response)
          response (p/promise (response/response 200 response)))))

(defn routes [& handlers]
  #(apply routing % handlers))

(def not-found
  (ANY "*" []
       (response/default-response 404)))
