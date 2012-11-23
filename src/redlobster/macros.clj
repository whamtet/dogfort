(ns redlobster.macros)

(defmacro defer [& forms]
  (if (number? (first forms))
    `(js/setTimeout (fn [] ~@(rest forms)) ~(first forms))
    `(.nextTick js/process (fn [] ~@forms))))

(defmacro promise [& forms]
  `(let [promise# (redlobster.promise/promise)
         realise# (fn [promise# value#]
                     (redlobster.promise/realise promise# value#))
         ~'realise (partial realise# promise#)]
     ~@forms
     promise#))
