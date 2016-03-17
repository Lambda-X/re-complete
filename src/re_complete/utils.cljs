(ns re-complete.utils
  (:require [clojure.string :as string]))

(defn regex-char-esc-smap
  "Escapes characters in string by \\ and |"
  [string]
  (let [esc-chars string]
    (zipmap esc-chars
            (map #(str "\\" % "|") esc-chars))))

(defn str-to-pattern
  "Functions transform given string to pattern"
  [string]
  (->> string
       (replace (regex-char-esc-smap string))
       (reduce str)
       re-pattern))


(defn partition-by-regexp
  "Function partitions characters from string into multiple strings by given regexp"
  [word trim-chars]
  (map #(string/join "" %)
       (partition-by #(re-find (str-to-pattern trim-chars) %) (mapv str word))))
