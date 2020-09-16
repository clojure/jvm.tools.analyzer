;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.jvm.tools.analyzer.examples.nsforms
  (:require [clojure.jvm.tools.analyzer :as analyze]))

(defn warn-on-naked-use [use-expr]
  (doseq [s (map :val (:args use-expr))
          :when (symbol? s)]
    (println "Warning: Naked use of" (name s) "in" (-> use-expr :env :ns :name))))

(defn use? [expr]
  (and (= :invoke (:op expr))
       (= :var (-> expr :fexpr :op))
       (= #'use (-> expr :fexpr :var))))

(defn find-and-analyze-use-forms [expr]
  (when (use? expr)
    (warn-on-naked-use expr))
  (doseq [child-expr (analyze/children expr)]
    (find-and-analyze-use-forms child-expr)))

(comment

  (find-and-analyze-use-forms
    (analyze/ast
      (ns sjfis (:use [clojure.set :only [union]]
                      clojure.repl))
      {:children true}))


  (def analyzed
    (doall (map analyze/analyze-ns
                '[clojure.test
                  clojure.set
                  clojure.java.io
                  clojure.stacktrace
                  clojure.pprint
                  clojure.walk
                  clojure.string
                  clojure.repl
                  clojure.core.protocols
                  clojure.template]
                (repeat '{:children true}))))

  (doseq [exprs analyzed
          exp exprs]
    (find-and-analyze-use-forms exp))
  )
