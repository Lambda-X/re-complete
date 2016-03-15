(ns re-complete.handlers
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch]]
            [goog.events :as events]
            [re-complete.app :as app]))


(def exclude-chars-default "")

(def sort-fn-default first)


;; --- Handlers ---

(register-handler
 :autocomplete-component
 (fn [db [_ linked-component-key input dictionary options]]
   (let [linked-component-keyword (keyword linked-component-key)
         previous-input (get-in db [:autocomplete :linked-components linked-component-keyword :text])
         sort-fn (:sort-fn options)
         exclude-chars (:exclude-chars options)
         filled-options (cond (and exclude-chars sort-fn) options
                              exclude-chars {:exclude-chars exclude-chars
                                             :sort-fn sort-fn-default}
                              sort-fn {:exclude-chars exclude-chars-default
                                       :sort-fn sort-fn}
                              :else {:exclude-chars exclude-chars-default
                                     :sort-fn sort-fn-default})]
     (-> db
         (assoc-in [:autocomplete :linked-components linked-component-keyword] {:text input
                                                                                :change-index 0
                                                                                :current-word ""
                                                                                :completions []
                                                                                :options filled-options})
         (app/autocomplete linked-component-keyword previous-input input dictionary filled-options)))))

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
                               (get-in db [:autocomplete :linked-components linked-component-key :options :exclude-chars]))))

;; --- Subscriptions ---

(register-sub
 :get-previous-input
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components (keyword linked-component-key) :text]))))

(register-sub
 :get-items-to-autocomplete
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:autocomplete :linked-components linked-component-key :completions]))))
