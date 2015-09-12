(ns dogfort.build)

(require 'cljs.build.api)

(defn -main [& [main]]
  (cljs.build.api/watch
   "src"
   {:main (symbol main)
    :output-to "main.js"
    :target :nodejs}))
