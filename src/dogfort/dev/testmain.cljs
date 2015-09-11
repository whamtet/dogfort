(ns dogfort.dev.testmain
  (:use-macros [redlobster.macros :only [promise defer-node waitp
                                         when-realised let-realised]]
               [dogfort.middleware.routes-macros :only [defroutes GET POST]])
  (:require-macros [cljs.node-macros :as n]
                   [hiccups.core :as hiccups])
  (:use [dogfort.http :only [run-http]]
        [dogfort.middleware.file :only [wrap-file]]
        [dogfort.middleware.body-parser :only [wrap-body-parser]]
        [cljs.node :only [log]]

        )
  (:require [cljs.nodejs]
            [dogfort.middleware.defaults :as defaults]
            [redlobster.promise :as p]
            [redlobster.mongo :as mongo]
            [dogfort.middleware.routes]
            [dogfort.util.response :as response]
            [dogfort.dev.nrepl :as nrepl]
            [dogfort.dev.test :as test]
            [hiccups.runtime]))

(cljs.nodejs/enable-util-print!)

(defn concept-item [item]
  [:li {:class (if (item "done") "done" "open")}
   [:form {:method "POST" :action (str "/check/" (item "_id"))}
    [:input.check {:type "submit" :value (if (item "done") "\u2611" "\u2610")}]]
   [:form {:method "POST" :action (str "/delete/" (item "_id"))}
    [:input.delete {:type "submit" :value "x"}]]
   [:span.todo (item "name")]])

(defn page-template [items]
  (hiccups/html
   [:html
    [:head
     [:link {:rel "stylesheet" :href "/screen.css"}]]
    [:body
     [:h1 "Cat Fort Assault Plan"]
     [:ul (map concept-item items)]
     [:form {:method "POST" :action "/new"}
      [:input {:type "text" :name "new"}]]]]))

(defroutes handler
  (GET "/" req
       {:status 200
        :body (pr-str req)
        :session {:hi {:value "therez"}}}))

(defn main [& args]
  (println "starting")
  (-> handler
      (defaults/wrap-defaults {:wrap-file "test-static"})
      (run-http {:port 5000})))

(test/run)

(set! *main-cli-fn* main)
