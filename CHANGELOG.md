# Changelog

0.3.2
- Add column numbers for Clojure 1.5.0+
- Remove `jvm` from namespaces
- Add `map->form` case

0.3.0
- New Hygienic transformation namespace, `analyze.hygiene`
  - `ast-hy`, AST -> hygienic AST
  - `emit-hy`, hygienic AST -> hygienic form
- `map->form` now extensible
- Fix emitting variadic method twice in `map->form` case for :fn-expr

0.2.6
- More macroexpansion cases
- `eval` forms after analysing them
- changes to :deftype*

0.2.5
- More cases for `map->form`
- Fix :fn-expr case for `map->form`, now emits `fn*` instead of `fn`

0.2.4
- More cases for `map->form`

0.2.3
- Add `analyze.emit-form/map->form`
- Support Clojure 1.4.0+

0.2.2
- Revert to not `eval`ing forms before analysing

0.2.1
- `eval` forms before analysing them

