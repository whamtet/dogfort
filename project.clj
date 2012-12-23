(defproject org.bodil/dogfort "0.1.0-SNAPSHOT"
  :description "A web server framework for Clojurescript on Node"
  :url "https://github.com/bodil/dogfort"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.bodil/redlobster "0.1.0"]
                 [hiccups "0.1.1"]]
  :plugins [[lein-cljsbuild "0.2.9"]
            [org.bodil/lein-noderepl "0.1.1"]]
  :cljsbuild {:builds
              [{:source-path "src"
                :compiler
                {:output-to "js/main.js"
                 :output-dir "js"
                 :optimizations :simple
                 :target :nodejs
                 :jar true}}]})
