(defproject org.clojars.whamtet/dogfort "0.2.0-SNAPSHOT"
  :description "A web server framework for Clojurescript on Node"
  :url "https://github.com/bodil/dogfort"
  :license {:name "Apache License, version 2.0"
            :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.bodil/redlobster "0.1.0"]
                 [org.clojure/clojurescript "1.7.48"]
                 [org.clojure/tools.nrepl "0.2.10"]
                 [hiccups "0.1.1"]]
  :plugins [
            [lein-cljsbuild "1.1.0"]
            [org.bodil/lein-noderepl "0.1.11"]
            [lein-npm "0.6.1"]
            [com.cemerick/clojurescript.test "0.3.3"]
            ]
  :npm {:dependencies [
                       [nrepl-client "0.2.3"]
                       [mongodb "2.0.42"]
                       [ws "0.8.0"]
                       ]}

  ;using dogfort.build instead

  :cljsbuild {:builds [{:source-paths ["src" "test"]
                        :compiler {:output-to "target/cljs/testable.js"
                                   :target :nodejs
                                   :optimizations :simple
                                   :pretty-print true}}]
              :test-commands {"unit-tests" ["node" :node-runner
                                            ;"this.literal_js_was_evaluated=true"
                                            "target/cljs/testable.js"
                                            ;"test/cemerick/cljs/test/extra_test_command_file.js"
                                            ]}}
  :aliases
  {"dev" ["run" "-m" "dogfort.build"]}
  )
