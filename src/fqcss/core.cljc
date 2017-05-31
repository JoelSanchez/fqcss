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
  (if (and (vector? child) (>= (count child) 1))
    (wrap-reagent child)
    child))

(defn wrap-reagent
  "Wraps a reagent component, adding the :fqcss property."
  [component]
  {:pre [(vector? component) (>= (count component) 1)]}
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

(defn- replace-css-classes
  "replace-css helper. Replaces the CSS classes ({app.example/something} -> .something__-301316910)"
  ([css]
    (replace-css-classes css {}))
  ([css aliases]
    ;; To avoid conflicts with CSS syntax, forbid ";", ":"" and whitespace
    (let [matches (re-seq #"\{[^;:\s]+?\}" css)]
      (reduce (fn [acc item]
                (let [kw (placeholder->kw item)
                      kw (if-let [expanded-ns (get aliases (namespace kw))]
                           (keyword expanded-ns (name kw))
                           kw)]
                  (string/replace acc item (resolve-kw kw))))
              css
              matches))))

(defn- replace-css-aliases
  "replace-css helper. Evaluates and removes the aliases ({alias example app.example}"
  [css]
  (let [matches (re-seq #"\{alias ([^;:\s]+?) ([^;:\s]+?)\}\n" css)]
    (reduce (fn [{:keys [css aliases]} [all alias ns-name :as match]]
              {:css (string/replace css all "")
               :aliases (assoc aliases alias ns-name)})
            {:css css :aliases {}}
            matches)))

(defn replace-css
  "Replaces the fqcss keywords in a CSS string"
  [css]
  (let [{:keys [css aliases]} (replace-css-aliases css)]
    (replace-css-classes css aliases)))