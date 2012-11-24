(ns dogfort.middleware.file
  (:require-macros [cljs.node-macros :as n])
  (:use-macros [redlobster.macros :only [promise waitp]])
  (:require [redlobster.stream :as stream]
            [redlobster.promise :as p]
            [dogfort.util.codec :as codec]
            [cljs.node :as node]))

(n/require "fs" fs)
(n/require "path" path)

(defn- normalise-path [^string file ^string root]
  (let [file (.join path root file)]
    (if (and (> (count file) (count root))
             (= root (.slice file 0 (count root))))
      file nil)))

(defn- get-file-stream [^string file opts]
  (promise
   (if-let [file (normalise-path file (:root opts))]
     (.exists fs file
              #(if %
                 (realise (stream/slurp file))
                 (realise-error nil)))
     (realise-error nil))))

(defn- expand-dir [^string path]
  (try*
   (.realpathSync fs path)
   (catch e (throw (format "Directory does not exist: %s" path)))))

(defn wrap-file [app ^string root-path & [opts]]
  (let [opts (merge {:root (expand-dir root-path)
                     :index-files? true
                     :allow-symlinks? false}
                    opts)]
    (fn [req]
      (if-not (= :get (:request-method req))
        (app req)
        (let [file (.slice (codec/url-decode (:uri req)) 1)
              file-stream (get-file-stream file opts)]
          (waitp file-stream
                 #(realise {:status 200
                            :headers {}
                            :body %})
                 #(realise (app req))))))))
