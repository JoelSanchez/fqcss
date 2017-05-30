(ns fqcss.core
  (:require [clojure.string :as string]))

#?(:cljs (enable-console-print!))

(defn resolve-kw [kw]
  (str (name kw) "__" (hash (namespace kw))))

(defn- process-property [[kw value]]
  (if (= kw :fqcss)
    [kw (clojure.string/join " " (map resolve-kw value))]
    [kw value]))

(defn- process-properties [props]
  (let [existing-classes (if-let [prop (:class props)]
                           (clojure.string/split prop #" ")
                           [])
        fqcss (map resolve-kw (:fqcss props))
        all-classes (concat existing-classes fqcss)]
    (if (> (count all-classes) 0)
      (-> props
          (assoc :class (clojure.string/join " " all-classes))
          (dissoc :fqcss))
      props)))

(defn- reagent-component->map
  [component]
  (let [element    (get component 0)
        properties (if (map? (get component 1)) (get component 1) nil)
        children   (if (map? (get component 1)) (subvec component 2) (subvec component 1))]
    {:element element
     :properties properties
     :children children}))

(defn- nil-or-empty [thing]
  "True if nil or empty coll"
  (or (nil? thing) (and (coll? thing) (empty? thing))))

(declare wrap-reagent)

(defn- maybe-wrap-reagent [child]
  (if (vector? child)
    (wrap-reagent child)
    child))

(defn wrap-reagent
  "Wraps a reagent component, adding the :fqcss property."
  [component]
  (let [{:keys [element properties children]} (reagent-component->map component)]
    (into [] (remove nil-or-empty (concat [element (process-properties properties)]
                                          (into [] (map maybe-wrap-reagent children)))))))

(defn- placeholder->kw
  "replace-css helper. Transforms something like {fqcss.core/something}
   to :fqcss.core/something"
  [placeholder]
  (-> placeholder
      (string/replace "{" "")
      (string/replace "}" "")
      (keyword)))

(def aliases
  "The replace-css aliases"
  (atom {}))

(defn- replace-css-classes
  "replace-css helper. Replaces the CSS classes ({app.example/something} -> .something--PG__1)"
  [css]
  (let [matches (re-seq #"\{[a-zA-Z0-9\-\.\/]*?\}" css)]
    (reduce (fn [acc item]
              (let [kw (placeholder->kw item)
                    kw (if-let [expanded-ns (get @aliases (namespace kw))]
                         (keyword expanded-ns (name kw))
                         kw)]
                (string/replace acc item (resolve-kw kw))))
            css
            matches)))

(defn- replace-css-aliases
  "replace-css helper. Evaluates and removes the aliases ({alias example app.example}"
  [css]
  (let [matches (re-seq #"\{alias ([a-zA-Z0-9\-\.\/]+?) ([a-zA-Z0-9\-\.\/]+?)\}\n" css)]
    (reduce (fn [acc [all alias ns-name :as match]]
              (swap! aliases assoc alias ns-name)
              (string/replace acc all ""))
            css
            matches)))

(defn replace-css
  "Replaces the fqcss keywords in a CSS string"
  [css]
  (let [css (-> css
                (replace-css-aliases)
                (replace-css-classes))]
    (reset! aliases {})
    css))