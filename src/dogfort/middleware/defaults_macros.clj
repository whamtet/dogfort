(ns dogfort.middleware.defaults-macros)

(defmacro wrap [handler & syms]
  `(->
    ~handler
    ~@(for [sym syms]
        `(~sym (get ~'options ~(-> sym str (.split "/") last keyword))))))
