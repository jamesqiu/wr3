;;;; 用Clojure重新实现wr3中的Cell, Row, Col, Table
;;;; cell: 不用实现
;;;; row, col: 用vector实现
;;;; table：用元素为row的vector实现
;;;;   数据结构：[[..] [..]]
;;;;   如：[["c0" "c1"] [38 17] [0 50] [44 75]]

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

(defn table-html
  "把table输出为html代码片段, m为<table>属性"
  ([table m]
    (let [align (fn [e] (when (number? e) {:align "right"}))
          f1 (fn [r td-th] 
               (html [:tr (apply str (map #(html [td-th (align %) %]) r))]))]
      (html 
        [:table (merge {:class "wr3table" :border 1} m) 
         [:thead 
          (f1 (first table) :th)]
         [:tbody
          (apply str (map #(f1 % :td) (rest table)))]])))
  ([table] (table-html table nil)))

(defn table-print
  "打印table"
  [table]
  (println (table-string table "\t")))

  
  