(ns crypto.challenge2
  "2. Fixed XOR
Write a function that takes two equal-length buffers (hex encoded) and produces their XOR sum.
The string:
 1c0111001f010100061a024b53535009181c - hex encoded
... after hex decoding, when xor'd against:
 686974207468652062756c6c277320657965 - hex encoded (hit the bull's eye)
... should produce:
 746865206b696420646f6e277420706c6179 - hex encoded (the kids don't play)"
  (:require [midje.sweet       :as m]
            [crypto.byte       :as byte]
            [crypto.binary     :as binary]
            [crypto.ascii      :as ascii]
            [crypto.base64     :as b64]
            [crypto.hex        :as hex]))

(def hex-to-bits ^{:private true
                   :doc "hexadecimal to bits"}
  (comp byte/to-bits hex/to-bytes))

(m/fact
  (hex-to-bits (hex/encode "abc")) => [0 1 1 0 0 0 0 1,
                                       0 1 1 0 0 0 1 0,
                                       0 1 1 0 0 0 1 1])

(defn- bitxor
  "Apply bit-xor to the seq using key as the key"
  [seq key]
  (map bit-xor seq key))

(m/fact
  (bitxor [0 0 0 0 1 1 1 1] [0 0 0 0 1 1 1 1])         => [0 0 0 0 0 0 0 0]
  (bitxor [0 0 0 0 1 1 1 1] [1 1 1 1 1 1 1 1])         => [1 1 1 1 0 0 0 0]
  (bitxor [0 0 0 0 1 1 1 1] [1 1 1 1 0 0 0 0])         => [1 1 1 1 1 1 1 1]
  (bitxor [1 1 1 1 1 1 1 1] [1 1 1 1 0 0 0 0])         => [0 0 0 0 1 1 1 1]
  (apply bitxor [[0 0 0 0 1 1 1 1] [1 1 1 1 1 1 1 1]]) => [1 1 1 1 0 0 0 0])

(defn xor
  "Compute xor of 2 hex strings"
  [h0 h1]
  (->> [h0 h1]
       (map hex-to-bits)
       (apply bitxor)
       (partition 8)
       (map binary/to-bytes)
       hex/encode))

(m/fact :one-way
  (xor "1c0111001f010100061a024b53535009181c" "686974207468652062756c6c277320657965") => "746865206b696420646f6e277420706c6179"
  (xor "746865206b696420646f6e277420706c6179" "686974207468652062756c6c277320657965") => "1c0111001f010100061a024b53535009181c")

(defn xor-with-checks
  "Compute the xor of 2 hex strings of same size."
  [h0 h1]
  {:pre [(= (count h0) (count h1))]}
  (xor h0 h1))

(m/fact
  (xor-with-checks "abc" "defv")                                                                  => (m/throws AssertionError "Assert failed: (= (count h0) (count h1))")
  (xor-with-checks "1c0111001f010100061a024b53535009181c" "686974207468652062756c6c277320657965") => "746865206b696420646f6e277420706c6179"
  (xor-with-checks "746865206b696420646f6e277420706c6179" "686974207468652062756c6c277320657965") => "1c0111001f010100061a024b53535009181c")
