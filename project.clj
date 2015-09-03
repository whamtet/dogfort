(defproject org.bodil/dogfort "0.1.0-SNAPSHOT"
  :description "A web server framework for Clojurescript on Node"
  :url "https://github.com/bodil/dogfort"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.bodil/redlobster "0.1.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [hiccups "0.1.1"]]
  :plugins [[lein-cljsbuild "1.1.0"]
            [org.bodil/lein-noderepl "0.1.1"]]
  :cljsbuild
  {:builds
   [{:compiler
     {:output-to "js/main.js",
      :output-dir "js",
      :target :nodejs,
      :jar true
      :optimizations :whitespace
      },
     :source-paths ["src"]}]})
