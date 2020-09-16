;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.jvm.tools.analyzer.examples.dynvars
  (:require [clojure.jvm.tools.analyzer :as analyze]))

(defn earmuffed? [sym]
  (let [s (name sym)]
    (and (< 2 (count s))
         (.startsWith s "*")
         (.endsWith s "*"))))

(defn check-def [expr]
  (let [^clojure.lang.Var v (:var expr)
        s (.sym v)]
    (when (and (earmuffed? s)
               (not (:is-dynamic expr)))
      (println "WARNING: Should" v "be marked dynamic?"))))

(defn find-and-check-defs [expr]
  (when (= :def (:op expr))
    (check-def expr))
  (doseq [child-expr (analyze/children expr)]
    (find-and-check-defs child-expr)))

(comment

  (find-and-check-defs
    (analyze/analyze-one {:ns {:name 'user} :context :eval}
                         '(def *a* 1)
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
                clojure.template])))

(doseq [exprs analyzed
        exp exprs]
  (find-and-check-defs exp))
  )
