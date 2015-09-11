(ns dogfort.dev.test
  (:require
   [dogfort.util.codec :refer [percent-encode url-encode url-decode
                               percent-decode
                               form-encode*
                               base64-decode
                               base64-encode
                               form-encode form-decode-str form-decode]])
  (:require-macros
   [dogfort.util.macros :refer [is are]])
  )

(defn run []
  (is (= (percent-encode " ") "%20"))
  (is (= (percent-encode "+") "%2B"))
  (is (= (percent-encode "foo") "%66%6F%6F"))

  (is (= (percent-decode "%s/") "%s/")) ;does it matter?
  (is (= (percent-decode "%20") " "))
  (is (= (percent-decode "foo%20bar") "foo bar"))
  ;  (is (= (percent-decode "foo%FE%FF%00%2Fbar" "ucs2") "foo/bar"))
  (is (= (percent-decode "%24") "$"))

  (is (= (url-encode "foo/bar") "foo%2Fbar"))
  ;  (is (= (url-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar"))
  (is (= (url-encode "foo+bar") "foo+bar"))
  (is (= (url-encode "foo bar") "foo%20bar"))

  (is (= (url-decode "foo%2Fbar") "foo/bar" ))
  ;  (is (= (url-decode "foo%FE%FF%00%2Fbar" "UTF-16") "foo/bar"))
  (is (= (url-decode "%") "%"))

  (let [str-bytes (js/Buffer. "foo?/+")]
    (is (.equals str-bytes (base64-decode (base64-encode str-bytes)))))

  (are [x y] (= (form-encode x) y)
       "foo bar" "foo+bar"
       "foo+bar" "foo%2Bbar"
       "foo/bar" "foo%2Fbar")
  ;  (is (= (form-encode "foo/bar" "UTF-16") "foo%FE%FF%00%2Fbar"))

  (are [x y] (= (form-encode x) y)
       {"a" "b"} "a=b"
       {:a "b"}  "a=b"
       {"a" 1}   "a=1"
       {"a" "b" "c" "d"} "a=b&c=d"
       {"a" "b c"}       "a=b+c")
  ;  (is (= (form-encode {"a" "foo/bar"} "UTF-16") "a=foo%FE%FF%00%2Fbar"))


  (is (= (form-decode-str "foo=bar+baz") "foo=bar baz"))
  ;  (is (nil? (form-decode-str "%D"))) ;wtf?

  (are [x y] (= (form-decode x) y)
       "foo"     "foo"
       "a=b"     {"a" "b"}
       "a=b&c=d" {"a" "b" "c" "d"}
       "foo+bar" "foo bar"
       "a=b+c"   {"a" "b c"}
       "a=b%2Fc" {"a" "b/c"})
  #_(is (= (form-decode "a=foo%FE%FF%00%2Fbar" "UTF-16")
           {"a" "foo/bar"}))
  )
