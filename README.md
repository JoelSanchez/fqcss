# FQCSS: namespaced CSS classes

[![Clojars Project](https://img.shields.io/clojars/v/fqcss.svg)](https://clojars.org/fqcss)

*Quick example*:

```Clojure
(ns app.some.namespace
  (:require [fqcss :refer [wrap-reagent defclasses resolve-kw]))

;; defclasses registers classes for this namespace

(defclasses
  [:something
   :something-item
   :other-thing])

;; At this point fqcss has an internal map which looks like this:

{:app.some.namespace/something ".something--PG__1"}
{:app.some.namespace/something-item ".something-item--PG__1"}
{:app.some.namespace/other-thing ".other-thing--PG__1"}

;; At the left side, a fully qualified keyword, which you'll use to refer to the unique class at the right.
;; "PG__1" is the generated identifier for this namespace.

;; Now we can use those classes (note the use of the class "something" from the namespace "app.some.other.namespace"):

(defn something []
  [:div.ventas {:class (clojure.string/join " " (map resolve-kw [::preheader :app.some.other.namespace/something])}
    [:div.ui.container
      [:div.ventas {:class (resolve-kw ::preheader-item)}
        [:strong "Att. cliente y pedidos: "]
        [:a "666 555 444"]
        [:div.ventas {:class (resolve-kw ::preheader-separator)} "|"]
        [:a "666 555 444"]
        [:div.ventas {:class (resolve-kw ::preheader-separator)} "|"]
        [:a "444 333 222"]]
      [:div.ventas {:class (resolve-kw ::preheader-separator)} "-"]
      [:div.ventas {:class (resolve-kw ::preheader-item)}
        [:strong "Horario:"]
        [:span "De Lunes a Viernes 09:00 - 13:30 / 16:00 - 19:30"]]]])

;; wrap-reagent provides a little syntax sugar:

(defn something []
  (wrap-reagent
      [:div.ventas {:fqcss [::preheader :app.some.other.namespace/something]}
        [:div.ui.container
          [:div.ventas {:fqcss [::preheader-item]}
            [:strong "Att. cliente y pedidos: "]
            [:a "666 555 444"]
            [:div.ventas {:fqcss [::preheader-separator]} "|"]
            [:a "666 555 444"]
            [:div.ventas {:fqcss [::preheader-separator]} "|"]
            [:a "444 333 222"]]
          [:div.ventas {:fqcss [::preheader-separator]} "-"]
          [:div.ventas {:fqcss [::preheader-item]}
            [:strong "Horario:"]
            [:span "De Lunes a Viernes 09:00 - 13:30 / 16:00 - 19:30"]]]]))


```

FQCSS works by modifying your stylesheet, replacing its special syntax (the keyword surrounded by curly braces):

```CSS
.{app.some.namespace/preheader} {
  padding: 8px 0px;
  background-color: #ecf0f1;
}
```

```Clojure

```

The generated CSS is:

```CSS
.preheader--PG__1 {
  padding: 8px 0px;
  background-color: #ecf0f1;
}
```


This library is only concerned with the generation and substitution of namespaced CSS classes. How you write your CSS is out of scope by design.

I'm using this as a replacement for CSS Modules.

## License

Copyright © 2017 Joel Sánchez

Distributed under the Eclipse Public License version 1.0
