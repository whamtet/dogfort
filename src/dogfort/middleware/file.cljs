(ns dogfort.middleware.file
  (:require-macros [cljs.node-macros :as n])
  (:use-macros [redlobster.macros :only [promise waitp let-realised]])
  (:require [redlobster.io :as io]
            [redlobster.promise :as p]
            [dogfort.util.codec :as codec]
            [dogfort.util.mime-type :as mime]
            [dogfort.util.time :as time]
            [cljs.node :as node]))

(n/require "fs" fs)
(n/require "path" path)
(n/require "crypto" crypto)

(defn- normalise-path [^string file ^string root]
  (let [file (.join path root file)]
    (if (and (> (count file) (count root))
             (= root (.slice file 0 (count root))))
      file nil)))

(defn- stat-file [^string file opts]
  (promise
   (if-let [file (normalise-path file (:root opts))]
     (.stat fs file
            (fn [err stats]
              (if err (realise-error err)
                (do (aset stats "path" file)
                  (realise stats)))))
     (realise-error nil))))

(defn- etag [stats]
  (-> (.createHash crypto "md5")
      (.update (str (.-ino stats) "/" (.-mtime stats) "/" (.-size stats)))
      (.digest "hex")))

(defn- last-modified [stats]
  (time/rfc822-date (.-mtime stats)))

(defn- expand-dir [^string path]
  (try
    (.realpathSync fs path)
    (catch :default e (throw (str "Directory does not exist: " path)))))

(defn- file-response [stats]
  (let [file (.-path stats)]
    (let-realised [s (io/binary-slurp file)]
                  {:status 200
                   :headers {:content-type (mime/ext-mime-type file)
                             :content-length (.-size stats)
                             :last-modified (last-modified stats)
                             :etag (etag stats)}
                   :body @s})))

(defn wrap-file [app ^string root-path & [opts]]
  (let [opts (merge {:root (expand-dir root-path)
                     :index-files? true
                     :allow-symlinks? false}
                    opts)]
    (fn [req]
      (if-not (or (= :get (:request-method req))
                  (= :head (:request-method req)))
        (app req)
        (let [file (.slice (codec/url-decode (:uri req)) 1)
              stat-p (stat-file file opts)]
          (waitp stat-p
                 #(realise (file-response %))
                 #(realise (app req))))))))
