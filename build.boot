(def +version+ "0.1.0-SNAPSHOT")

(set-env!
 :resource-paths #{"src" "demo" "html"}
 :dependencies '[;; Boot deps
                 [adzerk/boot-cljs            "1.7.228-1"       :scope "test"]
                 [adzerk/boot-cljs-repl       "0.3.0"           :scope "test"]
                 [adzerk/boot-reload          "0.4.4"           :scope "test"]
                 [pandeiro/boot-http          "0.7.1-SNAPSHOT"  :scope "test"]

                 ;; Boot test
                 [crisptrutski/boot-cljs-test "0.2.2-SNAPSHOT"  :scope "test"]

                 ;; Repl
                 [cider/cider-nrepl           "0.11.0-SNAPSHOT" :scope "test"]
                 [adzerk/boot-cljs-repl       "0.3.0"           :scope "test"]
                 [com.cemerick/piggieback     "0.2.1"           :scope "test"]
                 [weasel                      "0.7.0"           :scope "test"]
                 [org.clojure/tools.nrepl     "0.2.12"          :scope "test"]

                 ;; Dev deps
                 [org.clojure/clojure         "1.7.0"]
                 [org.clojure/clojurescript   "1.7.228"]
                 [reagent                     "0.5.0"]
                 [re-frame                    "0.7.0-alpha-3"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[crisptrutski.boot-cljs-test  :refer [test-cljs]]
 '[pandeiro.boot-http    :refer [serve]])

(task-options!
 pom {:project "re-complete"
      :version +version+
      :url "https://github.com/Lambda-X/re-complete"
      :description "Autocomplete plugin"
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}}
 test-cljs {:js-env :phantom})

(deftask auto-test []
  (merge-env! :resource-paths #{"test"})
  (comp (watch)
        (speak)
        (test-cljs)))

(deftask dev
  "Start the dev env..."
  []
  (set-env! :source-paths #{"src" "demo"})
  (comp (serve :dir "html")
        (watch)
        (speak)
        (reload :on-jsload 're-complete.example/main)
        (cljs-repl)
        (cljs :compiler-options {:closure-defines {"goog.DEBUG" false}
                                 :source-map :true
                                 :optimizations :none
                                 :source-map-timestamp true})))

(deftask build []
  (cljs :optimizations :advanced))
