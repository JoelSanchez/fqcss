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
  (testing "It should resolve a keyword representing a class"
    (is (= "something--PG__1" (fqcss/resolve-kw ::something)))))

(deftest test-wrap-reagent
  (testing "It should wrap a Reagent component"
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