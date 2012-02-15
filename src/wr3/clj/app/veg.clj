(ns wr3.clj.app.veg)

;;;; 蔬菜交易市场应用 vegetable market app

(use 'clojure.contrib.json)
(use 'hiccup.core)
(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.file 'wr3.clj.tb)
(use 'wr3.clj.web 'wr3.clj.gmap 'wr3.clj.db 'wr3.clj.meta)
(use '[wr3.clj.app.chartf :only (pie)])
(use 'wr3.clj.app.vegconf :reload) ; reload 才能使配置更改生效

(require '[wr3.clj.app.dbm :as dbm])

;----------------------------- layout 
(defn menu3list 
  [id]
  (let [id0 (or id "11-reg")
        menu3 (get app-menu2-sub (keyword id0))]
    (html
      (if (seq menu3)
        (map-indexed 
          (fn [i [k v]] 
            (html
              (eui-button 
                (merge v {:plain "true" :iconCls "icon-search" :title (format "%s (%s)" (name k) id0) :group id0}) 
                (format "%d. %s" (++ i) (name k)))
              [:br]))
          menu3)
      [:h3 (str id0 " 没有子菜单了")])
      ))) 
      
(defn- app-top
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id "layout_north" :style "height: 135px; padding: 10px;" }
    [:span {:class "layout_title"} "江桥市场蔬菜报表分析系统（演示）"]
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

(defn- app-left
  "layout.east"
  []
  (eui-region 
    "west" 
    {:title "快捷导航 (<a href=\"#\">回主页</a>)" :style "width: 300px"} 
    ))

(defn app-left-main
  "app: 右边主框架"
  []
  (eui-accord
    {:id "accord1" :style "" }
    (eui-accord-
      {:iconCls "icon-search"} "集团概况一览"
      "...")
    (eui-accord-
      {:iconCls "icon-search"} "集团企业情况"
      "...")
    (eui-accord-
      {:iconCls "icon-search"} "重点风险监测"
      "...")
    ))

(defn- app-foot
  "layout.south"
  []
  (eui-region 
    "south" 
    {:style "height: 30px; background: #cde; padding: 5px; text-align: center; color: #444"} 
    "版本信息: IDP集成数据平台系列——WebReport & 行业支撑平台 @" (year)))


(defn- app-main
  "layout.center"
  []
  (eui-region 
    "center" 
    {:border "false" :style "padding: 2px"} 
    [:iframe {:id "ifrm1" :src "" 
              :width "99%" :height "99%" :style "border: 1px dotted gray"}] ))


(defn index
  "app: 常用应用框架"
  []
  (eui-layout
    {:id "layout1" :onload "veg_onload()"}
    ;----------------------- north
    (app-top)
    ;----------------------- west
    (app-left)
    ;----------------------- center, 必须的
    (app-main)
    ;----------------------- east
;    (app-right)
    ;----------------------- south
    (app-foot)
    ))

(def ^{:doc "按钮id和表名的对照表"} m
  {"11-reg"  "TB_TR_REGISTER_RECORD"
   "12-reg2" "TB_TR_REGISTER_DETAIL_INFO"
   "13-trade" "TB_TR_TRADE_DETAIL_RECORD"
   "14-variety" "TB_BASIC_VARIETY_DETAIL"})

(defn rows [id]
  (let [id0 (or (m (left id "-count")) "TB_TR_REGISTER_RECORD")
        rt (dbm/rows ["mssql" id0])
        n2 (split rt " ")]
    (html [:h1 {:style "padding: 20px"} (format "\"%s\" （%s） 记录条数：" (meta-name id0) id0)]
          [:div {:style "font-family:微软雅黑,黑体; font-size: 30px; font-weight: bold; padding: 20px"} 
           (format "%s （约 %s）"  (first n2) (second n2))])
    ))


(defn cols [id]
  (dbm/cols ["mssql" (m (left id "-cols"))]) 
  )  

(defn data [id]
  (dbm/data ["mssql" (m (left id "-data"))] nil)
  )

(defn- format-date
  "把形如2011/12/20 ——> 2011年12月20日"
  [date]
  (format "%s年%s月%s日" (subs date 0 4) (subs date 5 7) (subs date 8 10)))
  
(defn- trade-dates 
  "得到交易明细流水表时间跨度"
  []
  (let [dbname "mssql"
        sql "select distinct convert(char(10), TradeDate, 111) from TB_TR_TRADE_DETAIL_RECORD order by convert(char(10), TradeDate, 111)"]
    (select-col dbname sql)))

(defn- trade-sum 
  "得到报表数据"
  []
  (let [dbname "mssql"
        sql "select a.VegeNo, b.VegeName, SUM(a.Num) sum from TB_TR_TRADE_DETAIL_RECORD a join TB_BASIC_VARIETY_DETAIL b on a.VegeNo=b.VegeNo group by a.VegeNo, b.VegeName order by a.VegeNo"
        rt (select-all dbname sql)
        r1 (partition 3 3 (repeat 3 {:vegename "-" :sum 0})rt) ; 每3行结果分成一组
        f (fn [e3] ; 把一组3条记录处理成一个tr
            (html [:tr 
                   (map #(html [:td (trim (:vegename %))] [:td {:align "right"} (:sum %)]) e3) 
                   [:td {:align "right" :style "font-weight:bold"} (apply + (map :sum e3))]])) 
        r2 (map f r1) ; 所有行（每行3个蔬菜种类）
        g (fn [e3] ; 把一组3条记录处理成只有数值 
            (map :sum e3)) ; 
        r3 (map g r1)
        r4 (apply map + r3)  ; 最后一行合计
        r5 (html [:tr {:style "font-weight:bold"} 
                  [:td "合计"] 
                  [:td {:align "right"} (first r4)] 
                  [:td "&nbsp;"]
                  [:td {:align "right"} (second r4)] 
                  [:td "&nbsp;"]
                  [:td {:align "right"} (last r4)] 
                  [:td {:align "right"} (apply + r4)]
                  ])
        ]
    (html r2 r5)
    ))

(defn app1 
  "app: 带标题，小计，总计的品种汇总报表"
  []
  (html
    [:table.wr3table {:border 1}
     [:caption "江桥市场蔬菜品种汇总"]
     [:thead 
      [:tr [:td {:colspan 7 :align "center"} (let [ds (map format-date (trade-dates))] (format "%s-%s" (first ds) (last ds)))]]
      [:tr (repeat 3 (html [:th "品名"] [:th "数量<br/>(百公斤)"])) [:th "总数量<br/>合计"]]]
     [:tbody 
      (trade-sum)]
     ] ))


(defn- trade-price 
  "得到报表数据"
  [date]
  (let [dbname "mssql"
        sql (str "select b.vegename, max(a.price) max,MIN(a.price) min" 
                 " from TB_TR_TRADE_DETAIL_RECORD a join TB_BASIC_VARIETY_DETAIL b on a.vegeno=b.vegeno" 
                 " where a.num>1 and convert(char(10), TradeDate, 111)='" date "'"
                 " group by a.VegeNo,b.vegename order by a.VegeNo")
        rt (select-all dbname sql)
        r1 (partition 3 3 (repeat 3 {:vegename "-" :max " " :min " "})rt) ; 每3行结果分成一组
        f (fn [e3] ; 把一组3条记录处理成一个<tr/>
            (html [:tr 
                   (map #(html 
                           [:td (trim (:vegename %))] 
                           [:td {:align "center"} (between (:vegename %) "(" ")")]
                           [:td {:align "right"} (format "%s-%s" (:min %)  (:max %))]) 
                        e3) ]))
        r2 (map f r1) ; 所有行（每行3个蔬菜种类） 
        ]
    (html r2)
    ))

(defn app2
  "app: 蔬菜成交价格表"
  [date]
  (let [ds (trade-dates)
        d (or date (first ds))]
    (html
      (map #(html (eui-button {:onclick (format "veg_price('%s')" %)} (format-date %)) " ") ds)
      [:table.wr3table {:border 1}
       [:caption "江 桥 市 场 蔬 菜 成 交 价"]
       [:thead 
        [:tr [:td {:colspan 9 :align "center"} (format-date d)]]
        [:tr (repeat 3 (html [:th "品名"] [:th "产地"] [:th "幅度价"]))]]
       [:tbody 
        (trade-price d)]
       ] 
      [:br])))
  
;;---------------- 临时测试，记着注释掉调用语句
(defn test2 []
;  (println (rows "11-reg-count"))
  (trade-dates)
)

;(def e3 '({:vegeno "10110", :vegename "蘑菇 ", :sum 92687.20M} {:vegeno "10111", :vegename "平菇 ", :sum 655.00M} {:vegeno "10113", :vegename "草菇 ", :sum 30.00M}))
;(trade-sum)



