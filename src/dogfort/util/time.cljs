;; A straight port of https://github.com/mikeal/filed/blob/master/rfc822.js

(ns dogfort.util.time
  (:require-macros [cljs.node-macros :as n]))

(def months ["Jan" "Feb" "Mar" "Apr" "May" "Jun"
             "Jul" "Aug" "Sep" "Oct" "Nov" "Dec"])
(def days ["Sun" "Mon" "Tue" "Wed" "Thu" "Fri" "Sat"])

(defn- pad-with-zero [val]
  (if (< (js/parseInt val 10) 10)
    (str "0" val) val))

(defn- get-tzo-string [tzo]
  (let [hours (.floor js/Math (/ tzo 60))
        mod-min (.abs js/Math (rem tzo 60))
        abs-hours (.abs js/Math hours)
        sign (if (> hours 0) "-" "+")]
    (str sign (pad-with-zero abs-hours)
         (pad-with-zero mod-min))))

(defn rfc822-date [^js/Date date]
  (str
   (days (.getDay date)) ", "
   (pad-with-zero (.getDate date)) " "
   (months (.getMonth date)) " "
   (.getFullYear date) " "
   (pad-with-zero (.getHours date)) ":"
   (pad-with-zero (.getMinutes date)) ":"
   (pad-with-zero (.getSeconds date)) " "
   (get-tzo-string (.getTimezoneOffset date))))
