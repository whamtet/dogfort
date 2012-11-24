(defproject org.bodil/dogfort "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]]
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
