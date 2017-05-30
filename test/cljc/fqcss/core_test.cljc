(ns fqcss.core-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is]])
                     [fqcss.core :as fqcss]))

(deftest test-resolve-kw
  (testing "It should resolve a keyword representing a class"
    (is (= (str "something__" (hash (namespace ::something))) (fqcss/resolve-kw ::something)))))

(deftest test-wrap-reagent
  (testing "It should wrap a Reagent component"
    (let [ns-hash (hash (namespace ::example))
          component
          [:div.something.example {:property "value" :fqcss [::something ::something-else]}
           [:div.other.thing {:other-property "value" :class "existing-class" :fqcss [::something]}]
           [:div.yet.another.thing {:example "stuff"}]]
          resolved-component
          [:div.something.example {:property "value" :class (str "something__" ns-hash " something-else__" ns-hash)}
           [:div.other.thing {:other-property "value" :class (str "existing-class something__" ns-hash)}]
           [:div.yet.another.thing {:example "stuff"}]]
          ]
      (is (= (fqcss/wrap-reagent component) resolved-component)))

    (let [ns-hash (hash (namespace ::example))
          component
          [:div.something.example {:property "value" :fqcss [::something ::something-else]}
           [:div.other.thing {:other-property "value" :class "existing-class" :fqcss [::something]}]
           [:div.yet.another.thing "Stuff"]]
          resolved-component
          [:div.something.example {:property "value" :class (str "something__" ns-hash " something-else__" ns-hash)}
           [:div.other.thing {:other-property "value" :class (str "existing-class something__" ns-hash)}]
           [:div.yet.another.thing "Stuff"]]
          ]
      (is (= (fqcss/wrap-reagent component) resolved-component)))))

(deftest test-replace-css
  (testing "It should replace class keywords in a CSS string"
    (let [ns-hash (hash (namespace ::example))
          css
          (str ".a-class { font-size: 14px; }\n"
               ".a-class.{fqcss.core-test/something} { background-color: black; }\n"
               ".{fqcss.core-test/something} { text-align: center; font-size: 14px; }\n"
               ".{fqcss.core-test/something} &.{fqcss.core-test/something-else} { font-size: 15px; }")
          replaced-css
          (str ".a-class { font-size: 14px; }\n"
               ".a-class.something__" ns-hash " { background-color: black; }\n"
               ".something__" ns-hash " { text-align: center; font-size: 14px; }\n"
               ".something__" ns-hash " &.something-else__" ns-hash " { font-size: 15px; }")]
      (is (= (fqcss/replace-css css) replaced-css)))))

(deftest test-aliases
  (testing "It should treat aliases with love"
    (let [ns-hash (hash "very.long.namespace.that.i.dont.want.to.type")
          css
          (str "{alias short very.long.namespace.that.i.dont.want.to.type}\n"
               ".{short/element} { font-size: 12px; }\n"
               ".{short/another-element} { text-align: center; }\n"
               ".{very.long.namespace.that.i.dont.want.to.type/button} { background-color: black; }")
          replaced-css
          (str ".element__" ns-hash " { font-size: 12px; }\n"
               ".another-element__" ns-hash " { text-align: center; }\n"
               ".button__" ns-hash " { background-color: black; }")]
      (is (= (fqcss/replace-css css) replaced-css)))))