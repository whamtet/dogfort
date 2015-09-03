(ns dogfort.build)

(require 'cljs.build.api)

(cljs.build.api/watch "src"
  {:main 'dogfort.testmain
   :output-to "main.js"
   :target :nodejs})
