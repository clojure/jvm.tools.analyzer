(ns clojure.tools.analyzer.emit-form
  (:require [clojure.core.typed :refer [ann typed-deps check-ns tc-ignore fn> ann-form print-env]]
            [clojure.tools.analyzer.types :refer [Expr]])
  (:import (clojure.lang Var Keyword Symbol)))

(ann emit-default Keyword)
(def emit-default ::emit-default)

(ann ^:nocheck derive-emit-default [Keyword -> nil])
(defn derive-emit-default [tag]
  (derive tag emit-default))

(declare map->form)

(ann emit-form [Expr -> Any])
(defn emit-form 
  "Return the form represented by the given AST."
  [expr]
  (map->form expr ::emit-default))

(print-env "before defmulti")

(ann map->form [Expr Keyword -> Any])
(defmulti map->form (fn [expr mode]
                      (let [_ (:op expr)]
                        (print-env "inside")
                        [(:op expr) mode])))

(print-env "after defmulti")

(defmacro map->form-default
  "Installs a method to map->form with the default dispatch.
  Also implicitly adds `mode` and `map->form` to local scope.
  `mode` is the current mode (a keyword) and `map->form` aliases
  map->form except it provides a 1-arity case that reuses the current
  mode. ie. (map->form f mode)"
  [disp args & body]
  (assert (keyword? disp))
  (assert (#{1} (count args)))
  (let [mode 'mode]
    `(defmethod map->form [~disp emit-default]
       [~(first args) ~mode]
       (let [~'map->form (fn>
                           ([f# :- ~'Expr] (map->form f# ~mode))
                           ([f# :- ~'Expr, custom-mode# :- ~'Keyword] (map->form f# custom-mode#)))]
         ~@body))))

(map->form-default :number [{:keys [val] :as expr}] val)
(map->form-default :constant [{:keys [val]}] (list 'quote val))
(map->form-default :string [{:keys [val]}] val)
(map->form-default :boolean [{:keys [val]}] val)
(map->form-default :keyword [{:keys [val]}] val)

(map->form-default :static-method
  [{:keys [^Class class method-name args] :as expr}]
  #_(ann-form expr Nothing)
  `(~(symbol (.getName class) (str method-name))
        ~@(map map->form args)))

(map->form-default :static-field
  [{:keys [^Class class field-name]}]
  (symbol (.getName class) (str field-name)))

(map->form-default :instance-field
  [{:keys [target field-name]}]
  (ann-form target Expr)
  (list '. (map->form target) (symbol field-name)))

(map->form-default :invoke
  [{:keys [fexpr args]}]
  `(~(map->form fexpr)
       ~@(map map->form args)))

(ann ^:nocheck var->symbol [Var -> Symbol])
(defn- var->symbol [^Var var]
  (symbol (str (ns-name (.ns var))) (str (.sym var))))

(map->form-default :the-var
  [{:keys [var]}]
  (list 'var (var->symbol var)))

(map->form-default :var
  [{:keys [var]}]
  (var->symbol var))

(map->form-default :instance-method
  [{:keys [target method-name args]}]
  `(~(symbol (str "." method-name))
       ~(map->form target)
       ~@(map map->form args)))

(map->form-default :new
  [{:keys [^Class class args]}]
  `(new ~(symbol (.getName class))
        ~@(map map->form args)))

(ann ^:nocheck clojure.core/hash-map (All [x] [x * -> (clojure.lang.PersistentHashMap x x)]))


(map->form-default :empty-expr [{:keys [coll]}] coll)
(map->form-default :vector [{:keys [args]}] (vec (map map->form args)))
(map->form-default :map [{:keys [keyvals]}] (apply hash-map (map map->form keyvals)))
(map->form-default :set [{:keys [keys]}] (set (map map->form keys)))

(ann ^:nocheck clojure.core/list* [Any * -> (clojure.lang.IPersistentList Any)])

(map->form-default :fn-expr
  [{:keys [name methods]}]
  (list* 'fn* 
         (concat
           (when name
             [name])
           (map map->form methods))))

(map->form-default :fn-method
  [{:keys [body required-params rest-param]}]
  `(~(vec (concat (map map->form required-params)
                  (when rest-param
                    ['& (map->form rest-param)])))
       ~(map->form body)))

(map->form-default :do
  [{:keys [exprs]}]
  (cond
    (empty? exprs) nil
    (= 1 (count exprs)) (map->form (first exprs))
    :else `(do ~@(map map->form exprs))))

(map->form-default :let
  [{:keys [is-loop binding-inits body]}]
  `(~(if is-loop
       'loop*
       'let*)
       ~(vec (apply concat (map map->form binding-inits)))
       ~(map->form body)))

(map->form-default :letfn
  [{:keys [binding-inits body]}]
  `(~'letfn*
       ~(vec (apply concat (map map->form binding-inits)))
       ~(map->form body)))

(map->form-default :recur
  [{:keys [args]}]
  `(recur ~@(map map->form args)))
          
;to be spliced
(map->form-default :binding-init
  [{:keys [local-binding init]}]
  (map map->form [local-binding init]))

(map->form-default :local-binding [{:keys [sym]}] sym)
(map->form-default :local-binding-expr [{:keys [local-binding]}] (map->form local-binding))

(map->form-default :if
  [{:keys [test then else]}] 
  `(if ~@(map map->form [test then else])))

(map->form-default :instance-of
  [{:keys [^Class class the-expr]}] 
  `(clojure.core/instance? ~(symbol (.getName class))
                           ~(map->form the-expr)))

(map->form-default :def
  [{:keys [^Var var init init-provided]}]
  `(def ~(.sym var) ~(when init-provided
                       (map->form init))))

(ann ^:nocheck clojure.core/partition-by 
     (All [x] [[x -> Any] (clojure.lang.Seqable x) -> (clojure.lang.LazySeq (clojure.lang.Seqable x))]))

;FIXME: methods don't print protocol/interface name
(map->form-default :deftype*
  [{:keys [name methods fields covariants ^Class compiled-class]}]
  (list* 'deftype* 
         (symbol (apply str (last (partition-by #{\.} (str name)))))
         name
         ;FIXME these should be hinted fields
         (vec (map map->form fields))
         :implements
         ;FIXME interfaces implemented
         []
         (map map->form methods)))

(map->form-default :new-instance-method
  [{:keys [name required-params body]}]
  (list name (vec (map map->form required-params))
        (map->form body)))

(map->form-default :import*
  [{:keys [class-str]}]
  (list 'import* class-str))

(map->form-default :keyword-invoke
  [{:keys [kw target]}] 
  (list (map->form kw) (map->form target)))

(map->form-default :throw
  [{:keys [exception]}] 
  (list 'throw (map->form exception)))

(map->form-default :try
  [{:keys [try-expr catch-exprs finally-expr]}] 
  (list* 'try (map->form try-expr)
         (concat
           (map map->form catch-exprs)
           (when finally-expr [(list 'finally (map->form finally-expr))]))))

(map->form-default :catch
  [{:keys [^Class class local-binding handler]}]
  (list 'catch (symbol (.getName class))
        (map->form local-binding) 
        (map->form handler)))

;; (from Compiler.java)
;;  //(case* expr shift mask default map<minhash, [test then]> table-type test-type skip-check?)
(map->form-default :case*
  [{:keys [the-expr tests thens default tests-hashes shift mask low high switch-type test-type skip-check]}]
  (list 'case*
        (map->form the-expr)
        shift
        mask
        (map->form default)
        (zipmap tests-hashes
                (map vector
                     (map map->form tests)
                     (map map->form thens)))
        switch-type
        test-type
        skip-check))

(ann ^:nocheck clojure.core/resolve 
     [clojure.lang.Symbol -> (U nil clojure.lang.Var Class)])

(assert (not (resolve 'mode))
        "Don't use mode as a var in this namespace: it is implicitly
        a local in map->form methods")

(comment
  (defmacro frm [f]
    `(-> (ast ~f) emit-form))

  (frm 1)
  (frm :a)
  
  (frm (+ 1 2))
  (frm (- 1 2))

  (frm (apply - 1 2))

  (frm (.var - 1 2))

  (frm (Integer. 1))

  (frm ())

  (frm [1])
  (frm [(- 1)])
  (frm {(- 1) 1})
  (frm #{(- 1) 1})

  (frm (let [a '(1 2)]
         (first 1)))
  (frm (loop [a '(1 2)]
         (first 1)))

  (frm (fn [{:keys [a]} b] 1))
  (frm (instance? Class 1))

  (frm nil)
  (frm (def a 1))
  (frm (defn ab [a] a))

  (frm (loop [a 1] (recur 1)))

  ; FIXME
  (frm (deftype A []
         clojure.lang.ISeq
         (first [this] this)))

  (frm (:a {}))
  (frm (throw (Exception. "a")))
  (frm (try 1 2 
         (catch Exception e 
           4)
         (catch Error e
           5)
         (finally 3.2)))
  (frm (Integer/toHexString 1))
  (frm (Integer/TYPE))
  (frm #'conj)
  
  (frm 'a)
  (frm (let [b 1] 
         [b 'a 1]))

  (frm #{1 2 3})
  (frm (case 1 2 3 4))
  (frm (case 1 :a 3 4))

  (frm (deftype A [a b]
         Object
         (toString [this])))
  (macroexpand
    '(deftype A [a b]
       Object
       (toString [this])))

   (frm (letfn [(a [b] b)
                (b [a] a)
                (c [c] a)]
          (a b c)))

  ;instance field
   (frm (.a 1))
  ;instance method
   (frm (.cancel (java.util.concurrent.FutureTask. #()) 1))
)
