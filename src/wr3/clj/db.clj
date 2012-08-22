;;;; wr3.db.* 的包装.
;;;; 使用：在 dbconfig.clj 中进行配置，(select "dbname1" sql)

(ns wr3.clj.db)

(use 'wr3.clj.dbconfig 'wr3.clj.s)
(import '(wr3.db DbSource DbServer) '(java.util Random) '(wr3.util Numberx))

(import java.util.prefs.Preferences)

(defn- prefs 
  "得到用户 Preferences，和wr3.clj包下的一个类关联（这里选 CljServlet）"
  [] 
  (Preferences/userNodeForPackage (class wr3.clj.ClojureCompletor)))

(defn quick-dbname
  "从Preferences读/写一个jdbc连接设置的map
  [dbname]读，[dbname quick-map]写"
  ([dbname]
    (read-string (.get (prefs) dbname "{}")))
  ([dbname quick-map]
    (.put (prefs) dbname (str quick-map))) )

(defn jdbc-map
  "从dbconfig.clj或者Preference得到jdbc驱动的map"
  [dbname]
  (if (.startsWith dbname "quick-")
    (quick-dbname dbname)
    ((keyword dbname) dbnames)))

(defn jdbc-conn
  "按照dbname的字符串名称从dbconfig.clj得到jdbc Connection
  patch：如果dbname是map类型如{:driver .. :url .. :username .. :password ..}，也可以"
  [dbname]
  (let [m (jdbc-map dbname)
        driver (:driver m)
        url (:url m)
        username (:username m)
        password (:password m)]
  (.jdbc (DbSource.) driver url username password)))

(defn jndi-conn
  "按照dbname的字符串名称从dbconfig.clj得到jndi Connection"
  [dbname]
  (let [m ((keyword dbname) dbnames)
        factory (:factory m)
        url (:url m)
        name2 (:name m)]
    (when (= (:driver m) 'jndi)
      (.jndi (DbSource.) factory url name2))))
;      (println (:factory m) (:url m)  (:name m)))))

(defn dbserver
  "把dbconfig的dbname转换为DbServer, 需要使用with-open函数来自动关闭，或者用DbServer.close()来手动关闭。"
  [dbname]
    (DbServer/create (jdbc-conn dbname)))

(defn query
  "sql查询dbconfig中指定dbname的数据库，返回wr3.Table类型"
  [dbname sql]
  (with-open
    [dbs (dbserver dbname)]
     (.query dbs sql)))

