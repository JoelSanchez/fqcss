# FQCSS: namespaced CSS classes

[![Clojars Project](https://img.shields.io/clojars/v/fqcss.svg)](https://clojars.org/fqcss)

*Abstract:*

I developed this because I wanted a simple alternative to other popular CSS modules solutions for CLJ/CLJS (found them too limited or too awkward). fqcss works by replacing namespaced keywords in CSS files with generated CSS classes.

Pull requests welcome. This library is currently in alpha state. Expect breaking changes.

*Quick example with Reagent*:

```Clojure
(ns app.some.namespace
  (:require [fqcss :refer [wrap-reagent replace-css resolve-kw]))

;; Define the namespaced classes as a vector in the :fqcss property:

(defn something []
  (wrap-reagent
      [:div {:class "example" :fqcss [::preheader :app.some.other.namespace/something]}
        [:div.ui.container
          [:div {:fqcss [::preheader-item]}
            [:div {:fqcss [::preheader-separator]} "|"]]
          [:div {:fqcss [::preheader-separator]} "-"]
          [:div {:fqcss [::preheader-item]}
            [:strong "Lorem ipsum"]]]]))


;; This results in:

(defn something []
  (wrap-reagent
      [:div {:class "example preheader__318088553 something__-1703565805"}
        [:div.ui.container
          [:div {:class "preheader-item__318088553"}
            [:div {:class "preheader-separator__318088553"} "|"]]
          [:div {:class "preheader-separator__318088553"} "-"]
          [:div {:class "preheader-item__318088553"}
            [:strong "Lorem ipsum"]]]]))

;; The same can be achieved without wrap-reagent:

(defn something []
  [:div {:class (clojure.string/join " " (concat ["example"] (map resolve-kw [::preheader :app.some.other.namespace/something])))}
    [:div.ui.container
      [:div {:class (resolve-kw ::preheader-item)}
        [:div {:class (resolve-kw ::preheader-separator)} "|"]]
      [:div {:class (resolve-kw ::preheader-separator)} "-"]
      [:div {:class (resolve-kw ::preheader-item)}
        [:strong "Lorem ipsum"]]]])
```

FQCSS works by processing your stylesheet, replacing its special syntax (the keyword surrounded by curly braces):

```CSS
.{app.some.namespace/preheader} {
  padding: 8px 0px;
  background-color: #ecf0f1;
}
```

To process a CSS file:

```Clojure
(replace-css (slurp "stylesheet.css")

```

The generated CSS is:

```CSS
.preheader__1552691312 {
  padding: 8px 0px;
  background-color: #ecf0f1;
}
```

This is how I integrate fqcss with sass in my projects: I watch changes in the "src/fqcss" directory, output the result from fqcss to the same relative paths in "src/scss", and then the SCSS watcher (lein-sassc) processes those files and turns them into a file in "resources/public/css/style.css". Here's the fqcss watcher:

```Clojure
(ns user
  (:require [mount.core :as mount :refer [defstate]]
            [async-watch.core :as watch]
            [clojure.core.async :refer [>! <! go close!]]
            [fqcss.core :as fqcss]))

(defn fqcss-start []
  (println "Starting fqcss")
  (let [changes (watch/changes-in "src/fqcss")]
    (go (while true
      (let [[op filename] (<! changes)]
        (when (and (clojure.string/ends-with? filename ".scss") (or (= (name op) "modify")
                                                                    (= (name op) "create")))
          (println "Processing fqcss (" (name op) ")")
          (let [new-path (clojure.string/replace filename "fqcss" "scss")]
            (println "\tSpitting to: " new-path)
            (spit new-path (fqcss/replace-css (slurp filename))))))))))

(defn fqcss-stop []
  (println "Stopping fqcss")
  (watch/cancel-changes))

(defstate fqcss
  :start
    (fqcss-start)
  :stop
    (fqcss-stop))
```

### Aliases

fqcss supports namespace aliases:

```
{alias short very.long.namespace.that.i.dont.want.to.type}
.{short/element} { font-size: 12px; }
.{very.long.namespace.that.i.dont.want.to.type/another-element} { background-color: black; }
```

Result:

```
.element__-214598257 { font-size: 12px; }
.another-element__-214598257 { background-color: black; }
```

Aliases should take a whole line.


## License

Copyright © 2017 Joel Sánchez

Distributed under the Eclipse Public License version 1.0
