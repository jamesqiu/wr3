(ns wr3.clj.file)

(import wr3.util.Filex java.io.IOException)
(import [java.io File InputStreamReader FileInputStream BufferedReader])

;;;----------------- 对文件每行进行处理

(defmacro line-filter-java
  "直接采用java的读取方式，配合loop可以做到最高的读取效率 "
  [fname enc & body]
  `(with-open [~'fis (FileInputStream. ~fname)
               ~'isr (InputStreamReader. ~'fis ~enc)
               ~'br (BufferedReader. ~'isr) ]
     ~@body))

(defn test-line-filter-java
  "处理260多万行的文档 "
  []
  (line-filter-java  
    "f:/cust2.txt" "utf-8"
    (loop [i 0] (if-let [line (.readLine br)] 
                  (recur (if (.startsWith line "005") (inc i) i))
                  (println i)))))
;(test-line-filter-java)

(defn- line-filter-enc
  "按enc读入文件的每行，对行进行处理。
  fname: 文件路径及名称;
  func:  处理行的函数，带一个参数为当前行，如： (fn [l] (println l)) "
  [fname enc func]
  (with-open [rdr (clojure.java.io/reader fname :encoding enc)]
    (doseq [line (line-seq rdr)] (func line))))

(defn line-filter
  "使用GBK编码来处理文件的每一行
  @see line-filter-enc"
  [fname func]
  (line-filter-enc fname "GBK" func))

(defn line-filter-utf8
  "使用UTF-8编码来处理文件的每一行
  @see line-filter-enc"
  [fname func]
  (line-filter-enc fname "UTF-8" func))

(defn file-text
  "获取文本文件所有内容的String
  @fname 文件路径及文件名，web下用 (.getRealPath request '/file/foo.txt') 
  @enc 'UTF-8' 'GB18030' 无此参数则采用系统缺省编码 "
  ([fname] (Filex/read fname))
  ([fname enc] (try (Filex/getFileText fname enc) (catch IOException e (.printStackTrace e)))))

(defn file-set-text
  "将String的内容写入文本文件"
  ([fname s] (Filex/write fname s)) ; 采用系统编码
  ([fname s enc] (try (Filex/setFileText fname s enc) (catch IOException e (.printStackTrace e)))))
    
(defn file-rename
  "将文件ffrom更名为fto
  @from 原始文件的全路径名
  @fto 新名字文件的全路径名"
  [ffrom fto]
  (try (.renameTo (File. ffrom) (File. fto)) (catch Exception e (println e))))

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

