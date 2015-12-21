(ns dogfort.middleware.defaults
  (:require
   [dogfort.middleware.file :as file]
   [dogfort.middleware.params :as params]
   [dogfort.middleware.keyword-params :as keyword-params]
   [dogfort.middleware.session :as session]
   [dogfort.middleware.cookies :as cookies]
   )
  (:require-macros
   [dogfort.middleware.defaults-macros :refer [wrap]]))

(defn wrap-defaults [handler options]
  (wrap (file/wrap-file handler (:wrap-file options "static"))
        session/wrap-session
        cookies/wrap-cookies
        keyword-params/wrap-keyword-params
        params/wrap-params
        ;session/wrap-session
        ))
