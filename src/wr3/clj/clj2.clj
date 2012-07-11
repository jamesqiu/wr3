;;;; 测试Clojure功能，临时使用，尽量写成函数避免干扰其他测试

(ns wr3.clj.clj2)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.tb 'wr3.clj.file 'wr3.clj.db); :reload)
(use 'hiccup.core)

;------------------ 测试table
(def t1 (table 5 3))
(defn test1 []
  (println (table-string t1 "\t"))
  (println (table-add t1 0 [1 2 3 4 5])))
;(test1)

;;----------------- 能分解成11个连续质数之和的质数叫magic数
(defn pnext
  "n之后的下一个质数"
  [n]
  (let [n2 (inc n)]
    (if (prime? n2) n2 (recur n2))))

(defn p11
  "质数n开始的11个质数"
  [n]
  (take 11 (lazy-cat [n] (p11 (pnext n)))))

(defn f [n] (take n (filter prime? (map #(apply + (p11 %)) primes))))
;(f 20) ; 前20个magic数

;;------------------ Project Euler 18
(comment " 注意：下面最后一行第6个“09”要改成9，否则被认为是不合法的8进制数
75
95 64
17 47 82
18 35 87 10
20 04 82 47 65
19 01 23 75 03 34
88 02 77 73 07 63 67
99 65 04 28 06 16 70 92
41 41 26 56 83 40 80 70 33
41 48 72 33 47 32 37 16 94 29
53 71 44 65 25 43 91 52 97 51 14
70 11 33 28 77 73 17 78 39 68 17 57
91 71 52 38 17 14 91 43 58 50 27 29 48
63 66 04 68 89 53 67 30 73 16 69 87 40 31
04 62 98 27 23 09 70 98 73 93 38 53 60 04 23
")
;; 算法：二进制，0就是不变，1就是inc
;; 0->(0 0 0 0 0 0 0 0 0 0 0 0 0 0 0) 16383->(0 1 1 1 1 1 1 1 1 1 1 1 1 1 1)
(def m [
[75]
[95 64]
[17 47 82]
[18 35 87 10]
[20 04 82 47 65]
[19 01 23 75 03 34]
[88 02 77 73 07 63 67]
[99 65 04 28 06 16 70 92]
[41 41 26 56 83 40 80 70 33]
[41 48 72 33 47 32 37 16 94 29]
[53 71 44 65 25 43 91 52 97 51 14]
[70 11 33 28 77 73 17 78 39 68 17 57]
[91 71 52 38 17 14 91 43 58 50 27 29 48]
[63 66 04 68 89 53 67 30 73 16 69 87 40 31]
[04 62 98 27 23 9 70 98 73 93 38 53 60 04 23]])
;; (f 0)->(0 0 0 0 0 0 0 0 0 0 0 0 0 0 0) (f 16383)->(0 1 1 1 1 1 1 1 1 1 1 1 1 1 1)
(defn f [n] (map #(if (bit-test n (- 14 %)) 1 0) (range 0 15)))
;; (f1 0)->(0 0 0 0 0 0 0 0 0 0 0 0 0 0 0) (f1 16383)->(0 1 2 3 4 5 6 7 8 9 10 11 12 13 14)
(defn f1 [n] (reductions + (f n)))
;; (f2 0)->794 (f2 16383)->724
(defn f2 [n] (apply + (map-indexed (fn [i e] (nth (nth m i) e)) (f1 n))))
;; 1074
;(apply max (map f2 (range 0 16384)))

;;------------------ project euler 19
;; 判断闰年
(defn leap [y] (or (zero? (rem y 400)) (and (zero? (rem y 4)) (not= 0 (rem y 100)))))
;; 判断y年m月有多少天
(defn days [y m] (cond
  (and (leap y) (= m 2)) 29
  (= m 2) 28
  (some (partial = m) [4 6 9 11]) 30
  :else 31))
;; [[1900 1 31] ... [2000 12 31]]
(def y-m-days-table  (for [y (range 1900 2001) m (range 1 13)] [y m (days y m)]))
(defn first-is-sunday? [y m]
  (let [past (take-while #(or (not= y (first %)) (not= m (second %))) y-m-days-table)
        n (apply + (map #(nth % 2) past))]
     (zero? (mod (inc n) 7))))
; 173, 头2个是1900年的，去掉 173-2=171
;(filter #(first-is-sunday? (first %) (second %))  (rest y-m-days-table))

;;------------------- project euler 21
(defn factors2 [n] (filter #(zero? (rem n %)) (range 1 n)))
(defn d [n] (apply + (factors2 n)))
(defn amicable-number? [a] (let [b (d a) c (d b)] (and (= a c) (not= a b))))
;(apply + (filter amicable-number? (range 1 10000)))

;-------------------- 测试table
(def t1 (table 5 0))
(def t2 (table 5 1))
(def t3 (table 5 3))
(defn test2 []
	(println "t1 " t1)
	(println "t2 " t2)
	(println "t3 " t3)
	(println (table-row t2 -1))
	(println (table-set t2 0 [1 2 3 4 6]))
	(println (table-add t1 0 [1 1 1 1 1 ]))
	(println (table-del t2 1))
	(println (table-cell t2 0 0))
	(println (table-cell t2 0 0 100))
	;(println "sub: " (subvec t1 0 2))
)
;(test2)

;------------------- Groovy类型可含变量的string, 如："${myname}: After 5 years ${myname} is ${(+ age 5)}"
(def myname "qh")
(def age3 30)
(def s "${myname}: After 5 years ${myname} is ${(+ age3 5)}")
(defn test-gstring []
  (println (gstring s)
           "\n"
           (gstring "${name} 's age is ${age} ${name}-${age}" {:name "邱晖" :age 30})
           "\n"
           (binding [myname "james" age3 40] (gstring s)))
  )
;(test-gstring)

;;; 取数字各位
;(digitals 1024)

;;; 切分变量
(defn var2words
  "helloClojureWorld->(hello Clojure World)"
  [v]
  (let [n (count v)
        u (for [i (range n) :when (Character/isUpperCase (nth v i))] i)
        u1 (if (zero? (first u)) u (cons 0 u))
        u2 (if (zero? (first u)) (concat (rest u) [n]) (concat u [n]))]
    (map #(.substring v %1 %2) u1 u2)))

(defn test-var2words []
  (doseq [e '(helloColojureWorld ThisIsAGoodMan isA ABC)]
    (prn (var2words (str e))))
  )
;(test-var2words)

;;;------- 打开jar包中的areacode文件进行查询
(import tool.Pinyin)
(def py (Pinyin/instance))
(def area-map
  (read-resource "areacode/areacode.txt"
                 (fn [r]
                   (for [e (vec (line-seq r))]
                     (let [code (subs e 0 6)
                           nams (subs e 7)
                           jp (.jp py nams)] (print-str code nams jp))))))

;; 写入area-map.clj文件
;(spit "area-map.clj" (seq area-map) :encoding "UTF-8")

(defn- f2 [ss s] (>= (.indexOf ss s) 0))
;(filter #(f2 % "hhsp") area-map)

;;;------ 对Dict汉英辞典 进行操作
(import tool.Dict)
(def dict (Dict/ec))
(defn list10 []
  (let [f10 (take 10 (.getAll dict))]
    (doseq [[k v] f10] (println k v))))
(defn search "模糊查询" [s]
  (for [[k v] (.getAll dict) :when (or (has? k s) (has? v s))] (print-str k v)))
;(join (search "index") "<br/>")

;(println (re-matches #"[a-zA-Z]+" "helloworld"))
;(println (split "hello-clojure-world" "-"))

(import wr3.util.Charsetx )
(defn test-hasChinese []
  (doseq [e ["cn1" "cn中文" "ｃｎ＿ａｂｃ１２３" "" nil]] (println e ": " (Charsetx/hasChinese e))))
;(test-hasChinese)

(wr3.clj.u/find-first prime? (range 1 100 3))

(defn qsort [c]
  (if (empty? c) '()
    (lazy-cat (qsort (filter #(< % (first c)) c))
              (filter #(= % (first c)) c)
              (qsort (filter #(> % (first c)) c)))))
;(qsort (shuffle (range 0 10)))

;;;------------------ 99 Lisp problems
;; http://www.ic.unicamp.br/~meidanis/courses/mc336/2006s2/funcional/L-99_Ninety-Nine_Lisp_Problems.html
;; '(a a a a b c c a a d e e e e) -> (A B C A D E)
(def l1 '(a a a a b c c a a d e e e e))
(def p08
  (loop [l l1 rt [] c nil]
    (if (empty? l) rt (recur (rest l) (if (not= (first l) c) (conj rt (first l)) rt) (first l)))))

;; '(a a a a b c c a a d e e e e) -> ((A A A A) (B) (C C) (A A) (D) (E E E E))
(defn pack [l] (if (empty? l) l
                 (let [ll (split-with (partial = (first l)) l)]
                   (cons (first ll) (pack (last ll))))))
(def p09 (pack l1))

;;;------------------ 杨辉三角形
(defn yanghui []
  (loop [r [1]] (if (= 7 (count r)) r
                  (let [r (map + (concat r [0]) (concat [0] r))]
                    (do (println r) (recur r)))))
  (println "-- over --")
  )
;(yanghui)

;;;------------------ 测试使用mongodb
(use 'somnium.congomongo)
(defn test-mongodb []
  (mongo! :db nil)
  (println (databases))
;  (println (collections))
;  (println (fetch-count :cust))
;  (let [rs (fetch :cust2 :limit 3)]
;    (doseq [r rs] (println (:name r))))
;
;  (set-database! "")
;  (println (collections))
;  (println (distinct-values :foo "name"))
  )
;(test-mongodb)

(defn test-mongodb2 []
  (mongo! :db "test")
  (let [rs (fetch :user)]
    (doseq [r rs] (println r)))
  (println (fetch-count :user))
  )
;(test-mongodb2)

(defn test-mongodb3 []
  (let [db "wr3"
        tb1 "dict_ce"
        tb2 "dict_ec"
        conn (make-connection db)
        ce (.getAll (Dict/ce))
        ec (.getAll (Dict/ec))]
    (with-mongo conn
      ;(mass-insert! tb2 (vec (for [[k v] ec] {:k k :v v})))
      (let [rt (fetch tb1 :where {:k (re-pattern (format "%s" "字典"))})]
        (doseq [e rt] (println (:k e) "=" (:v e))))
      )))
;(test-mongodb3)

(defn test-mongodb4 []
  (let [db "wr3"
        tb "pinyin"
        conn (make-connection db)
        py (.getAll (Pinyin/instance))]
    (with-mongo conn
      ;(mass-insert! tb2 (vec (for [[k v] ec] {:k k :v v})))
      ;(let [rt (fetch tb1 :where {:k (re-pattern (format "%s" "字典"))})]
      ;  (doseq [e rt] (println (:k e) "=" (:v e))))
      (mass-insert! tb (vec (for [[k v] py] {:k k :v v})))
      )))
;(test-mongodb4)

(defn test-mongodb5 []
  (let [fm_form1 [{:name "custname" :label "客户名" :order 100}
               {:name "custtype" :label "客户类型" :type {"1" "个人" "2" "企业"} :order 200}
               {:name "date" :label "开户日期" :type "input" :value "2011/6/3" :order 300}
               {:name "password" :label "密码" :type "password" :value "pass" :order 400}
               ]]
  (with-mongo (make-connection "crud")
    (let [rt (fetch-by-id "custInfo" (object-id "4dea50683e7ed42e2481f2ee"))]
      (println rt)
      (println (get rt (keyword "custname")))
      )
    ;(mass-insert! "fm_custInfo" fm_form1)
    ;(insert! "fm_custInfo" {:name "agree" :label "是否同意？" :type {"y" "同意该条款"} :order 500})
;    (let [rt (fetch "fm_custInfo" :sort {:order 1})]
;      ;(doseq [e rt] (println e))
;      (println (html (:type (first rt))))
;      )
    )))
;(test-mongodb5)

(require 'wr3.clj.meta) ; 写在函数里无效 

(defn test-meta []
  (println (wr3.clj.meta/meta-name 'org)))
;(test-meta)

(defn test-db
  []
  (let [dbname "abs"
        tbname "sys_infolder"
        sql (format "select cod,nam from %s where typ=3" tbname)]
    (with-open [dbs (dbserver dbname)]
      (let [rt (select-all dbname sql)]
        (doseq [r rt]
          (println
            (format
              "   %s [{:name \"%s\"\n\t:typ 'fd}]\n"
              (keyword (right (:cod r) "."))
              (:nam r)
              ))
          )))))
;(test-db) ;abcde

;;;--------- 500 vs 3500起征新税率
(defn test-tax [] 
  (let [n0 5000 n1 20000 step 1000]
    (doseq [n (range n0 (inc n1) step)]
      (let [t1 (int (tool.Tax/tax n))
            t2 (int (tool.Tax/taxNew n))] 
        (printf "%s: %s , %s, %s \n" n t1 t2 (- t1 t2)) ))))
;(test-tax)

;;;--------- 测试类型
(defn test-type 
  [coll]
  (cond 
    (vector? coll) "vector"
    (list? coll) "list"
    (set? coll) "set"
    (map? coll) "map"
    :else "other" ))
;(test-type '(1 2 3))

;;;--------- 测试名称规范化处理
(defn test-mines
  []
  (let 
    [mines  
     [["湖北三鑫金铜股份有限公司", 30.190000, 115.070000],
     ["内蒙古包头鑫达黄金矿业有限责任公司", 40.690000, 109.620000], ["河南金源黄金矿业有限责任公司", 34.170000, 112.080000], ["中国黄金集团夹皮沟矿业有限责任公司", 42.870000, 127.500000], ["陕西太白黄金矿业有限责任公司", 33.810000, 107.210000], ["河北金厂峪金矿", 40.300000, 118.450000], ["河南文峪金矿", 34.790000, 111.320000], ["河南秦岭金矿", 34.540000, 110.890000], ["黑龙江乌拉嘎金矿", 48.400000, 130.160000], ["中国黄金集团二道沟金矿", 41.790000, 120.780000], ["山东烟台鑫泰黄金矿业有限责任公司", 37.050000, 121.110000], ["中金黄金股份有限公司中原黄金冶炼厂", 34.740000, 111.230000], ["河北峪耳崖黄金矿业有限责任公司", 40.490000, 118.550000], ["潼关中金黄金矿业有限责任公司", 34.480000, 110.350000], ["潼关中金冶炼有限责任公司", 34.530000, 110.310000], ["苏尼特金曦黄金矿业有限责任公司", 42.390000, 112.900000], ["甘肃省天水李子金矿有限公司", 34.300000, 105.820000], ["辽宁金凤黄金矿业有限责任公司", 40.750000, 123.670000], ["黔西南金龙黄金矿业有限责任公司", 25.280000, 105.350000], ["贵州金兴黄金矿业有限责任公司", 25.520000, 105.500000], ["云南黄金有限责任公司", 25.040000, 102.720000], ["辽宁天利金业有限责任公司", 40.770000, 124.000000], ["吉林海沟黄金矿业有限责任公司", 42.650000, 128.100000], ["辽宁省排山楼金矿", 42.020000, 121.670000], ["辽宁新都黄金有限责任公司", 41.730000, 120.480000], ["陕西略阳横现河黄金矿业有限公司", 33.340000, 106.080000], ["白河县大湾银矿有限责任公司", 32.590000, 110.040000], ["陕西略阳铧厂沟金矿", 33.300000, 105.800000], ["商洛市恒源矿业公司", 33.730000, 110.160000], ["内蒙古金予矿业有限公司", 48.670000, 116.820000], ["四川通用投资有限公司阿西金矿", 33.570000, 102.960000], ["四川平武矿业有限公司", 32.460000, 104.410000], ["西藏中金矿业有限公司", 31.270000, 98.210000], ["广西凤山天承黄金矿业有限公司", 24.560000, 106.900000], ["陕西黄金公司", 34.250000, 108.960000], ["辽宁黄金公司", 41.120000, 122.350000], ["河北黄金公司", 37.740000, 114.970000], ["广西黄金公司", 22.820000, 108.330000], ["中国黄金四川公司", 30.650000, 104.070000], ["嵩县金牛有限责任公司", 34.080000, 112.000000]
     ]
     mines2 
     '[ 包头鑫达黄金矿业公司 凤山县宏益公司 甘肃省天水李子金矿有限公司 广西凤山天承公司 广西龙头山金矿 广西田林高龙黄金矿业有限责任公司 贵港市金地矿业有限责任公司 贵州金兴黄金矿业有限责任公司 河北东梁黄金矿业有限责任公司 河北金厂峪矿业有限责任公司 河北峪耳崖黄金矿业有限责任公司 河南金源黄金矿业有限责任公司 河南秦岭黄金矿业公司 河南文峪金矿 河南中原黄金冶炼厂有限责任公司 贺州市金琪矿业有限公司 黑龙江乌拉嘎黄金矿业有限责任公司 湖北鸡笼山黄金矿业有限公司 湖北三鑫金铜股份有限公司 怀来宏达矿业有限公司 吉林海沟黄金矿业有限责任公司 江苏省金源黄金有限责任公司 江西金山矿业有限公司 江西三和金业有限公司 辽宁二道沟黄金矿业公司 辽宁金凤公司 辽宁排山楼黄金矿业有限责任公司 辽宁天利金业有限责任公司 辽宁五龙公司 辽宁新都公司 灵宝双鑫黄金矿业有限责任公司 黔西南金龙黄金矿业有限责任公司 山东烟台鑫泰黄金矿业公司 陕西久盛矿业投资管理有限公司 陕西略阳铧厂沟 陕西略阳铧厂沟金矿 陕西太白黄金矿业有限责任公司 陕西鑫元科工贸股份有限公司 四川通用投资有限公司 嵩县金牛有限责任公司 嵩县前河矿业有限责任公司 嵩县源丰矿业有限公司 苏尼特金曦黄金矿业公司 潼关中金黄金矿业有限责任公司 潼关中金冶炼有限责任公司 西藏华泰龙矿业开发有限公司 西峡金泰矿业有限公司 云南黄金有限责任公司镇沅分公司 中国黄金集团夹皮沟矿业有限公司 中国黄金集团内蒙古矿业有限公司 中国黄金集团迁西鑫峪矿业有限责任公司 中国黄金集团石湖矿业有限公司 中国黄金集团新疆金滩矿业有限公司 中国黄金集团中原矿业有限公司 
       ]
     mines1 (map first mines)
     m1
     '(四川黄金公司 辽宁二道沟金矿 吉林夹皮沟矿业 河南中原黄金冶炼厂 云南黄金 内蒙古包头鑫达金矿 内蒙古金予矿业 吉林海沟金矿 商洛市恒源矿业 四川平武矿业 四川通用投资阿西金矿 山东烟台鑫泰金矿 嵩县金牛 广西凤山天承金矿 广西黄金公司 河北峪耳崖金矿 河北金厂峪金矿 河北黄金公司 河南文峪金矿 河南秦岭金矿 河南金源金矿 湖北三鑫金铜 潼关中金冶炼 潼关中金金矿 甘肃省天水李子金矿 白河县大湾银矿 苏尼特金曦金矿 西藏中金矿业 贵州金兴金矿 辽宁天利金业 辽宁新都黄金 辽宁省排山楼金矿 辽宁金凤金矿 辽宁黄金公司 陕西太白金矿 陕西略阳横现河金矿 陕西略阳铧厂沟金矿 陕西黄金公司 黑龙江乌拉嘎金矿 黔西南金龙金矿)
     m2
     '(中原矿业 内蒙古矿业 吉林夹皮沟矿业 新疆金滩矿业 石湖矿业 迁西鑫峪矿业 云南黄金镇沅分公司 凤山县宏益公司 包头鑫达金矿 吉林海沟金矿 四川通用投资 山东烟台鑫泰金矿 嵩县前河矿业 嵩县源丰矿业 嵩县金牛 广西凤山天承公司 广西田林高龙金矿 广西龙头山金矿 怀来宏达矿业 江苏省金源黄金 江西三和金业 江西金山矿业 河北东梁金矿 河北峪耳崖金矿 河北金厂峪矿业 河南中原黄金冶炼厂 河南文峪金矿 河南秦岭金矿 河南金源金矿 湖北三鑫金铜 湖北鸡笼山金矿 潼关中金冶炼 潼关中金金矿 灵宝双鑫金矿 甘肃省天水李子金矿 苏尼特金曦金矿 西峡金泰矿业 西藏华泰龙矿业开发 贵州金兴金矿 贵港市金地矿业 贺州市金琪矿业 辽宁二道沟金矿 辽宁五龙公司 辽宁天利金业 辽宁排山楼金矿 辽宁新都公司 辽宁金凤公司 陕西久盛矿业 陕西太白金矿 陕西略阳铧厂沟 陕西略阳铧厂沟金矿 陕西鑫元科工贸 黑龙江乌拉嘎金矿 黔西南金龙金矿)     
     rm
     '(公司 金矿 矿业 冶炼厂)
     f-rm (fn [s] (reduce #(replace-all (str %1) (str %2) "") s rm))]
      (println (sort (map f-rm m1)))
      (println (sort (map f-rm m2)))
;     (println (count mines) "," (count mines2) )
;     (println (sort m1))
;     (println (sort m2))
     ))
;(test-mines)

;;;------------- 
;给定一个含有n个元素的整形数组a，再给定一个和sum，求出数组中满足给定和的所有元素
;组合，举个例子，设有数组a[6] = { 1, 2, 3, 4, 5, 6 }，sum = 10，则满足和为10的所
;有组合是 ((1 2 3 4) (2 3 5) (1 4 5) (1 3 6) (4 6))
(use '[clojure.contrib.combinatorics :only (selections subsets)])
(defn test-sum [a s]
  (println "[1 2 3]全排列：" (clojure.contrib.combinatorics/lex-permutations '[1 2 3]))
  (println "[1 2 3]全组合(子集):" (rest (clojure.contrib.combinatorics/subsets '[1 2 3])))
  (println "全排列组合："
    (let [s "abc"] 
      (mapcat (fn [i]  (filter #(apply distinct? %) (selections s i))) 
              (range 1 (inc (count s))))))
  (println "10 =" (filter #(= 10 (apply + %)) (subsets [1 2 3 4 5 6]))))
;(test-sum [1 2 3 4 5 6] 10)

(defn index-of-any [s cc]
  (let [f (fn [s c] (let [i (.indexOf s (str c))] (if (>= i 0) i nil)))
        rt (some #(f s %) (seq cc))]
    (or rt -1)))
;(index-of-any "hello world" "abc")

(defn binding-params 
  "把params自动绑定到同名的fname函数参数中， 例如：(binding-params 'fp3' {:p1 10 :pa 20 :pb 30 :g +})"
  [fname params]
  (let [arg-names (fargs "wr3.clj.clj2" fname)
        arg-values (map #(params (name %)) arg-names)]
    (apply (fget "wr3.clj.clj2" fname) arg-values) 
    ))

(defn fp3 [pa pb] (* pa pb))
(defn fp4 [c1 g] (reduce g c1))
  
;(binding-params "fp4" {"p1" 10 "pa" 20 "pb" 30 "g" pow "c1" [2 3 4]})

;-------------- 数独
(comment " 数独规格，除了每行列不同，还必须每3x3格必须含1～9
@m 8x8矩阵
@i 行号
@j 列号
@e 元素
@r 某行
@c 计数count

")
(def m0
  [[8 0 0 0 0 0 0 0 0]
   [0 0 3 6 0 0 0 0 0]
   [0 0 0 0 9 0 2 0 0]
   [0 5 0 0 0 7 0 0 0]
   [0 0 0 0 4 5 7 0 0]
   [0 0 0 1 0 0 0 3 0]
   [0 0 1 0 0 0 0 6 8]
   [0 0 8 5 0 0 0 1 0]
   [0 9 0 0 0 0 4 0 0]])

(defn no0? "m矩阵的i行是否不含0"
  ([m i] (not (some #(= % 0) (nth m i))))
  ([m] (when m 
         (let [rows (count m)] (every? #(no0? m %) (range rows))))))

(defn n-ij "m矩阵中i行j列不是0的所有数的set"
  [m i j]
  (let [n-i (remove zero? (nth m i))
        n-j (remove zero? (map #(nth % j) m))]
    (set (concat n-i n-j))))

(defn rand-e "m中i行j列的一个随机数，填不出来则为nil"
  [m i j]
  (let [full-set (set (range 1 (inc (count m)))) 
        r (vec (clojure.set/difference full-set (n-ij m i j)))]
    (if (empty? r) nil (rand-nth r))))
  
(defn put-e "m矩阵的i行j列随机填一个合理的数字，填不出返回nil"
  [m i j] 
  (when m
    (let [e0 (nth (nth m i) j)
          e1 (if (zero? e0) (rand-e m i j) e0)]
      (if e1 (assoc-in m [i j] e1) nil))))

(defn put-r1 "m中i行随机填一次合适的随机数，填不出返回nil"
  [m i]
  (when m
    (let [r (nth m i)]
      (reduce #(put-e %1 i %2) m (range (count m))))))

(defn put-r
  [m i]
  (loop [c 0] (let [m1 (put-r1 m i)] (if (or m1 (> c 1000)) m1 
                                       (recur (inc c))))))

(defn put-m "m中随机填满，填不出返回nil"
  [m]
  (when m
    (reduce #(put-r %1 %2) m (range (count m))) ))

(defn print-m [m] (doseq [r m] (println r)) (println "------"))
;;----------------
(def m2 [[1 0 0]
         [2 0 1]
         [0 0 0]])

(defn shudu [m0]
  (loop [m m0 c 0] 
    (let [m1 (put-m m)]
      (if (or (> c 10) (no0? m)) (print-m m)
        (recur (if m1 m1 m0) (inc c))))))

;(print-m (put-rand (put-rand m0 2 8) 0 1))
;(put-r m0 8)
;(shudu m0)



