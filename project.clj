(defproject org.clojure/jvm.tools.analyzer "0.3.5-SNAPSHOT"
  :description "Interface to Clojure Analyzer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/tools.trace "0.7.3"]
                 [org.clojure/clojurescript "0.0-1586"]]
  
  ;:repositories {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}

  :source-paths ["src/main/clojure"]
  :test-paths ["src/test/clojure"])
