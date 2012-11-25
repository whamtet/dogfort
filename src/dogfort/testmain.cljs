(ns dogfort.testmain
  (:use-macros [redlobster.macros :only [promise defer-node
                                         when-realised let-realised]]
               [dogfort.middleware.routes-macros :only [defroutes GET POST]])
  (:require-macros [cljs.node-macros :as n]
                   [hiccups.core :as hiccups])
  (:use [dogfort.http :only [run-http]]
        [dogfort.middleware.file :only [wrap-file]]
        [dogfort.middleware.body-parser :only [wrap-body-parser]]
        [cljs.node :only [log]])
  (:require [cljs.nodejs]
            [redlobster.promise :as p]
            [dogfort.middleware.routes]
            [dogfort.util.response :as response]
            [mongo.core :as mongo]
            [hiccups.runtime]))

(n/require "fs" fs)

(def coll
  (let-realised
   [db (mongo/connect "localhost" 27017 "dogfort")]
   (mongo/collection @db "items")))

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
  (GET "/" []
       (when-realised
        [coll]
        (let-realised
         [docs (mongo/find-all @coll {})]
         (response/response 200 (page-template @docs)))))
  (POST "/new" [new]
        (when-realised
         [coll]
         (let-realised
          [docs (mongo/save! @coll {"name" new "done" false})]
          (response/redirect-after-post "/"))))
  (POST "/delete/:id" [id]
        (when-realised
         [coll]
         (let-realised
          [docs (mongo/delete-id! @coll id)]
          (response/redirect-after-post "/"))))
  (POST "/check/:id" [id]
        (when-realised
         [coll]
         (let-realised
          [docs (mongo/update-id! @coll id #(assoc % "done" (not (% "done"))))]
          (response/redirect-after-post "/")))))

(defn main [& args]
  (run-http (wrap-body-parser (wrap-file handler "test-static")) {:port 1337}))

(set! *main-cli-fn* main)
