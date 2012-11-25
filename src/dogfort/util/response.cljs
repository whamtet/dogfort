(ns dogfort.util.response)

(def status-codes
  {100 "Continue"
   101 "Switching Protocols"
   200 "OK"
   201 "Created"
   202 "Accepted"
   203 "Non-Authoritative Information"
   204 "No Content"
   205 "Reset Content"
   206 "Partial Content"
   300 "Multiple Choices"
   301 "Moved Permanently"
   302 "Found"
   303 "See Other"
   304 "Not Modified"
   305 "Use Proxy"
   307 "Temporary Redirect"
   400 "Bad Request"
   401 "Unauthorized"
   402 "Payment Required"
   403 "Forbidden"
   404 "Not Found"
   405 "Method Not Allowed"
   406 "Not Acceptable"
   407 "Proxy Authentication Required"
   408 "Request Timeout"
   409 "Conflict"
   410 "Gone"
   411 "Length Required"
   412 "Precondition Failed"
   413 "Request Entity Too Large"
   414 "Request-URI Too Long"
   415 "Unsupported Media Type"
   416 "Requested Range Not Satisfiable"
   417 "Expectation Failed"
   500 "Internal Server Error"
   501 "Not Implemented"
   502 "Bad Gateway"
   503 "Service Unavailable"
   504 "Gateway Timeout"
   505 "HTTP Version Not Supported"})

(def status-cats
  {100 "http://25.media.tumblr.com/tumblr_lwjgzc5VCs1qzhbl2o1_1280.jpg"
   101 "http://24.media.tumblr.com/tumblr_lwjgzc5VCs1qzhbl2o2_1280.jpg"
   200 "http://24.media.tumblr.com/tumblr_lwjgxg7jrJ1qzhbl2o1_1280.jpg"
   201 "http://25.media.tumblr.com/tumblr_lwjgxg7jrJ1qzhbl2o2_1280.jpg"
   202 "http://25.media.tumblr.com/tumblr_lwjgxg7jrJ1qzhbl2o3_1280.jpg"
   204 "http://24.media.tumblr.com/tumblr_lwjgxg7jrJ1qzhbl2o4_1280.jpg"
   206 "http://25.media.tumblr.com/tumblr_lwjgxg7jrJ1qzhbl2o5_1280.jpg"
   207 "http://25.media.tumblr.com/tumblr_lwjgxg7jrJ1qzhbl2o6_1280.jpg"
   300 "http://25.media.tumblr.com/tumblr_lwjgtfRJGj1qzhbl2o2_1280.jpg"
   301 "http://25.media.tumblr.com/tumblr_lwjgtfRJGj1qzhbl2o3_1280.jpg"
   302 "http://24.media.tumblr.com/tumblr_lwjgtfRJGj1qzhbl2o1_1280.jpg"
   303 "http://25.media.tumblr.com/tumblr_lwjgtfRJGj1qzhbl2o4_1280.jpg"
   304 "http://25.media.tumblr.com/tumblr_lwjgtfRJGj1qzhbl2o5_1280.jpg"
   305 "http://24.media.tumblr.com/tumblr_lwjgtfRJGj1qzhbl2o6_1280.jpg"
   307 "http://25.media.tumblr.com/tumblr_lwjgtfRJGj1qzhbl2o7_1280.jpg"
   400 "http://24.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o1_1280.jpg"
   401 "http://24.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o2_1280.jpg"
   402 "http://24.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o3_1280.jpg"
   403 "http://25.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o4_1280.jpg"
   404 "http://25.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o5_1280.jpg"
   405 "http://25.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o6_1280.jpg"
   406 "http://25.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o7_1280.jpg"
   408 "http://24.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o8_1280.jpg"
   409 "http://24.media.tumblr.com/tumblr_lwjgmsfFs31qzhbl2o9_1280.jpg"
   410 "http://25.media.tumblr.com/tumblr_lwjgd4GlG21qzhbl2o7_1280.jpg"
   411 "http://24.media.tumblr.com/tumblr_lwjgd4GlG21qzhbl2o6_1280.jpg"
   413 "http://25.media.tumblr.com/tumblr_lwjgd4GlG21qzhbl2o5_1280.jpg"
   414 "http://25.media.tumblr.com/tumblr_lwjgd4GlG21qzhbl2o4_1280.jpg"
   416 "http://24.media.tumblr.com/tumblr_lwjgd4GlG21qzhbl2o3_1280.jpg"
   417 "http://25.media.tumblr.com/tumblr_lwjgd4GlG21qzhbl2o2_1280.jpg"
   418 "http://25.media.tumblr.com/tumblr_lwjgd4GlG21qzhbl2o1_1280.jpg"
   422 "http://24.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o1_1280.jpg"
   423 "http://24.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o2_1280.jpg"
   424 "http://24.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o3_1280.jpg"
   425 "http://24.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o4_1280.jpg"
   426 "http://25.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o5_1280.jpg"
   429 "http://25.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o6_1280.jpg"
   431 "http://24.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o7_1280.jpg"
   444 "http://24.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o8_1280.jpg"
   450 "http://25.media.tumblr.com/tumblr_lwjg4pjFFI1qzhbl2o9_1280.jpg"
   500 "http://25.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o1_1280.jpg"
   502 "http://24.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o2_1280.jpg"
   503 "http://24.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o11_1280.jpg"
   506 "http://25.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o12_1280.jpg"
   507 "http://25.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o3_1280.jpg"
   508 "http://24.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o4_1280.jpg"
   509 "http://24.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o5_1280.jpg"
   599 "http://25.media.tumblr.com/tumblr_lwjfwtx7P81qzhbl2o6_1280.jpg"})

(defn response [status body & [content-type]]
  {:status status
   :headers (if content-type {:content-type content-type} {})
   :body body})

(defn default-response [status]
  (response status
            (flatten
             ["<html><head>"
              "<style>"
                   "body { text-align: center; }"
                   "img { box-shadow: 0px 8px 32px black; }"
                   "</style>"
                   (let [label (str status " " (status-codes status))]
                     ["<title>" label "</title></head><body>"
                      (if-let [cat (status-cats status)]
                       ["<h1><img src=\"" cat "\" alt=\"" label "\"></h1>"]
                       ["<h1>" label "</h1>"])
                      "</body></html>"])])
            "text/html"))

(defn redirect [url]
  {:status 302
   :headers {:location url}
   :body ""})

(defn redirect-after-post [url]
  {:status 303
   :headers {:location url}
   :body ""})
