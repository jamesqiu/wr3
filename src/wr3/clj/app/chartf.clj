(ns wr3.clj.app.chartf)

;;;; 生成Chart Flash控件图形的html代码片段 <embed ... />
;;;; todo: 根据data自动建议采用哪种chart-type

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.n)
(use 'hiccup.core)

;; -------------- data-xml data参数说明
;; 1、带标注的一组或者多组值，使用{}：
;;    {1季度 50 2季度 20 3季度 40 4季度 30}
;;    {category [1季度 2季度 3季度 4季度] 产品额 [50 20 40 30] 服务额 [9 3 6 4]}
;;
;; 2、不带标注的一组或者多组值，使用[]，'()可以用vec函数转换为[]：
;;    [10 20 30 40]
;;    [[10 20 30 40] [9 3 6 4]]
;; 生成如：
;; <chart caption=.. xAxisName=.. yAxisName=.. baseFontSize=..>
;;   <set name=.. value=.. />
;;   <set name=.. value=.. />
;;   <set name=.. value=.. />
;; </chart>

(defn- data-type
  "分析data数据结构得到data的类型"
  [data]
  (cond 
    ;;------ 无标注1组值
    (and (vector? data) (not (vector? (first data)))) 
    :1     
    ;;------ 无标注n组值
    (and (vector? data) (vector? (first data))) 
    :n      
    ;;------ 有标注1组值
    (and (map? data) (not (vector? (val (first data))))) 
    :1+label 
    ;;------ 有标注n组值
    (and (map? data) (vector? (val (first data)))) 
    :n+label
    ;;------ 其他数据类型
    :else nil))    
  
(defn- dataset
  "根据data的类型生成 <dataset>..</dataset> 或者 <set .. />"
  [data]
  (case (data-type data)
    ;;------ 无标注1组值
    :1     
    (html (for [e data] (html [:set {:value e}])))
    ;;------ 无标注n组值
    :n     
    (html [:categories (for [e (first data)] [:category {:label ""}])] 
          (map-indexed 
            (fn [i e] 
              (html
                [:dataset {:seriesName (str "数据" (inc i))} 
                 (map #(html [:set {:value %}]) e)]))
            data))
    ;;------ 有标注1组值
    :1+label     
    (html [:categories (for [[k v] data] [:category {:label k}])]  ; 增加category, 2011-12-12
          (for [[k v] data] (html [:set {:label k :value v}]))) ; :name -> :label
    ;;------ 有标注n组值
    :n+label     
    (let [categories (first data)
          dataset0 (fnext data)
          datasets (nnext data)]
      (html [:categories (for [category (val categories)] [:category {:label category}])]
            [:dataset {:seriesName (name (key dataset0))} 
             (for [s (val dataset0)] [:set {:value s}]) ]
            (for [dataset datasets] 
              [:dataset {:seriesName (name (key dataset)) :showValues 0 :parentYAxis "S"} 
               (for [s (val dataset)] [:set {:value s}])]) ))
    ;;------ 其他数据类型暂不处理
    nil))

(defn- data-xml 
  "从label和data生成dataXML，参数说明：
  data: 4种可能的类型 {..},  {.. [] .. []},  [..],  [[..] [..]]
  label: {:title .. :x .. :y ..} 也可以为空"
  ([data label] 
    (let [label2 (into
                   {}
                   (for [[k v] label] 
                     (cond (= k :x) [:xAxisName v]
                           (= k :y) [:yAxisName v]
                           (= k :title) [:caption v]
                           :else [k v])))
          data2 (dataset data)]
      (html 
        [:chart 
         (merge {:showLegend 1 :showNames 1 :decimalPrecision 0 :formatNumberScale 0 :baseFontSize 12} label2)
         data2])
      ))
  ([data] (data-xml data nil)) )

(defn- data-xml-cup
  "为cup水杯图单独生成dataXML.
  data参数格式：70 
  label参数格式：{:lowerLimit 0 :upperLimit 100}"
  [data label]
  (html
    [:chart (merge {:upperLimit 100 :lowerLimit 0 
                    :tickMarkGap 5 :numberSuffix "%25" :cylFillColor "99FFFF" :baseFontColor "CC0000" :baseFontSize 12}
                   label)
     [:value data]] ))

(defn- data-xml-panel
  "为仪表盘图单独生成dataXML
  data参数: {:value 90 :range [90 100 120 150]}
  label参数: {:lowerLimit 90 :upperLimit 150}"
  [data label]
  (let [v (:value data)
        rs (:range data)
        ranges (map vector (butlast rs) (next rs))] ; ranges ([90 100] [100 120] [120 150])
    (html
      [:chart (merge {:lowerLimit 0 :upperLimit 100 :bgColor "00FFFF,FFFFFF" :baseFontSize 12}  label)
       [:colorRange (html (for [r ranges] [:color {:minValue (first r) :maxValue (second r)}]))]
       [:dials [:dial {:value v}]]
       [:trendpoints [:point {:value v :displayValue v :dashed 1}]]] )))
  
(defn- embed 
  "生成<embed ..>，参数：
  ctype: 类型如'bar', 'line', 'pie', 'lines', 'bars', 'barline'
  data: 有|无标注的一组|多组数据
  label: 标题及x、y轴的标注
  m: 可变参数，取前两个，第一个为图形width，第二个为图形height，之后的忽略"
  [ctype data label & m]
  (let [ctype2 (case (.toLowerCase (name ctype)) 
                 "bar" "Column3D" 
                 "line" "Spline" 
                 "pie" "Pie3D" 
                 "circle" "Doughnut3D"
                 "bars" "MSColumn3D"
                 "lines" "MSLine" ; "MSSpline" -> "MSLine", 2011-12-12
                 "stack" "StackedColumn3D"
                 "area" "StackedArea2D"
                 "barline" "MSColumn3DLineDY"
                 "tower" "Pyramid"
                 "funnel" "Funnel"
                 "cup" "Cylinder"
                 "panel" "AngularGauge"
                 (name ctype)) ; 可直接使用swf文件名(不含.swf后缀)
        f (case ctype "cup" data-xml-cup "panel" data-xml-panel data-xml)
        dataXML (replace-all (f data label) "\"" "'")
        html2 (html [:embed {:src (format "%s/chartf/%s.swf" webapp ctype2)
                             :width (or (first m) 600) :height (or (second m) 400)
                             :type "application/x-shockwave-flash" :quality "high"
                             :flashVars "_flashVars_" } ])
        ]
    (replace-first html2 "_flashVars_" (str "dataXML=" dataXML)) ))

(defn- is-1? 
  "判断是否1组值（有或者没有label）"
  [data] (= \1 (first (name (data-type data)))))
  

;;;---------- 常用线柱饼环图形
(defn bar 
  "1条bar或者多条bar"
  [data label & m]
  (if (is-1? data) 
    (apply embed "bar" data label m)
    (apply embed "bars" data label m) ))

(defn line 
  "1条line或者多条line"
  [data label & m]
  (if (is-1? data)
    (apply embed "line" data label m)
    (apply embed "lines" data label m) ))

(defn pie 
  "1个pie"
  [data label & m] 
  (apply embed "pie" data label m))

(defn circle
  "1个circle环"
  [data label & m] 
  (apply embed "circle" data label m))

(defn stack
  "堆切的bar"
  [data label & m] 
  (apply embed "stack" data label m))

(defn area 
  "堆切的area"
  [data label & m] 
  (apply embed "area" data label m))

(defn barline
  "组合bar和line"
  [data label & m] 
  (apply embed "barline" data label m))

;;;---------- 其他图形
(defn tower
  "金字塔图"
  [data label & m] 
  (apply embed "tower" data label m))

(defn funnel
  "漏斗图：用于筛选"
  [data label & m] 
  (apply embed "funnel" data label m))
  
(defn cup
  "水杯图：用于完成百分比"
  [data label & m] 
  (apply embed "cup" data label m))

(defn panel
  "仪表盘"
  [data label & m] 
  (apply embed "panel" data label m))
  
;;------------ 应用  
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
                                (bar data0 label0) " "
                                (line data0 label0) " "
                                (circle data0 label0) " "
                                (pie data0 label0) )}
         {:title "多序列图形" :html (html
                                 (bar data3 label3) " "
                                 (line data3 label3) " "
                                 (stack data3 label3) " "
                                 (area data3 label3) " "
                                 (barline data3 label3) )}
         {:title "其他图形" :html (html
                                (tower {"工作人口" 500000000 "成人人口" 700000000 "中国人口" 1500000000 "世界人口" 5000000000} {}) " "
                                (funnel {"所有账户" 5000 "活动账户" 3000 "价值客户" 1500 "VIP客户" 300} {}) " "
                                (cup 65 {:lowerLimit 0 :upperLimit 100} 200) " "
                                (panel {:value 135 :range [90 100 120 150]} {:lowerLimit 90 :upperLimit 150}) 
                                (panel {:value 87 :range [30 50 55 60 65 70 75 80 85 90 100]} {:lowerLimit 30 :upperLimit 100}) 
                                )}
         {:title "地图图形" :html (html 
                                [:a {:href (format "%s/c/chartf/chinamap" webapp) :target "_blank"} 
                                 "中国地图 (另页打开)"] )} 
         ]) )))

