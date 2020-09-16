;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.jvm.tools.analyzer.examples.docstring
  "Warns on suspected misplaced docstrings in function definitions.
  Entry point `find-and-check-defs`"
  (:require [clojure.jvm.tools.analyzer :as analyze]))

(defn check-def [exp]
  (when (= :fn-expr (-> exp :init :op))
    (doseq [method (-> exp :init :methods)]
      (let [body (:body method)]
        (when (and (= :do (:op body))
                   (< 1 (count (-> body :exprs))))
          (let [first-exp (-> body :exprs first)]
            (when (= :string (:op first-exp))
              (binding [*out* *err*]
                (println "WARNING: Suspicious string, possibly misplaced docstring," (-> exp :var))))))))))

(defn find-and-check-defs [exp]
  (when (= :def (:op exp))
    (check-def exp))
  (doseq [child-exp (analyze/children exp)]
    (find-and-check-defs child-exp)))

;; Examples

;; Check a good chunk of the core library

(comment
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
  (find-and-check-defs exp))

;; One form at a time

  (find-and-check-defs
    (analyze/analyze-one {:ns {:name 'clojure.repl} :context :eval}
                         '(defn a []
                            "asdf"
                            (+ 1 1))
                         {:children true}))
  )
