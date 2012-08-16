;;;; 用Clojure重新实现wr3中的Cell, Row, Col, Table
;;;; cell: 不用实现
;;;; row, col: 用vector实现
;;;; table：用元素为row的vector实现
;;;;   数据结构：[[..] [..]]
;;;;   如：[["c0" "c1"] [38 17] [0 50] [44 75]]
;;;;   注：空table为[[]]或者直接nil; 只有头的table为[[c1 c2 c3]].

(ns wr3.clj.tb)

(use 'wr3.clj.n 'wr3.clj.s)

;;; -------------------- row

;; 用一个vector来表示row
(defn row?
  "是否vector表示，需要vector类型以便做assoc"
  [coll]
  (vector? coll))

(defn row
  "[n]得到长度为n的随机数Row;
  [n init]使用特定的cell初始化一个Row"
  ([n]
    (vec (random-n n 100)))
  ([n init]
    (vec (repeat n init))))

(defn row-from
  "从其他基本coll类型（list，map，set）转换为vector类型
  '(1 2 3 4) -> [1 2 3 4]
  #{1 2 3 4} -> [1 2 3 4]
  {1 10 2 20 3 40} -> [1 10 2 20 3 30]
  其他类型返回空vector[]"
  [coll]
  (cond
    (vector? coll) coll
    (list? coll) (vec coll)
    (set? coll) (vec coll)
    (map? coll) (reduce into coll)
    :else [] ))

(defn row-get
  "Row指定列号的Cell, i: 0为第一个元素,-1或者(size()-1)为最后一个元素"
  [row i]
  (nth row (safe-index i (count row))))

(defn row-set
  "把row在第i个位置的元素替换成cell"
  [row i cell]
  (let [i1 (safe-index i (count row))]
    (assoc row i1 cell)) )

(defn row-del
  "删除指定位置i的cell"
  [row i]
  (let [n (count row) i1 (safe-index i n)]
    (vec (concat (subvec row 0 i1) (subvec row (inc i1))))))

(defn row-sub
  "取本Row的一个子行[fromIndex, toIndex), 0是第一个，-1是最后一个"
  [row from-index to-index]
  (slice row from-index to-index))

(defn row-add
  "在row尾部增加一个cell；或者在位置i之前增加一个cell。"
  ([row cell] (conj row cell))
  ([row i cell] (let [n (count row) i1 (safe-index i n)]
                      (vec (concat (take i1 row) [cell] (subvec row i1))))))

(defn row-join
  "row和另一个row1的数据合并"
  [row row2]
  (vec (concat row row2)))

;;; ------------- table 构建

(defn table-head
  "一个参数n，得到一个指定长度的head: [c0 c1 c2 ...];
  两个参数[table head], 重新设置table的head"
  ([n]
    (vec (for [i (range n)] (str "c" i))))
  ([table head]
    (vec (cons head (rest table)))))

(defn table
  "得到一个随机整数的table. cols为列数目，rows为行数目"
  [cols rows]
  (vec (cons (table-head cols) (for [i (range rows)] (row cols)))))

(defn result-table
  "把contrib.sql查出来的结果集转换成 wr3.clj.table [[c1 c2 ..] [v v ..] ..] 
  @rt clojure.sql查询的结果集，lazy的，形如 [{:c1 v :c2 v ..} {..} ..]；当为空结果集[]时，本函数返回[[]] "
  [rt]
  (let [heads (vec (map name (keys (first rt)))) 
        data (for [row rt] (vec (vals row)))]
    (into [heads] data)))

