;;;; Form/Document 模型的 CRUD，使用本地 mongoDB 存储
;;;; mongoDB 的"crud"数据库中，"fm_" 开头的collection用来记录各个字段；对应名称的 collection 用来记录各个文档.
;;;; 如：集合 fm_custInfo 中含字段说明； 集合 custInfo 中含所有用 fm_custInfo 创建的文档。
;;;; todo: 指定其他模板来显示文档
(ns wr3.clj.app.crud)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.meta)
(use 'hiccup.core)
(use 'somnium.congomongo)
(import wr3.util.Datetime)
  
;;-------- 例子：如何表达 fm_form1 的所有字段
(def fm_form1 [{:name "custname" :label "客户名" :type "input" :order 100}
               {:name "custtype" :label "客户类型" :type {1 "个人" 2 "企业"} :order 200}
               {:name "date" :label "开户日期" :type "input" :value "2011/6/3" :order 300}
               {:name "password" :label "密码" :type "password" :value "pass" :order 400}
               {:name "agree" :label "是否同意？" :type {"y" "同意该条款"} :order 500}
               ])

(defn- forms
  "得到crud数据库的所有form"
  []
  (let [db "crud"
        conn (make-connection db)]
    (with-mongo conn
      (collections))))

(defn index
  "app: crud 操作首页，可设计form，可查看view"
  []
  (let [fms1 (remove-e (forms) "system.indexes")
        fms2 (group-by #(.startsWith % "fm_") fms1)
        fm_forms (fms2 true)
        forms (fms2 false)]
  (html-body 
    ""
    [:div#design 
     [:h2 "设计器"]
     (for [f fm_forms] 
       [:div [:a {:href (format "%s/c/crud/design/%s" webapp (.substring f 3))} f]])]
    [:br][:hr]
    [:div#doc 
     [:h2 "文档视图"]
     (for [f forms] 
       [:div [:a {:href (format "%s/c/crud/view/%s" webapp f)} f]])]     
     )))

(defn- get-form
  "从mongodb得到form各字段的设置"
  [fm]
  (with-mongo (make-connection "crud")
    (fetch fm :sort {:order 1})
    ))

(defn- get-doc
  "从form得到指定id的doc"
  [form id]
  (when id
    (with-mongo (make-connection "crud")
      (fetch-by-id form (object-id id)))))

(defn- html-field
  "由field设置得到其html片段, [f] ：form新建doc的字段，[f v]: doc已有的字段"
  ([f]
    (let [n (:name f)
          t (or (:type f) "input")
          v2 (:value f)]
      (cond
        ;--- password
        (= t "password")
        [:input {:name n :type "password" :value v2}]
        ;--- checkbox
	      (and (map? t) (= 1 (count t)))
	      [:input {:name n :type "checkbox" :value (key (first t))} (val (first t))]
        ;--- select
	      (map? t)
	      [:select {:name n} (for [[k v] t]
                            [:option {:value k} (format "%s - %s" (name k) v)])]
        ;--- text
        :else
	      [:input {:name n :type "text" :value v2}]
	      )
      ))
  ([f v0]
    (let [n (:name f)
          t (or (:type f) "input")
          v2 v0]
      (cond
        ;--- password
        (= t "password")
        [:input {:name n :type "password" :value v2}]
        ;--- checkbox
	      (and (map? t) (= 1 (count t)))
	      [:input {:name n :type "checkbox" :value (key (first t))
                (if v2 :checked :foo) "checked"} (val (first t))]
        ;--- select
	      (map? t)
	      [:select {:name n} (for [[k v] t]
                            [:option {:value k
                                      (if (= v2 (name k)):selected :foo) "selected"}
                             (format "%s - %s" (name k) v)])]
        ;--- text
	      :else
	      [:input {:name n :type "text" :value v2}]
          )))
  )

(defn form
  "app: crud/form/fm1/001, 根据form名创建一个新文档，或者显示给定id号的文档"
  [ids]
  (let [fm (get-id ids "custInfo")
        sid (second ids)
        doc2 (get-doc fm sid)]
    (html-body 
      {:onload "crud_form_onload()" :js "app-crud.js"}
      [:h2 [:a {:href (format "%s/c/crud/view/%s" webapp fm)} "列表"]]
      [:form.wr3form {:action (format "%s/c/crud/save" webapp)}
       [:input {:name "form" :type "hidden" :value fm}]
       (when doc2 [:input {:name "_id" :type "hidden" :value sid}])
       [:table {:border 0}
        [:caption (meta-name fm)]
        [:tbody
         (for [f (get-form (str "fm_" fm))]
           (html [:tr
                  [:td (:label f) ": "]
                  [:td (if-not doc2 (html-field f) (html-field f (get doc2 (keyword (:name f)))))] ]))]
        [:tfoot
         [:tr {:align "center"} [:td {:colspan 2}
                                 [:input {:type "submit" :value (if doc2 " 更新文档 " " 新建文档 ")}]"&nbsp;"
                                 [:input {:type "reset" :value " 重置为初始值 "}]"&nbsp;"
                                 (when doc2 [:input {:type "button" :value " 删 除 "}])"&nbsp;"
                                 ]]]]
       [:br]
       [:div "提示信息：" [:span#msg ""]]
       ])))

(defn design
  "app: 设计更改form的字段, 整体更新，可拖拽更改顺序，双击插入或者附加。"
  [id]
  (let [fm (or id "custInfo")]
    (with-mongo (make-connection "crud")
      (let [fs (fetch (str "fm_" fm) :sort {:order 1})
            style1 {:style "border: 1px dashed blue; padding: 5px;"}
            style2 {:style "border: 0px solid blue"}
            style3 {:style "border: 1px dashed gray; padding: 5px; color: gray; background-color: #f0f0f0;"}
            fn1 (fn [l n t] (html [:li style2 "&nbsp;"
                                   "label: " [:input {:value l :size 15}]" "
                                   "name: " [:input {:value n :size 15}]" "
                                   "type: " [:input {:value t :size 30}]] ))
            fn2 (fn [f] (let [name2 (:name f)
                              label2 (:label f)
                              type2 (:type f)
                              order2 (:order f)]
                          (fn1 label2 name2 type2)))
            ]
        (html-body 
          {:onload "crud_design_onload()" :js "app-crud.js"} 
          [:h2 (format "%s (%s)" fm (meta-name fm))][:br]
          [:div [:button#add " 增加字段 "]][:br]
          [:div#add {:style "display:none"} (fn2 {:label "" :name "" :type ""})][:br]
          [:form#design {:action "/c/crud/design-save"}
           [:input {:type "hidden" :name "form" :value fm}]
           [:input {:type "hidden" :name "size" :value "0"}]
           [:ul.sort style1 "字段：" (map fn2 fs)]
           [:input#submit {:type "submit" :value "提交表单设置"}]"&nbsp;"
           [:input {:type "reset" :value "重置为初始值"}]"&nbsp;"
           ]
          [:br]
          [:ul.sort style3 "垃圾箱："]
          [:button#clear "清空垃圾箱"]
          )))))

(defn design-save
  "service: 保存form的字段设计"
  [request]
  (let [m (query-vars request)
        size (Integer/parseInt (m "size"))
        form (str "fm_" (m "form")) ; 注意：要加"fm_"前缀
        form-fs (vec (for [i (range size)] 
                       (let [n (.trim (m (str "name_" i)))
                             l (.trim (m (str "label_" i)))
                             t0 (.trim (m (str "type_" i)))
                             t (if (and (.startsWith t0 "{") (.endsWith t0 "}")) (read-string t0) t0)]
                         {:name n :label l :type t :order (inc i)})))
        ]
    (with-mongo (make-connection "crud")
      (let [ds (drop-coll! form)
            mi (mass-insert! form form-fs)]
        "设计保存完成"
        ))))

(defn save
  "service: 保存当前form编辑的doc"
  [request]
  (let [m (query-vars request)
        form (m "form")
        update? (contains? m "_id")
        _id (m "_id")]
    ;(println m)
    (with-mongo (make-connection "crud")
      (if update?
        (do (update! form {:_id (object-id _id)} (dissoc m "form" "_id"))
          (html (format "更新成功! (%s)" (Datetime/datetime))))
        (do (insert! form (dissoc m "form"))
          (html (format "添加成功! (%s) | " (Datetime/datetime))
                [:a {:href (format "%s/c/crud/form/%s" webapp form)} "继续新建文档"]))))))

(defn delete
  "service: "
  [request]
  (let [m (query-vars request)
        form (m "form")
        _id (m "_id")]
    (with-mongo (make-connection "crud")
      (destroy! form {:_id (object-id _id)}) )
    (format "完成删除！\n%s (%s)" form _id)
    ))
  
(defn view
  "app: 显示指定form的所有doc的列表（前1000条）"
  [id]
  (let [fm (or id "custInfo")
        form (get-form (str "fm_" fm))
        heads (map :name form)
        head-labels (map :label form)]
    (with-mongo (make-connection "crud")
      (let [rt (fetch fm :limit 1000)]
        (html-body {:onload "crud_view_onload()" :js "app-crud.js"}
            [:table.wr3table {:border 1}
             [:caption (format "\"%s\" 列表" (meta-name fm))]
             [:thead [:tr [:th "序号"] (for [e form] [:th {:title (:name e)} (:label e)]) [:th "操作"] ]]
             [:tbody
              (map-indexed 
                (fn [i r] [:tr {:title "双击查看"}
                           [:td (inc i)]
                           (for [e heads] [:td (r (keyword e))])
                           [:td.view [:a {:href (format "%s/c/crud/form/%s/%s" webapp fm (:_id r))} "查看"]] ])
                rt) ]
             [:tfoot [:tr [:td {:colspan (+ 2 (count heads))} [:a {:href (format "%s/c/crud/form/%s" webapp fm)} "新增文档"]]]]
             ] )))))
