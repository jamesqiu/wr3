(ns ^{:doc "
  把wr3.clj.chart包装成app或者service
  todo: 根据data自动建议采用哪种chart-type
  @modify jamesqiu 2012-3-11 flash增加wmode="opaque" 以免挡住easyui弹出的内容如右键菜单，dialog等
  "}
     wr3.clj.app.chartf)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.n)
(use 'hiccup.core)
(use 'wr3.clj.chart)
  
;;------------ 应用  
(defn png
  "app: 根据参数得到一个静态png图形的html片段.
  usage: 
    chartf/png?t=bar&data=[3 1 4 1 5 9]
    chartf/png?t=barline&data=[3 1 4 1 5 9] [10 40 10 40 20 10]
  @param t 可以是 bar line pie barline 之一
  @param data '[3 1 4 1 5 9]' 或 '[1 3 5] [10 20 30] [2 3 5]' "
  [t data]
  (let [t (if (#{"bar" "line" "pie" "barline"} t) t "bar")
        data (or data (vec (random-n 10)))
        s (format "(img %s %s)" t data) ] ; (img bar [3 1 4 1 5 9] [1 4 1 5 9 2])
    (println s)
    (eval (read-string s))))

(defn flash
  "app: 根据参数得到一个静态png图形的html片段.
  usage:
    chartf/flash?t=bar&data=[3 1 4 1 5]&title=标题1&x=横轴&y=纵轴
    chartf/flash?t=barline&data=[[3 1 4 1 5] [1 4 1 4 2 1]]
    chartf/flash?data={季度1 50 季度2 20 季度3 40 季度4 30}
    chartf/flash?t=bars&data={category [季度1 季度2 季度3 季度4] 产品额 [50 20 40 30] 服务额 [9 3 6 4]} 
  @param t 可以是 bar line pie circle bars lines stack area barline tower funnel cup panel
  @param data 参考usage, 注意 bars, lines 只能接受多组数据；barline 只能接受两组数据；其他只能接受一组数据
  @title 标题标识
  @x x横轴的标识
  @y y纵轴的标识  
  @size '800x600'
   "
  [t data title x y size]
  (let [t (or t "bar")
        data (or data (str (vec (random-n 10))))
        label {:title (or title "图形示例") :x (or x "序号") :y (or y "值")}
        size (or size "600x400")
        [width height] (split size "x") ]
    (embed t (read-string data) label width height)))

(defn index 
  "app: 各种图形示例
  usage(注意把单引号改成双引号): 
    chartf?data=[10 30 20 50 40]&title=tt&x=xx&y=yy } 
    chartf?data=[[2 3 9 2 7] [10 30 20 50 40]]
    chartf?data={季度1 50 季度2 20 季度3 40 季度4 30}
    chartf?data={category [季度1 季度2 季度3 季度4] 产品额 [50 20 40 30] 服务额 [9 3 6 4]} 
  @param t 可以是 bar line pie circle bars lines stack area barline tower funnel cup panel
  @param data 参考usage, 注意 bars, lines 只能接受多组数据；barline 只能接受两组数据；其他只能接受一组数据
  @title 标题标识
  @x x横轴的标识
  @y y纵轴的标识  
  "
  [t data title x y]
  (let [t (or t "bar")
        data (or data (str (vec (random-n 10))))
        label {:title (or title "图形示例") :x (or x "序号") :y (or y "值")}]
    (html-body 
      ""
      [:center [:h1 "静态图、Flash图示例"]]
      [:h2         
       [:a {:href (format "%s/c/chartf/dojo" webapp)} "查看Flash图基本例子"] " &nbsp; | &nbsp; "
       [:a {:href (format "%s/c/chartf/static" webapp)} "查看静态图基本例子"] " &nbsp; | &nbsp; "
       [:a {:href (format "%s/c/chartf/tailings" webapp)} "尾矿库统计汇总图例子"] " &nbsp; | &nbsp; "
       [:label "石油石化相关图表例子: &nbsp; "] 
       (map (fn [[k v]] (html [:a {:href (format "%s/c/chartf/%s" webapp k)} v] " &nbsp; ")) 
            {"d1" "杯图" "d2" "仪表板图" "d3" "油品综合分析图"}) ]
      [:br][:br]
      [:center (embed t (read-string data) label)] 
      [:h2 "可以通过url来传递数据信息及辅助标识，如下："]
      (for [uri [""
                 "data=[10 30 20 50 40]&title=tt&x=xx&y=yy"
                 "data=[[2 3 9 2 7] [10 30 20 50 40]]"
                 "data={季度1 50 季度2 20 季度3 40 季度4 30}"
                 "data={category [季度1 季度2 季度3 季度4] 产品额 [50 20 40 30] 服务额 [9 3 6 4]}"]]
        (html [:a {:href (format "/c/chartf?t=%s&%s" t uri) :style "margin-left: 30px"} (format "/c/chartf?t=%s&%s" t uri)] [:br]))
      [:h3 "注：参数t是类型，可为 bar line pie circle bars lines stack area barline tower funnel cup panel" [:br]
       (space 7) "其中 bars, lines 只能接受多组数据；barline 只能接受两组数据；其他只能接受一组数据"]
      )))

(defn static
  "app: 测试png静态图片文件"
  []
  (html-body
    (img line (random-n 50))
    (img bar (random-n 10) (random-n 10) (random-n 10))
    (img pie (random-n 5) (random-n 5))
    (img barline) ))

(defn dojo
  "app: 各种图形示例，采用dojo TABs"
  []
  (let [data0 (zipmap (map #(str "值" %) (range 10)) (random-n 10)) ;(vec (random-n 10))
        label0 {:title "简单图形" :x "序号" :y "数值"}
        
        data1 [(vec (random-n 12)) (vec (random-n 12))]
        label1 {:title "销售表2" :x "月份" :y "销售额"} 
        
        label2 {:caption "销售额"}
        data2 {"1季度" 50 "2季度" 20 "3季度" 40 "4季度" 30}
        
        label3 {:caption "销售表" :x "季度" :y "销售额"}
        data3 {:category ["1季" "2季" "3季" "4季"] 
               "国内" [18 20 30 25] "国外" [3 5 9 8] "内部" [2 3 5 10] } ]
    (html-dojo 
      ""
      (dojo-tabs 
        {:id "tabs3" :style "width: 100%; height: 100%"} 
        [{:title "基本图形" :html (html
                                (barf data0 label0) " "
                                (linef data0 label0) " "
                                (circlef data0 label0) " "
                                (pief data0 label0) )}
         {:title "多序列图形" :html (html
                                 (barf data3 label3) " "
                                 (linef data3 label3) " "
                                 (stackf data3 label3) " "
                                 (areaf data3 label3) " "
                                 (barlinef data3 label3) )}
         {:title "其他图形" :html (html
                                (towerf {"工作人口" 500000000 "成人人口" 700000000 "中国人口" 1500000000 "世界人口" 5000000000} {}) " "
                                (funnelf {"所有账户" 5000 "活动账户" 3000 "价值客户" 1500 "VIP客户" 300} {}) " "
                                (cupf 65 {:lowerLimit 0 :upperLimit 100} 200) " "
                                (panelf {:value 135 :range [90 100 120 150]} {:lowerLimit 90 :upperLimit 150}) 
                                (panelf {:value 87 :range [30 50 55 60 65 70 75 80 85 90 100]} {:lowerLimit 30 :upperLimit 100}) 
                                )}
         {:title "地图图形" :html (html 
                                [:a {:href (format "%s/c/chartf/chinamap" webapp) :target "_blank"} 
                                 "中国地图 (另页打开)"] )} 
         ]) )))

;;;----------------- 地图
(defn chinamap
  "app: 显示中国地图，需要单独的data xml文件支持"
  []
  (html [:html 
         [:head meta-utf8 
          [:link {:type "text/css" :rel "stylesheet" 
                  :href (format "%s/dojoroot/dojo/resources/dojo.css" webapp)}]
          [:script {:type "text/javascript" :src (format "%s/js/FusionMaps.js" webapp)} ""]           
          ]
         [:body
          [:h2 {:align "center"} "中国区划地图2（点击安徽支持JavaScript）"]
          [:table {:width "98%" :border "0" :cellspacing "0" :cellpadding "3" :align "center"}
           [:tr
            [:td {:valign "top" :class "text" :align "center"}
             [:div#mapdiv {:align "center"} "FusionMaps."]
             [:script {:type "text/javascript"}
              (format
                "var map = new FusionMaps(\"%s/chartf/FCMap_China2.swf\", \"Map1Id\", \"1000\", \"600\", \"0\", \"0\");"
                webapp)
              (format
                ;"map.setDataURL(\"%s/chartf/chinamap.xml\");"		   
                "map.setDataURL(\"%s/c/chartf/chinamap2?content-type=text/xml&charset=gbk\");"		   
                webapp)
              "map.render(\"mapdiv\");"
              ]
          ]]]]]))


(import wr3.util.Charsetx)
(defn chinamap2
  "service: flash map所需数据文件的例子，注意：需要gbk编码，访问需在url后加上 ?xml=gbk "
  []
  (str
    "<?xml version=\"1.0\" encoding=\"gbk\"?>"
    (html
      [:map {:showCanvasBorder 1 :canvasBorderColor "f1f1f1" :canvasBorderThickness 2 :borderColor "00324A" :fillColor "F0FA00" :hoverColor "C0D2F8"
             :baseFontSize 13}
       [:colorRange
        [:color {:minValue 0 :maxValue 80 :displayValue "拥挤" :color "ff8888"}]
        [:color {:minValue 80 :maxValue 90 :displayValue "一般" :color "FFCC33"}]
        [:color {:minValue 90 :maxValue 100 :displayValue "宽敞" :color "88ff88"}]
        ]
       [:data
        [:entity {:id "CN.AH" :value 81 :displayValue "安徽" :toolText "安徽省" :link "JavaScript:alert('Welcome to AnHui!')" :color ""}]
        [:entity {:id "CN.BJ" :value 70 :displayValue "北京" :toolText "北京市" :link "" :color ""}]
        [:entity {:id "CN.CQ" :value 60 :displayValue "重庆" :toolText "重庆市" :link "" :color ""}]
        [:entity {:id "CN.FJ" :value 80 :displayValue "福建" :toolText "福建省" :link "" :color ""}]
        [:entity {:id "CN.GS" :value 80 :displayValue "甘肃" :toolText "甘肃省" :link "" :color ""}]
        [:entity {:id "CN.GD" :value 80 :displayValue "广东" :toolText "广东省" :link "" :color ""}]
        [:entity {:id "CN.GX" :value 80 :displayValue "广西" :toolText "广西区" :link "" :color ""}]
        [:entity {:id "CN.GZ" :value 80 :displayValue "贵州" :toolText "贵州省" :link "" :color ""}]
        [:entity {:id "CN.HA" :value 95 :displayValue "海南" :toolText "海南省" :link "" :color ""}]
        [:entity {:id "CN.HB" :value 80 :displayValue "河北" :toolText "河北省" :link "" :color ""}]
                                                                         
        [:entity {:id "CN.HE" :value 70 :displayValue "河南" :toolText "河南省" :link "" :color ""}]
        [:entity {:id "CN.HU" :value 80 :displayValue "湖北" :toolText "湖北省" :link "" :color ""}]
        [:entity {:id "CN.HL" :value 90 :displayValue "黑龙" :toolText "黑龙江" :link "" :color ""}]
        [:entity {:id "CN.HN" :value 80 :displayValue "湖南" :toolText "湖南省" :link "" :color ""}]
        [:entity {:id "CN.JS" :value 80 :displayValue "江苏" :toolText "江苏省" :link "" :color ""}]
        [:entity {:id "CN.JX" :value 80 :displayValue "江西" :toolText "江西省" :link "" :color ""}]
        [:entity {:id "CN.JL" :value 80 :displayValue "吉林" :toolText "吉林省" :link "" :color ""}]
        [:entity {:id "CN.LN" :value 80 :displayValue "辽宁" :toolText "辽宁省" :link "" :color ""}]
        [:entity {:id "CN.NM" :value 80 :displayValue "内蒙" :toolText "内蒙区" :link "" :color ""}]
        [:entity {:id "CN.NX" :value 80 :displayValue "宁夏" :toolText "宁夏区" :link "" :color ""}]
                                                                         
        [:entity {:id "CN.QH" :value 80 :displayValue "青海" :toolText "青海省" :link "" :color ""}]
        [:entity {:id "CN.SA" :value 80 :displayValue "陕西" :toolText "陕西省" :link "" :color ""}]
        [:entity {:id "CN.SD" :value 80 :displayValue "山东" :toolText "山东省" :link "" :color ""}]
        [:entity {:id "CN.SH" :value 80 :displayValue "上海" :toolText "上海市" :link "" :color ""}]
        [:entity {:id "CN.SX" :value 80 :displayValue "山西" :toolText "山西省" :link "" :color ""}]
        [:entity {:id "CN.SC" :value 80 :displayValue "四川" :toolText "四川省" :link "" :color ""}]
        [:entity {:id "CN.TJ" :value 80 :displayValue "天津" :toolText "天津市" :link "" :color ""}]
        [:entity {:id "CN.XJ" :value 80 :displayValue "新疆" :toolText "新疆区" :link "" :color ""}]
        [:entity {:id "CN.XZ" :value 90 :displayValue "西藏" :toolText "西藏区" :link "" :color ""}]
        [:entity {:id "CN.YN" :value 80 :displayValue "云南" :toolText "云南省" :link "" :color ""}]
                                                                         
        [:entity {:id "CN.ZJ" :value 80 :displayValue "浙江" :toolText "浙江省" :link "" :color ""}]
        [:entity {:id "CN.MA" :value 80 :displayValue "澳门" :toolText "澳门区" :link "" :color ""}]
        [:entity {:id "CN.HK" :value 50 :displayValue "香港" :toolText "香港区" :link "" :color ""}]
        [:entity {:id "CN.TA" :value 90 :displayValue "台湾" :toolText "台湾岛" :link "" :color ""}]
        ]
       [:styles
        [:definition
         [:style {:type "animation" :name "animX" :param "_xscale" :start 0 :duration 1}]
         [:style {:type "animation" :name "animY" :param "_yscale" :start 0 :duration 1}] ]
        [:application
         [:apply {:toObject "PLOT" :styles "animX,animY"}] ]] ])))

;;---------------------- d, d1, d2, d3 为直真视通生成石油石化相关图表
(defn- d
  [h1 h2 f data]
  (html
    [:div {:align "center" :style "border: 1px solid gray; float: left; margin: 20px"}
     [:h1 h1] [:h2 h2 " （单位：万吨）"]
     (f data {}) ]))

(defn d1
  "app: cup杯图"
  []
  (html-body
    {}
    [:style (str "div { float:left; margin:20px }")]
    (for [[h1 v0 v1 d] [["常减压" 16000 14500 160] 
                        ["催化裂化" 4800 4600 48] 
                        ["焦化" 1600 1500 16]
                        ["重整" 1500 1200 15]
                        ["乙烯" 250000 205000 2500] ]]
      [:div {:align "center"}
       [:h1 "装置负荷率——" h1] 
       [:h2 {:style "color: #aa5500"} "小时能力: " v0] 
       [:h2 {:style "color: green"} "小时处理量: " v1] 
       (cupf (double (/ v1 d)) {:lowerLimit 0 :upperLimit 100} 200)] ) ))

(defn d2
  "app: panel仪表板图"
  []
  (html-body
    ""
    [:style (str "div { float:left; margin:20px }")]
    (for [[h1 v0 v1 l0 l1] [["柴汽比"     2.07  2.09  2.0 2.1] 
                            ["汽煤柴收率" 64.37 63.33 50  80] 
                            ["汽柴油收率" 61.44 60.66 50  70]
                            ["乙烯收率比" 38.0  33.8  10  50] ]]
      [:div {:align "center"}
       [:h1 "经济技术指标——" h1] 
       [:h2 {:style "color: #aa5500"} (format "计划: %s%%" v0)] 
       [:h2 {:style "color: green"} (format "实际: %s%%" v1)] 
       (panelf {:value v1 :range [l0 v0 l1]} {:lowerLimit l0 :upperLimit l1})] ) ))

(defn d3
  "app: 油品综合分析图"
  []
  (html-body 
    ""
    (d "油品购进分析" (str "全国总计：" (apply + [600 55 36 23 120]))
       pief (zipmap '[直属炼厂 地方炼厂 中石油 中石化 社会经营单位] [600 55 36 23 120]))
    (d "油品类型分析" ""
       pief (zipmap '[汽油 柴油 煤油] [196 480 9]))
    (d "客户类型分析" ""
       pief (zipmap '[加油站零售用户 加油站机构用户 各类加油站 再流通环节] [302 82 14 159]))
    (d "评估类型分析" ""
       pief (zipmap '[自有类型 客存类型 代管类型] [392 130 26]))
    (d "油库类型分析" ""
       pief (zipmap '[油库 加油站] [492 103]))
    (d "购进量趋势分析" "全国地区"
       linef {:category (vec (map #(str % "月") (range 1 13))) 
             "柴油" (vec (map #(+ 500 %) (random-n 12 200)))
             "汽油" (vec (map #(+ 100 %) (random-n 12 100))) 
             "煤油" (vec (map #(+ 50 %) (random-n 12 20))) } )
    (d "销售量价分析" "全国地区"
       barlinef {:category (vec (map #(str % "月") (range 1 13))) 
                "销售量" (vec (map #(+ 600 %) (random-n 12 300)))
                "销售价格" (vec (map #(+ 6 %) (random-n 12 4))) } )
    (d "库存量成本分析" "全国地区"
       barlinef {:category (vec (map #(str % "月") (range 1 13))) 
                "库存量" (vec (map #(+ 600 %) (random-n 12 300)))
                "库存成本" (vec (map #(+ 400 %) (random-n 12 600))) } )
    (html 
      [:div {:align "center" :style "float: left; margin: 20px"}
       [:h1 "销售量同比分析"] [:h2 "全国地区销售量：900万吨"]
       (panelf {:value 23 :range [-100 -50 0 50 100]} {:lowerLimit -100 :upperLimit 100})])
    (html 
      [:div {:align "center" :style "float: left; margin: 20px"}
       [:h1 "销售量环比分析"] [:h2 "全国地区销售量：900万吨"]
       (panelf {:value -3 :range [-100 -50 0 50 100]} {:lowerLimit -100 :upperLimit 100})])
    ))
  
;;; 黄金集团尾矿库
(defn- bar-t
  [title data & m]
  (html
    [:div {:align "center" :style "float: left; margin: 5px; border: 1px dotted lightgray"}
     [:h1 title]
     (apply barf data {} m)]))

(defn tailings []
  (html-body
    ""
    [:div {:align "center"}
     [:h1 {:style "color: black"} "尾矿库现状统计汇总表"]
     [:h2 {:style "color: red"} "尾矿库总数: 77"]]
    (bar-t "安全度" {"正常库" 73, "病库" 3, "险库" 1, "危库" 0} 250 200)
    (bar-t "等别" {"一等库" 0, "二等库" 2, "三等库" 18, "四等库" 40, "五等库" 13} 300 200)
    (bar-t "使用状态" {"生产在用" 54, "在建库" 5, "停用库" 6, "闭库过程中" 1, "闭库完成" 9} 400 200)
    (bar-t "筑坝方式" {"上游式" 61, "下游式" 5, "中线式" 1, "其他方式" 6} 300 200)
    (bar-t "目前总坝高" (into {} (reverse {"≥100米" 1, "(100,60]米" 10, "(60,30]米" 27, "＜30米" 29})) 360 200)
    (bar-t "设计库容（万立）" {"≥10000" 1, "(10000-1000]" 3, "(1000-100]" 45, "＜100" 25, "未知" 3} 350 200)
    (bar-t "已堆积库容（万立）" {"≥10000" 0, "(10000-1000]" 0, "(1000-100]" 21, "＜100" 47, "达到和超过设计库容" 1, "未知" 9} 460 200)
    (bar-t "生产在用库剩余服务年限" {"≤1年" 6, "(1-3]年" 5, "(3-5]年" 9, "(5-10]年" 13, "＞10年" 12} 360 200)
    (bar-t "安全许可证" {"有效期内" 20, "半年内到期" 9, "已经过期" 6, "无证" 51} 350 200)
    ))
