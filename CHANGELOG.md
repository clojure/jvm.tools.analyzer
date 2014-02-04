# 0.6.1

- Upgrade ClojureScript to 0.0-2138

# 0.6.0

## (MAJOR!) BREAKING CHANGES

- Rename all `clojure.tools.analyzer.*` namespace prefixs to `clojure.jvm.tools.analyzer.*`
  - Includes CLJS: `cljs.tools.analyzer` -> `cljs.jvm.tools.analyzer`
  - Migration path: immediately rename your references to the old namespaces
    (there is no migration path)
  - This change is urgent and necessary to eliminate stepping on the new contribs 
    `clojure.tools.analyzer` and `clojure.tools.analyzer.jvm`

## Enhancements

- Add dynamic var `clojure.jvm.tools.analyzer/*eval-ast*` to toggle automatic evaluation
  of AST's
  - defaults to evaluating the AST after analysis

# 0.5.2 - Released 1 October 2013

- Fix expr-seq

# 0.5.1 - Released 22 September 2013

## BREAKING CHANGES

- Change interface to analyze-ns
  - takes a namespace symbol and options. See docstring

## ENHANCEMENTS

- add hinted fields to deftype*
- add analyze-file
- add :source-path to env, the current file being analyzed

# 0.4.3

- Support Clojurescript

# 0.4.2

- Add set! to map->form

# 0.4.1

- Remove tools.trace for Maven builds (still in project.clj)

# 0.4.0

- Add macroexpanders
- Increment minor version because of breaking changes to :children

# 0.3.5

## BREAKING CHANGES

- Eliminate CHILDREN and JAVA-OBJ global state
  - AST ops now accept an optional map of options
  eg. `(ast (let [a 1] a) {:children true})`
- Experimental approach to children keys
  - :children is a vector of vector key-value pairs (similar in structure to an ordered map)
  - keys are a vector representing the path to the child expression, for use with `get-in`
  - values are a map of options
    - empty options indicate the child is a normal expression
    - :exprs? set to `true` indicates child is a sequence of expressions

eg. :children of the :if node

```clojure
[[[:the-expr]  {}]
 [[:tests] {:exprs? true}] 
 [[:thens] {:exprs? true}] 
 [[:default] {}]]})
```

eg. How to iterate over these children

```clojure
(let [expr (ast (let [a 1] a) {:children true})]
  (for [[path {:keys [exprs?]}] (:children expr)
        :let [in (get-in expr path)]
        child-expr (if exprs?
                     in
                     [in])]
    ;child-expr here is the child expr
    ))
```

# 0.3.2

- Add column numbers for Clojure 1.5.0+
- Remove `jvm` from namespaces
- Add `map->form` case

# 0.3.0

- New Hygienic transformation namespace, `analyze.hygiene`
  - `ast-hy`, AST -> hygienic AST
  - `emit-hy`, hygienic AST -> hygienic form
- `map->form` now extensible
- Fix emitting variadic method twice in `map->form` case for :fn-expr

# 0.2.6

- More macroexpansion cases
- `eval` forms after analysing them
- changes to :deftype*

# 0.2.5

- More cases for `map->form`
- Fix :fn-expr case for `map->form`, now emits `fn*` instead of `fn`

# 0.2.4

- More cases for `map->form`

# 0.2.3

- Add `analyze.emit-form/map->form`
- Support Clojure 1.4.0+

# 0.2.2

- Revert to not `eval`ing forms before analysing

# 0.2.1

- `eval` forms before analysing them
