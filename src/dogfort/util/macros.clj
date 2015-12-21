(ns dogfort.util.macros)

(defmacro are [v body & rest]
  (let [
        [_ a b] body
        f `(fn ~v (if-not ~body (prn ~v ~a ~b)))
        a (vec (take-nth 2 rest))
        b (vec (take-nth 2 (drop 1 rest)))
        ]
    `(dorun (map ~f ~a ~b))))

(defmacro is [assert]
  `(if-not ~assert (prn '~assert)))

(defmacro testing [msg & body]
  `(do
     (println "testing" ~msg)
     ~@body))
