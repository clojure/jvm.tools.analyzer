# jvm.tools.analyzer: An Interface to Clojure's Analyzer

Clojure's analysis compilation phase holds rich information about Clojure forms, like type/reflection information.

jvm.tools.analyzer provides an interface to this phase, callable a la carte. The output is similar to ClojureScript's analyzer.

Supports Clojure 1.4.0 or later.

# Releases and Dependency Information

Latest stable release is 0.5.2.

Leiningen dependency information:

```clojure
[org.clojure/jvm.tools.analyzer "0.5.2"]

; for very recent releases
:repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
```

Maven dependency information:

```XML
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>jvm.tools.analyzer</artifactId>
  <version>0.5.2</version>
</dependency>
```

# Differences from tools.analyzer.jvm

The tools.analyzer.* libraries are Clojure compilers/analyzers written in Clojure, with tools.analyzer.jvm
targetting the JVM.

This library jvm.tools.analyzer is a set of tools for manipulating the output of Compiler.java, the official
Clojure compiler written in Java as of Clojure 1.5.

# Caveats (of provided Clojure AST analysis)

## Implicit Evalutation

Every AST node is `eval`ed after it is processed by analysis. This is to recognise
scope introduced by `require` and `refer`, and other global side effects.
It follows that analysing a `def` will result in it being redefined as if it were
evaluated.

## Future deprecation

This library will be deprecated if and when a sufficient Clojure-in-Clojure compiler
is implemented. For now, `jvm.tools.analyzer` is probably your best bet for libraries
you want to build *today*.

## Fragile Implementation

The implementation consists of reflective calls to the Clojure JVM Compiler to extract
AST data. It should work with Clojure 1.4.0 or later, but there may be subtle
changes in c.l.Compiler.java which we don't account for. It is optimised to support the latest
versions of Clojure (1.5.1, as of 8 April 2013).

## Non-standard AST

The shape of the AST map is exactly based on the Compiler's internal representation. No effort
has been made to conform to a ClojureScript-like AST. In practice, the main differences are:

- local bindings are wrapped in a `:local-binding-expr` node
- there are several AST nodes for constants (eg. `:constant`, `:nil`, `:empty-expr`)
- several interop nodes (no :dot)
- some ops/fields have different names

I highly recommend browsing the implementation of `clojure.tools.analyzer` to check the current
state of the AST. It should be familiar if you have experience with the ClojureScript analyzer.

# Usage (Clojure)

## Generating AST from syntax

Note: Column numbers are only supported with Clojure 1.5.0 or later.

```clojure

clojure.tools.analyzer=> (ast [1])
{:op :constant, :env {:locals {}, :ns {:name clojure.tools.analyzer}}, :val [1]}

clojure.tools.analyzer=> (-> (ast (if true 1 2)) clojure.pprint/pprint)
{:op :if,
 :env
 {:column 10,
  :line 4,
  :locals {},
  :ns {:name clojure.tools.analyzer}},
 :test
 {:op :boolean,
  :env {:locals {}, :ns {:name clojure.tools.analyzer}},
  :val true},
 :then
 {:op :number,
  :env {:locals {}, :ns {:name clojure.tools.analyzer}},
  :val 1},
 :else
 {:op :number,
  :env {:locals {}, :ns {:name clojure.tools.analyzer}},
  :val 2}}
nil

clojure.tools.analyzer=> (-> (ast (fn [x] (+ x 1))) clojure.pprint/pprint)
{:op :fn-expr,
 :env {:line 5, :locals {}, :ns {:name clojure.tools.analyzer}},
 :methods
 ({:op :fn-method,
   :env {:locals {}, :ns {:name clojure.tools.analyzer}},
   :body
   {:op :do,
    :env
    {:source "REPL",
     :column 18,
     :line 5,
     :locals {},
     :ns {:name clojure.tools.analyzer}},
    :exprs
    ({:op :static-method,
      :env
      {:source "REPL",
       :column 18,
       :line 5,
       :locals {},
       :ns {:name clojure.tools.analyzer}},
      :class clojure.lang.Numbers,
      :method-name "add",
      :method
      {:name add,
       :return-type java.lang.Number,
       :declaring-class clojure.lang.Numbers,
       :parameter-types [java.lang.Object long],
       :exception-types [],
       :flags #{:static :public}},
      :args
      ({:op :local-binding-expr,
        :env {:locals {}, :ns {:name clojure.tools.analyzer}},
        :local-binding
        {:op :local-binding,
         :env {:locals {}, :ns {:name clojure.tools.analyzer}},
         :sym x,
         :tag nil,
         :init nil},
        :tag nil}
       {:op :number,
        :env {:locals {}, :ns {:name clojure.tools.analyzer}},
        :val 1}),
      :tag nil})},
   :required-params
   ({:op :local-binding,
     :env {:locals {}, :ns {:name clojure.tools.analyzer}},
     :sym x,
     :tag nil,
     :init nil}),
   :rest-param nil}),
 :variadic-method nil,
 :tag nil}
nil
```

