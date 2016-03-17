(ns re-complete.utils-test
  (:require [re-complete.utils :as utils]
            [cljs.test :refer-macros [deftest is]]))

(deftest regex-char-esc-smap
  (is (= (utils/regex-char-esc-smap "?()[]") {"?" "\\?|", "(" "\\(|", ")" "\\)|", "[" "\\[|", "]" "\\]|"})))

(deftest partition-by-regexp
  (is (= (utils/partition-by-regexp "(appricot?)" "?()[]") '("(" "appricot" "?" ")"))))
