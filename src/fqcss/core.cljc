(ns fqcss.core)

(def ^:private pseudo-gensym-id
  "The pseudo-gensym atom"
  (atom 0))

(defn- pseudo-gensym-nextid
  "Increments the pseudo-gensym atom and returns the new value"
  []
  (swap! pseudo-gensym-id inc))

(defn pseudo-gensym-reset
  "Resets the pseudo-gensym atom"
  []
  (reset! pseudo-gensym-id 0))

(defn pseudo-gensym
  "Independent version of gensym. This allows generating the same classes in both client and server"
  ([] (pseudo-gensym "PG__"))
  ([prefix-string] (. clojure.lang.Symbol (intern (str prefix-string (str (pseudo-gensym-nextid)))))))

