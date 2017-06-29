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
           [:div.yet.another.thing {:example "stuff"}]
           [:div.last.thing "Stuff"]]
          resolved-component
          [:div.something.example {:property "value" :class (str "something__" ns-hash " something-else__" ns-hash)}
           [:div.other.thing {:other-property "value" :class (str "existing-class something__" ns-hash)}]
           [:div.yet.another.thing {:example "stuff"}]
           [:div.last.thing "Stuff"]]
          ]
      (is (= (fqcss/wrap-reagent component) resolved-component))))
  (testing "It should fail to wrap an empty vector"
    (is (= "exception" (try (fqcss/wrap-reagent [])
                            (catch #?(:clj Error :cljs :default) e (str "exception")))))))

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
      (is (= (fqcss/replace-css css) replaced-css))))
  (testing "It should not be confused when confronted with empty CSS declarations"
    (let [ns-hash (hash "my.namespaced")
          css
          (str ".a-class { font-size: 14px; }\n"
               ".{my.namespaced/class} {}\n"
               ".{my.namespaced/class2} { }")
          replaced-css
          (str ".a-class { font-size: 14px; }\n"
               ".class__" ns-hash " {}\n"
               ".class2__" ns-hash " { }")]
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

(deftest test-lists
  (testing "It should wrap lists of components correctly"
    (let [input [:div {:fqcss [::a]}
                 '([:div {:fqcss [::b]}
                    [:a "a"]]
                   [:div {:fqcss [::b]}
                    [:a "b"]])]]
      (is (= (fqcss/wrap-reagent input)
             [:div {:class "a__-704642599"}
              '([:div {:class "b__-704642599"} [:a "a"]]
                [:div {:class "b__-704642599"} [:a "b"]])])))))