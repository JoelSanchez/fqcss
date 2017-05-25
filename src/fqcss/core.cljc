(ns fqcss.core
  (:require [clojure.tools.trace :refer [trace]]
            [clojure.string :as string]))

(def ^:private pseudo-gensym-id
  "The pseudo-gensym atom"
  (atom 0))

(defn- pseudo-gensym-nextid
  "Increments the pseudo-gensym atom and returns the new value"
  []
  (swap! pseudo-gensym-id inc))

(defn- pseudo-gensym-reset
  "Resets the pseudo-gensym atom"
  []
  (reset! pseudo-gensym-id 0))

(defn- pseudo-gensym
  "Independent version of gensym. This allows generating the same classes in both client and server"
  ([] (pseudo-gensym "PG__"))
  ([prefix-string] (. clojure.lang.Symbol (intern (str prefix-string (str (pseudo-gensym-nextid)))))))

(def ^:private gensym-map
  (atom {}))

(defn- pseudo-gensym-for-ns
  "Returns the pseudo-gensym associated with the given namespace.
   Creates and associates one if none exists"
  [ns-symbol]
  (if-let [gs (get @gensym-map ns-symbol)]
    gs
    (let [new-gs (pseudo-gensym)]
      (swap! gensym-map assoc ns-symbol new-gs)
      new-gs)))

(defn reset
  "Resets the gensym map, the class map, and the pseudo-gensym ID."
  []
  (reset! gensym-map {})
  (reset! pseudo-gensym-id 0))

(defn resolve-kw [kw]
  (str (name kw) "--" (pseudo-gensym-for-ns (.getName *ns*))))

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

(defn wrap-reagent
  "Wraps a reagent component, adding the :fqcss property."
  [component]
  (let [{:keys [element properties children]} (reagent-component->map component)]
    (into [] (remove nil-or-empty (concat [element (process-properties properties)]
                                          (into [] (map wrap-reagent children)))))))

(defn- placeholder->kw
  "replace-css helper. Transforms something like {fqcss.core/something}
   to :fqcss.core/something"
  [placeholder]
  (-> placeholder
      (string/replace "{" "")
      (string/replace "}" "")
      (keyword)))

(defn replace-css
  "Replaces the fqcss keywords in a CSS string"
  [css]
  (let [matcher (re-matcher #"\{[a-zA-Z0-9\-\.\/]*?\}" css)]
    (loop [css css]
      (if-let [match (re-find matcher)]
        (recur (string/replace css
                               match
                               (resolve-kw (placeholder->kw match))))
        css))))