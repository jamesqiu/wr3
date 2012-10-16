; 项目、任务管理。
;   - 数据库采用mongodb，库为task，数据集：
;     person:  人员（姓名，简拼，全班，是否离职）
;     task:    项目任务（任务描述; 目前所有人员id; 参与开始、截至时间；包含的子项目）
; 设计: 
;   - 用mongodb和本地文件进行存储
;   - 涉及：时间、地点、人物（角色）、事由;
;   - 用Tag标签或者关键字来分组、查询
;   - 在地图上显示客户地址，先到省，每省一个写项目名，多个写项目个数，点击到市
;   - 个人用，也可授权别人输入东西
;   - 离职的人用灰色表示,可以用时间轴显示新加入和退出的变化
; todo: with-mdb 换成 with-mdb2

(ns wr3.clj.app.task)

(use 'hiccup.core)
(use 'wr3.clj.n 'wr3.clj.s 'wr3.clj.u 'wr3.clj.web 'wr3.clj.nosql)

(def dbname "task")

(defn index 
  []
  (with-mdb dbname
    (let [persons (mdb-get :person) ; 所有人{id name ..} 
          persons-id (map mdb-id persons)
          persons-name (map :name persons)
          persons-map (zipmap persons-id persons-name)
          task-order (-> (mdb-get :etc :where {:_id "task-order"}) first :order)
          task-order-map (into {} (map-indexed (fn [i e] [e i]) task-order))
          ;tasks (mdb-get :task)
          ;tasks (mapcat (fn [id] (filter #(= (mdb-id %) id) (mdb-get :task))) task-order)
          tasks (sort-by #(task-order-map (mdb-id %)) (mdb-get :task))
          persons-avoid (remove #(in? % (mapcat :persons tasks)) persons-id)
          task (fn [t] ; 得到一个task的html片段
                  [:li {:style "border: 0px solid blue; list-style-type: none"}
                   [:table {:style "border: 0px solid red"}
                    [:tr
                     [:td.tcount {:width 15 :align "right" :style "color: red"} 
                      (count (:persons t))]
                     [:td.tname {:id (mdb-id t) :title "双击可改任务名" :align "right" 
                                 :style "width: 60px; vertical-align: middle" } 
                      (:name t)]
                     [:td.task 
                      (map 
                        #(html [:div.person {:id %} (get persons-map %)]) 
                        (:persons t))]]]])
          ]
      (html-jquery
        {:onload "task_onload()"}
        [:h2 (format "%s (人数：%s)" "所有任务" (count persons)) "&nbsp;"
	        [:button#add-task "加任务" ] "&nbsp;"
	        [:button#add-person  "加人员" ]
         ]
        [:ul.avoid {:style "background-color: #ddffff"}
         (task {:_id "001" :name "可用人选" :persons persons-avoid})]
        [:ul.all
         (map task tasks) 
         ] 
        [:div.result {:title "请输入并确认: " :style "width: 300px; height: 200px"}]
        ))))
  
(defn- input-dialog 
  "app ajax: 弹出输入对话框，可进行修改或者新增
  参数id仅在修改时起作用；v参数为要录入的内容；label为提示输入的内容。"
  [id v label]
  (html
      label
      [:input {:id id :type "text" :value v}]))

(defn change-task-name 
  "app ajax: 弹出修改task名称，或增加task"
  [ids]
  (input-dialog (first ids) (second ids) "任务名称: "))

(defn change-person-name
  "app ajax: 弹出修改person名称，或增加person"
  [ids]
  (input-dialog (first ids) (second ids) "人员姓名: "))

(defn person
  "app: 人员维护, 暂未用"
  []
  (with-mdb dbname
    (let [persons (mdb-get :person)]
      (html-body
        ""
        [:table.wr3table {:border 1}
         (map 
           #(html [:tr 
                   [:td (:name %)]
                   [:td (:py %)]
                   [:td (:jp %)]
                   ]) 
           persons)]))))

(defn task-save
  "service: 保存一个task, 不处理001的可用人员集合"
  [request]
  (let [vars (query-vars request) 
        task-id (vars "id")
        task-name (vars "name")
        persons (split (vars "persons") " ")]
    ;(println (format "%s(%s): %s" task-id task-name persons))
    (with-mdb dbname 
      (if (empty? task-id)
        (mdb-add :task {:name task-name :person persons}) 
        (mdb-upd :task 
                 {:_id (mdb-id task-id)} 
                 {:name task-name :persons persons}))
      persons)))

(defn person-add 
  "service: 增加一个用户"
  [id]
  (let [id1 (or id nil)
        tb :person]
    (when id1
      (with-mdb dbname
        (mdb-add tb (into {:name id1} (test-pinyin id1))) ))))

(defn task-order-save
  "service: 保存task的顺序列表"
  [id]
  (let [id1 (or id nil)
        order (split id1 " ")]
    (with-mdb dbname
      (mdb-upd "etc"
               {:_id "task-order"}
               {:_id "task-order" :order order}))))
  
(defn task-name-save
  "service: 修改指定id的task的名称"
  [ids]
  (let [id1 (first ids)
        name1 (second ids)]
    (with-mdb dbname
      (mdb-upd :task
               {:_id (mdb-id id1)}
               {"$set" {:name name1}}))))

;;------ temp test
(import wr3.util.Datetime)

(defn t2 
  "service: 输入日期自动完成"
  [id]
  (let [s (or id "")]
    (cond
      ; 年，年头
      (or (= s "20") (= s "n") (= s "nt")) (format "%s-1-1" (year))
      ; 年尾
      (or (= s "nw")) (format "%s-12-31" (year))      
      ; 月，月头
      (or (= s "y") (= s "yt")) (format "%s-%s-1" (year) (month))
      ; 月末，月尾
      (or (= s "ym") (= s "yw")) (format "%s-%s-%s" (year) (month) (last-day))
      ; 上月今天
      (or (= s "sy")) (month-prev)
      ; 下月今天
      (or (= s "xy")) (month-next)
      ; 日，今天，today
      (or (= s "r") (= s "jt") (= s "j") (= s "t")) (date)
      ; 周x
      (or (re-matches #"z\d" s)) (let [w0 (week-day) w1 (to-int (.substring s 1))] 
                                   (Datetime/date (Datetime/add 0 0 (- w1 w0))))
                                       
      :else s
      )))
  
;------ test
(defn- task-test []
  (with-mdb dbname
    ;(map :name (mdb-get :person))
    ;(persons-map)
    ;(map #(vector (mdb-id %) (:name %)) (mdb-get :person))
    ;(let [r (mdb-get :person)] (apply hash-map (mapcat #(list (mdb-id %) (:name %)) r)))
    ;(mapcat :persons (mdb-get :task))
    ;(let [persons (persons-map)]
    ;  (map #(->> % :persons (select-keys persons)) (mdb-get :task)))
    ;(mdb-get :task)
    ;(map #(vector % (mdb-get :person {:_id (mdb-id %)})) (-> (mdb-get :task :limit 1) first :persons))
    ;(-> (mdb-get-id :person "4e4215061f077e124aab1056") first :name)
    (mdb-del :task {:name "3"})
    ;(mdb-del :task {:name "可用人选"})
    ;(mdb-add :task {:name "产品研发" :person ["何明" "郑舰"]})
    ;(mdb-del-coll :task )
    ;(let [tasks (mdb-get :task)
    ;      order ["4e450e91cf3effacaee0de60" "4e47a34ac83c0000000046d3" "4e450e97cf3effacaee0de66"]
    ;      order-map (into {} (map-indexed (fn [i e] [e i]) order))]
    ;  (sort-by #(order-map (mdb-id %)) tasks))      
    ;(mapcat (fn [id] (filter #(= (mdb-id %) id) tasks)) order) )
    ;(-> (mdb-get :etc :where {:_id "task-order"}) first :order)
    ;(mdb-add :etc {:name "qh" :age 30})
    ;(mdb-upd :etc {:_id (mdb-id "4e4a85bcc13e8f83039edb2b")} {"$set" {:age 37}})
      
    )
  
    ;(with-mdb "wr3" (-> (mdb-get "pinyin" :limit 1 :where {:k "中"}) first mdb-id) ) 
  )
;(task-test)


