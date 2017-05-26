(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [clojure.repl :refer :all]
            [figwheel-sidecar.repl-api :as f]))

(set! *warn-on-reflection* true)

(defmacro init-aliases
  []
  `(do
     (~'ns-unalias ~''user ~''fqcss)
     (~'alias ~''fqcss 'fqcss.core)
     ))

(defmacro add-dependency
  "A macro for adding a dependency via Pomegranate.
   Usage: (add-dependency [cheshire \"5.7.0\"])
   Remember that you still need to (require) or (use) the new namespaces."
  [dependency]
  `(do (~'require '[cemerick.pomegranate])
       (~'cemerick.pomegranate/add-dependencies :coordinates '[~dependency]
        :repositories (~'merge cemerick.pomegranate.aether/maven-central
                       {"clojars" "http://clojars.org/repo"}))))

(clojure.tools.namespace.repl/set-refresh-dirs "src" "dev")

(def figwheel-system (atom nil))

(defn start
  "This starts the figwheel server and watch based auto-compiler."
  []
  (reset! figwheel-system (f/start-figwheel!)))

(defn stop
  "Stop the figwheel server and watch based auto-compiler."
  []
  (@figwheel-system))

(defn cljs-repl
  "Launch a ClojureScript REPL that is connected to your build and host environment."
  []
  (f/cljs-repl))

(defn r
  "Refreshes the changed namespaces"
  []
  (tn/refresh)
  (init-aliases)
  :done)