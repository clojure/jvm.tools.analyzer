(ns cljs.tools.analyzer
  "Interface to Clojurescript's analyzer.
  Entry point `analyze-path` and `analyze-one`"
  (:require [cljs.analyzer :as ana]
            [cljs.compiler :as comp]
            [cljs.core]))

;;;;;;;;;;;;;;;;;;;;;;;;
;; Interface

(defn ^:private empty-env-in-ns [nsym]
  (let [info (@ana/namespaces nsym)]
    (assert info (str "Namespace " nsym " does not exist"))
    {:ns info :context :statement :locals {}}))

(defn analyze-form-in-ns 
  "Analyze a single form in namespace nsym."
  ([nsym form] 
   (comp/with-core-cljs 
     (ana/analyze (empty-env-in-ns nsym) form))))

(defn analyze-form 
  "Analyze a single form in the current namespace."
  ([form] (analyze-form-in-ns ana/*cljs-ns* form)))

(defmacro ast-in-ns
  "Returns the abstract syntax tree representation of the given form,
  evaluated in the given namespace"
  ([nsym form] `(analyze-form-in-ns '~nsym '~form)))

(defmacro ast 
  "Returns the abstract syntax tree representation of the given form,
  evaluated in the current namespace"
  ([form] `(analyze-form '~form)))

(comment
         (ast 1))
