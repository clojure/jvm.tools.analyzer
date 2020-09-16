;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns clojure.jvm.tools.analyzer.examples.reflection
  "Same as *warn-on-reflection*"
  (:require [clojure.jvm.tools.analyzer :as analyze]))

(defn check-new [exp]
  (when (not (:ctor exp))
    (println "WARNING: Unresolved constructor" (:class exp) (-> exp :env :ns :name))))

(defn check-static-method [exp]
  (when (not (:method exp))
    (println "WARNING: Unresolved static method" (:method-name exp) (:class exp) (-> exp :env :ns :name))))

(defn check-instance-method [exp]
  (when (not (:method exp))
    (println "WARNING: Unresolved instance method" (:method-name exp) (:class exp) (-> exp :env :ns :name))))

(defn check-static-field [exp]
  (when (not (:field exp))
    (println "WARNING: Unresolved static field" (:field-name exp) (:class exp) (-> exp :env :ns :name))))

(defn check-instance-field [exp]
  (when (not (:field exp))
    (println "WARNING: Unresolved instance field" (:field-name exp) (:class exp) (-> exp :env :ns :name))))


(defn check-for-reflection [exp]
  (condp = (:op exp)
    :new (check-new exp)
    :static-method (check-static-method exp)
    :instance-method (check-instance-method exp)
    :static-field (check-static-field exp)
    :instance-field (check-instance-field exp)
    nil)

  (doseq [c (analyze/children exp)]
    (check-for-reflection c)))

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
              (repeat {:children true}))))

(doseq [exprs analyzed
        exp exprs]
  (check-for-reflection exp))

(analyze/analyze-one {:ns {:name 'clojure.core} :context :eval} 
                     '(Integer. (+ 1 1))
                     {:children true})
(analyze/analyze-one {:ns {:name 'clojure.core} :context :eval} 
                     '(Integer. (+ 1 1))
                     {:children true})
(analyze/analyze-one {:ns {:name 'clojure.core} :context :eval} 
                     '(Integer. (+ 1 (even? 1)))
                     {:children true})
)