## Syntax from AST


```clojure
clojure.tools.analyzer=> (require '[clojure.tools.analyzer.emit-form :as e])
nil
clojure.tools.analyzer=> (-> (ast 1) e/emit-form)
1
clojure.tools.analyzer=> (-> (ast [(+ 1 2)]) e/emit-form)
[(clojure.lang.Numbers/add 1 2)]
```

# Macroexpander

Use `clojure.tools.analyzer/macroexpand` as a substitute
for `macroexpand` for fully macroexpanding forms.

`clojure.tools.analyzer.hygienic/macroexpand` returns a hygienic form.

# Known Issues

## Evaluating forms

Currently the analyzer evaluates each form after it is analyzed.

## Incorrect handling of Var mappings within the same form

`analyze` is a thin wrapper over `clojure.lang.Compiler`, so to get our
hands on analysis results some compromises are made.

The following form normally evaluates to the Var `clojure.set/intersection`, but
analyses to `clojure.core/require`.


```clojure
;normal evaluation
(eval
 '(do 
    (require '[clojure.set])
    (refer 'clojure.set 
           :only '[intersection] 
           :rename '{intersection require})
    require))
;=> #'clojure.set/intersection

;analysis result
(-> (ast 
      (do (require '[clojure.set])
        (refer 'clojure.set 
               :only '[intersection] 
               :rename '{intersection require})
        require))
  :exprs last :var)
;=> #'clojure.core/require
```

# Usage (Clojurescript)

All vars are identical to the Clojure implementation, where relevant,
except the namespace prefix is `cljs.tools.analyzer` instead of
`clojure.tools.analyzer`.

Some examples:

Normal AST generation:

```clojure
(cljs.tools.analyzer/ast 1)
;=> {:op :constant, :env {:ns {:defs {a {:column 18, :line 2, :file nil, :name cljs.user/a}}, :name cljs.user}, :context :statement, :locals {}}, :form 1}
```

Hygienic transformation:

```clojure
(cljs.tools.analyzer.hygienic/macroexpand
  '(let [a 1 a a b a a a] a))
;=> (let* [a 1 a11306 a b a11306 a11307 a11306] (do a11307))
```

# Developer Information

- [Github Project](https://github.com/clojure/jvm.tools.analyzer)
- [Bug Tracker](http://dev.clojure.org/jira/browse/JVMTA)
- [Continuous Integration](http://build.clojure.org/job/jvm.tools.analyzer/)
- [Compatibility Test Matrix](http://build.clojure.org/job/jvm.tools.analyzer-test-matrix/)

# Todo

- analyze a leiningen `project.clj` file
- analyze `clojure.core`
- use :locals if necessary

# Examples

See `clojure.tools.analyzer.examples.*` namespaces.

# Contributors

- Jonas Enlund (jonase)
- Nicola Mometto (Bronsa)
- Chris Gray (chrismgray)

## License

Copyright © Ambrose Bonnaire-Sergeant, Rich Hickey & contributors.

Licensed under the EPL (see the file epl.html).
