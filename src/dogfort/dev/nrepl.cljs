(ns dogfort.dev.nrepl
   (:use-macros [redlobster.macros :only [promise]])
  (:use [cljs.reader :only [read-string]])
  )

;; var nreplClient = require('nrepl-client');
;; nreplClient.connect({port: 7889}).once('connect', function() {
;;     var expr = '(+ 3 4)';
;;     client.eval(expr, function(err, result) {
;;         console.log('%s => ', expr, err || result);
;;         client.end();
;;     });
;; });

(def nrepl (js/require "nrepl-client"))
;(def conn (.connect nrepl (clj->js {:port 50000})))

(defn my-eval [form]
  (promise
   (.eval conn
          (pr-str form)
          (fn [err result]
            (if err
              (realise-error "erz")
              (-> result js->clj (get-in [0 "value"]) read-string realise))))))
