(ns clojure.tools.analyzer.test.records)

(defrecord MyRecord [a])

(let [^clojure.tools.analyzer.test.records.MyRecord r (->MyRecord 1)]
  (.a r))
