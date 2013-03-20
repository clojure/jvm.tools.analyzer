(ns clojure.tools.analyzer.examples.side-effect
  "Warns on invocations of `set!` inside transactions.
  Entry point `forbid-side-effects-in-transaction`"
  (:require [clojure.tools.analyzer :as analyze]
            [clojure.reflect :as reflect]))

(def transaction-method
  "dosync reduces to a call to this method"
  (let [membrs (-> (reflect/reflect clojure.lang.LockingTransaction) :members)]
    (first (filter #(= 'runInTransaction (:name %)) membrs))))

(defn warn-on-side-effect [exp]
  (when (= :set! (:op exp))
    (binding [*out* *err*]
      (println "WARNING: Side effect in transaction")))
  (doseq [child-exp (:children exp)]
    (warn-on-side-effect child-exp)))

(defn forbid-side-effects-in-transaction [exp]
  (when (and (= :static-method (:op exp))
             (= transaction-method (:method exp)))
    (warn-on-side-effect (first (:args exp))))
  (doseq [child-exp (:children exp)]
    (forbid-side-effects-in-transaction child-exp)))

;; Examples

;; Check a chunk of the core library

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
                clojure.template])))

(doseq [exprs analyzed
        exp exprs]
  (forbid-side-effects-in-transaction exp))

;; Check individual form

(do
  (reset! analyze/CHILDREN true)
  (forbid-side-effects-in-transaction
    (analyze/analyze-one '{:ns {:name clojure.core} :context :eval}
                         '(dosync 
                            (do 
                              (fn [] (set! *ns* 'ww)) ; TODO need context information from compiler, or to find it
                              (set! *ns* 'ss)
                              (set! *ns* 'blah))))))
  )
