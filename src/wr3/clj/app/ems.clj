(ns wr3.clj.app.ems)
;;;; 应急指挥管理系统demo：中国黄金集团cnggc, 中国有色集团cnmc

(use 'clojure.contrib.json)
(use 'hiccup.core)
(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.file 'wr3.clj.tb)
(use 'wr3.clj.web 'wr3.clj.gmap 'wr3.clj.db)
(use '[wr3.clj.app.chartf :only (pie)])
(use 'wr3.clj.app.emsconf :reload) ; reload 才能使配置更改生效

(defn gmap
  "app: 调用google map页面，执行main.js中如ems_gorgs(), eui_gorgs2()的js函数"
  [ids]
  (googlemap (format "ems_%s('%s')" (first ids) (or (second ids) ""))))

(defn- sql-data
  "得到[[c1 c2] [] [] .. ]格式数据"
  [sql]
  (let [dbname "cnggc"
        data (query dbname sql)]
    (wr3-table data)))
    
(defn- dd-tailings
  "从dd_tailing尾矿库字典表得到code和name的hashmap"
  [dd-type]
  (let [t (sql-data (format "select code,name from dd_tailings where type='%s'" dd-type))]
    (into {} (rest t))))

(defn data1 
  "service: 得到org所有企业的名称、坐标、位置的json数据"
  []
  (json-str (rest (sql-data "SELECT [name],[x],[y],[address] FROM [org] where x is not null order by x desc"))))

(defn data2 
  "service: 得到org某省企业的名称、坐标、位置的json数据"
  [id]
  (let [id0 (or id "北京")
        sql (format "SELECT [name],[x],[y],[address] FROM [org] where x is not null and province='%s' order by x desc" id0)]
    (json-str (rest (sql-data sql)))))

(defn data3 
  "service: 得到各企业id、坐标(x,y)、尾矿库数量的json数据"
  []
  (let [sql (str "select a.orgid,b.name,b.x,b.y, COUNT(*) c"
                 " from tailings a join org b on a.orgid=b.orgid "
                 " group by a.orgid,b.name,b.x,b.y order by COUNT(*) desc")]
    (json-str (rest (sql-data sql)))))

(defn data4 
  "service: 得到指定id的企业名称、坐标(x,y)、邮编、地址json数据"
  [id]
  (let [id0 (or id "ORG0000001")
        sql (format "select name,x,y,postcode,address,province from org where orgid='%s'" id0)]
    (json-str (rest (sql-data sql)))))

(defn- app-main
  "layout.center，main这个region是必须的。"
  []
  (eui-region 
    "center" 
    {:border "false" :style "padding: 2px"} 
    [:iframe {:id "ifrm1" :src (format "%s/c/ems/gmap/gorgs" webapp) 
              :width "99%" :height "99%" :style "border: 1px dotted gray"}] ))

(defn- app-right
  "layout.east"
  []
  (eui-region 
    "east" 
    {:title "安全生产运行管理" :style "width: 300px"} 
    ))

(defn- group
  "集团统计视图的html片段"
  []
  (let [t1 (sql-data "select count(*) from org")]
    (html
      [:div "集团公司应急指挥中心——应急响应中心应急电话："
       "集团公司内部电话拨打 <font color=red><u>2095</u></font>，<br/>"
       "外部电话拨打 <font color=red><u>010-84116377</u></font><br/>"
       "今日安全值班：张景奎 (<font color=red>13911274651</font>)"][:br]
      [:div
       [:a {:href "/static/plan-2010.html" :target "_blank"} "总体应急预案"] " | "
       [:a {:href "/static/plan-2010.html#_Toc310460372" :target "_blank"} "专项应急预案"]][:br]
      [:label "黄金集团下属企业："] (str (first (second t1)) " 家")
      [:a#map0 {:href "#" :title "查看位置"} "(地图显示)"] [:br]
      [:div {:style "" :title "点击图形查看该地区企业"} ]
      [:h2 {:title "全库容≥100万m3或者坝高≥30 m的尾矿库" :style "color: red"}
       [:img {:src "/img/alert.ico"}]" "
       (format "重大危险源（尾矿库）：%s 个" 42)] 
      [:a#gtail {:href "#"} " (地图显示)"]
      )))

(defn- org
  "所有机构名的html片段"
  []
  (let [t (rest (sql-data "SELECT orgid,name,province FROM org where x is not null order by province"))
        t2 (sql-data "select province, COUNT(*) from org where province is not null group by province order by COUNT(*) desc") 
        f (fn [r] (html (first r) ": "
                        [:img {:class "province" :province (first r) :src "/img/vline.png" 
                               :height 10 :width (* 10 (second r))}] " " 
                        (second r) "家" [:br]))]
    (html
      [:h4 "各省公司数量："]
      (map f (rest t2))
      [:h4 "公司列表："]
      (map-indexed 
        (fn [i e] (html 
                    [:a {:class "org" :id (first e) :href "#" :title (str "在地图上进行定位" (first e))} 
                     (str (inc i) ". " (last e) ": "(second e))]
                    [:br])) 
        t))))

(defn- tailings
  "尾矿库的html片段"
  []
  (let [t1 (rest (sql-data "select name, orgid,capacity,height from tailings where capacity>=100 or height>=30"))]
    (html
      [:a#chartf {:href "#"} "中国黄金集团尾矿库现状统计汇总表 "][:br]
      [:a#gtail {:href "#"} " (地图显示)"][:br]
      [:h2 {:title "全库容≥100万m3或者坝高≥30 m的尾矿库" :style "color: red"}
       [:img {:src "/img/alert.ico"}]" "
       (format "重大危险源（尾矿库）：%s 个" (count t1))]
      (map-indexed 
        (fn [i e] 
          (let [c (nth e 2) h (last e)]
            (html 
              [:div {:title (format "全库容：%.0f%s，坝高：%.0f%s" 
                                    c (if (>= c 100) "≥100万方" "") 
                                    h (if (>= h 30) "≥30米" ""))} 
               (format "%s. %s" (inc i) (first e))])))
        t1)
      )))
  
(defn app-right-main
  "app: 右边主框架"
  []
  (eui-accord
    {:id "accord1" :style "" }
    (eui-accord-
      {:iconCls "icon-search"} "集团公司情况"
      (group))
    (eui-accord-
      {:iconCls "icon-search"} "集团企业情况"
      (org))
    (eui-accord-
      {:iconCls "icon-search"} "重点风险监测"
      (tailings))
    ))

(defn index
  "app: 应用框架"
  []
  (eui-layout
    {:id "layout1" :onload "ems_onload()"}
    ;----------------------- north
    ;----------------------- west
    ;----------------------- center
    (app-main)
    ;----------------------- east
    (app-right)
    ;----------------------- south
    ))

;----------------------------- layout 中国有色集团

(defn- app-top1
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id "layout_north1" :style "height: 135px; padding: 10px;" }
    [:span {:class "layout_title1"} "中国有色集团安全生产信息管理平台"]
    [:div {:style "float: right"} "当前用户: 安全生产监督管理部"]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher "eui_search1" :style "width: 250px;"} 
       [:div {:name "范围A" :iconCls "icon-ok" :style "margin: 0px"} "功能搜索"]
       [:div {:name "范围B" :iconCls "icon-tip" :style "margin: 0px"} "局部搜索"])]          
    ; 1、2级导航条
    (eui-tabs 
      {:style "height: 65px; position: absolute; left:0px; bottom: 0px;"}
      (map-indexed 
        (fn [i [m1 m2]] 
          (eui-tab1 
            (html [:span {:style "font-size: 14px; padding-left: 10px; padding-right: 10px"} m1]) 
            {:closable "false" :style "padding: 4px" (if (= (++ i) (count app-menu1)) "selected" "foo") "true"} 
            (map 
              #(eui-button 
                 (merge {:plain "true" :iconCls "icon-arrow"} (val %)) (key %)) 
              m2))) 
        app-menu1)
      )))

(defn- app-foot1
  "layout.south"
  []
  (eui-region 
    "south" 
    {:style "height: 30px; background: #cde; padding: 5px; text-align: center; color: #444"} 
    "版本信息: IDP集成数据平台系列——行业支撑平台 @" (year)))

(defn layout
  "app: 常用应用框架"
  []
  (eui-layout
    {:id "layout1" :onload "ems_onload()"}
    ;----------------------- north
    (app-top1)
    ;----------------------- west
;    (app-left)
    ;----------------------- center
    (app-main)
    ;----------------------- east
    (app-right)
    ;----------------------- south
    (app-foot1)
    ))

;----------------------------- layout 中国黄金集团有限公司

(defn menu3list 
  [id]
  (let [id0 (or id "11-duty")
        menu3 (get app-menu2-sub (keyword id0))]
    (html
      (if (seq menu3)
        (map-indexed 
          (fn [i [k v]] 
            (html
              (eui-button 
                {:plain "true" :iconCls "icon-search" :title (name k) :group "gmap"} 
                (format "%d. %s" (++ i) (name k)))
              [:br]))
          menu3)
      [:h3 (str id0 " 没有子菜单了")])
      ))) 
      
(defn- app-top2
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id "layout_north2" :style "height: 135px; padding: 10px;" }
    [:div {:style "float: right; color: #ccc"} "当前用户: 安全环保部"]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher "eui_search1" :style "width: 250px;"} 
       [:div {:name "范围A" :iconCls "icon-ok" :style "margin: 0px"} "功能搜索"]
       [:div {:name "范围B" :iconCls "icon-tip" :style "margin: 0px"} "局部搜索"])]          
    ; 1、2级导航条
    (eui-tabs 
      {:style "height: 65px; position: absolute; left:0px; bottom: 0px;"}
      (map-indexed 
        (fn [i [m1 m2]] 
          (eui-tab1 
            (html [:span {:style "font-size: 14px; padding-left: 10px; padding-right: 10px"} m1]) 
            {:closable "false" :style "padding: 4px"} 
            (map 
              #(eui-button 
                 (merge {:plain "true" :iconCls "icon-arrow" :group2 "menu2"} (val %)) (key %)) 
              m2))) 
        app-menu2)
      )))

