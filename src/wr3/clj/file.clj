(ns wr3.clj.file)

(import wr3.util.Filex java.io.IOException)

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

(defn file-text
  "获取文本文件所有内容的String
  @fname 文件路径及文件名，web下用 (.getRealPath request '/file/foo.txt') 
  @enc 'UTF-8' 'GB18030' 无此参数则采用系统缺省编码 "
  ([fname] (Filex/read fname))
  ([fname enc] (try (Filex/getFileText fname enc) (catch IOException e (.printStackTrace e)))))

(defn file-set-text
  "将String的内容写入文本文件"
  ([fname s] (Filex/write fname s))
  ([fname s enc] (try (Filex/setFileText fname s enc) (catch IOException e (.printStackTrace e)))))
    
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

