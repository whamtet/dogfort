(ns mongo.core
  (:use [cljs.yunoincore :only [clj->js]])
  (:use-macros [redlobster.macros :only [defer-node]])
  (:require [redlobster.promise :as p]
            [cljs.node :as node]))

(def mongodb (js/require "mongodb"))

(def Db (aget mongodb "Db"))
(def Server (aget mongodb "Server"))
(def Collection (aget mongodb "Collection"))
(def ^:export ObjectID (aget mongodb "ObjectID"))

(defn connect
  ([host port db]
     (let [server (Server. host port)]
       (defer-node (.open (Db. db server)))))
  ([host db]
     (connect host 27017 db))
  ([db]
     (connect "localhost" db)))

(defn collection [db coll]
  (Collection. db coll))

(defn save! [coll doc]
  (let [doc (clj->js doc)]
    (defer-node (.save coll doc) js->clj)))

(defn find-all [coll query]
  (defer-node (.find coll (clj->js query))
    #(defer-node (.toArray %) js->clj)))

(defn update-id! [coll id updater]
  (defer-node (.find coll (clj->js {:_id (ObjectID. id)}))
    (fn [cursor]
      (defer-node (.nextObject cursor)
        (fn [doc]
          (let [doc (updater (js->clj doc))]
            (save! coll doc)))))))

(defn delete-id! [coll id]
  (let [_id (ObjectID. id)]
    (defer-node (.remove coll (clj->js {:_id _id})
                         (clj->js {:safe true}))
      js->clj)))
