(ns dogfort.build)

(require 'cljs.build.api)

(defn -main [& args]
  (cljs.build.api/watch
   "src"
   {:main 'dogfort.dev.testmain
    :output-to "main.js"
    :target :nodejs}))
