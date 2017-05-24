(defproject fqcss "0.0.1"
  :description "FQCSS: namespaced CSS classes"

  :url "https://github.com/JoelSanchez/fqcss"

  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.542"]
                 [garden "1.3.2"]]

  :test-paths ["test/clj"]
  :source-paths ["src"]
  :jvm-opts ["-XX:-OmitStackTraceInFastThrow"]
  :repl-options {:init-ns user}

  :plugins [[lein-auto "0.1.2"]
            [lein-doo "0.1.6"]
            [lein-cljsbuild "1.1.4"]]

  :cljsbuild
  {:builds
   [{:id           "test"
     :source-paths ["src/" "test/cljs"]
     :compiler     {:output-to     "resources/public/test/app.test.js"
                    :output-dir    "resources/public/test/out"
                    :main          'fqcss.runner
                    :optimizations :none}}]}

  :profiles {:dev {:dependencies [[org.clojure/tools.namespace "0.2.11"]]
                   :source-paths ["dev"]}}

  :auto {:default {:file-pattern #"\.(clj|cljs|cljc|edn)$"}})