(defn- app-left2
  "layout.east"
  []
  (eui-region 
    "west" 
    {:title "快捷导航 (<a href=\"#\">回主页</a>)" :style "width: 300px"} 
    ))

(defn app-left-main2
  "app: 右边主框架"
  []
  (eui-accord
    {:id "accord1" :style "" }
    (eui-accord-
      {:iconCls "icon-search"} "集团概况一览"
      (group))
    (eui-accord-
      {:iconCls "icon-search"} "集团企业情况"
      (org))
    (eui-accord-
      {:iconCls "icon-search"} "重点风险监测"
      (tailings))
    ))

(defn layout2
  "app: 常用应用框架"
  []
  (eui-layout
    {:id "layout1" :onload "ems_layout2()"}
    ;----------------------- north
    (app-top2)
    ;----------------------- west
    (app-left2)
    ;----------------------- center
    (app-main)
    ;----------------------- east
;    (app-right)
    ;----------------------- south
    (app-foot1)
    ))

; 测试
(defn tailings-dim
  [dd-name dd-col]
  (let [t (sql-data (format "select %s, COUNT(*) count from tailings where %s is not null group by %s" dd-col dd-col dd-col))
        t2 (into {} (rest t))]
    (into {} (map (fn [[k v]] [v (or (t2 k) 0)]) (dd-tailings dd-name)))))

;(tailings-dim "安全度" "safety")
;(tailings-dim "等别" "grade")
;(tailings-dim "使用状态" "status")
;(tailings-dim "筑坝方式" "dam_type")
;(into {} (rest (sql-data "select license,COUNT(*) from tailings_license group by license")))

