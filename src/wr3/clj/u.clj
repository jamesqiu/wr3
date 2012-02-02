;;;; utility函数，和其他语言对应的函数补充，算法函数，日期时间函数
(ns wr3.clj.u)

(defn permutations
  "得到集合coll的全排列
   算法：排列abcd，就是把每一个元素(a b c d)轮流拿出来排在余下元素(bcd acd abd abc)全排列各项最前面"
  [coll]
  (let [rm (fn [c e] (filter #(not= e %) c))] ; 从集合c中移除元素e
    (if (empty? coll) (list nil)
      (mapcat (fn [e] (map (fn [c] (cons e c)) (permutations (rm coll e)))) coll))))

(defn combines
  "得到集合coll的所有组合
   算法：'[a b e f]的组合就是0001～1111的所有二进制数对应的组合(该位为1就是选取，为0就是不选）"
  [coll]
  (let [N (count coll)
        upp (bit-shift-left 1 N) ; 上界，如[a b c d] 是 16
        c1 (fn [n] (for [i (range N) :when (bit-test n i)] (nth coll i))) ; 根据数字得到一个组合 2r1001 -> [a d]
        ]
    (map c1 (range 1 upp))))

;;;------------------ 和其他语言对应的函数名alias

(def size count)
(def len count)
(def length count)
(def ++ inc)
(def -- dec)

; (reduce [f val coll]) 就是 Scala 的 foldLeft
(def fold-left reduce)

(defn fold-right
  "和 Scala 的 foldRight 一样"
  [f initval coll]
  (reduce #(f %2 %1) (reverse (cons initval coll))))

(defn all-true?
  "所有项为true才返回true"
  [& conds]
  (every? #(true? %) conds))

(defn find-first
  "查找集合中符合条件的第一个元素"
  [pred coll]
  (some #(when (pred %) %) coll))

(def exist? find-first) ; some 返回true/nil；exist? 返回符合条件的元素（第一个）或nil

(defn print-seq
  "打印集合，每行一个元素"
  [coll]
  (doseq [e coll] (println e)))

;;;---------------------------- ns 和 函数动态调用

(defn ns-exist?
  "根据namespace字符串查询，是否存在该namespace，如: (ns-check \"wr3.clj.u\") "
  [ns]
  (try (require (symbol ns)) true
    (catch Exception _ (do (println "ns-exist? error: " _) false))))

(defn fget
  "根据namespace字符串，函数名字符串，得到函数，没有则为nil.
   要禁止reload，可在启动jvm时设置 -Dwr3.clj.reload=0 "
  [ns fn]
  (let [n (symbol ns) f (symbol fn)
        not-reload? (= "0" (System/getProperty "wr3.clj.reload"))]
    (if (not (ns-exist? ns)) nil
      (do
        (if not-reload?
          (require n)
          (require n :reload)) ; 使用 :reload 就可以动态转载，但可能对性能有所影响。
        (ns-resolve (the-ns n) f)))))

(defn fcall
  "根据namespace字符串，函数名字符串，参数列表来调用函数"
  [ns fn & args]
  (let [func (fget ns fn)]
    (if (nil? func) (format "fcall error: (%s/%s) not found." ns fn)
      (try (apply func args)
        (catch Exception e 
          (do 
            (.printStackTrace e) ; 调试用，便于定位如NullPointerException等比较恶心的错误
            (format "fcall error: (%s/%s %s) (%s)" ns fn args e)))))))

(defmacro fargs0  
  "传入当前namespace下的函数名（非字符串），得到函数参数数组（有函数重载的，按第一个）；见fargs"
  [f]
  `(-> ~f var meta :arglists first))

(defn fargs 
  "根据namespace字符串，函数名字符串，得到函数参数数组（有函数重载的，按第一个）；
  若要直接用函数作为参数，见fargs0"
  [ns fn]
    (-> (fget ns fn) meta :arglists first))

(defn fargc
  "根据namespace字符串，函数名字符串，得到函数参数个数（有函数重载的，按第一个）"
  [ns fn]
  (count (fargs ns fn)))

;;;-------------- source-fn 和 source 函数的utf-8修正
(import '(java.io LineNumberReader InputStreamReader PushbackReader))

;; 从clojure.repl/source-fn更改，修改为以UTF-8进行文件读以保证中文正确
(defn source-fn2
  [x]
  (when-let [v (resolve (symbol x))]
    (when-let [filepath (:file (meta v))]
      (when-let [strm (.getResourceAsStream (clojure.lang.RT/baseLoader) filepath)]
        (with-open [rdr (LineNumberReader. (InputStreamReader. strm "UTF-8"))]
          (dotimes [_ (dec (:line (meta v)))] (.readLine rdr))
          (let [text (StringBuilder.)
                pbr (proxy [PushbackReader] [rdr]
                      (read [] (let [i (proxy-super read)]
                                 (.append text (char i))
                                 i)))]
            (read (PushbackReader. pbr))
            (str text)))))))
 
(defmacro source2
  "source 函数的utf修正"
  [n]
  `(println (or (source-fn2 '~n) (str "Source not found"))))

(defmacro debug 
  "打印变量名及其值"
  ([v] `(println "------" '~v ":" ~v))
  ([v1 v2] `(println "-----" '~v1 ":" ~v1 "," '~v2 ":" ~v2))
  ([v1 v2 v3] `(println "-----" '~v1 ":" ~v1 "," '~v2 ":" ~v2 "," '~v3 ":" ~v3)))

(defn ?
  "如果v非nil则返回v，为nil则返回default。
  (? nil 10) ; 10
  (? 3 10) ; 3" 
  [v default] (if (nil? v) default v))

(defn class-methods 
  "得到Java类的所有方法并排序，用法：
  (class-methods String)
  (class-methods System)
  (class-methods java.util.Date)"
  [c] (sort (map #(.getName %) (.getMethods c))))

(defmacro kw 
  "查询当前所有ns中含特定字符串的函数，如: (kw -index)"
  [s] `(filter #(>= (.indexOf (str %) (name '~s)) 0)
               (sort (keys (mapcat ns-publics (all-ns))))))


;;;------------- datetime
(import wr3.util.Datetime)

(defn datetime [] (Datetime/datetime))
(defn date [] (Datetime/date))
(defn times [] (Datetime/time))

(defn year [] (Datetime/year))
(defn month [] (Datetime/month))
(defn day [] (Datetime/day))

(defn last-day [] (Datetime/lastDay))
(defn week-day [] (Datetime/weekDay))

(defn month-prev 
  "1月前的那一天"
  [] (Datetime/date (Datetime/add 0 -1 0))) 

(defn month-next 
  "1月后的那一天"
  [] (Datetime/date (Datetime/add 0 1 0))) 


