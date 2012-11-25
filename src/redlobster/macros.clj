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

(defmacro when-realised
  "Given a sequence of promises, defer execution of the body until they have
all been successfully realised. If one or more of the promises fail, do not
execute anything. Return a promise that will realise with the result of
evaluating the forms, or fail with the value of the first dependent promise
to fail."
  [promises & forms]
  `(redlobster.promise/defer-until-realised
     ~promises
     (fn [] ~@forms)))

(defmacro let-realised
  "Like `when-realised`, except it takes a binding form of variable/promise
pairs instead of just a list of variables, and binds these to the macro scope."
  [bindings & forms]
  `(let ~bindings
     (redlobster.promise/defer-until-realised
       ~(vec (map first (partition 2 bindings)))
       (fn [] ~@forms))))

(defmacro defer-node
  "Appends a callback to a given form which takes two arguments `[error value]`
and executes it, returning a promise that will fail with `error` if `error`
is truthy, and realise with `value` if `error` is falsy. This is a common
Node callback idiom, so this macro can be useful for wrapping Node calls in
promises, eg.:

    (defer-node (.readFile fs \"/etc/passwd\"))

The above code will call fs.readFile() and return a promise that will realise
with the file's contents when the operation is done, or fail with an appropriate
error if the operation returns one.

Optionally, you can specify a transformer function to apply to the success value
before it's realised. `js->clj` is a likely candidate."
  ([form transformer]
     `(let [promise# (redlobster.promise/promise)
            callback# (fn [error# value#]
                        (if error#
                          (redlobster.promise/realise-error promise# error#)
                          (redlobster.promise/realise promise#
                                                      (~transformer value#))))]
        (~@form callback#)
        promise#))
  ([form]
     `(defer-node ~form identity)))
