(ns dogfort.middleware.nested-params-test
  (:require-macros
   [dogfort.util.macros :refer [is are testing]])
  (:require
   [dogfort.middleware.nested-params :refer [wrap-nested-params nested-params-request]]
   [clojure.string :as string]
   ))

(let [handler (wrap-nested-params :params)]
  (testing "nested parameter maps"
    (are [p r] (= (handler {:params p}) r)
         {"foo" "bar"}      {"foo" "bar"}
         {"x[y]" "z"}       {"x" {"y" "z"}}
         {"a[b][c]" "d"}    {"a" {"b" {"c" "d"}}}
         {"a" "b", "c" "d"} {"a" "b", "c" "d"}))
  (testing "nested parameter lists"
    (are [p r] (= (handler {:params p}) r)
         {"foo[]" "bar"}         {"foo" ["bar"]}
         {"foo[]" ["bar" "baz"]} {"foo" ["bar" "baz"]})
    (let [params (handler {:params {"a[x][]" ["b"], "a[x][][y]" "c"}})]
      (is (= (keys params) ["a"]))
      (is (= (keys (params "a")) ["x"]))
      (is (= (set (get-in params ["a" "x"])) #{"b" {"y" "c"}})))
    (let [params (handler {:params {"a[][x]" "c", "a[][y]" "d"}})]
      (is (= (keys params) ["a"]))
      (is (= (set (params "a")) #{{"x" "c"} {"y" "d"}}))))
  (testing "duplicate parameters"
    (are [p r] (= (handler {:params p}) r)
         {"a" ["b" "c"]}    {"a" ["b" "c"]}
         {"a[b]" ["c" "d"]} {"a" {"b" ["c" "d"]}}))
  (testing "parameters with newlines"
    (are [p r] (= (handler {:params p}) r)
         {"foo\nbar" "baz"} {"foo\nbar" "baz"}))
  (testing "parameters are already nested"
    (is (= {"foo" [["bar" "baz"] ["asdf" "zxcv"]]}
           (handler {:params {"foo" [["bar" "baz"] ["asdf" "zxcv"]]}}))))
  (testing "pre-nested vector of maps"
    (is (= {"foo" [{"bar" "baz"} {"asdf" "zxcv"}]}
           (handler {:params {"foo" [{"bar" "baz"} {"asdf" "zxcv"}]}}))))
  (testing "pre-nested map"
    (is (= {"foo" [{"bar" "baz" "asdf" "zxcv"}]}
           (handler {:params {"foo" [{"bar" "baz" "asdf" "zxcv"}]}}))))
  (testing "double-nested map"
    (is (= {"foo" {"key" {"bar" "baz" "asdf" "zxcv"}}}
           (handler {:params {"foo" {"key" {"bar" "baz" "asdf" "zxcv"}}}})))))

#_(let [handler (wrap-nested-params :params
                                  {:key-parser #(string/split % #"\.")})]
  (testing ":key-parser option"
    (are [p r] (= (handler {:params p}) r)
         {"foo" "bar"}      {"foo" "bar"}
         {"x.y" "z"}       {"x" {"y" "z"}}
         {"a.b.c" "d"}    {"a" {"b" {"c" "d"}}}
         {"a" "b", "c" "d"} {"a" "b", "c" "d"})))

;(is (fn? nested-params-request))
