; Much code from Compojure and Snout here

(ns dogfort.middleware.routes-macros)

(def ^:private +splat+ #"\*")
(def ^:private +literal+ #"/\w*")
(def ^:private +keyword+  #"/:[\w\-]+")
(def ^:private +params+ #"\?.*")

(defn- lex1 [route]
  (if-let [x (and (.startsWith route "/:") (re-find +keyword+ route))]
    [(keyword (.substring x 2)) (.substring route (count x))]
    (if-let [x (re-find +splat+ route)]
      ["*" nil]
      (if-let [x (re-find +literal+ route)]
        [x (.substring route (count x))]
        (if-let [x (re-find +params+ route)]
          ["?" (.substring x 1)])))))

(defn- make-matcher [route]
  (loop [[type route] (lex1 route)
         matcher []]
    (if (nil? type)
      matcher
      (recur (lex1 route)
             (conj matcher type)))))

(defn- assoc-&-binding [binds req sym]
  (assoc binds sym `(dissoc (:params ~req)
                            ~@(map keyword (keys binds))
                            ~@(map str (keys binds)))))

(defn- assoc-symbol-binding [binds req sym]
  (assoc binds sym `(get-in ~req [:params ~(keyword sym)]
                            (get-in ~req [:params ~(str sym)]))))

(defn- vector-bindings
  "Create the bindings for a vector of parameters."
  [args req]
  (loop [args args, binds {}]
    (if-let [sym (first args)]
      (cond
        (= '& sym)
          (recur (nnext args) (assoc-&-binding binds req (second args)))
        (= :as sym)
          (recur (nnext args) (assoc binds (second args) req))
        (symbol? sym)
          (recur (next args) (assoc-symbol-binding binds req sym))
        :else
          (throw (Exception. (str "Unexpected binding: " sym))))
      (mapcat identity binds))))

(defmacro let-request [[bindings request] & forms]
  (if (vector? bindings)
    `(let [~@(vector-bindings bindings request)] ~@forms)
    `(let [~bindings ~request] ~@forms)))

(defn- compile-route [method route bindings forms]
  `(fn [request#]
     (dogfort.middleware.routes/eval-route
      request# ~method ~(make-matcher route)
      (fn [request#]
        (let-request [~bindings request#]
                     ~@forms)))))

(defmacro GET [route bindings & forms]
  (compile-route :get route bindings forms))

(defmacro POST [route bindings & forms]
  (compile-route :post route bindings forms))

(defmacro PUT [route bindings & forms]
  (compile-route :lout route bindings forms))

(defmacro DELETE [route bindings & forms]
  (compile-route :delete route bindings forms))

(defmacro HEAD [route bindings & forms]
  (compile-route :head route bindings forms))

(defmacro OPTIONS [route bindings & forms]
  (compile-route :options route bindings forms))

(defmacro PATCH [route bindings & forms]
  (compile-route :patch route bindings forms))

(defmacro ANY [route bindings & forms]
  (compile-route nil route bindings forms))

(defmacro defroutes [name & routes]
  `(def ~name (dogfort.middleware.routes/routes ~@routes)))
