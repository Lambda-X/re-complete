(ns re-complete.handlers
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]
            [goog.events :as events]
            [re-complete.app :as app]))



;; --- Handlers ---

(register-handler
 :autocomplete-component
 (fn [db [_ linked-component-key input autocomplete-data options]]
   (let [linked-component-keyword (keyword linked-component-key)
         previous-input (get-in db [:autocomplete :linked-components linked-component-keyword :text])
         sort-fn (:sort-fn options)
         filter-regex (:filter-regex options)
         filled-options (cond (and filter-regex sort-fn) options
                              filter-regex {:filter-regex filter-regex
                                            :sort-fn first}
                              sort-fn {:filter-regex ""
                                       :sort-fn sort-fn}
                              :else {:filter-regex ""
                                     :sort-fn first})]
     (-> db
         (assoc-in [:autocomplete :linked-components linked-component-keyword] {:text input
                                                                                :change-index 0
                                                                                :current-word ""
                                                                                :completions []
                                                                                :options filled-options})
         (app/autocomplete linked-component-keyword previous-input input autocomplete-data filled-options)))))

(register-handler
 :clear-autocomplete-items
 (fn [db [_ linked-component-key]]
   (assoc-in db [:autocomplete :linked-components linked-component-key :completions] [])))


(register-handler
 :add-autocompleted-word
 (fn [db [_ linked-component-key selected-word]]
   (app/add-autocompleted-word db
                               (get-in db [:autocomplete :linked-components linked-component-key :change-index])
                               selected-word
                               linked-component-key
                               (get-in db [:autocomplete :linked-components linked-component-key :options :filter-regex]))))

;; --- Subscriptions ---

(register-sub
 :get-previous-input
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components (keyword linked-component-key) :text]))))

(register-sub
 :get-items-to-autocomplete
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components linked-component-key :completions]))))
