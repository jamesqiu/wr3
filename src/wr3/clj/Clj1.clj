;;;; 含-main方法可运行的.clj
(ns wr3.clj.Clj1 
  (:gen-class))
 
(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.tb 'wr3.clj.db) 
(import '(wr3.db DbServer RowFilter) '(wr3 Row) '(tool Pinyin) '(wr3.bank IDUtil))

(defn npn
  "(pow n n)"
  [n]
  (apply * (repeat n n)))

(defn test1 []
	(def t1 (table 5 3))
	(println (table-string t1))
)

;;------------------- cbs400 导出数据
(def sql
  "select id,pid,name from cust where type='1'")
(defn namejp [name] 
  (let [pinyin (Pinyin/instance)
        jp (.jp pinyin name)] 
    (if (> (count jp) 3) (.substring jp 0 3) jp)))

(defn line [id pid name] (format "%s\t%s\t%s\t%s\t%s\t%s\t%s\t%s"
                           id
                           pid
                           (namejp name)
                           (-> pid (.substring 0 6))                    ;areacode
                           (-> pid (.substring 6 10) Integer/parseInt)  ;year
                           (-> pid (.substring 10 12) Integer/parseInt) ;month
                           (-> pid (.substring 12 14) Integer/parseInt) ;day
                           (-> pid (.substring 16 17) Integer/parseInt  (mod 2));gender
                           ))
(defn cbs400
  "从postgresql的cbs400库中导出数据"
  []
  (let [con (jdbc-conn "postgre")
        dbs (DbServer/create con)
        i (atom 0) ; 计算有正常身份证的用户数目
        row-filter (proxy [RowFilter] []
                     (process [row]
                       (let [id (.toString (.get row 0))
                             pid0 (.toString (.get row 1))
                             name (.toString (.get row 2))
                             pid (IDUtil/to18 pid0)]
                         (do
                           (when pid (do (swap! i inc) (println (line id pid name))))
                           true) )))]
    ;(println (.query dbs sql))
    (doto dbs
      (.process sql row-filter)
      (.close))
    (println "i=" @i)    ))

(defn -main
  [& args]
  (println "------ 测试 ------c")
 ; (println (npn 4))
 ; (println "(power 9 9) =" (npn 9))
 ; (use 'wr3.clj.s)
 ; (println (map capitalize (split "hello clojure in eclipse" " ")))
  (test1)
 ;(cbs400)
)