(defn wr3-table 
  "把wr3.Table转换为wr3.clj.table"
  [t]
  (let [alist (.toList t)]
    (vec (map #(vec %) alist))))

;;; ------------- table row、cell操作

(defn- ^{:private true} safe-row-index
  "得到安全的行号，table至少得有thead, 此时返回0"
  [table i]
  (let [n (dec (count table))]
    (assert (>= n 0)) ; table至少得有个thead，否则报错
    (inc (safe-index i n))))

(defn table-row
  "得到指定行号的Row"
  [table i]
  (nth table (safe-row-index table i)))

(defn table-add
  "增加一行至表尾, 或者到指定行之前（0第一行，-1最后一行）"
  ([table row] (conj table row))
  ([table i row] (let [i1 (safe-row-index table i)]
                   (vec (concat (take i1 table) [row] (subvec table i1))))))

(defn table-del
  "删除table中的一行。i：0第一行,-1最后一行"
  [table i]
  (let [i1 (safe-row-index table i)]
    (if (<= (count table) 1) table (row-del table i1))))

(defn table-set
  "替换table中的某一行。i：0第一行,-1最后一行"
  [table i row]
  (let [i1 (safe-row-index table i)]
    (assoc-in table [i1] row)))

(defn table-cell
  "3个参数[table i j]：得到指定行,列的Cell。i为行号，j为列号。0第一行/列,-1最后一行/列；
  4个参数[table [i j] cell]：设置指定行,列的Cell值。"
  ([table i j]
    (let [i1 (safe-row-index table i)
          cols (count (first table))
          j1 (safe-index j cols)]
      (-> table (nth i1) (nth j1))))
  ([table i j cell]
    (let [i1 (safe-row-index table i)
          cols (count (first table))
          j1 (safe-index j cols)]
      (assoc-in table [i1 j1] cell))))

(defn table-sum
  "对全是数值的table所有行求和，得到一个row. 
  注：(conj t (table-sum t)) 可以得到增加sum行后的table"
  [table]
  (let [sum (apply map + (rest table))]
    (vec sum)))

(defn table-sum-col
  "对全是数值的table所有列求和，得到一个col. 
  注：(table-add-col t (table-sum-col t)) 可以得到增加sum行后的table"
  ([table] (table-sum-col table "sum"))
  ([table th]
    (let [sum (map #(apply + %) (rest table))]
      (into [th] sum))))

(defn table-add-col
  "table追加一列col"
  [table col]
  (vec (map-indexed (fn [i r] (conj r (nth col i))) table)))

(defn table-indexed
  "为table增加编号列，thead头前增加th0，各列前增加1开始的序号。
  @th0 如 '序号' '行号' '' 等 "
  ([table th0] (if (or (nil? table) (empty? table) (empty? (first table))) []
                 (vec (map-indexed (fn [i r] (into [(if (zero? i) th0 i)] r)) table))))
  ([table] (table-indexed table "序号")))
  
(defn table-col
  "得到table的第j列"
  [table j]
  (let [j1 (safe-index j (count (first table)))
        col (map #(nth % j1) table)]
    (vec col)))

;;; ------------- table 类型（form-table list-table rotate-table aggre-table group-table frame-table cross-table cube-table）

; (form-table data 
;   {:id "fm1" 
;    :meta {:name "名称" :age "年龄" :gender "性别"} 
;    :dd {:gender {0 "女" 1 "男" 2 "未知"}}
;    })

; (list-table data
;
;
;

;;; ------------- table 输出

(defn table-string
  "把table输出为换行的文本，元素之间的分隔符sep，缺省为\", \""
  ([table sep] (join (map #(join % sep) table) "\n"))
  ([table] (table-string table ", ")))

(use 'hiccup.core)

(defn table-print
  "打印table"
  [table]
  (println (table-string table "\t")))

(defn td-align
  "根据类型确定td的align属性"
  [td]
  {:align (if (number? td) "right" "left")})

(defn bgcolor-css
  "根据行号得到设置行背景色的css片段"
  [row-index]
  {:class (if (odd? row-index) "odd" "even")})
  
(defn table-html
  "把table输出为html代码片段, 可定制化
  @m 定制化参数如 {:attr '' .. } 支持如下的参数和函数：
  attr: <table>属性如： {:style '..' :title '..'}
  f-th: 行列头元素的处理函数如：(fn [col-index th] [:th i th])
  f-td: 数据项处理函数如 (fn [row-index col-index td row] [:td (merge {:col col-index} (align-by-type td)) td])
        其中row-index / col-index为0开始的行/列号，td为元素内容，row为元素所在行如 [c1 c2 ..]
  "
  ([table] (table-html table nil))
  ([table m]
    (let [attr (:attr m)
          f-th (or (:f-th m) (fn [i th] [:th th]))
          f-td (or (:f-td m) (fn [row-index col-index td row] [:td (td-align td) td]))          
          [thead tbody] [(first table) (rest table)]
          f-head (or (:f-head m) (fn [thead] 
                                   (map-indexed (fn [col-index th] (f-th col-index th)) thead)))
          f-row (or (:f-row m) (fn [row-index row] 
                                 [:tr (bgcolor-css row-index) 
                                  (map-indexed (fn [col-index td] (f-td row-index col-index td row)) row)]))]
      (html 
        [:table (merge {:class "wr3table" :border 1} attr) 
         [:thead (f-head thead)]
         [:tbody (map-indexed (fn [row-index row] (f-row (inc row-index) row)) tbody) ]]))))

(defn result-html
  "把clojure.sql 的 resultset 格式 [{:c1 v :c2 v ..} {..} ..] 输出为html代码片段, 可定制化
  @m 定制化参数如 {:attr '' .. } 支持如下的参数和函数：
  attr: <table>属性如： {:style '..' :title '..'}
  f-head: 列头处理函数，例如 (fn [thead] (map-indexed (fn [i th] [:th th]) thead)) 
  f-row: 数据行处理函数，例如 (fn [row-index row] [:tr (map (fn [td] [:td td]) row)])
  f-th: 行列头单个元素的处理函数如：(fn [col-index th] [:th i th])
  f-td: 单个数据项处理函数如 (fn [row-index col-index [k v] row] [:td (merge {:col col-index} (td-align v)) v])
        其中row-index / col-index为0开始的行/列号，[k v]为列名和元素内容内容，row为元素所在行如 {:c1 v :c2 v ..}
  "
  ([result] (result-html result nil))
  ([result m]
    (let [attr (:attr m)
          f-th (or (:f-th m) (fn [i th] [:th th]))
          f-td (or (:f-td m) (fn [row-index col-index [k v] row] [:td (td-align v) v]))          
          thead (vec (map name (keys (first result))))
          f-head (or (:f-head m) (fn [thead] 
                                   (map-indexed (fn [col-index th] (f-th col-index th)) thead)))
          f-row (or (:f-row m) (fn [row-index row] 
                                 [:tr (bgcolor-css row-index) 
                                  (map-indexed (fn [col-index td] (f-td row-index col-index td row)) row)]))]
      (html 
        [:table (merge {:class "wr3table" :border 1} attr) 
         [:thead (f-head thead)]
         [:tbody (map-indexed (fn [row-index row] (f-row (inc row-index) row)) result) ]]))))
  
(defn cross-table
  "生成交叉表的html片段
  @data [[c1 c2 v] ..] c1表示left维度，c2表示top维度，如 (['北京' 'A级' 10] ['上海' 'B级' 15] .. ) 
  @m 定制化参数如 {:caption '' .. } 支持如下的参数和函数：
  caption: [str] 表标题
  dim-top-name / dim-left-name: [str] 列头/行头的维度名称
  f-dim-left / f-dim-top: 行/列头元素的处理函数如：(fn [dim] [:i dim])
  f-value: 数据项处理函数  
  "
  ([data] (cross-table data nil))
  ([data m]
    (let [caption (:caption m)
          dim-top-name (:dim-top-name m)
          dim-left-name (:dim-left-name m)
          dims-left (distinct (for [[c1 c2 v] data] c1))
          dims-top (distinct (for [[c1 c2 v] data] c2))
          f-dim-left (:f-dim-left m)
          f-dim-top (:f-dim-top m)
          f-dim (fn [f dim] (if f (f dim) dim))
          f-value (fn [v] (if-let [f (:f-value m)] (f v) v)) 
          ]
      (html
        [:table {:class "wr3table" :border 1} 
         (when caption [:caption caption])
         [:tr 
          [:td {:class "crossLine"} [:div {:class "dimTop"} dim-top-name] [:div {:class "dimLeft"} dim-left-name]]; (format "%s &nbsp; \\ &nbsp; %s" dim-left-name dim-top-name)] 
          (for [dim-top dims-top] [:th {:group "dim_top"} (f-dim f-dim-top dim-top)])]
         (for [dim-left dims-left]
           [:tr
            [:th {:style "text-align: left" :group "dim_left"} (f-dim f-dim-left dim-left)]
            (for [dim-top dims-top]
              [:td {:align "right"} (sum (for [[c1 c2 v] data :when (and (= c1 dim-left) (= c2 dim-top))] (f-value v)))])
            ]) ] ))))
  
(defn cross-data
  "生成cross-table所需的data，如：[[left1 top1 v1] [left2 top2 v2] ...]
  @rs: 数据库查询结果集
  @fs: 字段列表，给出2个字段列表示left和top进行count聚合，给出3个字段则最后一个表示统计值进行sum聚合；
  如[:admin :grade]或者 [:admin :grade :value] "
  [rs fs]
  (if (= 2 (count fs)) 
    (for [r rs] [((first fs) r) ((second fs) r) 1])
    (for [r rs] (vec (map #(get r %) fs)))))

