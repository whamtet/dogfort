;; Ported from Ring

(ns dogfort.util.codec
  "Encoding and decoding utilities."
  (:use [dogfort.util.data :only [assoc-conj]])
  (:require [clojure.string :as str]
            [cljs.nodejs]
            ))

(cljs.nodejs/enable-util-print!)

(defn- double-escape [^String x]
  (.replace (.replace x "\\" "\\\\") "$" "\\$"))

(defn- number->hex [num]
  (.toUpperCase (.toString num 16)))

(defn percent-encode
  "Percent-encode every character in the given string using either the specified
  encoding, or UTF-8 by default."
  [^String unencoded & [^String encoding]]
  (let [buf (js/Buffer. unencoded (or encoding "utf8"))
        bytes (map #(str "%" (number->hex (aget buf %))) (range (.-length buf)))]
    (str/join bytes)))

(defn- parse-bytes [encoded-bytes]
  (->> (re-seq #"%.." encoded-bytes)
       (map #(subs % 1))
       (map #(js/parseInt % 16))
       (clj->js)
       (js/Buffer.)))

(defn percent-decode
  "Decode every percent-encoded character in the given string using the
  specified encoding, or UTF-8 by default."
  [^String encoded & [^String encoding]]
  (str/replace encoded
               #"(?:%..)+"
               (fn [chars]
                 (-> (parse-bytes chars)
                     (.toString (or encoding "utf8"))
                     (.replace "\\" "\\\\")
                     #_(double-escape)))))

(defn url-encode
  "Returns the url-encoded version of the given string, using either a specified
  encoding or UTF-8 by default."
  [unencoded & [encoding]]
  (str/replace
   unencoded
   #"[^A-Za-z0-9_~.+-]+"
   #(double-escape (percent-encode % encoding))))

(defn ^String url-decode
  "Returns the url-decoded version of the given string, using either a specified
  encoding or UTF-8 by default. If the encoding is invalid, nil is returned."
  [encoded & [encoding]]
  (percent-decode (str/replace encoded #"[+]" " ") encoding))

(defn base64-encode
  "Encode a Buffer into a base64 encoded string."
  [unencoded]
  (.toString unencoded "base64"))

(defn base64-decode
  "Decode a base64 encoded string into a Buffer."
  [^string encoded]
  (js/Buffer. encoded "base64"))

#_(defprotocol FormEncodeable
    (form-encode* [x encoding]))

#_(extend-protocol FormEncodeable
    string
    (form-encode* [unencoded encoding]
                  (url-encode unencoded encoding))
    PersistentHashMap
    (form-encode* [params encoding]
                  (letfn [(encode [x] (form-encode* x encoding))
                          (encode-param [[k v]] (str (encode (name k)) "=" (encode v)))]
                    (->> params
                         (mapcat
                          (fn [[k v]]
                            (if (or (seq? v) (sequential? v) )
                              (map #(encode-param [k %]) v)
                              [(encode-param [k v])])))
                         (str/join "&"))))
    default
    (form-encode* [x encoding]
                  (form-encode* (str x) encoding)))

(defn form-encode* [params encoding]
  (if (map? params)
    (letfn [(encode [x] (form-encode* x encoding))
            (encode-param [[k v]] (str (encode (name k)) "=" (encode v)))]
      (->> params
           (mapcat
            (fn [[k v]]
              (if (or (seq? v) (sequential? v) )
                (map #(encode-param [k %]) v)
                [(encode-param [k v])])))
           (str/join "&")))
    (url-encode (str params) encoding)))

(defn form-encode
  "Encode the supplied value into www-form-urlencoded format, often used in
  URL query strings and POST request bodies, using the specified encoding.
  If the encoding is not specified, it defaults to UTF-8"
  [x & [encoding]]
  (->
   (form-encode* x (or encoding "utf8"))
   (str/replace #"\+" "%2B")
   (str/replace #"%20" "+")))

(defn form-decode-str
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default."
  [^String encoded & [encoding]]
  (url-decode encoded (or encoding "utf8")))

(defn form-decode
  "Decode the supplied www-form-urlencoded string using the specified encoding,
  or UTF-8 by default. If the encoded value is a string, a string is returned.
  If the encoded value is a map of parameters, a map is returned."
  [^String encoded & [encoding]]
  (if (< (.indexOf encoded "=") 0)
    (form-decode-str encoded encoding)
    (reduce
     (fn [m param]
       (if-let [[k v] (str/split param #"=" 2)]
         (assoc-conj m (form-decode-str k encoding) (form-decode-str v encoding))
         m))
     {}
     (str/split encoded #"&"))))
