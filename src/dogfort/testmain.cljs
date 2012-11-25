(ns dogfort.testmain
  (:use-macros [redlobster.macros :only [defer promise]]
               [dogfort.middleware.routes-macros :only [defroutes GET]])
  (:require-macros [cljs.node-macros :as n])
  (:use [dogfort.http :only [run-http]]
        [dogfort.middleware.file :only [wrap-file]]
        [cljs.node :only [log]])
  (:require [cljs.nodejs]
            [dogfort.middleware.routes]))

(n/require "fs" fs)

(defroutes handler
  (GET "/foo/:bar" [bar]
       ["<h1>Hello " bar "!</h1>"]))

(defn main [& args]
  (run-http (wrap-file handler ".") {:port 1337}))

(set! *main-cli-fn* main)
