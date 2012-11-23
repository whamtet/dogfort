(ns cljs.node
  (:use [cljs.yunoincore :only [clj->js]])
  (:use-macros [cljs.node-macros :only [require]]))

(defn log [& args] (apply (.-log js/console) (map str args)))
