;; Copied from cljs.core HEAD pending a release
;; https://github.com/clojure/clojurescript/commit/cd66e6b9e63ad5ef1896a9c7a117148beb04301d

(ns cljs.yunoincore)

(defprotocol IEncodeJS
  (-clj->js [x] "Recursively transforms clj values to JavaScript")
  (-key->js [x] "Transforms map keys to valid JavaScript keys. Arbitrary keys are
  encoded to their string representation via (pr-str x)"))

(extend-protocol IEncodeJS
  default
  (-key->js [k]
    (if (or (string? k)
            (number? k)
            (keyword? k)
            (symbol? k))
      (-clj->js k)
      (pr-str k)))

  (-clj->js [x]
    (cond
      (keyword? x) (name x)
      (symbol? x) (str x)
      (map? x) (let [m (js-obj)]
                 (doseq [[k v] x]
                   (aset m (-key->js k) (-clj->js v)))
                 m)
      (coll? x) (apply array (map -clj->js x))
      :else x))

  nil
  (-clj->js [x] nil))

(defn clj->js
   "Recursively transforms ClojureScript values to JavaScript.
sets/vectors/lists become Arrays, Keywords and Symbol become Strings,
Maps become Objects. Arbitrary keys are encoded to by key->js."
   [x]
   (-clj->js x))
