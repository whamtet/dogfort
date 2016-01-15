(ns dogfort.middleware.params
  "Middleware to parse url-encoded parameters from the query string and request
  body."
  (:require [dogfort.util.codec :as codec]
            [dogfort.util.request :as req]
            [redlobster.stream :as stream]
            [redlobster.promise :as p])
  (:use-macros [redlobster.macros :only [promise let-realised]]))

(defn- parse-params [params encoding]
  (let [params (codec/form-decode params encoding)]
    (if (map? params) params {})))

(defn assoc-query-params
  "Parse and assoc parameters from the query string with the request."
  {:added "1.3"}
  [request encoding]
  (merge-with merge request
              (if-let [query-string (:query-string request)]
                (let [params (parse-params query-string encoding)]
                  {:query-params params, :params params})
                {:query-params {}, :params {}})))

#_(defn slurp [body]
  (println "slurping")
  (promise
   (let [sb (js/Array.)]
     (.on body "data" #(.push sb %))
     (.on body "end" #(realise (.join sb ""))))))

#_(defn assoc-form-params
  "Parse and assoc parameters from the request body with the request."
  {:added "1.2"}
  [handler request encoding]
  (if-let [body (and (req/urlencoded-form? request) (:body request))]
    (let-realised
     [body (slurp body)]
     (println "slurped")
     (let [params (parse-params @body encoding)
           request (merge-with merge request {:form-params params, :params params})]
       (let-realised [response (handler request)]
                     @response)))
    (handler
     (merge-with
      merge
      request
      {:form-params {}, :params {}}))))

(defn params-request
  "Adds parameters from the query string and the request body to the request
  map. See: wrap-params."
  {:arglists '([request] [request options])
   :added "1.2"}
  [handler request & [opts]]
  (let [encoding (or (:encoding opts)
                     (req/character-encoding request)
                     "UTF-8")
        request (if (:query-params request)
                  request
                  (assoc-query-params request encoding))
        ]
    (handler request)))

(defn wrap-params
  "Middleware to parse urlencoded parameters from the query string and form
  body (if the request is a url-encoded form). Adds the following keys to
  the request map:

  :query-params - a map of parameters from the query string
  :form-params  - a map of parameters from the body
  :params       - a merged map of all types of parameter

  Accepts the following options:

  :encoding - encoding to use for url-decoding. If not specified, uses
  the request character encoding, or \"UTF-8\" if no request
  character encoding is set."
  {:arglists '([handler] [handler options])}
  [handler & [options]]
  (fn [request]
    (params-request handler request options)))
