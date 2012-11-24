(ns redlobster.macros)

(defmacro defer
  "Run the given forms in the next tick of the event loop, or if the
first argument is a number, run the following forms after the given
number of milliseconds have elapsed."
  [& forms]
  (if (number? (first forms))
    `(js/setTimeout (fn [] ~@(rest forms)) ~(first forms))
    `(.nextTick js/process (fn [] ~@forms))))

(defmacro promise
  "Return a promise that will be realised by the given forms. The functions
`realise` and `realise-error` will be available inside the macro body, and one
of these should be called at some point within the forms to realise the promise."
  [& forms]
  `(let [promise# (redlobster.promise/promise)
         realise# (fn [promise# value#]
                    (redlobster.promise/realise promise# value#))
         realise-error# (fn [promise# value#]
                          (redlobster.promise/realise-error promise# value#))
         ~'realise (partial realise# promise#)
         ~'realise-error (partial realise-error# promise#)]
     ~@forms
     promise#))

(defmacro waitp
  "Creates a promise that waits for another promise to be realised, and
calls the provided success or failure function respectively to realise the
created promise. As with the `promise` macro, the functions `realise` and
`realise-error` will be available inside the macro body and should be called
to realise the promise."
  [join-promise success failure]
  `(let [promise# (redlobster.promise/promise)
         realise# (fn [promise# value#]
                    (redlobster.promise/realise promise# value#))
         realise-error# (fn [promise# value#]
                          (redlobster.promise/realise-error promise# value#))
         ~'realise (partial realise# promise#)
         ~'realise-error (partial realise-error# promise#)]
     (redlobster.promise/on-realised ~join-promise
                                     ~success
                                     ~failure)
     promise#))
