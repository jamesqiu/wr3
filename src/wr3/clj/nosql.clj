; mongodb 等 NoSQL 数据库的 utility 函数

(ns wr3.clj.nosql)

(use 'somnium.congomongo)

(defmacro with-mdb
  "连接到本地 dbname 库进行操作"
  [db & body]
  `(with-mongo (make-connection (name ~db))
     ~@body))

;------- 得到 mongodb 的:_id
(defn mdb-id
  "得到文档记录o的:_id字段的24位字符串形式, 或者把24位字符串转换成object-id"
  [o]
  (if (string? o) (object-id o) (-> o :_id str)))

;------- mongodb CRUD 操作

(defn mdb-add
  "为tb集合增加1条或者n条记录（每个记录是一个map），
  (with-mdb 
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
  [tb & limit]
  (apply fetch tb limit))

(defn mdb-get-one
  "得到tb集合的1行元素，"
  [tb & limit]
  (apply fetch-one tb limit))

;------ 其他操作

(defn mdb-del-coll
  [tb]
  (drop-coll! tb))

(defn mdb-get-id
  [tb id]
  (mdb-get tb :where {:_id (mdb-id id)}))

;------ 采用 mongodb 数据的应用

(defn pinyin
  "从本地 mongodb 的wr3库pinyin表中获得字符串的拼音：全拼和简拼"
  [s]
  (let [db "wr3"
        tb :pinyin
        f (fn [c] (let [kv (mdb-get-one tb :where {:k (str c)})
                        v (get kv :v)]
                    (if v (apply str (butlast v)) (str c))))]
    (with-mdb db
      {:py (apply str (map f s)) 
       :jp (apply str (map #(first (f %)) s))} )))

(defn dict
  "给出中文或者英文，从 mongodb 得到对应释义（模糊查询）"
  [s]
  (let [w (.toLowerCase s)
        isword? (fn [word] (re-matches #"[a-zA-Z]+" word))
        db "wr3"
        tb (if (isword? w) "dict_ec" "dict_ce") ]
    (with-mdb db
      (let [rt (mdb-get tb :where {:k (re-pattern (format "%s" w))})]
        (map #(dissoc % :_id) rt)))))


; ------ test
;(with-mdb :task (map #(vector (mdb-id %) (:name %)) (mdb-get :person :only [:_id :name])))
;(with-mdb :task (mdb-get-id :person "4e4215061f077e124aab1056"))
;(with-mdb :test
;  (mdb-upd :foo 
;    {:_id "007"}
;    {:_id "007" :name "邱晖2" :age 37}))
;(use 'wr3.clj.u)
;(print-seq (dict "Hello"))


