(ns {{name}}.core
  (:use-macros [dogfort.middleware.routes-macros :only [defroutes GET POST ANY]])
  (:require-macros)
  (:use [dogfort.http :only [run-http]]
        )
  (:require [cljs.nodejs]
            [dogfort.middleware.defaults :as defaults]
            [dogfort.middleware.routes]))

(cljs.nodejs/enable-util-print!)

(defroutes handler
  (ANY "/" req
       {:status 200
        :body (pr-str req)
        :session {:hi "there"}})
  )

(defn main [& args]
  (println "starting")
  (-> handler
      (defaults/wrap-defaults {:wrap-file "test-static"})
      (run-http {:port 5000})))

(set! *main-cli-fn* main)
