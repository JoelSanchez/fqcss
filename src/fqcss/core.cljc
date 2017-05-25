(ns fqcss.core
  (:require [clojure.tools.trace :refer [trace]]))

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

(def ^:private class-map
  "A map associating namespaced keywords to generated CSS classes"
  (atom {}))

(defn reset
  "Resets the gensym map, the class map, and the pseudo-gensym ID. Useful for testing.
   This will undo any calls to defclass / defclasses already made."
  []
  (reset! gensym-map {})
  (reset! pseudo-gensym-id 0)
  (reset! class-map {}))

(defn defclass
  "Generates given CSS class"
  [kw]
  (swap! class-map assoc (keyword (str (.getName *ns*)) (name kw))
                         (str (name kw) "--" (pseudo-gensym-for-ns (.getName *ns*)))))

(defn resolve-kw [kw]
  (get @class-map kw))

(defn defclasses
  "Generates given CSS classes"
  [classes]
  (last (doall (map defclass classes))))

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

(defn- replace-css*
  "replace-css helper. Replaces one fqcss keyword in the CSS string"
  [css [kw cls]]
  (clojure.string/replace css (str "{" (namespace kw) "/" (name kw) "}") cls))

(defn replace-css
  "Replaces the fqcss keywords in a CSS string"
  [css]
  (reduce replace-css* css @class-map))