(defn index 
  [request]
  "app: 各种图形示例"
  (let [vars (query-vars request)
        ctype (or (vars "ctype") "bar")
        data (or (vars "data") (str (vec (random-n 10))))
        label (or (vars "label") (str {:title "例子" :x "序号" :y "值"}))]
    (html-body ""
      [:a {:href (format "%s/c/chartf/dojo" webapp)} "查看基本例子"] " &nbsp; | &nbsp; "
      [:label "石油石化相关图表例子: &nbsp; "] 
      (map (fn [[k v]] (html [:a {:href (format "%s/c/chartf/%s" webapp k)} v] " &nbsp; ")) 
           {"d1" "杯图" "d2" "仪表板图" "d3" "油品综合分析图"})
      [:br]
      (embed ctype (read-string data) (read-string label)) )))

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
                "map.setDataURL(\"%s/c/chartf/chinamap2?charset=gbk\");"		   
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
    [:div {:align "center"}
     [:h1 "装置负荷率——常减压"] 
     [:h2 {:style "color: #aa5500"} "小时能力: 16000"] 
     [:h2 {:style "color: green"} "小时处理量: 14500"] 
     (cup (double (/ 14500 160)) {:lowerLimit 0 :upperLimit 100} 200)]
    [:div {:align "center"}
     [:h1 "装置负荷率——催化裂化"] 
     [:h2 {:style "color: #aa5500"} "小时能力: 4800"] 
     [:h2 {:style "color: green"} "小时处理量: 4600"] 
     (cup (double (/ 4600 48)) {:lowerLimit 0 :upperLimit 100} 200)]
    [:div {:align "center"}
     [:h1 "装置负荷率——焦化"] 
     [:h2 {:style "color: #aa5500"} "小时能力: 1600"] 
     [:h2 {:style "color: green"} "小时处理量: 1500"] 
     (cup (double (/ 1500 16)) {:lowerLimit 0 :upperLimit 100} 200)]
    [:div {:align "center"}
     [:h1 "装置负荷率——重整"] 
     [:h2 {:style "color: #aa5500"} "小时能力: 1500"] 
     [:h2 {:style "color: green"} "小时处理量: 1200"] 
     (cup (double (/ 1200 15)) {:lowerLimit 0 :upperLimit 100} 200)]
    [:div {:align "center"}
     [:h1 "装置负荷率——乙烯"] 
     [:h2 {:style "color: #aa5500"} "小时能力: 250000"] 
     [:h2 {:style "color: green"} "小时处理量: 205000"] 
     (cup (double (/ 205000 2500)) {:lowerLimit 0 :upperLimit 100} 200)]
    ))

