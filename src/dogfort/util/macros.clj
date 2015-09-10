(ns dogfort.util.macros)

(defmacro are [v body & rest]
  (let [
        f `(fn ~v (println ~body))
        a (take-nth 2 rest)
        b (take-nth 2 (drop 1 rest))
        ]
    `(dorun (map ~f ~a ~b))))
