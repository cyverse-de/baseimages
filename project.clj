(defproject org.cyverse/dummy "0.0.0."
  :description "This project.clj file is only here for the sake of installing some dependencies"
  :url "https://github.com/cyverse-de/baseimages"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [cheshire "5.6.3"]
                 [clj-http "3.4.1"]
                 [medley "0.8.4"]
                 [slingshot "0.12.2"]]
  :plugins [[jonase/eastwood "0.2.3"]
            [test2junit "1.2.2"]]
  :eastwood {:exclude-namespaces [:test-paths]
             :linters [:wrong-arity :wrong-ns-form :wrong-pre-post :wrong-tag :misplaced-docstrings]})
