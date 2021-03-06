;   Copyright (c) Rich Hickey and contributors. All rights reserved.
;   The use and distribution terms for this software are covered by the
;   Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;   which can be found in the file epl-v10.html at the root of this distribution.
;   By using this software in any fashion, you are agreeing to be bound by
;   the terms of this license.
;   You must not remove this notice, or any other, from this software.

(ns cljs.jvm.tools.analyzer
  "Interface to Clojurescript's analyzer.
  Entry point `analyze-path` and `analyze-one`"
  (:require [cljs.analyzer :as ana]
            [cljs.util :as util]
            [cljs.compiler :as comp]
            [cljs.env :as env]
            [clojure.java.io :as io]))

;;;;;;;;;;;;;;;;;;;;;;;;
;; Interface

(defn analyze-form-in-ns 
  "Analyze a single form in namespace nsym."
  ([nsym form] 
   (env/ensure
     (binding [ana/*cljs-ns* nsym]
       (comp/with-core-cljs 
         (ana/analyze (ana/empty-env) form))))))

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

(defn analyze-ns
  "Returns a sequence of abstract syntax trees for each form in
  the namespace."
  [ns]
  (env/ensure
    (let [f (util/ns->relpath ns)
          res (if (re-find #"^file://" f) (java.net.URL. f) (io/resource f))]
      (assert res (str "Can't find " f " in classpath"))
      (binding [ana/*cljs-ns* 'cljs.user
                ana/*cljs-file* (.getPath ^java.net.URL res)]
        (with-open [r (io/reader res)]
          (let [env (ana/empty-env)
                pbr (clojure.lang.LineNumberingPushbackReader. r)
                eof (Object.)]
            (loop [asts []
                   r (read pbr false eof false)]
              (let [env (assoc env :ns (ana/get-namespace ana/*cljs-ns*))]
                (if-not (identical? eof r)
                  (recur (conj asts (ana/analyze env r)) (read pbr false eof false))
                  asts)))))))))

(comment
         (ast 1))
