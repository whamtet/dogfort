(ns redlobster.stream
  (:require-macros [cljs.node-macros :as n])
  (:use [cljs.node :only [log]]
        [cljs.yunoincore :only [clj->js]]))

(n/require "stream" Stream)

(defprotocol IStream
  (readable? [this])
  (writable? [this])
  (set-encoding [this encoding])
  (pause [this])
  (resume [this])
  (destroy [this])
  (pipe [this destination])
  (pipe [this destination options])
  (write [this data])
  (write [this data encoding])
  (end [this])
  (end [this data])
  (end [this data encoding])
  (destroy-soon [this]))

(extend-protocol IStream
  Stream
  (readable? [this] (.-readable this))
  (writable? [this] (.-writable this))
  (set-encoding [this encoding] (.setEncoding this encoding))
  (pause [this] (.pause this))
  (resume [this] (.resume this))
  (destroy [this] (.destroy this))
  (pipe [this destination] (.pipe this destination))
  (pipe [this destination options] (.pipe this destination (clj->js options)))
  (write [this data] (.write this data))
  (write [this data encoding] (.write this data encoding))
  (end [this] (.end this))
  (end [this data] (.end this data))
  (end [this data encoding] (.end this data encoding))
  (destroy-soon [this] (.destroySoon this)))
