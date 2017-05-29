(ns fqcss.core-test
  (:require #?(:clj  [clojure.test :refer :all]
               :cljs [cljs.test :refer-macros [deftest testing is]])
                     [fqcss.core :as fqcss]))

(deftest test-pseudo-gensym-for-ns
  (testing "It should generate a gensym for every namespace"
    (fqcss/reset)
    (is (= 'PG__1 (@#'fqcss.core/pseudo-gensym-for-ns 'fqcss.core-test)))
    (is (= 'PG__1 (@#'fqcss.core/pseudo-gensym-for-ns 'fqcss.core-test)))
    (is (= 'PG__2 (@#'fqcss.core/pseudo-gensym-for-ns 'example-ns)))))

(deftest test-resolve-kw
  (fqcss/reset)
  (testing "It should resolve a keyword representing a class"
    (is (= "something--PG__1" (fqcss/resolve-kw ::something)))))

(deftest test-wrap-reagent
  (testing "It should wrap a Reagent component"
    (fqcss/reset)
    (let [component
          [:div.something.example {:property "value" :fqcss [::something ::something-else]}
           [:div.other.thing {:other-property "value" :class "existing-class" :fqcss [::something]}]
           [:div.yet.another.thing {:example "stuff"}]]
          resolved-component
          [:div.something.example {:property "value" :class "something--PG__1 something-else--PG__1"}
           [:div.other.thing {:other-property "value" :class "existing-class something--PG__1"}]
           [:div.yet.another.thing {:example "stuff"}]]
          ]
      (is (= (fqcss/wrap-reagent component) resolved-component)))

    (let [component
          [:div.something.example {:property "value" :fqcss [::something ::something-else]}
           [:div.other.thing {:other-property "value" :class "existing-class" :fqcss [::something]}]
           [:div.yet.another.thing "Stuff"]]
          resolved-component
          [:div.something.example {:property "value" :class "something--PG__1 something-else--PG__1"}
           [:div.other.thing {:other-property "value" :class "existing-class something--PG__1"}]
           [:div.yet.another.thing "Stuff"]]
          ]
      (is (= (fqcss/wrap-reagent component) resolved-component)))))

(deftest test-replace-css
  (testing "It should replace class keywords in a CSS string"
    (fqcss/reset)
    (let [css
          ".a-class { font-size: 14px; }
           .a-class.{fqcss.core-test/something} { background-color: black; }
           .{fqcss.core-test/something} { text-align: center; font-size: 14px; }
           .{fqcss.core-test/something} &.{fqcss.core-test/something-else} { font-size: 15px; }"
          replaced-css
          ".a-class { font-size: 14px; }
           .a-class.something--PG__1 { background-color: black; }
           .something--PG__1 { text-align: center; font-size: 14px; }
           .something--PG__1 &.something-else--PG__1 { font-size: 15px; }"]
      (is (= (fqcss/replace-css css) replaced-css)))))

(deftest test-aliases
  (testing "It should treat aliases with love"
    (fqcss/reset)
    (let [css
          (str "{alias short very.long.namespace.that.i.dont.want.to.type}\n"
               ".{short/element} { font-size: 12px; }\n"
               ".{short/another-element} { text-align: center; }\n"
               ".{very.long.namespace.that.i.dont.want.to.type/button} { background-color: black; }")
          replaced-css
          (str ".element--PG__1 { font-size: 12px; }\n"
               ".another-element--PG__1 { text-align: center; }\n"
               ".button--PG__1 { background-color: black; }")]
      (is (= (fqcss/replace-css css) replaced-css)))))