(ns dogfort.middleware.defaults
  (:require
   [dogfort.middleware.file :as file]
   [dogfort.middleware.params :as params]
   [dogfort.middleware.keyword-params :as keyword-params]
   [dogfort.middleware.session :as session]
   )
  (:require-macros
   [dogfort.middleware.defaults-macros :refer [wrap]]))

(defn wrap-defaults [handler static-folder options]
  (wrap (file/wrap-file handler (:wrap-file options "static"))
        keyword-params/wrap-keyword-params
        params/wrap-params
        ;session/wrap-session
        ))
