(ns dogfort.middleware.session
  "Middleware for maintaining browser sessions using cookies.

  Sessions are stored using types that adhere to the
  dogfort.middleware.session.store/SessionStore protocol.
  Ring comes with two stores included:

    dogfort.middleware.session.memory/memory-store
    dogfort.middleware.session.cookie/cookie-store"
  (:require [dogfort.middleware.cookies :as cookies]
            [dogfort.middleware.session.store :as store]
            [dogfort.middleware.session.memory :as mem]
            [redlobster.promise :as p])
  (:use-macros
   [redlobster.macros :only [promise waitp let-realised]]
   ))

(defn- session-options
  [options]
  {:store        (:store options (mem/memory-store))
   :cookie-name  (:cookie-name options "ring-session")
   :cookie-attrs (merge {:path "/"
                         :http-only true}
                        (:cookie-attrs options)
                        (if-let [root (:root options)]
                          {:path root}))})

(defn- bare-session-request
  [request & [{:keys [store cookie-name]}]]
  (let [req-key  (get-in request [:cookies cookie-name :value])
        session  (store/read-session store req-key)
        session-key (if session req-key)]
    (merge request {:session (or session {})
                    :session/key session-key})))

(defn session-request
  "Reads current HTTP session map and adds it to :session key of the request.
  See: wrap-session."
  {:arglists '([request] [request options])
   :added "1.2"}
  [request & [options]]
  (-> request
      cookies/cookies-request
      (bare-session-request options)))

(defn- bare-session-response
  [response {session-key :session/key}  & [{:keys [store cookie-name cookie-attrs]}]]
  (let [new-session-key (if (contains? response :session)
                         (if-let [session (response :session)]
                            (if (:recreate (meta session))
                              (do
                                (store/delete-session store session-key)
                                (store/write-session store nil session))
                              (store/write-session store session-key session))
                            (if session-key
                              (store/delete-session store session-key))))
        session-attrs (:session-cookie-attrs response)
        cookie {cookie-name
                (merge cookie-attrs
                       session-attrs
                       {:value (or new-session-key session-key)})}
        response (dissoc response :session :session-cookie-attrs)]
    (if (or (and new-session-key (not= session-key new-session-key))
            (and session-attrs (or new-session-key session-key)))
      (assoc response :cookies (merge (response :cookies) cookie))
      response)))

(defn session-response
  "Updates session based on :session key in response. See: wrap-session."
  {:arglists '([response request] [response request options])
   :added "1.2"}
  [response request & [options]]
  (let-realised
   [response response]
   (bare-session-response @response request options)))

(defn wrap-session
  "Reads in the current HTTP session map, and adds it to the :session key on
  the request. If a :session key is added to the response by the handler, the
  session is updated with the new value. If the value is nil, the session is
  deleted.

  Accepts the following options:

  :store        - An implementation of the SessionStore protocol in the
                  dogfort.middleware.session.store namespace. This determines how
                  the session is stored. Defaults to in-memory storage using
                  dogfort.middleware.session.store/memory-store.

  :root         - The root path of the session. Any path above this will not be
                  able to see this session. Equivalent to setting the cookie's
                  path attribute. Defaults to \"/\".

  :cookie-name  - The name of the cookie that holds the session key. Defaults to
                  \"ring-session\"

  :cookie-attrs - A map of attributes to associate with the session cookie.
                  Defaults to {:http-only true}."
  ([handler]
     (wrap-session handler {}))
  ([handler options]
     (let [options (session-options options)]
       (fn [request]
         (let [new-request (session-request request options)]
           (-> (handler new-request)
               (session-response new-request options)))))))
