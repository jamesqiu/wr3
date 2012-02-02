; mongodb �� NoSQL ���ݿ�� utility ����

(ns wr3.clj.nosql)

(use 'somnium.congomongo)

(defmacro with-mdb
  "���ӵ����� dbname ����в���"
  [db & body]
  `(with-mongo (make-connection (name ~db))
     ~@body))

;------- �õ� mongodb ��:_id
(defn mdb-id
  "�õ��ĵ���¼o��:_id�ֶε�24λ�ַ�����ʽ, ���߰�24λ�ַ���ת����object-id"
  [o]
  (if (string? o) (object-id o) (-> o :_id str)))

;------- mongodb CRUD ����

(defn mdb-add
  "Ϊtb��������1������n����¼��ÿ����¼��һ��map����
  (with-mdb 
    (mdb-add :tb1 {:name 'qh' :age 30}) 
    (mdb-add :tb1 {:name 'qh' :age 30} {:name 'james' :age 40} {:name 'qiu}) 
  )"
  [tb & m]
  (mass-insert! tb (vec m)))

(defn mdb-del
  "��tb����ɾ���ض�����m��һ����:_id���ļ�¼��
  (with-mdb
    (mdb-del :tb1 p1)
    (mdb-del :tb1 {:name 'QH' :age 30}) 
  )"
  [tb m]
  (destroy! tb m))

(defn mdb-upd
  "Ϊtb���ϸ����ض�����m��һ����:_id���ļ�¼Ϊm2��
  (with-mdb
    (mdb-upd :tb1 p1 {:name 'qh'})
    (mdb-upd :tb1 {:_id '01'} (merge p1 {:age 30})) 
    (mdb-upd :etc {:_id (mdb-id '4e4a85bcc13e8f83039edb2b')} {'$set' {:age 37}})  ; ֻ����һ���ֶ�
    ; ��������£������������ӣ�
    (mdb-upd :foo 
      {:_id '007'}
      {:_id '007' :name '��' :age 37})
  )"
  [tb m m2]
  (update! tb m m2))
  
(defn mdb-get
  "ѡ��tb���ϵ�Ԫ��, û��limit������ѡ��ȫ����
  (with-mdb
    (mdb-select :task :person :limit 1)
    (mdb-select :task :person :where {:jp \"qh\"})
  )"
  [tb & limit]
  (apply fetch tb limit))

(defn mdb-get-one
  "�õ�tb���ϵ�1��Ԫ�أ�"
  [tb & limit]
  (apply fetch-one tb limit))

;------ ��������

(defn mdb-del-coll
  [tb]
  (drop-coll! tb))

(defn mdb-get-id
  [tb id]
  (mdb-get tb :where {:_id (mdb-id id)}))

;------ ���� mongodb ���ݵ�Ӧ��

(defn pinyin
  "�ӱ��� mongodb ��wr3��pinyin���л���ַ�����ƴ����ȫƴ�ͼ�ƴ"
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
  "�������Ļ���Ӣ�ģ��� mongodb �õ���Ӧ���壨ģ����ѯ��"
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
;    {:_id "007" :name "����2" :age 37}))
;(use 'wr3.clj.u)
;(print-seq (dict "Hello"))


