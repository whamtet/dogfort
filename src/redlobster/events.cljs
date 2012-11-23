(ns redlobster.events
  (:require-macros [cljs.node-macros :as n])
  (:use [cljs.node :only [log]]
        [cljs.yunoincore :only [clj->js]]))

(n/require "events" [EventEmitter])

(defn event-emitter []
  (EventEmitter.))

(defprotocol IEventEmitter
  (on [emitter event listener])
  (once [emitter event listener])
  (remove-listener [emitter event listener])
  (remove-all-listeners [emitter])
  (remove-all-listeners [emitter event])
  (listeners [emitter event])
  (emit [emitter event args]))

(defn- unpack-event [event]
  (if (keyword? event)
    (name event)
    event))

(extend-protocol IEventEmitter
  EventEmitter
  (on [emitter event listener]
    (.on emitter (unpack-event event) listener))
  (once [emitter event listener]
    (.once emitter (unpack-event event) listener))
  (remove-listener [emitter event listener]
    (.removeListener emitter (unpack-event event) listener))
  (remove-all-listeners [emitter]
    (.removeAllListeners emitter))
  (remove-all-listeners [emitter event]
    (.removeAllListeners emitter (unpack-event event)))
  (listeners [emitter event]
    (js->clj (.listeners emitter (unpack-event event))))
  (emit [emitter event args]
    (.apply (.-emit emitter) emitter
            (apply array (cons (unpack-event event) args)))))
