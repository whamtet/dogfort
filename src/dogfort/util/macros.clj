(ns dogfort.util.macros)

(defmacro are [v body & rest]
  (let [
        f `(fn ~v (if-not ~body (prn '~body ~v)))
        a (vec (take-nth 2 rest))
        b (vec (take-nth 2 (drop 1 rest)))
        ]
    `(dorun (map ~f ~a ~b))))

(defmacro is [assert]
  `(if-not ~assert (prn '~assert)))
