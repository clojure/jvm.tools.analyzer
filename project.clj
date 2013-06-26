(defproject org.clojure/jvm.tools.analyzer "0.4.1-SNAPSHOT"
  :description "Interface to Clojure Analyzer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.trace "0.7.3"]
                 [org.clojure/clojurescript "0.0-1586"]
                 [org.clojure/core.typed "0.1.18-SNAPSHOT"
                  :exclude [org.clojure/tools.namespace
                            org.clojure/jvm.tools.analyzer]]
                 [org.clojure/tools.namespace "0.2.4-SNAPSHOT"]]
  
  ;:repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}

  :profiles {:dev {:repl-options {:port 64363}}}

  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"])
