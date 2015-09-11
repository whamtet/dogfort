(ns dogfort.middleware.session.cookie
  "A session storage engine that stores session data in encrypted cookies.
  Less secure than the ring version.  Woof woof!
  "
  (:require [dogfort.middleware.session.store :refer [SessionStore]]
            [dogfort.util.codec :as codec]
            ;[clojure.tools.reader.edn :as edn]
            ;[crypto.random :as random]
            ;[crypto.equality :as crypto]
            [cljs.reader :refer [read-string]]
            [cljs.nodejs]
            )
  #_(:import [java.security SecureRandom]
           [javax.crypto Cipher Mac]
           [javax.crypto.spec SecretKeySpec IvParameterSpec]))

(cljs.nodejs/enable-util-print!)

(def crypto (js/require "crypto"))

(def ^{:private true
       :doc "Algorithm to generate a HMAC."}
  hmac-algorithm
  "HmacSHA256")

(def ^{:private true
       :doc "Type of encryption to use."}
  crypt-type
  "AES")

(def ^{:private true
       :doc "Full algorithm to encrypt data with."}
  crypt-algorithm
  "aes-256-ctr")

#_(defn- hmac
  "Generates a Base64 HMAC with the supplied key on a string of data."
  [key data]
  (doto
    (.createHmac crypto hmac-algorithm key)
    (.update data)
    (.digest "base64")))

(def hmac (js/Function. "key" "text" "return dogfort.middleware.session.cookie.crypto.createHmac('sha1', key).update(text).digest('base64')"))

(defn- encrypt
  "Encrypt a string with a key."
  [key data]
  (let [cipher (.createCipher crypto crypt-algorithm key)] ;incorrect
    (str
    (.update cipher data "utf8" "base64")
    (.final cipher "base64"))))

(defn- decrypt
  "Decrypt an array of bytes with a key."
  [key data]
  (let [decipher (.createDecipher crypto crypt-algorithm key)] ;incorrect
    (str
    (.update decipher data "base64" "utf8")
    (.final decipher "utf8"))))

(defn- get-secret-key
  "Get a valid secret key from a map of options, or create a random one from
  scratch."
  [options]
  (or (:key options) (apply str (repeat 16 #(rand-nth "abcdefghikjlmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890")))))

(defn- ^String serialize [x]
  (pr-str x))

(defn- seal
  "Seal a Clojure data structure into an encrypted and HMACed string."
  [key data]
  (let [data (encrypt key (pr-str data))]
  (str data "--" (hmac key data))))

(defn- unseal
  "Retrieve a sealed Clojure data structure from a string"
  [key string]
  (let [[data mac] (.split string "--")]
    (if (= mac (hmac key data))
      (read-string (decrypt key data))
      (println string mac (hmac key data) "fail")
      )))

(deftype CookieStore [secret-key]
  SessionStore
  (read-session [_ data]
    (if data (unseal secret-key data)))
  (write-session [_ _ data]
    (seal secret-key data))
  (delete-session [_ _]
    (seal secret-key {})))

#_(defn- valid-secret-key? [key]
  (and (= (type (byte-array 0)) (type key))
       (= (count key) 16)))

(defn cookie-store
  "Creates an encrypted cookie storage engine. Accepts the following options:

  :key - The secret key to encrypt the session cookie. Must be exactly 16 bytes
         If no key is provided then a random key will be generated. Note that in
         that case a server restart will invalidate all existing session
         cookies."
  ([] (cookie-store {}))
  ([options]
    (let [key (get-secret-key options)]
;      (assert (valid-secret-key? key) "the secret key must be exactly 16 bytes")
      (CookieStore. key))))
