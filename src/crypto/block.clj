(ns crypto.block
  "Block"
  (:require [midje.sweet  :as m]
            [crypto.ascii :as ascii]))

(defn split
  "Compute 2 n-block chars, [0..n] and [n..n+1]"
  [start-idx block-size data]
  (let [r (->> data
               (drop start-idx)
               (take (* 2 block-size))
               (partition block-size))]
    (if (-> r count odd?)
      (-> r butlast vec)                 ;; we drop the last block which is not a couple
      r)))                               ;; else we return just the computed data

(m/fact
  (split 0 3 (mapcat ascii/to-bits "hello, dude")) => [[0 1 1] [0 1 0]]
  (split 0 6 (mapcat ascii/to-bits "hello, dude")) => [[0 1 1 0 1 0] [0 0 0 1 1 0]]
  (split 2 2 "hello world!")                 => [[\l \l] [\o \space]]
  (split 0 2 "he")                           => []
  (split 0 6 "hello world! <6b")             => [[\h \e \l \l \o \space] [\w \o \r \l \d \!]])

(defn make-blocks
  "Make nb-blocks of size n with the string s. If nb-blocks is too large, return by convention 4 blocks."
  ([n s]
     (make-blocks (-> s count (/ n) int) n s))
   ([nb-blocks n s]
      (let [l (- nb-blocks (mod nb-blocks 2))]
        (for [i (range 0 l)] (split (* n i) n s)))))

(m/fact
  (make-blocks 2 2 "hello worl")
  (make-blocks 4 2 "hello worl")                                       => [[[\h \e] [\l \l]]
                                                                           [[\l \l] [\o \space]]
                                                                           [[\o \space] [\w \o]]
                                                                           [[\w \o] [\r \l]]]
  (make-blocks 5 "little by little, we close the line")                => [[[\l \i \t \t \l] [\e \space \b \y \space]]
                                                                           [[\e \space \b \y \space] [\l \i \t \t \l]]
                                                                           [[\l \i \t \t \l] [\e \, \space \w \e]]
                                                                           [[\e \, \space \w \e] [\space \c \l \o \s]]
                                                                           [[\space \c \l \o \s] [\e \space \t \h \e]]
                                                                           [[\e \space \t \h \e] [\space \l \i \n \e]]])

(defn shift
  "n-shift the sequence of data"
  [n data]
  (if (= 0 n)
    data
    (let [l     (count data)
          [h t] (split-at (mod n l) data)]
      (concat t h))))

(m/fact
  (shift 0  [:a :b])             => [:a :b]
  (shift 3  [:a :b :c :d :e :f]) => [:d :e :f :a :b :c]
  (shift 1  [:a :b :c :d :e :f]) => [:b :c :d :e :f :a]
  (shift -1 [:a :b :c])          => [:c :a :b])

(defn transpose
  "Given an input of data, inject a block of data at the nth position"
  [n block data]
  (let [l (-> block count inc)]
    (->> data
         (map (fn [seq]
                (->> seq
                     (split-at (mod n l))
                     (#(let [[h t] %]
                         (concat h (conj t block))))
                     flatten))))))

(m/fact
  (transpose 0 [1 :a] [[1 :a] [2 :b] [3 :c]]) => [[1 :a 1 :a] [1 :a 2 :b] [1 :a 3 :c]]
  (transpose 1 [1 :a] [[1 :a] [2 :b] [3 :c]]) => [[1 1 :a :a] [2 1 :a :b] [3 1 :a :c]]
  (transpose 2 [1 :a] [[1 :a] [2 :b] [3 :c]]) => [[1 :a 1 :a] [2 :b 1 :a] [3 :c 1 :a]]
  (transpose 3 [1 :a] [[1 :a] [2 :b] [3 :c]]) => [[1 :a 1 :a] [1 :a 2 :b] [1 :a 3 :c]]
  (transpose 1 [2 :b] [[1 :a] [2 :b] [3 :c]]) => [[1 2 :b :a] [2 2 :b :b] [3 2 :b :c]]
  (transpose 2 [3 :c] [[1 :a] [2 :b] [3 :c]]) => [[1 :a 3 :c] [2 :b 3 :c] [3 :c 3 :c]])
