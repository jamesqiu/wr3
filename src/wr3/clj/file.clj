(ns wr3.clj.file)

;;;----------------- 对文件每行进行处理
(defn- line-filter-enc
  "按enc读入文件的每行，对行进行处理。
  fname: 文件路径及名称;
  func:  处理行的函数，带一个参数为当前行，如： (fn [l] (println l)) "
  [fname func enc]
  (with-open [rdr (clojure.java.io/reader fname :encoding enc)]
    (doseq [line (line-seq rdr)] (func line))))

(defn line-filter
  "使用GBK编码来处理文件的每一行
  @see line-filter-enc"
  [fname func]
  (line-filter-enc fname func "GBK"))

(defn line-filter-utf8
  "使用UTF-8编码来处理文件的每一行
  @see line-filter-enc"
  [fname func]
  (line-filter-enc fname func "UTF-8"))

;;--- usage:
;(def n (atom 0))
;(defn- f1 [line] (swap! n inc))
;(defn- f2 [line] (when (.startsWith line "北京") (do (println line) (swap! n inc))))
;(line-filter "f:/lib/Clojure/test/gbk.txt" f2)
;(println "n =" @n)

;;;-------------------- 处理classpath路径下（含jar包中）的文件
(defn read-resource
  "处理classpath路径下（含jar包中）的文件.
  @resource 资源文件的路径.
  @f 带Reader参数的处理函数"
  [resource f]
  (with-open [r (-> resource
                  ClassLoader/getSystemResourceAsStream
	                java.io.InputStreamReader.
	                java.io.BufferedReader.)]
    (f r)))

