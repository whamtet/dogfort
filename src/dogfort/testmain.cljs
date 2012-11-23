(ns dogfort.testmain
  (:use-macros [redlobster.macros :only [defer promise]])
  (:require-macros [cljs.node-macros :as n])
  (:use [dogfort.http :only [run-http]]
        [cljs.node :only [log]])
  (:require [cljs.nodejs]))

(n/require "fs" fs)

(defn handler [request]
  (promise
   (defer 1000
     (realise {:status 200
               :headers {"Content-Type" "text/plain"}
               :body (.createReadStream fs "README.md")}))))

(defn main [& args]
  (run-http handler {:port 1337}))

(set! *main-cli-fn* main)
