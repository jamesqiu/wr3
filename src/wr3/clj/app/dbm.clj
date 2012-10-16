;;;; 数据库业务数据分析管理(DataBase Management)
(ns wr3.clj.app.dbm)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.web 'wr3.clj.chart 'wr3.clj.db 'wr3.clj.meta)
(use 'wr3.clj.dbconfig)
(use 'hiccup.core)
(import wr3.db.DbServer)

;(def dbs (dbserver "abs"))
;(def tables (.tables dbs))

(defn- <tr>
  "从sequence生成如<tr><th>..</th><th>..</th></tr>的html片段,
  如果sequence中的元素为map类型如{:v '100' :title 'foo..' :style 'color: red'}之类，取出:v的值为内容，其他作为属性。"
  [r th-td]
  (html [:tr (for [e r] (if (map? e)
                          [th-td (dissoc e :v) (get e :v)]
                          [th-td e])) ]))

(defn index
  "app: 分析特定一个数据库"
  [id]
  (let [dbname (or id "bank")] ; postgre, abs, mssql/bank
    (with-open [dbs (dbserver dbname)]
      (let [tables (.tables dbs)
            db (.database dbs)]
        (html
          [:html head-set
           [:body {:onload "dbm_onload();"}
            [:div {:style "float: left"}
             [:img  {:src (product-logo dbname)}][:br]
             [:h2 "数据源：" [:br] dbname [:br] (format "(%s库)" (meta-name dbname))]
             [:h2 "当前数据库：" [:br] db]]
            [:table#t01 {:border 1 :class "wr3table" :style "float: left; margin: 10px"}
             [:caption
              [:span#dbname dbname] ; js将读取该dbname
              [:span " 的表: " (count tables)] " -> "
              [:a {:href "#" :onclick "$(\"table#t01\").tablesorter();"} "点列头排序"]]
             [:thead (<tr> '(序号 描述 表名 精确记录数 大约记录数) :th)]
             [:tbody
              (map-indexed
                (fn [i t] [:tr
                           [:td (inc i)]
                           [:td.meta (meta-name t)]
                           [:td.tbname [:a {:href "#"} t]]
                           [:td.tbrows {:align "right"} [:img {:src "/img/loading.gif"}]]
                           [:td.tbrows2 {:align "right"} ".."]
                           ])
                tables)
              ]
             [:tfoot [:tr [:td {:colspan 5 :align "center"} [:i "-- IDP-WebReport --"]]]]]
            [:div#cols {:style "float: left;"}]
            ]])))))

(defn rows
  "service: 传入库名/表名，返回记录数"
  [ids]
  (let [dbname (get-id ids "abs")
        tbname (or (second ids) "cust")]
    (with-open [dbs (dbserver dbname)]
      (let [n (.rows dbs tbname)
            a (wr3.bank.Currency/about n)]
        (str n " " a)))))

(defn- meta-map
  "得到列名和属性的map：
{'pid' {:sid 1 :meta '证件号' :type 'varchar' :len 21},
 'column2' {..},
 ..}"
  [dbs tbname]
  (let [t (.. dbs meta (columns tbname) toList)]
    (into {} (for [r (rest t)]
               (let [sid (first r)
                     nam (second r)
                     mt (meta-name nam)
                     len (nth r 3)
                     typ (.toLowerCase (last r))]
                 [nam {:sid sid :meta (meta-name nam) :type typ :len len}] )))))

(defn cols
  "service: 传入库名/表名，返回列类型"
  [ids]
  (let [dbname (get-id ids "abs")
        tbname (or (second ids) "cust")]
    (with-open [dbs (dbserver dbname)]
      (let [m (meta-map dbs tbname) 
            d (select-row dbname (select-limit-sql dbname tbname "*" 1))] ; 表第一行数据
        (html
          (when (> (count m) 30) 
            (html
              [:a {:target "_blank"
                   :href (format "%s/c/dbm/data/%s/%s?html" webapp dbname tbname)} "查看更多数据"][:br]))
          [:table {:class "wr3table" :border 1}
           [:caption [:h1 tbname " 的列"]]
           [:tbody
            (<tr> '(序号 列名 描述 类型 数据示例) :th)
            (for [[k v] m]
              (let [sid (:sid v)
                    nam (if (dd-map tbname k) 
                          {:v [:b k] :title "是数据字典"} ; 是数据字典的则标为粗体 
                          k) 
                    met (:meta v)
                    type-len (format "%s (%d)" (:type v) (:len v))
                    dat (find-first (fn [[k2 v2]] (.equalsIgnoreCase (name k2) k)) d)]
                (<tr> [sid nam met type-len dat] :td)))
            ]
           [:tfoot [:tr [:td {:colspan "5" :align "center"}
                         [:a {:target "_blank"
                              :href (format "%s/c/dbm/data/%s/%s?html" webapp dbname tbname)} "查看更多数据"]]]]
           ])))))

(defn dview
  "service: 传入库名/表名，返回前100条数据"
  [ids request]
  (let [dbname (get-id ids "abs")
        tbname (or (second ids) "cust")]
    (with-open [dbs (dbserver dbname)]
      (let [m (meta-map dbs tbname)
            rt (select-all dbname (select-limit-sql dbname tbname "*" 100))
            fn1 (fn ^{:doc "表头一个th的内容：列名+meta+类型"} [kv] 
                  (let [th (name (key kv))
                        mt (some (fn [[k2 v2]] (when (.equalsIgnoreCase k2 th) v2)) m)] ; (m th)
                    (format "%s<br/>%s<br/>%s(%s)" th (:meta mt) (:type mt) (:len mt))))
            head (concat ["序号"] (map fn1 (first rt)))
            data (map-indexed (fn [i r] (concat [(inc i)] (map val r))) rt)
            table-html (html
                         [:table {:border 1 :class "wr3table"}
                          [:thead (<tr> head :th)]
                          [:tbody (for [r data] (<tr> r :td))]]
                         [:script "$('table.wr3table').tablesorter();"])] ; 增加点击表头排序功能
          table-html  ))))
  
(defn- jqueryui-wrap 
  "使用jquery ui进行格式化"
  [table-html]
  (html
    [:html head-set
     [:body {:onload ""}
      [:div#example [:ul
                     [:li [:a {:href "#t1"} [:span "数据展示"] ]]
                     [:li [:a {:href "/c/dbm/service2"} [:span "列分析"] ]]]
       [:div#t1 table-html]]
      [:script "$('#example').tabs();"]]]))
  
(defn data
  "service: 传入库名/表名，返回前100条数据"
  [ids request]
  (let [dbname (get-id ids "abs")
        tbname (or (second ids) "cust")]
    (with-open [dbs (dbserver dbname)]
      (let [m (meta-map dbs tbname)
            rt (select-all dbname (select-limit-sql dbname tbname "*" 100))
            fn1 (fn [kv] (let [th (name (key kv))
                               mt (some (fn [[k2 v2]] (when (.equalsIgnoreCase k2 th) v2)) m)] ; (m th)
                           (format "%s<br/>%s<br/>%s(%s)" th (:meta mt) (:type mt) (:len mt))))
            head (concat ["序号"] (map fn1 (first rt)))
            data (map-indexed (fn [i r] (concat [(inc i)] (map val r))) rt)
            table-html (html
                         [:table {:border 1 :class "wr3table"}
                          [:thead (<tr> head :th)]
                          [:tbody (for [r data] (<tr> r :td))]]
                         [:script "$('table.wr3table').tablesorter();"])]
        (if ((query-vars request) "html") ; 如果带&html参数则显示jquery ui；否则显示原始html
          (jqueryui-wrap table-html)
          table-html)))))

(defn quick
  "app: 快速进行数据库连接，如连本地库"
  []
  (html
    [:html head-set
     [:body {:onload "quick_onload();"}
      [:div
       (for [p '[sqlserver postgresql h2 mysql]] [:img.logo {:src (product-logo-by-id p) :alt (str p)}])]
      [:hr]
      [:div#in]
     ]]))

(def quickin-default ; 用于quickin，缺省数据库连接配置
  {:sqlserver {:driver (:sqlserver drivers)
               :url (gstring (:sqlserver urls) {:ip "localhost:1433" :db "master"})
               :username "sa"
               :password "password"}
   :postgresql {:driver (:postgresql drivers)
             :url (gstring (:postgresql urls) {:ip "localhost:5432" :db "postgres"})
             :username "postgres"
             :password "password"}
   :h2 {:driver (:h2 drivers)
        :url (gstring (:h2 urls) {:db "testdb"})
        :username ""
        :password ""}
   :mysql {:driver (:mysql drivers)
           :url (gstring (:mysql urls) {:ip "localhost:3306" :db ""})
           :username "root"
           :password "root"}
   })

(defn quickin
  "service: 快速输入连接各种数据库的配置，quick input"
  [id]
  (let [ptype (or id "h2")
        pm (quickin-default (keyword ptype))]
    (html
      [:style "table.in tr td {vertical-align: middle}"]
      [:h2 ptype]
      [:table.in
       [:tr
        [:td "驱动: "]
        [:td [:label#driver {:style "color: gray"} (:driver pm)]]]
       [:tr
        [:td "地址: "]
        [:td [:input#url {:type "text" :value (:url pm) :size 100}]]]
       [:tr
        [:td "用户: "]
        [:td [:input#username {:type "text" :value (:username pm) :size 20}]]]
       [:tr
        [:td "密码: "]
        [:td [:input#password {:type "text" :value (:password pm) :size 20}]]]
       [:tr
        [:td {:colspan 2}
         [:button#submit {:style "font-family: 微软雅黑;font-size: 18px"} "好了，进行连接..."]
         [:span#result {:style "padding-left: 10px; font-size: 14px"}]]]
       ]
      [:script "quickin_submit();"]
      )))

(defn quickin_dbname
  "service: 设置quick_dbname于Preferences"
  [request]
  (let [qs (query-vars request)
        nam "quick-db"]
    ; 以"quick-db"名称保存到Preference中，有则覆盖
    (quick-dbname nam {:driver (qs "driver")
                       :url (qs "url")
                       :username (qs "username")
                       :password (qs "password")})
    ;读取测试连接
    (let [conn (jdbc-conn nam)]
      (if conn (do (.close conn) 1) -1)
    )))

(import wr3.util.Stringx)
(defn jquery-tabs
  "传入一个表示tabs标签名和内容（或链接）的map, 如：
   [{:label 'T1' :link '#t1' :content '...'} {:label 'T2' :link '/s1'}]，
   生成jquery ui的tabs"
  [tabs]
  (let [id (Stringx/uid)]
    (html
      [:div {:id id}
       [:ul
        (for [tab tabs] [:li [:a {:href (:link tab)} [:span (:label tab)]]])]
       (for [tab tabs :when (.startsWith (:link tab) "#")] [:div {:id (.substring (:link tab) 1)} (:content tab)])
       ]
      [:script (format "$('#%s').tabs();" id)])))

(defn test1 []
  (html
    [:html head-set
     [:body (jquery-tabs [{:label "Content 1" :link "#t1" :content "cn中文"}
                          {:label "content 2" :link "/c/dbm/service2"}
                          {:label "第三个" :link "#t3" :content "...<br/>..."}])]]))

(defn test2
  "测试 jquery tabs的原始写法"
  []
  (html
    [:html head-set
     [:body {:onload "$('#example').tabs();"}
      [:div {:id "example"}
       [:ul
        [:li [:a {:href "#t1"} [:span "Content 1"]]]
        [:li [:a {:href "./service2"} [:span "Content 2"]]]
        [:li [:a {:href "#t3"} [:span "Content 3"]]]
        ]
       [:div#t1 "cn中文"]
       [:div#t3 "33333333333333"]
       ]]]))

(defn service2 []
  (html
    (for [i (range 10)] [:h2 "this is service2"])))