(defn d2
  "app: panel仪表板图"
  []
  (html-body
    ""
    [:style (str "div { float:left; margin:20px }")]
    [:div {:align "center"}
     [:h1 "经济技术指标——柴汽比"] 
     [:h2 {:style "color: #aa5500"} "计划: 2.07"] 
     [:h2 {:style "color: green"} "实际: 2.09"] 
     (panel {:value 2.09 :range [2.0 2.07 2.1]} {:lowerLimit 2.0 :upperLimit 2.1})]
    [:div {:align "center"}
     [:h1 "经济技术指标——汽煤柴收率"] 
     [:h2 {:style "color: #aa5500"} "计划: 64.37%"] 
     [:h2 {:style "color: green"} "实际: 63.33%"] 
     (panel {:value 63.33 :range [50 64.37 80]} {:lowerLimit 50 :upperLimit 80})]
    [:div {:align "center"}
     [:h1 "经济技术指标——汽柴油收率"] 
     [:h2 {:style "color: #aa5500"} "计划: 61.44%"] 
     [:h2 {:style "color: green"} "实际: 60.66%"] 
     (panel {:value 60.66 :range [50 61.44 70]} {:lowerLimit 50 :upperLimit 70})]
    [:div {:align "center"}
     [:h1 "经济技术指标——乙烯收率比"] 
     [:h2 {:style "color: #aa5500"} "计划: --.--%"] 
     [:h2 {:style "color: green"} "实际: 33.8%"] 
     (panel {:value 33.8 :range [10 26 38 50]} {:lowerLimit 10 :upperLimit 50})]
    ))

