(ns crypto-challenge.base64
  "A namespace to try and learn to encrypt from string to base64"
  (:use [midje.sweet :only [fact future-fact]])
  (:require [crypto-challenge.dico   :as d]
            [crypto-challenge.binary :as b]))

(defn comp24
  "Given a partition of 24 bits, compute the complement structure"
  [b]

  (case (count b) 8  [(b/comp-after 12 b) [\= \=]] ;; complement 4 bits to be able to have 2 bytes (12 bits) and we complements with 2 = chars
                  16 [(b/comp-after 18 b) [\=]]    ;; complement 2 bits to be able to have 3 bytes (18 bits) and we complements with 1 = char
                  [b                  []]))

(fact
  (comp24 [1 1 1 1 1 1 1 1])                                 => [[1 1 1 1 1 1 1 1 0 0 0 0]                         [\= \=]]
  (comp24 [1 1 1 1 1 1 1 1 0 0 0 0 0 0 1 1])                 => [[1 1 1 1 1 1 1 1 0 0 0 0 0 0 1 1 0 0]             [\=]]
  (comp24 [1 1 1 1 1 1 1 1 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1]) => [[1 1 1 1 1 1 1 1 0 0 0 0 0 0 1 1 1 1 1 1 1 1 1 1] []])

(defn encode
  "ascii to base64"
  [s]
  (->> (partition-all 3 s)
       (mapcat
        (fn [s] (mapcat (comp b/bin int) s)))
       (partition-all 24)
       (mapcat (fn [b]
                 (let [[part complement] (comp24 b)
                       p24               (->> part
                                              (partition 6)
                                              (map (comp d/base64 b/to-num)))]
                   (concat p24 complement))))
       (clojure.string/join "")))

(fact
  (encode "Man is distinguished, not only by his reason, but by this singular passion from other animals, which is a lust of the mind, that by a perseverance of delight in the continued and indefatigable generation of knowledge, exceeds the short vehemence of any carnal pleasure.")
  => "TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlzIHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2YgdGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGludWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRoZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=")