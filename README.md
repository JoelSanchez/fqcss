# FQCSS: namespaced CSS classes

[![Clojars Project](https://img.shields.io/clojars/v/fqcss.svg)](https://clojars.org/fqcss)

tl;dr: conflict-free CSS classes, write CSS however you want (Clojure, SCSS, plain SCSS...), write components however you want (Rum, Reagent, HTML...) -- the library doesn't care.

I developed this because I wanted to avoid conflicts between CSS classes from different namespaces. Say, you have a "header" component in a namespace, and you want to use that same name in another component because it has semantic sense (like the header of a data-table component, for example). You'd probably come up with some prefix like "data-table-header", but why bother? Isn't the namespace of the component enough for that? Now, of course you can do this:

```
.app_plugins_data-table_components_data-table--header {
    font-size: 2em;
    font-weight: bold;
}
```

But that results in three problems:

* You'd have to refer to the long CSS classes every single time in your components (see below).
* It's verbose, hence tiring, hence error-prone.
* It's not very readable, because the dot means something else in CSS (that's why you'd need to use "_" or something)

Just look at this:

```Clojure
(defn header []
  [:div.app_plugins_data-table_components_data-table--header
    [:div.app_plugins_data-table_components_data-table--header-text "Hi"]])
```

Ew. There's got to be a better way! What if we could just use qualified keywords to refer to the classes of our namespace? Something like this (won't work, of course):

```Clojure
(defn header []
  [:div {:class [::header]}
    [:div {:class [::header-text]} "Hi"]])
```

That'd be great, hence fqcss was born. You declare the classes as a vector of qualified keywords in the :fqcss property, and you wrap your component with a call to fqcss.core/wrap-reagent:

```Clojure
(defn header []
  (wrap-reagent
    [:div {:fqcss [::header]}
      [:div {:fqcss [::header-text]} "Hi"]]))
```

And this is the result (assuming app.plugins.data-table.components.data-table namespace):

```Clojure
[:div {:class "header__-153777894"}
  [:div {:class "header-text__-153777894"} "Hi"]]
```

Here "-153777894" is (hash (.getName *ns*)), that is, the hash of the name of the namespace where the component lives.

wrap-reagent is just Syntax Sugar (tm), so you can use fqcss.core/resolve-kw instead:

```Clojure
(defn header []
  [:div {:class (resolve-kw ::header)}
    [:div {:class (resolve-kw ::header-text)} "Hi"]])
```

Now, this is nice, but how do I write the CSS? If you remember, our CSS looked like this:

```
.app_plugins_data-table_components_data-table--header {
    font-size: 2em;
    font-weight: bold;
}
```

We can rewrite it such that fqcss can replace the namespaced classes for us, like this:

```
.{app.plugins.data-table.components.data-table/header} {
    font-size: 2em;
    font-weight: bold;
}
```

This is the generated CSS:

```
.header__-153777894 {
    font-size: 2em;
    font-weight: bold;
}
```

"But that's still verbose, I want my money back", you say. I know. That's why you can do this:

```
{alias data-table app.plugins.data-table.components.data-table}

.{data-table/header} {
    font-size: 2em;
    font-weight: bold;
}
.{data-table/header-text} {
    color: black;
}
```

Note that the alias comes before the namespace, just like with clojure.core/alias. Also, they have to take the whole line.

Now, how do we get that CSS file / string / whatever processed by fqcss? Easy:

```Clojure
(fqcss.core/replace-css (slurp "style.css"))
```

That's it for the API.

### How do I integrate this with my project?

I'm going to explain how I do it, but all of it is up to you.

I have this project structure:

```
my-project
  src
    clj
      my-project
        components
          data-table.clj
    fqcss
      my-project
        components
          data-table.scss
    scss
      my-project
        components
          data-table.scss
```

I watch for file changes in the "fqcss" directory. Every time a file is updated or created, it is processed by fqcss and put into the same relative path under the "scss" directory:

```
fqcss/my-project/components/data-table.scss -> (replace-css) -> scss/my-project/components/data-table.scss
```

Then the SCSS watcher processes that file, then figwheel detects the change in the CSS file, then it gets loaded, then everything's good.

Here's my fqcss watcher:

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

Pull requests welcome. This library is currently in alpha state. Expect breaking changes.


## License

Copyright © 2017 Joel Sánchez

Distributed under the Eclipse Public License version 1.0