(import wr3.db.ResultsetFilter)
(use 'wr3.clj.tb)

(defn dbs-process
  "sql查询dbconfig中指定dbname的数据库，使用DbServer.process(ResultsetFilter), 返回wr3.clj.tb类型[[..] [..]]"
  [dbname sql]
  (with-open [dbs (dbserver dbname)]
    (.maxRows dbs 10)
    (let [cols (atom 0)
          table (atom [])
          limit (atom 0)
          rs-filter (proxy [ResultsetFilter] []
                     (head [heads]
                           (reset! cols (count heads))
                           (swap! table #(table-add % (vec heads))))
                     (row [resultset]
                          (if (>= @limit 10000) false
                            (let [row (vec (map #(.getString resultset (inc %)) (range 0 @cols)))]
                              (swap! table #(table-add % row))
                              (swap! limit inc)
                              true))))]
      (.process dbs sql rs-filter)
      @table)))

;;;-------------------------- 封装clojure.contrib.sql
(use 'clojure.contrib.sql)


(defn is-product?
  "判断数据库类型.
  product-key是String/keyword/symbol, 如 :sqlserver, 'oracle, \"db2\", :postgresql, :sybase, :mysql等"
  [dbname product-key]
  (has? (:driver (jdbc-map dbname)) (.toLowerCase (name product-key))))

(defn from-dbname
  "从dbconfig.clj的wr3数据源格式，得到contrib.sql所需数据源格式"
  [dbname]
  (let [m (jdbc-map dbname)
        p0 (right (:url m) "jdbc:")
        p1 (left p0 ":")
        p2 (right p0 ":")]
    {:classname (:driver m)
     :subprotocol p1
     :subname p2
     :user (:username m)
     :password (:password m)
     }))

(defn select
  "使用contrib.sql来执行sql查询
  dbname: 在dbconfig.clj中定义的名称@String
  sql: sql语句@String
  f: 以resultset为参数的函数"
  [dbname sql f]
  (with-connection (from-dbname dbname)
    (with-query-results rs [sql] (f rs))))

(defn select-all
  "取出整个结果集，如：[{:id 01 :name james} {:id 02 :name qh}]"
  [dbname sql]
  (select dbname sql (fn [rs] (into [] rs))))

(defn select-col
  "取出结果集的第一列的值，不含列名，如：[101 102 103]"
  [dbname sql]
  (select dbname sql (fn [rs] (vec (map (comp second first) rs)))))

(defn select-row
  "取出结果集的第一行，含列名，如：{:id 001 :name james :phone 13301350000}"
  [dbname sql]
  (first (select-all dbname sql)))

(defn select-one
  "取出结果集的第一行的第一列的值"
  [dbname sql]
  ((comp val first) (select-row dbname sql)))

;;;----------------------------------- 推断列类型
;;------是否id（固定长度或者递增）
(defn is-fixed-id?
  "判断是否固定长度的id；判断条件：trim后的字符串等长，没有相同元素"
  [data]
  (let [d2 (map #(.trim (str %)) data)
        c1 (apply = (map count d2))
        c2 (unique? d2)]
    (and c1 c2)))

(defn- avg [c] (/ (apply + c) (double (count c))))
(defn is-seq-id?
  "判断是否自增长的id。判断条件：没有相同元素，先后项差值基本为1"
  [data]
  (let [d2 (map #(Numberx/toLong (str %) 0) data)
        c1 (unique? d2)
        c2 (< (Math/abs (avg (map - (rest d2) (butlast d2)))) 1.2)]
    (and c1 c2)))

(defn is-date?
  "判断该列（sequence类型）是否日期类型"
  [date]
  ; tood
  false)

(def product-coll '[sqlserver oracle db2 sybase informix mysql postgresql h2])

(use '[wr3.clj.web :only (webapp)])

(defn product-logo-by-id
  "根据产品标识（:sqlserver :oracle :db2 :sybase :informix :mysql :postgresql :h2）来得到其uri地址"
  [db-key]
    (format "%s/img/database/%s.png" (or webapp "") (name db-key)))

(defn product-logo
  "根据dbname得到其产品标识图的uri"
  [dbname]
	(let [p (some #(when (is-product? dbname %) %) product-coll)]
    (product-logo-by-id (or p "other"))))

(defn test-product []
  (println (is-product? "postgre" 'postgresql))
  (println (product-logo "postgre"))
  (println (product-logo "foo"))
  )

(defn select-limit-sql
  "返回取数据前n条的原生sql语句"
  [dbname table-name col-name limit]
  (cond
    (is-product? dbname 'sqlserver) (format "select top %s %s from %s" limit col-name table-name)
    (is-product? dbname 'db2) (format "select %s from %s fetch first %s rows only" col-name table-name limit)
    (is-product? dbname 'oracle) (format "select %s from %s where rownum<=%s" col-name table-name limit)
    :else (format "select %s from %s limit %s" col-name table-name limit)))

(defn col-type
  "通过名字判断列类型（id"
  [col-name]
  (let [c (.. col-name trim toLowerCase)]
    (cond
      (or (= c "sid") (= c "id")) "ID"
      (= c "pid") "证件号"
      (or (= c "code") (= c "cod") (.endsWith c "code") (.endsWith c "cod")) "代码"
      (.endsWith c "id") "ID"
      (or (= c "type") (= c "typ") (.endsWith c "type") (.endsWith c "typ")) "类型"
      (or (= c "name") (= c "nam") (.endsWith c "name") (.endsWith c "nam")) "名称"
      (or (= c "sex") (= c "gender") (= c "xb") (.endsWith c "sex") (.endsWith c "gender")) "性别"
      :else "不推断" )))

(defn is-id?
  "判断数据库某列是否ID(定长或者序列)"
  [dbname table-name col-name]
  (let [data (select-col dbname (select-limit-sql dbname table-name col-name 1000))]
    (or (is-fixed-id? data) (is-seq-id? data))))

(defn is-code?
  "判断数据表某列是否维度（代码、分类、日期等）; 判断条件：约1000个值能被分成2类至100类"
  [dbname table-name col-name]
  (let [data (select-col dbname (select-limit-sql dbname table-name col-name 1000))
        d2 (set data)
        c1 (count data)
        c2 (count d2)]
    (and (< 1 c2 100) (< (double (/ c2 c1)) 0.2))))

(defn- is-number? [v] (try (do (Double/valueOf v) true) (catch Exception _ false)))

(defn is-measure?
  "判断某数据列是否指标; 判断条件：数字类型可计算"
  [dbname table-name col-name]
  (let [data (select-col dbname (select-limit-sql dbname table-name col-name 1000))]
    (every? is-number? data)))

(import [org.hibernate.dialect H2Dialect SQLServerDialect  PostgreSQLDialect MySQLDialect ])
(wr3.util.Logx/hibernate) ; 不打印hibernate的无聊信息

(defn ddl
  "得到dbname的tbname的指定目标数据库类型的ddl"
  [dbname tbname dbtype]
  (let [types {:h2 (H2Dialect.)
               :sqlserver (SQLServerDialect.)
               :postgresql (PostgreSQLDialect.) }]
    (with-open [dbs (dbserver dbname)]
      (let [met (.meta dbs)]
        (.ddl met tbname ((keyword dbtype) types)))
        )))

;;------------------------------------ test query sql
;(println "test query:\n" (select-col "postgre" "select * from cust limit 10"))
;(println "is-id:" (is-id? "abs" "cust" "id"))
;(println "is-product:" (is-product? 'postgre 'postgre))
;(println "is-seq-id:" (is-seq-id? [-1 -2 -3 -4 -5 -6 -7 -8 -9 -10 -12 -14]))
;(println "is-code:" (is-code? "abs" "custpn" "gender"))
;(println "is-measure:" (is-measure? "abs" "custpn" "areacode"))
;(println "col-type:" (col-type ""))
;(println "rows:" (select-one 'abs "select count(*) from cust"))
;(println "tables:" (with-open [dbs (dbserver 'abs)]
;                     (doseq [t (.tables dbs)] (println t (.rows dbs t)))))

(defn db-test2 []
  (select
    "postgre"
    "select * from cust limit 7"
    (fn [rs]
      ;(doseq [r rs] (println r))
      (println (vec (map :name rs)))
      )))
;(test2)

(defn db-test1 []
  (let [t1 (query "postgre" "select * from custpn limit 25000")]
	  ;(println (map type (last t1)))
	  (println (.rows t1))
	  (println (map type (last (.toList t1))))
	  ;(def t2 (.toList t1))
	  ;(doseq [r t2] (println (vec r)))
   )
)
;(test1)

;(println (ddl "abs" "custpn" :sqlserver))
;(test-product)
;(println (product-logo-by-id "other2"))

;(dbserver "quick-db")

(from-dbname "gold")