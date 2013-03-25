Clojars Dependency: `[org.clojure/jvm.tools.analyzer "0.3.2"]`

# Interface to Clojure's Analyzer

Clojure's analysis compilation phase holds rich information about Clojure forms, like type/reflection information.

_analyze_ provides an interface to this phase, callable a la carte. The output is similar to ClojureScript's analyzer.

Supports Clojure 1.4.0 or later.

## Releases and Dependency Information

Latest stable release is 0.3.2.

Leiningen dependency information:

```clojure
[org.clojure/jvm.tools.analyzer "0.3.2"]
```

Maven dependency information:

```XML
<dependency>
  <groupId>org.clojure</groupId>
  <artifactId>jvm.tools.analyzer</artifactId>
  <version>0.3.2</version>
</dependency>
```

# Usage

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
