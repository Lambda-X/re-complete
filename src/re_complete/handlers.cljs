(ns re-complete.handlers
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :refer [register-handler
                                   register-sub
                                   dispatch
                                   subscribe]]
            [re-complete.app :as app]))


(def trim-chars-default "")

(def case-sensitive-default false)

;; --- Handlers ---

(register-handler
 :options
 (fn [db [_ linked-component-key options]]
   (let [trim-chars (:trim-chars options)
         case-sensitive? (:case-sensitive? options)
         filled-options (cond (and trim-chars case-sensitive?) {:trim-chars trim-chars
                                                                :case-sensitive? true}
                              trim-chars {:trim-chars trim-chars
                                          :case-sensitive? case-sensitive-default}
                              case-sensitive? {:case-sensitive? true
                                               :trim-chars trim-chars-default}
                              :else {:trim-chars trim-chars-default
                                     :case-sensitive? case-sensitive-default})]
     (assoc-in db [:re-complete :linked-components (keyword linked-component-key)] {:options filled-options}))))

(register-handler
 :focus
 (fn [db [_ linked-component-key focus?]]
   (assoc-in db [:re-complete :linked-components (keyword linked-component-key) :focus] focus?)))

(register-handler
 :dictionary
 (fn [db [_ linked-component-key dictionary]]
   (assoc-in db [:re-complete :linked-components (keyword linked-component-key) :dictionary] dictionary)))

(register-handler
 :input
 (fn [db [_ linked-component-key input]]
   (let [linked-component-keyword (keyword linked-component-key)
         previous-input (get-in db [:re-complete :linked-components linked-component-keyword :text])
         options (get-in db [:re-complete :linked-components linked-component-keyword :options])
         dictionary (get-in db [:re-complete :linked-components linked-component-keyword :dictionary])
         index (app/index previous-input input)
         current-word (app/current-word input index)]
     (-> db
         (assoc-in [:re-complete :linked-components linked-component-keyword :text] input)
         (assoc-in [:re-complete :linked-components linked-component-keyword :change-index] index)
         (assoc-in [:re-complete :linked-components linked-component-keyword :current-word] current-word)
         (assoc-in [:re-complete :linked-components linked-component-keyword :completions] (app/completions current-word dictionary options))))))

(register-handler
 :add-completed-word
 (fn [db [_ linked-component-key selected-word]]
   (app/add-completed-word db linked-component-key selected-word)))

(register-handler
 :keys-handling
 (fn [db [_ linked-component-key key-code onclick-callback node]]
   (let [selected-item (get-in db [:re-complete :linked-components linked-component-key :selected-item])
         items-to-complete (get-in db [:re-complete :linked-components linked-component-key :completions])
         focus? (get-in db [:re-complete :linked-components linked-component-key :focus])]
     (if focus?
       (cond (= key-code 40) (app/select-next-item db linked-component-key)
             (= key-code 38) (app/select-previous-item db linked-component-key)
             (= key-code 13) (do (when onclick-callback (onclick-callback))
                                 (app/add-completed-word db linked-component-key (second selected-item)))
             (= key-code 9) (do (when onclick-callback (onclick-callback))
                                (app/add-completed-word db linked-component-key (first items-to-complete)))
             (= key-code 32) (do (set! (.-scrollTop node) 100) 
                                 db))
       db))))

;; --- Subscriptions ---

(register-sub
 :get-previous-input
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:re-complete :linked-components (keyword linked-component-key) :text]))))

(register-sub
 :get-items-to-complete
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:re-complete :linked-components linked-component-key :completions]))))

(register-sub
 :get-selected-item
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:re-complete :linked-components linked-component-key :selected-item]))))
