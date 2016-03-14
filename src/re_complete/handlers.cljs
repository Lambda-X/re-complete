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
 (fn [db [_ linked-component-key options text]]
   (assoc-in db [:autocomplete :linked-components linked-component-key] {:text text
                                                                         :change-index 0
                                                                         :current-word ""
                                                                         :completions []
                                                                         :options options})))
(register-handler
 :clear-autocomplete-items
 (fn [db [_ linked-component-key]]
   (assoc-in db [:autocomplete :linked-components linked-component-key :completions] [])))


(register-handler
 :autocomplete
 (fn [db [_ linked-component-key previous-input input autocomplete-data]]
   (let [options (get-in db [:autocomplete :linked-components linked-component-key :options])]
     (app/autocomplete db linked-component-key previous-input input autocomplete-data (if options
                                                                                        (if (:sort-fn options)
                                                                                          options
                                                                                          (assoc options :sort-fn first))
                                                                                        {:options {:sort-fn first}})))))

(register-handler
 :add-autocompleted-word
 (fn [db [_ linked-component-key selected-word]]
   (app/add-autocompleted-word db
                               (get-in db [:autocomplete :linked-components linked-component-key :change-index])
                               selected-word
                               linked-component-key
                               (get-in db [:autocomplete :linked-components linked-component-key :options :new-item-regex]))))

;; --- Subscriptions ---

(register-sub
 :get-previous-input
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components (keyword linked-component-key) :text]))))

(register-sub
 :get-items-to-autocomplete
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components linked-component-key :completions]))))