(defn d3
  "app: 油品综合分析图"
  []
  (html-body 
    ""
    (d "油品购进分析" (str "全国总计：" (apply + [600 55 36 23 120]))
       pie (zipmap '[直属炼厂 地方炼厂 中石油 中石化 社会经营单位] [600 55 36 23 120]))
    (d "油品类型分析" ""
       pie (zipmap '[汽油 柴油 煤油] [196 480 9]))
    (d "客户类型分析" ""
       pie (zipmap '[加油站零售用户 加油站机构用户 各类加油站 再流通环节] [302 82 14 159]))
    (d "评估类型分析" ""
       pie (zipmap '[自有类型 客存类型 代管类型] [392 130 26]))
    (d "油库类型分析" ""
       pie (zipmap '[油库 加油站] [492 103]))
    (d "购进量趋势分析" "全国地区"
       line {:category (vec (map #(str % "月") (range 1 13))) 
             "柴油" (vec (map #(+ 500 %) (random-n 12 200)))
             "汽油" (vec (map #(+ 100 %) (random-n 12 100))) 
             "煤油" (vec (map #(+ 50 %) (random-n 12 20))) } )
    (d "销售量价分析" "全国地区"
       barline {:category (vec (map #(str % "月") (range 1 13))) 
                "销售量" (vec (map #(+ 600 %) (random-n 12 300)))
                "销售价格" (vec (map #(+ 6 %) (random-n 12 4))) } )
    (d "库存量成本分析" "全国地区"
       barline {:category (vec (map #(str % "月") (range 1 13))) 
                "库存量" (vec (map #(+ 600 %) (random-n 12 300)))
                "库存成本" (vec (map #(+ 400 %) (random-n 12 600))) } )
    (html 
      [:div {:align "center" :style "float: left; margin: 20px"}
       [:h1 "销售量同比分析"] [:h2 "全国地区销售量：900万吨"]
       (panel {:value 23 :range [-100 -50 0 50 100]} {:lowerLimit -100 :upperLimit 100})])
    (html 
      [:div {:align "center" :style "float: left; margin: 20px"}
       [:h1 "销售量环比分析"] [:h2 "全国地区销售量：900万吨"]
       (panel {:value -3 :range [-100 -50 0 50 100]} {:lowerLimit -100 :upperLimit 100})])
    ))
  
;;; 黄金集团尾矿库
(defn- bar-t
  [title data & m]
  (html
    [:div {:align "center" :style "float: left; margin: 5px; border: 1px dotted lightgray"}
     [:h1 title]
     (apply bar data {} m)]))

(defn tailings []
  (html-body
    ""
    [:div {:align "center"}
     [:h1 {:style "color: black"} "中国黄金集团尾矿库现状统计汇总表"]
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
