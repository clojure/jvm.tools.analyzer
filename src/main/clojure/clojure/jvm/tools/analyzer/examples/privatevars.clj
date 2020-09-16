;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.jvm.tools.analyzer.examples.privatevars
  (:require [clojure.jvm.tools.analyzer :as analyze]
            [clojure.set :as set]
            [clojure.pprint :as pp]))

(defn- unused-fn [] nil)
(def ^:private unused-var 0)

(defn defs [expr]
  (apply concat
         (when (= :def (:op expr)) [(:var expr)])
         (map defs (analyze/children expr))))

(defn private-defs [expr]
  (filter #(:private (meta %))
          (defs expr)))

(defn var-count [expr]
  (if (= :var (:op expr))
    {(:var expr) 1}
    (apply merge-with +
           (map var-count (analyze/children expr)))))

(defn check-usage-of-private-vars [exprs]
  (let [v-count (apply merge-with + (map var-count exprs))]
    (doseq [pvar (mapcat private-defs exprs)]
      (when-not (get v-count pvar)
        (println "Private variable" pvar "is never used")))))

(comment
(def analyzed
  (doall
    (map analyze/analyze-ns
       '[clojure.test
         clojure.set
         clojure.java.io
         clojure.stacktrace
         clojure.pprint
         clojure.walk
         clojure.string
         clojure.repl
         clojure.core.protocols
         clojure.template
         clojure.jvm.tools.analyzer.examples.privatevars])))

(doseq [exprs analyzed]
  (check-usage-of-private-vars exprs))
  )
