# FQCSS: namespaced CSS classes

[![Clojars Project](https://img.shields.io/clojars/v/fqcss.svg)](https://clojars.org/fqcss)

*Abstract:*

I developed this because I wanted a simple alternative to other popular CSS modules solutions for CLJ/CLJS (found them too limited or too awkward). fqcss works by replacing namespaced keywords in CSS files with generated CSS classes.

Pull requests welcome. This library is currently in alpha state. Expect breaking changes.

*Quick example with Reagent*:

```Clojure
(ns app.some.namespace
  (:require [fqcss :refer [wrap-reagent defclasses resolve-kw]))

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
      [:div {:class "example preheader--PG__1 something--PG__2"}
        [:div.ui.container
          [:div {:class "preheader-item--PG__1"}
            [:div {:class "preheader-separator--PG__1"} "|"]]
          [:div {:class "preheader-separator--PG__1"} "-"]
          [:div {:class "preheader-item--PG__1"}
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
.preheader--PG__1 {
  padding: 8px 0px;
  background-color: #ecf0f1;
}

You could set up a watcher for the output of your favorite CSS preprocessor (SASS in my case) and call fqcss to postprocess that CSS file:

```
sass-input.scss -> sass-output.css -> fqcss-output.css
```


## License

Copyright © 2017 Joel Sánchez

Distributed under the Eclipse Public License version 1.0
