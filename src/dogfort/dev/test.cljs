(ns dogfort.dev.test
  (:require
   [dogfort.util.codec :refer [percent-encode url-encode url-decode
                               form-encode form-decode-str form-decode]])
  (:require-macros
   [dogfort.util.macros :refer [are]])
  )

(defn run []
  (println (= (percent-encode " ") "%20"))
  (println (= (percent-encode "+") "%2B"))
  (println (= (percent-encode "foo") "%66%6F%6F"))

  (println (= (percent-decode "%s/") "%s/"))
  (println (= (percent-decode "%20") " "))
  (println (= (percent-decode "foo%20bar") "foo bar"))
  (println (= (percent-decode "foo%FE%FF%00%2Fbar" "UTF-16") "foo/bar"))
  (println (= (percent-decode "%24") "$"))

  (println (= (url-encode "foo/bar") "foo%2Fbar"))
  (println (= (url-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar"))
  (println (= (url-encode "foo+bar") "foo+bar"))
  (println (= (url-encode "foo bar") "foo%20bar"))

  (println (= (url-decode "foo%2Fbar") "foo/bar" ))
  (println (= (url-decode "foo%FE%FF%00%2Fbar" "UTF-16") "foo/bar"))
  (println (= (url-decode "%") "%"))

  ;#_(deftest test-base64-encoding
  ;   (let [str-bytes (.getBytes "foo?/+" "UTF-8")]
  ;     (println (Arrays/equals str-bytes (base64-decode (base64-encode str-bytes))))))

  (are [x y] (= (form-encode x) y)
       "foo bar" "foo+bar"
       "foo+bar" "foo%2Bbar"
       "foo/bar" "foo%2Fbar")
  (println (= (form-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar"))

  (are [x y] (= (form-encode x) y)
       {"a" "b"} "a=b"
       {:a "b"}  "a=b"
       {"a" 1}   "a=1"
       {"a" "b" "c" "d"} "a=b&c=d"
       {"a" "b c"}       "a=b+c")
  (println (= (form-encode {"a" "foo/bar"} "UTF-16") "a=foo%FE%FF%00%2Fbar"))


  (println (= (form-decode-str "foo=bar+baz") "foo=bar baz"))
  (println (nil? (form-decode-str "%D")))

  (are [x y] (= (form-decode x) y)
       "foo"     "foo"
       "a=b"     {"a" "b"}
       "a=b&c=d" {"a" "b" "c" "d"}
       "foo+bar" "foo bar"
       "a=b+c"   {"a" "b c"}
       "a=b%2Fc" {"a" "b/c"})
  (println (= (form-decode "a=foo%FE%FF%00%2Fbar" "UTF-16")
              {"a" "foo/bar"}))
  )
