(ns dogfort.testmain
  (:use-macros [redlobster.macros :only [defer promise]])
  (:require-macros [cljs.node-macros :as n])
  (:use [dogfort.http :only [run-http]]
        [dogfort.middleware.file :only [wrap-file]]
        [cljs.node :only [log]])
  (:require [cljs.nodejs]))

(n/require "fs" fs)

(defn handler [request]
  (promise
   (defer 1000
     (realise {:status 200
               :headers {:content-type "text/html"}
               :body "<h1>Hello sailor!</h1>"}))))

(defn main [& args]
  (run-http (wrap-file handler ".") {:port 1337}))

(set! *main-cli-fn* main)
