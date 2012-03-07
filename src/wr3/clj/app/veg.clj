(ns wr3.clj.app.veg)

;;;; 蔬菜交易市场应用 vegetable market app

(use 'clojure.contrib.json)
(use 'hiccup.core)
(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.file 'wr3.clj.tb)
(use 'wr3.clj.web 'wr3.clj.gmap 'wr3.clj.db 'wr3.clj.meta)
(use 'wr3.clj.app.vegconf); :reload) ; reload 才能使配置更改生效

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
                (format "%s. %s" (++ i) (name k)))
              [:br]))
          menu3)
      [:h3 (str id0 " 没有子菜单了")])
      (str " ")
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
    {:title "快捷导航 (<a href=\"/c/veg/\">回主页</a>)" :style "width: 300px"} 
    ))

(defn app-left-main
  "app: 右边主框架"
  []
  (eui-accord
    {:id "accord1" :style "" }
    (eui-accord-
      {:iconCls "icon-search"} "基础数据查看"
      "说明：4个基础数据表的原始数据查看。")
    (eui-accord-
      {:iconCls "icon-search"} "统计报表管理"
      "说明：蔬菜部3个报表示例。")
    (eui-accord-
      {:iconCls "icon-search"} "数据分析"
      "说明：进程登记表维度指标分析。")
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

(defn rows 
  "service: 给出表对应id如14-variety-count，得到表记录条数"
  [id]
  (let [id0 (or (m (left id "-count")) "TB_TR_REGISTER_RECORD")
        rt (dbm/rows ["mssql" id0])
        n2 (split rt " ")]
    (html [:h1 {:style "padding: 20px"} (format "\"%s\" （%s） 记录条数：" (meta-name id0) id0)]
          [:div {:style "font-family:微软雅黑,黑体; font-size: 30px; font-weight: bold; padding: 20px"} 
           (format "%s （约 %s）"  (first n2) (second n2))])
    ))

(defn cols 
  "service: 调用dbm.clj中的函数显示表的列信息"
  [id]
  (dbm/cols ["mssql" (m (left id "-cols"))]) )

(defn data 
  "service: 调用dbm.clj中的函数显示表的数据"
  [id]
  (dbm/data ["mssql" (m (left id "-data"))] nil) )

(defn- format-date
  "把形如2011/12/20 ——> 2011年12月20日"
  [date]
  (format "%s年%s月%s日" (subs date 0 4) (subs date 5 7) (subs date 8 10)))
  
(defn- trade-dates 
  "得到交易明细流水表时间跨度"
  []
  (let [dbname "mssql"
        sql (str "select distinct convert(char(10), TradeDate, 111) "
                 " from TB_TR_TRADE_DETAIL_RECORD"
                 " order by convert(char(10), TradeDate, 111)")]
    (select-col dbname sql)))

(defn- trade-sum 
  "得到报表数据"
  []
  (let [dbname "mssql"
        sql (str "select a.VegeNo, b.VegeName, SUM(a.Num) sum "
                 " from TB_TR_TRADE_DETAIL_RECORD a join TB_BASIC_VARIETY_DETAIL b on a.VegeNo=b.VegeNo"
                 " group by a.VegeNo, b.VegeName order by a.VegeNo")
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
    (html r2) ))

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
  

(defn- trade-quot 
  "得到行情价报表数据"
  [date]
  (let [dbname "mssql"
        sql (str "select b.vegename, sum(a.num) sum,max(a.price) max,MIN(a.price) min" 
                 " from TB_TR_TRADE_DETAIL_RECORD a join TB_BASIC_VARIETY_DETAIL b on a.vegeno=b.vegeno" 
                 " where a.num>1 and convert(char(10), TradeDate, 111)='" date "'"
                 " group by a.VegeNo,b.vegename order by a.VegeNo")
        rt (select-all dbname sql)
        r1 (partition 3 3 (repeat 3 {:vegename "-" :sum " " :max " " :min " "})rt) ; 每3行结果分成一组
        f (fn [e3] ; 把一组3条记录处理成一个<tr/>
            (html [:tr 
                   (map #(html 
                           [:td (trim (:vegename %))] 
                           [:td {:align "center"} (between (:vegename %) "(" ")")]
                           [:td {:align "right"} (:sum %)]
                           [:td {:align "right"} (format "%s-%s" (:min %)  (:max %))]) 
                        e3) ]))
        r2 (map f r1) ; 所有行（每行3个蔬菜种类） 
        ]
    (html r2) ))

(defn app3
  "app: 蔬菜行情表"
  [date]
  (let [ds (trade-dates)
        d (or date (first ds))]
    (html
      (map #(html (eui-button {:onclick (format "veg_quot('%s')" %)} (format-date %)) " ") ds)
      [:table.wr3table {:border 1}
       [:caption "江桥市场蔬菜行情价"]
       [:thead 
        [:tr [:td {:colspan 12 :align "center"} (format-date d)]]
        [:tr (repeat 3 (html [:th "品名"] [:th "产地"] [:th "数量(百公斤)"] [:th "成交价(元)"]))]]
       [:tbody 
        (trade-quot d)] ]
      [:br])))

(import wr3.table.CrossTable)

(defn app4 
  "app: 进场登记表日期-属地分析"
  []
  (let [dbname "mssql"
        tb "TB_TR_REGISTER_RECORD"
        sql (str "select convert(char(10), EnterDate, 111) date, substring(TruckNo,1,1) no, SUM(SayWeight) sum from " tb
                 " where substring(TruckNo,1,1)<>''"
                 " group by convert(char(10), EnterDate, 111), substring(TruckNo,1,1)" 
                 " order by convert(char(10), EnterDate, 111), sum(SayWeight) desc")
        wr3tb (query dbname sql)
        rt (doto (CrossTable/create) 
             (.top "date") 
             (.left "no") 
             (.measure "sum") 
             (.sum true) 
             (.meta ["日期" "属地" "重量"])
             (.data wr3tb)) ]
    (html 
      [:h1 (str (meta-name tb) " 日期/属地维度-重量分析")]
      (replace-all (.html rt) "null" "-") )))

;; 从meta.clj的dds定义中得到一个表的所有维度字段名称，如：("SignMode" "Area" ...)
(defn- table-dims [tb] (map name (keys (dds (keyword tb)))))
  
(require 'wr3.clj.app.chartf)

(defn app5
  "app: 进场登记表字典维度分析。
  dim：维度名称，如'SignMode','Status'等"
  [dim]
  (let [dim (or dim "SignMode")
        dim-key (keyword (.toLowerCase dim))
        dbname "mssql"
        tb "TB_TR_REGISTER_RECORD"
        dd (dd-map tb dim)
        sql (str "select " dim ", count(*) count, SUM(SayWeight) weight from " tb  
                    " group by " dim
                    " order by COUNT(*) desc")
        rt (select-all dbname sql)
        fratio (fn [r k] (format "%.2f%%" (* 100 (/ (double (k r)) (apply + (map k rt))))))
        ]
    (html 
      (map #(html (eui-button {:onclick (format "veg_enter_dict('%s')" %)} (meta-name %)) " ") (table-dims tb))
      [:h1 "进场登记表数据 字典维度-重量指标分析"]
      [:table.wr3table {:border 1}
       [:caption (meta-name dim)]
       [:thead 
        [:tr [:th (meta-name dim)] [:th "计数"] [:th "数量占比"] [:th "过磅称重"] [:th "重量占比"]]]
       [:tbody 
        (map #(html [:tr 
                     [:td (dd (dim-key %))] 
                     [:td (:count %)] 
                     [:td (fratio % :count)] 
                     [:td (:weight %)]
                     [:td (fratio % :weight)] 
                     ]) rt)]
       ]
      (wr3.clj.app.chartf/pie 
        (into {} (map #(vector (dd (dim-key %)) (:weight %)) rt)) 
        {:title "简单图形" :x "序号" :y "数值"}) 
      ) ))

(defn app6
  "app: 进场登记表日期维度分析"
  []
  (let [dbname "mssql"
        tb "TB_TR_REGISTER_RECORD"
        sql (str "select convert(char(10), EnterDate, 111) date, count(*) count, SUM(SayWeight) weight from " tb
                 " group by convert(char(10), EnterDate, 111)" 
                 " order by convert(char(10), EnterDate, 111) ")
        rt (select-all dbname sql)
        fratio (fn [r k] (format "%.2f%%" (* 100 (/ (double (k r)) (apply + (map k rt))))))
        ]
    (html
      [:h1 "进场登记表 日期维度-重量指标分析"]
      [:table.wr3table {:border 1}
       [:thead 
        [:tr [:th "日期"] [:th "计数"] [:th "数量占比"] [:th "过磅称重"] [:th "重量占比"]]]
       [:tbody 
        (map #(html [:tr 
                     [:td (:date %)] 
                     [:td (:count %)] 
                     [:td (fratio % :count)] 
                     [:td (:weight %)]
                     [:td (fratio % :weight)] 
                     ]) rt)]]
      (wr3.clj.app.chartf/bar 
        (into {} (map #(vector (:date %) (:weight %)) rt)) 
        {:title "示意图" :x "日期" :y "称重"}) 
      )))

(defn app7 
  ""
  []
  (html-body 
    [:div#div1 "app7"]
    [:br]
    [:a#bt-app7 {:href "#" :onclick "veg_app7()"} "test"]))

;;---------------- 临时测试，记着注释掉调用语句
(defn test2 []
;  (println (rows "11-reg-count"))
  (trade-dates)
)

;(def e3 '({:vegeno "10110", :vegename "蘑菇 ", :sum 92687.20M} {:vegeno "10111", :vegename "平菇 ", :sum 655.00M} {:vegeno "10113", :vegename "草菇 ", :sum 30.00M}))
;(trade-sum)
;(app5 "SignMode")



