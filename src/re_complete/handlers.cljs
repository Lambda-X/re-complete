(ns re-complete.handlers
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]
            [goog.events :as events]
            [re-complete.app :as app]))


(def trim-chars-default "")

(def sort-fn-default first)


;; --- Handlers ---

(register-handler
 :options
 (fn [db [_ linked-component-key options]]
   (let [sort-fn (:sort-fn options)
         trim-chars (:trim-chars options)
         filled-options (cond (and trim-chars sort-fn) options
                              trim-chars {:trim-chars trim-chars
                                          :sort-fn sort-fn-default}
                              sort-fn {:trim-chars trim-chars-default
                                       :sort-fn sort-fn}
                              :else {:trim-chars trim-chars-default
                                     :sort-fn sort-fn-default})]
     (assoc-in db [:autocomplete :linked-components (keyword linked-component-key)] {:options filled-options}))))

(register-handler
 :dictionary
 (fn [db [_ linked-component-key dictionary]]
   (assoc-in db [:autocomplete :linked-components (keyword linked-component-key) :dictionary] dictionary)))

(register-handler
 :input
 (fn [db [_ linked-component-key input]]
   (let [linked-component-keyword (keyword linked-component-key)
         previous-input (get-in db [:autocomplete :linked-components linked-component-keyword :text])
         options (get-in db [:autocomplete :linked-components linked-component-keyword :options])
         dictionary (get-in db [:autocomplete :linked-components linked-component-keyword :dictionary])
         index (app/index previous-input input)
         current-word (app/current-word input index)]
     (-> db
         (assoc-in [:autocomplete :linked-components linked-component-keyword :text] input)
         (assoc-in [:autocomplete :linked-components linked-component-keyword :change-index] index)
         (assoc-in [:autocomplete :linked-components linked-component-keyword :current-word] current-word)
         (assoc-in [:autocomplete :linked-components linked-component-keyword :completions] (app/completions current-word dictionary options))))))

(register-handler
 :clear-autocomplete-items
 (fn [db [_ linked-component-key]]
   (assoc-in db [:autocomplete :linked-components linked-component-key :completions] [])))


(register-handler
 :add-autocompleted-word
 (fn [db [_ linked-component-key selected-word]]
   (update-in db [:autocomplete :linked-components linked-component-key :text]
              #(app/autocomplete-word-to-string
                (get-in db [:autocomplete :linked-components linked-component-key :change-index])
                (get-in db [:autocomplete :linked-components linked-component-key :options :trim-chars])
                %
                selected-word))))

;; --- Subscriptions ---

(register-sub
 :get-previous-input
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components (keyword linked-component-key) :text]))))

(register-sub
 :get-items-to-autocomplete
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components linked-component-key :completions]))))
