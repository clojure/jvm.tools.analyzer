(ns clojure.jvm.tools.analyzer.test.records)

(defrecord MyRecord [a])

(let [^clojure.jvm.tools.analyzer.test.records.MyRecord r (->MyRecord 1)]
  (.a r))
