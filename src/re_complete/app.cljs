(ns re-complete.app
  (:require [re-complete.utils :as utils]
            [clojure.string :as string]
            [goog.events :as events]
            [re-frame.core :refer [dispatch subscribe]]))

(defn case-sensitivity [case-sensitive? dictionary-item input]
  (if (= input "")
    nil
    (if case-sensitive?
      (string/starts-with? dictionary-item input)
      (string/starts-with? (string/lower-case dictionary-item) (string/lower-case input)))))

(defn items-to-complete
  "List of the items to autocomplete by given input and list of the all items"
  [case-sensitive? dictionary input]
  (if (= input nil)
    []
    (filter #(case-sensitivity case-sensitive? % input) dictionary)))

(defn index [previous-input input]
  "Finds the index where the change occured"
  (let [real-index (count (take-while #(identical? (first %) (second %))
                                      (map vector previous-input input)))]
    (if (< (count previous-input) (count input))
      real-index
      (- real-index 1))))

(defn current-word [input index]
  "Finds the current word based on index"
  (let [current-word-item (last (string/split (->> input
                                                   (take (inc index))
                                                   (reduce str))
                                              #" "))]
    (if (->> input
             (take (inc index))
             last
             (string/blank?))
      ""
      current-word-item)))

(defn opening-excluded-chars [word excluded-chars]
  (if ((set (map #(= (first word) %) excluded-chars)) true)
    (opening-excluded-chars (apply str (rest word)) excluded-chars)
    word))

(defn closing-excluded-chars [word excluded-chars]
  (if ((set (map #(= (last word) %) excluded-chars)) true)
    (closing-excluded-chars (apply str (butlast word)) excluded-chars)
    word))

(defn completions [word dictionary {:keys [trim-chars case-sensitive?]}]
  (let [new-text (-> word
                     (opening-excluded-chars trim-chars)
                     (closing-excluded-chars trim-chars))]
    (items-to-complete case-sensitive? dictionary new-text)))

(defn words-to-index
  "Words to change index"
  [index text]
  (let [text-to-index (-> index
                          inc
                          (take text))]
    (string/split (string/join "" text-to-index) #" ")))

(defn index-of-word
  "Index of word for autocompletion"
  [index text]
  (-> (words-to-index index text)
      count
      dec))

(defn index-in-word
  "Position index in word for autocompletion"
  [index text]
  (if (= (count (words-to-index index text)) 1)
    index
    (->> (words-to-index index text)
         drop-last
         (string/join "")
         count inc
         (- index))))

(defn complete-word-with-trimmed-chars
  "Autocomplete word and ignore regex at the beginning and at the end of the word"
  [index-in-word word word-to-complete trim-chars]
  (let [partitioned-by-trimmed-chars (vec (utils/partition-by-regexp word trim-chars))
        index-of-part-to-complete (-> index-in-word
                                      inc
                                      (take word)
                                      (utils/partition-by-regexp trim-chars)
                                      count
                                      dec)]
    (->> (update-in partitioned-by-trimmed-chars [index-of-part-to-complete]
                    #(str word-to-complete))
         (string/join ""))))

(defn complete-word-to-string
  "Autocomplete word with trimmed chars to input string"
  [index trim-chars text word-to-complete]
  (->> (update-in (string/split text #" ") [(index-of-word index text)]
                  #(complete-word-with-trimmed-chars (index-in-word index text)
                                                     %
                                                     word-to-complete
                                                     trim-chars))
       (string/join " ")))


(defn clear-complete-items [db linked-component-key]
  (assoc-in db [:re-complete :linked-components linked-component-key :completions] []))

(defn clear-selected-item [db linked-component-key]
  (assoc-in db [:re-complete :linked-components linked-component-key :selected-item] nil))

(defn next-item [db linked-component-key]
  (let [suggestion-list (get-in db [:re-complete :linked-components linked-component-key :completions])
        suggestion-indexed-vector (map-indexed vector suggestion-list)
        selected-item (get-in db [:re-complete :linked-components linked-component-key :selected-item])
        next-selected-item-index (inc (first selected-item))]
    (if-not selected-item
      (first suggestion-indexed-vector)
      (if (= (count suggestion-list) next-selected-item-index)
        (first suggestion-indexed-vector)
        (nth suggestion-indexed-vector next-selected-item-index)))))

(defn previous-item [db linked-component-key]
  (let [suggestion-list (get-in db [:re-complete :linked-components linked-component-key :completions])
        suggestion-indexed-vector (map-indexed vector suggestion-list)
        selected-item (get-in db [:re-complete :linked-components linked-component-key :selected-item])
        previous-selected-item-index (dec (first selected-item))]
    (if-not selected-item
      (last suggestion-indexed-vector)
      (if (= -1 previous-selected-item-index)
        (last suggestion-indexed-vector)
        (nth suggestion-indexed-vector previous-selected-item-index)))))

(defn add-completed-word [db linked-component-key selected-word]
  (do (-> db
          (update-in [:re-complete :linked-components linked-component-key :text]
                     #(complete-word-to-string
                       (get-in db [:re-complete :linked-components linked-component-key :change-index])
                       (get-in db [:re-complete :linked-components linked-component-key :options :trim-chars])
                       %
                       selected-word))
          (clear-complete-items linked-component-key)
          (clear-selected-item linked-component-key))))

(defn clear-completions
  [db linked-component-key]
  (assoc-in db [:re-complete :linked-components linked-component-key :completions] []))

(defn scrolling-down [linked-component-key selected-item-index node current-view options]
  (let [number-of-visible-items (:visible-items options)
        one-item-height (:item-height options)
        selected-item-number (+ 1 selected-item-index)]
    (cond (> selected-item-number (second @current-view)) (do (set! (.-scrollTop node) (* selected-item-number one-item-height))
                                                              (swap! current-view (fn [current-view]
                                                                                    [(inc (second current-view)) (+ (second current-view) number-of-visible-items)])))
          (= selected-item-number 1) (do (set! (.-scrollTop node) 0)
                                         (reset! current-view [1 number-of-visible-items]))
          :else nil)))

(defn scrolling-up [linked-component-key selected-item-index node current-view items-to-complete options]
  (let [number-of-visible-items (:visible-items options)
        number-of-items-to-complete (count items-to-complete)
        one-item-height (:item-height options)
        selected-item-number (+ 1 selected-item-index)
        current-position (* selected-item-number one-item-height)]
    (cond (< selected-item-number (first @current-view)) (do (set! (.-scrollTop node) (- current-position (* number-of-visible-items one-item-height)))
                                                             (swap! current-view (partial mapv dec)))
          (> selected-item-number (second @current-view)) (do (set! (.-scrollTop node) (* one-item-height number-of-items-to-complete))
                                                              (reset! current-view [(- number-of-items-to-complete number-of-visible-items) number-of-items-to-complete]))
          :else nil)))

(defn keys-handling [linked-component-key onclick-callback node current-view]
  (.addEventListener js/window "keydown"
                     (let [items (subscribe [:get-items-to-complete linked-component-key])
                           selected (subscribe [:get-selected-item linked-component-key])]
                       (fn [e]
                         (let [key-code (.-keyCode e)]
                           (when (and (#{13 38 40 9 27} key-code)
                                      (seq @items))
                             (if-not (and (= 13 key-code) (nil? @selected))
                               (do
                                 (dispatch [:keys-handling linked-component-key key-code onclick-callback node current-view])
                                 (.stopPropagation e)
                                 (.preventDefault e))
                               (dispatch [:clear-completions linked-component-key]))))))
                     true))
