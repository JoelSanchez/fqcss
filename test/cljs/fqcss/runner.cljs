(ns fqcss.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [fqcss.macro-test]))

(doo-tests
  'fqcss.macro-test)
