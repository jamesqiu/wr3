; mongodb 等 NoSQL 数据库的 utility 函数

(ns wr3.clj.nosql)

(use 'somnium.congomongo)

(defn mdb-conn [db] (make-connection (name db)))
  
(defmacro with-mdb
  "连接到本地 dbname 库进行操作。
  注意：不关闭连接，适用于简单测试，可使用返回的lazy结果；见 with-mdb2 "
  [db & body]
  `(with-mongo (make-connection (name ~db))
     ~@body))

(defmacro with-mdb2
  "连接到本地 dbname 库进行操作。
  注意：如果body执行的结果是lazy的，必须先持久化，否则报错：this Mongo has been closed "
  [db & body]
  `(let [~'mdb-conn (make-connection (name ~db))]
     (try
       (with-mongo ~'mdb-conn ~@body)
       (finally (close-connection ~'mdb-conn)) )))

(defn mdb-close [conn] (close-connection conn))
  
;------- 得到 mongodb 的:_id
(defn mdb-id
  "得到文档记录o的:_id字段的24位字符串形式, 或者把24位字符串转换成object-id.
  @o 一行含mongodb系统_id的文档记录，或者24位字符串
  @return 如果是24位字符串，返回object-id; 如果是mongodb记录对象，返回24位字符串。
  @author jamesqiu 2012-3-3"
  [o]
  (cond 
    (and (string? o) (= 24 (count o))) (object-id o) 
    (o :_id) (-> o :_id str)
    :else o))

;------- mongodb CRUD 操作

(defn mdb-add
  "为tb集合增加1条或者n条记录（每个记录是一个map），
  (with-mdb 'db'
    (mdb-add :tb1 {:name 'qh' :age 30}) 
    (mdb-add :tb1 {:name 'qh' :age 30} {:name 'james' :age 40} {:name 'qiu}) 
  )"
  [tb & m]
  (mass-insert! tb (vec m)))

(defn mdb-del
  "从tb集合删除特定条件m（一般有:_id）的记录，
  (with-mdb
    (mdb-del :tb1 p1)
    (mdb-del :tb1 {:name 'QH' :age 30}) 
  )"
  [tb m]
  (destroy! tb m))

(defn mdb-upd
  "为tb集合更新特定条件m（一般有:_id）的记录为m2，
  (with-mdb
    (mdb-upd :tb1 p1 {:name 'qh'})
    (mdb-upd :tb1 {:_id '01'} (merge p1 {:age 30})) 
    (mdb-upd :etc {:_id (mdb-id '4e4a85bcc13e8f83039edb2b')} {'$set' {:age 37}})  ; 只更新一个字段
    ; 存在则更新，不存在则增加：
    (mdb-upd :foo 
      {:_id '007'}
      {:_id '007' :name '邱' :age 37})
  )"
  [tb m m2]
  (update! tb m m2))
  
(defn mdb-get
  "选择tb集合的元素, 没有limit参数则选择全部。
  (with-mdb
    (mdb-select :task :person :limit 1)
    (mdb-select :task :person :where {:jp \"qh\"})
  )"
  [tb & options]
  (apply fetch tb options))

(defn mdb-get-one
  "得到tb集合的1行元素，"
  [tb & options]
  (apply fetch-one tb options))

;------ 其他操作

(defn mdb-del-coll
  [tb]
  (drop-coll! tb))

(def mdb-get-id fetch-by-id)

(defn mdb-group
  "进行类似group by count(*)操作。注意：出来的数字全部变成64位float。
  @tb 表名。
  @dim group by的维度[:dim1 :dim2 ..]，不能名为count，否则冲突"
  [tb dim]
  (group tb 
         :key (reverse dim)
         :initial {:count 0} 
         :reducefn "function(obj,prev){prev.count++;}"))

(defn mdb-sum
  "进行类似group by sum(..),count(*)操作。注意：出来的数字全部变成64位float。
  @tb 表名。
  @dim 维度，group by的field，如：[:dim1 :dim2 ..]，不能名为count，否则冲突
  @indic 进行sum的指标 [:score :..] 不能名为count。 "
  [tb dim indic]
  (group tb 
         :key (reverse dim)
         :initial (merge {:count 0} (zipmap indic (repeat (count indic) 0))) 
         :reducefn (format "function(obj,prev){prev.count++; %s }" 
                            (apply str (map #(format "prev['%s']+=obj['%s']; " (name %) (name %)) indic)) )))

;------ 采用 mongodb 数据的应用

(defn test-pinyin
  "从本地 mongodb 的wr3库pinyin表中获得字符串的拼音：全拼和简拼"
  [s]
  (let [db "wr3"
        tb :pinyin
        f (fn [c] (let [kv (mdb-get-one tb :where {:k (str c)})
                        v (get kv :v)]
                    (if v (apply str (butlast v)) (str c))))]
    (with-mdb2 db
      {:py (apply str (map f s)) 
       :jp (apply str (map #(first (f %)) s))} )))

(defn test-dict
  "给出中文或者英文，从 mongodb 得到对应释义（模糊查询）"
  [s]
  (let [w (.toLowerCase s)
        isword? (fn [word] (re-matches #"[a-zA-Z]+" word))
        db "wr3"
        tb (if (isword? w) "dict_ec" "dict_ce") ]
    (with-mdb2 db
      (let [rt (mdb-get tb :where {:k (re-pattern (format "%s" w))})]
        (vec (map #(dissoc % :_id) rt))))))


; ------ test
;(with-mdb :task (map #(vector (mdb-id %) (:name %)) (mdb-get :person :only [:_id :name])))
;(with-mdb :task (mdb-get-id :person "4e4215061f077e124aab1056"))
;(with-mdb :test
;  (mdb-upd :foo 
;    {:_id "007"}
;    {:_id "007" :name "邱晖2" :age 37}))
;(use 'wr3.clj.u)
;(println (dict "Hello"))
;(pinyin "中文")


