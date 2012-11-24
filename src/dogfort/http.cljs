(ns dogfort.http
  (:use [cljs.yunoincore :only [clj->js]])
  (:require-macros [cljs.node-macros :as n])
  (:require [cljs.node :as node]
            [redlobster.events :as e]
            [redlobster.stream :as s]
            [redlobster.promise :as p]))

(n/require "http" http)
(n/require "url" url)
(n/require "stream" Stream)

(defprotocol IHTTPResponseWriter
  (-write-response [data res] "Write data to a http.ServerResponse"))

(extend-protocol IHTTPResponseWriter
  string
  (-write-response [data res]
    (.write res data)
    (.end res))

  PersistentVector
  (-write-response [data res]
    (.write res (apply str data))
    (.end res))

  List
  (-write-response [data res]
    (.write res (apply str data))
    (.end res))

  js/Buffer
  (-write-response [data res]
    (.write res data)
    (.end res))

  Stream
  (-write-response [data res]
    (e/on data :data #(.write res %1))
    (e/on data :end #(.end res))))

(defn- send-result [res ring-result]
  (let [{:keys [status headers body]} ring-result]
    (set! (.-statusCode res) status)
    (doseq [[header value] headers]
      (.setHeader res header (clj->js value)))
    (-write-response body res)))

(defn- send-error-page [res status err]
  (set! (.-statusCode res) status)
  (.setHeader res "content-type" "text/html")
  (-write-response (str "<h1>Error " status "</h1>") res))

(defn- build-listener [handler options]
  (fn [req res]
    (let [{uri "pathname" query "search"} (js->clj (.parse url (.-url req)))
          headers (js->clj (.-headers req))
          conn (.-connection req)
          address (js->clj (.address conn))
          peer-cert-fn (.-getPeerCertificate conn)
          ring-req
          {:server-port (address "port")
           :server-name (address "address")
           :remote-addr (.-remoteAddress conn)
           :uri uri
           :query-string query
           :scheme "http"
           :request-method (keyword (.toLowerCase (.-method req)))
           :content-type (headers "content-type")
           :content-length (headers "content-length")
           :character-encoding nil
           :ssl-client-cert (when peer-cert-fn (peer-cert-fn))
           :headers headers
           :body req}
          result (handler ring-req)]
      (p/on-realised result
                     #(send-result res %)
                     #(send-error-page res 500 %)))))

(defn run-http [handler options]
  (let [server (.createServer http (build-listener handler options))]
    (.listen server (:port options))))
