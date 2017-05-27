(defproject fqcss "0.0.5"
  :description "FQCSS: namespaced CSS classes"

  :url "https://github.com/JoelSanchez/fqcss"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.542"]
                 [lein-doo "0.1.7"]]

  :plugins [[lein-auto "0.1.2"]
            [lein-doo "0.1.6"]
            [lein-figwheel "0.5.10"]
            [lein-cljsbuild "1.1.5" :exclusions [[org.clojure/clojure]]]]

  :test-paths ["test/cljc"]
  :source-paths ["src"]
  :jvm-opts ["-XX:-OmitStackTraceInFastThrow"]
  :repl-options {:init-ns user :port 4002}

  :cljsbuild {:test-commands {"test" ["lein" "doo" "chrome" "test" "once"]}
              :builds
              [{:id "dev"
                :source-paths ["src"]

                ;; the presence of a :figwheel configuration here
                ;; will cause figwheel to inject the figwheel client
                ;; into your build
                :figwheel {:on-jsload "fqcss.core/on-js-reload"
                           ;; :open-urls will pop open your application
                           ;; in the default browser once Figwheel has
                           ;; started and complied your application.
                           ;; Comment this out once it no longer serves you.
                           :open-urls ["http://localhost:3449/index.html"]}

                :compiler {:main fqcss.core
                           :asset-path "js/compiled/out"
                           :output-to "resources/public/js/compiled/fqcss.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           ;; To console.log CLJS data-structures make sure you enable devtools in Chrome
                           ;; https://github.com/binaryage/cljs-devtools
                           :preloads [devtools.preload]}}

               {:id           "test"
                :source-paths ["src/" "test/cljc" "test/cljs"]
                :compiler     {:output-to     "resources/public/test/app.test.js"
                               :output-dir    "resources/public/test/out"
                               :main          fqcss.runner
                               :optimizations :none}}

               ;; This next build is an compressed minified build for
               ;; production. You can build this with:
               ;; lein cljsbuild once min
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/fqcss.js"
                           :main fqcss.core
                           :optimizations :advanced
                           :pretty-print false}}]}

  :figwheel {}



  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]
                                  [org.clojure/tools.trace "0.7.9"]
                                  [binaryage/devtools "0.9.2"]
                                  [figwheel-sidecar "0.5.10"]
                                  [com.cemerick/piggieback "0.2.1"]]
                   :source-paths ["dev"]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}}

  :auto {:default {:file-pattern #"\.(clj|cljs|cljc|edn)$"}})
