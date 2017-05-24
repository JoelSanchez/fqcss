(ns user
  (:require [clojure.tools.namespace.repl :as tn]
            [clojure.repl :refer :all]))

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

(defn r
  "Refreshes the changed namespaces"
  []
  (tn/refresh)
  (init-aliases)
  :done)