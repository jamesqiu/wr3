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
(def fmt format)

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

(defn get-key
  "从hashmap中根据key取出value，key满足指定的函数f。
  @arg f 处理key的函数，返回true|false
  @arg m hashmap
  用法：(get-key #(.equalsIgnoreCase 'b' %) {'a' 'aa' 'B' 'bb'}) ; 'bb' "
  [f m]
  (some (fn [[k v]] (when (f k) v)) m))

;;;---------------------------- ns 和 函数动态调用

(defn ns-exist?
  "根据namespace字符串查询，是否存在该namespace，如: (ns-exist? \"wr3.clj.u\") "
  [ns]
  (try (require (symbol ns)) true
    (catch Exception _ (do (println "ns-exist? error: " _) false))))

(defn fget0
  "根据namespace字符串，函数名字符串，得到函数，没有则为nil.
   要禁止reload，可在启动jvm时设置 -Dwr3.clj.reload=0 "
  [ns fn]
  (let [n (symbol ns) f (symbol fn)
        not-reload? (= "0" (System/getProperty "wr3.clj.reload"))]
    (if (not (ns-exist? ns)) nil
      (do
        (if not-reload?
          (require n)
          (require n :reload)) ; 使用 :reload 就可以动态转载，但对性能有较大影响。
        (ns-resolve (the-ns n) f)))))

(defn require+
  "require引用namespace
  @ns 可以是symbol或者str类型，如'wr3.clj.app.bank' 
  @reload? "
  [ns reload?]
  (let [n (symbol ns)]
    (if reload? (require n :reload) (require n))))
  
(defn fget
  "不自动require，需要在调用本函数前自行调用require+函数"
  [ns fn]
  (let [[n f] (map symbol [ns fn])]
    (when (ns-exist? ns) (ns-resolve (the-ns n) f) )))

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

(defn app-timestamp
  "传入app名称，得到最后更新日期，用于自动文件reload
  @app 如'wr3.clj.app.bank', 指 wr3/clj/app/bank.clj "
  [app]
  (let [fname (str (wr3.util.Stringx/replaceAll app "." "/") ".clj")
        res (.getResource (clojure.lang.RT/baseLoader) fname)]
    (.lastModified (java.io.File. (.getPath res)))))

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
  "打印多个变量名及其值 (debug v1 v2 v3) "
  ([v] `(println "------" '~v ":" ~v))
  ([v & next]
    `(let [m1# ~v]
       (when m1# (do (debug ~v) (debug ~@next)) ) )))

(defmacro debug-str
  "打印多个变量名及其值到字符串，如 (debug-vs v1 v2 v3) "
  ([v] `(str '~v ":" ~v "\n"))
  ([v & next]
    `(let [m1# ~v]
       (when m1# (str (debug-str ~v) (debug-str ~@next)) ) )))


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

;(use 'clojure.java.browse)
(defmacro demo 
  "打开 clojuredocs.org 查询 clojure.core 的函数使用样例"
  [f]
  `(let [~'f2 (str '~f) ; map? 等以?结尾的变为map_q
         ~'f2 (if (.endsWith ~'f2 "?") (str (.substring ~'f2 0 (dec (count ~'f2))) "_q") ~'f2)]
     (clojure.java.browse/browse-url ; 形如 “->” 需要encode成 -%3E 这样browse-url才能打开
       (str "http://clojuredocs.org/clojure_core/clojure.core/" (java.net.URLEncoder/encode ~'f2) "#examples"))))

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
(defn date-parse
  "日期解析
  @s 格式如 2011-1-2 2011-12-03 
  @fmt 无此参数则只解析到年月日
  @return Calendar "
  ([s] (Datetime/parse s))
  ([s fmt] (Datetime/parse s fmt)))
(defn date-format
  "日期格式化，例如 (date-format '2012-3-5' 'yyyy年MM月dd日') (date-format '2012-7-27 17:2:20' 'yyyy-MM-dd HH:mm:ss')
  @s 如：'2012-3-5' '2012-03-5' 
  @fmt 如：'yyyyMd' 'yyyy年MM月dd日' 'yyyy-MM-dd HH:mm:ss' "
  [s fmt] (Datetime/format (date-parse s fmt) fmt))
(defn date-add 
  "日期增减，从今天或者指定日期, (date-add '2013-4-05' 1 -1 10) ; '2014-3-15'
  @from 日期String如 '2012-1-1'
  @return 字符串如 2012-3-5 "
  ([y m d] (Datetime/date (Datetime/add y m d)))
  ([from y m d] (Datetime/date (Datetime/add (date-parse from) y m d))))
(defn days 
  "计算字符表示的日期与今天相隔的天数, today - d "
  [d]
  (Datetime/days (date-parse d) (date-parse (date))))

;; conj cons 函数对vector和list的行为不一致，很容易引起混乱，改造如下：
(defn conj+ 
  "conj 和 conj+ 的区别，(conj  [1 2] 3) -> [1 2 3] , (conj '(1 2) 3) -> '(3 1 2)
  conj+统一增加元素在后，(conj+ [1 2] 3) -> [1 2 3] , (conj+ '(1 2) 3) -> '(1 2 3)"
  ([coll x] (if (list? coll) (concat coll (list x)) (conj coll x)))
  ([coll x & xs] (if (list? coll) (concat coll (list x) xs) (apply conj coll x xs))))

(defn cons+
  "cons 和 cons+ 的区别，(cons  1 '(2 3)) -> '(1 2 3), (cons  1 [2 3]) -> '(1 2 3), 类型由vector变为list；
  cons+统一为和输入一致，(cons+ 1 '(2 3)) -> '(1 2 3), (cons+ 1 [2 3]) ->  [1 2 3]"
  [x coll] (if (vector? coll) (into (vector x) coll) (cons x coll)))


(defn map-key-rename
  "@m map
  @ ks0 要更改的原key列表，如：['k1' 'k3'] 
  @ ks1 要更改到的key列表，如：['k11' 'k33'] "
  [m ks0 ks1]
  (let [vs0 (map m ks0)
        m1 (zipmap ks1 vs0)]
    (into (apply dissoc m ks0) m1) ))

;(days "2012-7-21")
