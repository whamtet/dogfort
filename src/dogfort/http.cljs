(ns dogfort.http
  (:require-macros [cljs.node-macros :as n])
  (:require [cljs.node :as node]
            [redlobster.events :as e]
            [redlobster.stream :as s]
            [redlobster.promise :as p]
            [dogfort.util.response :as response]))

(n/require "http" http)
(n/require "url" url)
(n/require "stream" Stream)
(n/require "ws" ws)

(defprotocol IHTTPResponseWriter
  (-write-response [data res] "Write data to a http.ServerResponse"))

(defn- send-result [res ring-result]
  (if-not (:keep-alive ring-result)
    (if ring-result
      (let [{:keys [status headers body end-stream?]} ring-result]
        (set! (.-statusCode res) status)
        (doseq [[header value] headers]
          (.setHeader res (clj->js header) (clj->js value)))
        (when (-write-response body res)
          (.end res))
        (when (and (s/stream? body) end-stream?)
          (.end body))))))

(defn- send-error-page [res status err]
  (send-result res (response/default-response 500)))

(extend-protocol IHTTPResponseWriter

  nil
  (-write-response [data res] true)

  string
  (-write-response [data res]
                   (.write res data)
                   true)

  PersistentVector
  (-write-response [data res]
                   (doseq [i data] (-write-response i res))
                   true)

  List
  (-write-response [data res]
                   (doseq [i data] (-write-response i res))
                   true)

  LazySeq
  (-write-response [data res]
                   (doseq [i data] (-write-response i res))
                   true)

  js/Buffer
  (-write-response [data res]
                   (.write res data)
                   true)

  Stream
  (-write-response [data res]
                   (e/on data :error #(send-error-page res 500 %))
                   (.pipe data res)
                   false))

(defn- build-listener [handler options]
  (fn [req res]
    (let [
          url (.parse url (.-url req))
          uri (.-pathname url)
          query (.-search url)
          query (if query (.substring query 1))
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
           :body req
           :response res
           }
          result (handler ring-req)]
      (p/on-realised result
                     #(send-result res %)
                     #(send-error-page res 500 %)))))

(defn ws-handler [handler websocket]
  (let [
        upgrade-req (.-upgradeReq websocket)
        url (.parse url (.-url upgrade-req))
        uri (.-pathname url)
        query (.-search url)
        query (if query (.substring query 1))
        headers (js->clj (.-headers upgrade-req))
        conn (.-connection upgrade-req)
        address (js->clj (.address conn))
        ]
    (handler {:server-port (address "port")
              :server-name (address "address")
              :uri uri
              :query-string query
              :headers headers
              :websocket websocket
              :websocket? true
              :request-method :get
              })))

(defn run-http [handler options]
  (let [server (.createServer http (build-listener handler options))
        wss (ws.Server. #js{:server server})
        ]
    (.on wss "connection" #(ws-handler handler %))
    (.listen server (:port options))))
