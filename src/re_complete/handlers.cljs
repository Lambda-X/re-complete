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
         keys-handling (if (:keys-handling options)
                         (:keys-handling options)
                         (:keys-handling nil))
         filled-options (cond (and trim-chars case-sensitive?) {:trim-chars trim-chars
                                                                :case-sensitive? true
                                                                :keys-handling keys-handling}
                              trim-chars {:trim-chars trim-chars
                                          :case-sensitive? case-sensitive-default
                                          :keys-handling keys-handling}
                              case-sensitive? {:case-sensitive? true
                                               :trim-chars trim-chars-default
                                               :keys-handling keys-handling}
                              :else {:trim-chars trim-chars-default
                                     :case-sensitive? case-sensitive-default
                                     :keys-handling keys-handling})]
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
         (assoc-in [:re-complete :linked-components linked-component-keyword :completions] (app/completions current-word dictionary options))
         (assoc-in [:re-complete :linked-components linked-component-keyword :selected-item] nil)))))

(register-handler
 :add-completed-word
 (fn [db [_ linked-component-key selected-word]]
   (app/add-completed-word db linked-component-key selected-word)))

(register-handler
 :clear-selected-item
 (fn [db [_ linked-component-key]]
   (app/clear-selected-item db linked-component-key)))

(register-handler
 :clear-completions
 (fn [db [_ linked-component-key]]
   (app/clear-completions db linked-component-key)))

(register-handler
 :keys-handling
 (fn [db [_ linked-component-key key-code onclick-callback node current-view]]
   (let [selected-item (get-in db [:re-complete :linked-components linked-component-key :selected-item])
         items-to-complete (get-in db [:re-complete :linked-components linked-component-key :completions])
         focus? (get-in db [:re-complete :linked-components linked-component-key :focus])
         options (get-in db [:re-complete :linked-components linked-component-key :options])
         keys-handling (:keys-handling options)]
     (if focus?
       (cond (= key-code 40) (let [next-item (app/next-item db linked-component-key)
                                   db (assoc-in db [:re-complete :linked-components linked-component-key :selected-item] next-item)]
                               (when keys-handling
                                 (app/scrolling-down linked-component-key (first next-item) node current-view keys-handling))
                               db)
             (= key-code 38) (let [previous-item (app/previous-item db linked-component-key)
                                   db (assoc-in db [:re-complete :linked-components linked-component-key :selected-item] previous-item)]
                               (when keys-handling
                                 (app/scrolling-up linked-component-key (first previous-item) node current-view items-to-complete keys-handling))
                               db)
             (= key-code 13) (let [db (app/add-completed-word db linked-component-key (second selected-item))]
                               (when onclick-callback (onclick-callback))
                               db)
             (= key-code 9) (let [db (app/add-completed-word db linked-component-key (first items-to-complete))]
                              (when onclick-callback (onclick-callback))
                              db)
             (= key-code 27) (app/clear-completions db linked-component-key))
       db))))

(register-handler
 :selected-item
 (fn [db [_ linked-component-key hovered-item]]
   (let [suggestion-list (get-in db [:re-complete :linked-components linked-component-key :completions])
         suggestion-indexed-vector (map-indexed vector suggestion-list)
         hovered-item-with-index (first (filter (comp (partial = hovered-item) second) suggestion-indexed-vector))]
     (assoc-in db [:re-complete :linked-components linked-component-key :selected-item] hovered-item-with-index))))

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

(register-sub
 :get-options
 (fn [db [_ linked-component-key]]
   (reaction (get-in @db [:re-complete :linked-components (keyword linked-component-key) :options]))))
