(ns fqcss.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [fqcss.core-test]))

(doo-tests
 'fqcss.core-test)
