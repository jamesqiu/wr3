;;;;--------------- mongodb 管理
;;;; todo: 进行条件查询
(ns wr3.clj.app.mdb)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.web 'wr3.clj.chart 'wr3.clj.db)
(use 'wr3.clj.nosql)
(use 'hiccup.core)
(use 'somnium.congomongo)

(defn index
  "app: 本地mongodb的管理界面"
  []
  (with-mdb2 "test"
    (let [dbs (sort (databases))
          f (fn [dbname] (format "<b>%s</b> (%s)"
                                 (html [:a.db {:href "#"} dbname])
                                 (with-db dbname (count (collections)))))]
      (html-body
        {:onload "mdb_onload()"}
        [:img {:src (format "%s/img/database/mongodb.png" webapp)}]
        [:div {:style "float: right"}
         [:h2 "对meta库的dict表进行管理（增、删、改）"]
         [:a {:href (format "%s/c/mdb/dict" webapp)} "点击进入"]]
        [:h2 "数据库: "]
        [:div (join (map f dbs) " | ")]
        [:div#collections "..."]
        [:div#data "..."] ))))

(defn db
  "service: 给定db，给出collections及记录数，并按名称及记录数排序"
  [id]
  (with-mdb2 (or id "local")
    (let [dbs (databases)
          db0 (or id "locale")
          db (if (in? db0 dbs) db0 "local")
          dbc (into {} (for [c (collections)] [c (fetch-count (keyword c))]))
          dbc2 (sort-by key dbc)
          f (fn [c] (for [[k v] c] (format "<b>%s</b> (%s)"
                                           (html [:a.coll {:href "#" :onclick (format "mdb_data(this, '%s','%s');" db k)} k]) v)))]
      (html
        [:h2 (format "[%s] 的集合表: " db)]
        [:div (join (f dbc2) " &nbsp;|&nbsp; ")]) )))

(defn data
  "service：给定db和collection，得到前1000条数据"
  [ids]
  (let [dbname (first ids)
        collection (second ids)
        limit 1000]
    (with-mdb2 dbname
      (html
        [:h2 (format "[%s.%s]的数据(前%s条)：" dbname collection limit)]
        (for [r (fetch (keyword collection) :limit limit)] 
          (html [:div (str (update-in r [:_id] #(str %)))]))))))

;;;--------------------- meta/dict管理
(defmacro with-dict
  "连接到meta库的dict表进行操作; 在调用中可以直接使用以下变量：dbname，tbname"
	[& body]
	`(let [~'dbname "meta"
        ~'tbname :dict
        ~'conn (make-connection ~'dbname)]
    (try
      (with-mongo ~'conn ~@body)
      (finally (close-connection ~'conn)))))

(defn dict
  "app: 对meta库的dict表进行管理（增、删、改）"
  []
  (with-dict
    (let [tb (fetch tbname)
          style {:style "font-family: Consolas"}]
      (html
        [:html head-set
         [:body {:onload "dict_onload();"}
          [:div style
           [:button.create "新增"]" "
           [:input {:type "text" :size 150 :value "{:code \"\" :name \"\" :type \"db\" :etc \"\"}"}]]
          [:br]
          (map-indexed
            (fn [i r]
              [:div style
               [:span (format "(%s) " (inc i))]
               [:span.id {:style "color:gray"} (-> r :_id str)]"&nbsp;"
               [:button.delete "删除X"]"&nbsp;"
               [:button.update "更新U"]
               [:input {:type "text" :value (dissoc r :_id) :size 150}]
               ])
            tb)
          ]]))))

(defn create
  "service: 传入{..}，创建新条目"
  [id]
  (let [m (read-string id)]
    (with-dict
      (insert! tbname m))))

(defn update
  "service: 传入id和{:name ..}，进行update该id的值"
  [ids]
  (let [id1 (first ids)
        m (read-string (second ids))]
    (with-dict
      (let [o (fetch-one tbname :where {:_id (object-id id1)})]
        (update! tbname o m)
        ))))

(defn delete
  "service: 传入id，删除该条记录"
  [id]
  (let [id1 id]
    (with-dict
      (destroy! tbname {:_id (object-id id1)}))))

(require 'wr3.clj.meta)
(defn temp
  "临时使用"
  []
  (println 
    (with-dict 
      (map #(list (:code %) (:name %) (:type %)) (fetch tbname :where {:code "org"}))
;      (destroy! :dict {:type "fd"})
      ))

;  (with-dict 
;    (println (count wr3.clj.meta/dict))
;    (doseq [[k v] wr3.clj.meta/dict]
;      (when (not= (get (first v) :type) 'db) 
;        (let [code1 (name k) 
;              first1 (first v) 
;              name1 (first1 :name) 
;              type1 (name (first1 :type))]
;          (insert! :dict {:code code1 :name name1 :type type1})
;          (println code1 name1 type1)
;          ))))

  (println "ok"))

;(temp)
