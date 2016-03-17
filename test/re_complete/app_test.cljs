(ns re-complete.app-test
  (:require [re-complete.app :as app]
            [cljs.test :refer-macros [deftest is]]))

(def previous-input "appricot ( tomato")

(def input-text "appricot (b tomato")

(def word-to-autocomplete "broccolini")

(def options {:trim-chars "()"
              :sort-fn count})

(def dictionary '("appricot" "broccolini" "tomato" "bok choy" "bell peppers" "amaranth" "leek"))

(deftest items-to-autocomplete
  (is (= (app/items-to-autocomplete dictionary "b") '("broccolini" "bok choy" "bell peppers"))))

(deftest index
  (is (= (app/index previous-input input-text) 10)))

(deftest current-word
  (is (= (app/current-word input-text (app/index previous-input input-text)) "(b")))

(deftest completions
  (is (= (app/completions (app/current-word input-text (app/index previous-input input-text)) dictionary options) ["bok choy" "broccolini" "bell peppers"])))

(deftest words-to-index
  (is (= (app/words-to-index (app/index previous-input input-text) input-text) ["appricot" "(b"])))

(deftest index-of-word
  (is (= (app/index-of-word (app/index previous-input input-text) input-text) 1)))

(deftest index-in-word
  (is (= (app/index-in-word (app/index previous-input input-text) input-text) 1)))

(deftest autocomplete-word-with-trimmed-chars
  (let [change-index (app/index previous-input input-text)]
    (is (= (app/autocomplete-word-with-trimmed-chars (app/index-in-word change-index input-text) (app/current-word input-text change-index) word-to-autocomplete (:trim-chars options)) "(broccolini"))))

(deftest autocomplete-word-to-string
  (is (= (app/autocomplete-word-to-string (app/index previous-input input-text) (:trim-chars options) input-text word-to-autocomplete) "appricot (broccolini tomato")))
