(set-env!
 :resource-paths #{"src" "demo" "html"}
 :dependencies '[;; Boot deps
                 [adzerk/boot-cljs            "1.7.228-1"       :scope "test"]
                 [adzerk/boot-cljs-repl       "0.3.0"           :scope "test"]
                 [adzerk/boot-reload          "0.4.4"           :scope "test"]
                 [pandeiro/boot-http          "0.7.1-SNAPSHOT"  :scope "test"]
                 [degree9/boot-semver         "1.2.4"           :scope "test"]
                 [adzerk/bootlaces            "0.1.13"          :scope "test"]

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
 '[pandeiro.boot-http    :refer [serve]]
 '[boot-semver.core      :refer :all]
 '[adzerk.bootlaces      :refer :all])

(def +version+ (get-version))

(bootlaces! +version+)

(task-options!
 pom {:project 're-complete
      :version +version+
      :url "https://github.com/ScalaConsultants/re-complete"
      :description "Autocomplete plugin"
      :license {"Eclipse Public License" "http://www.eclipse.org/legal/epl-v10.html"}}
 test-cljs {:js-env :phantom})

(deftask auto-test []
  (set-env! :resource-paths #(conj % "test"))
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

(deftask version-file
  "A task that includes the version.properties file in the fileset."
  []
  (with-pre-wrap [fileset]
    (boot.util/info "Add version.properties...\n")
    (-> fileset
        (add-resource (java.io.File. ".") :include #{#"^version\.properties$"})
        commit!)))

(deftask build []
  (merge-env! :source-paths #{"src" "demo"} :resource-paths #{"html"})
  (comp (version-file)
        (cljs :optimizations :advanced)))
