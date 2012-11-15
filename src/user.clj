; Clojure repl启动后执行在classpath下的user.clj
(print "执行user.clj: ")

(defn cljh 
  "usage: 在repl中敲(cljh)"
  []
  (println "--- 恢复命名空间：
(refer 'clojure.core :exclude '(map set))    ; 恢复除map，set外的所有命名空间
(refer 'clojure.core :only '(println prn))   ; 恢复println和prn的命名空间
  ")
  (println "--- 引用其他Clojure包：
(require 'clojure.contrib.math) ; '不能少
(require '[clojure.contrib.math :as m]) ; 使用alias简写

(use 'clojure.contrib.math)
(use '[clojure.contrib.math :only (round floor ceil)]) ; '[]不能少
(use '[clojure.contrib.str-utils :only (str-join)])

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.file 'wr3.clj.tb)
  ")
  (println "--- 引用Java类：
(import java.io.File)
(import [java.io File InputStream] [java.util Random])
  ")
  )

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.file); 'wr3.clj.tb)
(println "(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.file)")
(println "ok! (cljh) (class-methods) for more...")
