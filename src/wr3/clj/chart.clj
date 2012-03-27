(ns ^{:doc "生成图片图形系列，以及flash图形系列。
  可以Clojure调用本文件中的函数生成图形，也可以使用http://site/c/chartf/png?..和http://site/c/chartf/flash?..从url直接得到图形。
 --- 图片图形的chart系列 ---
 usage:
  (uri bar     [])
  (uri bar     [1 2 3 4 5])
  (uri line    [1 3 5 7] [2 4 6 8] [0 5 10 15])
  (uri pie     [1 3 5 7] [2 4 6 8] [0 5 10 15])
  (uri barline [1 3 5 7] [2 4 6 8] [0 5 10 15])
  (html [:img {:src (str \"/chart?\" (uri pie [1 3 5 7] [2 4 6 8] [0 5 10 15]))}]) 
 或者直接：
  (img bar [])
  (img bar [1 2 3])
  (img line [1 2 3] [10 20 30] [100 200 300])
  (img barline [1 2 3 4 5])
 --- Flash图形系列 ---
  data参数说明
  1、不带标注的一组或者多组值，使用[]，'()可以用vec函数转换为[]：
     [10 20 30 40]
     [[10 20 30 40] [9 3 6 4]]
  2、带标注的一组或者多组值，使用{}：
     {1季度 50 2季度 20 3季度 40 4季度 30}
     {category [1季度 2季度 3季度 4季度] 产品额 [50 20 40 30] 服务额 [9 3 6 4]} 
  生成如下的字符串：
  <chart caption=.. xAxisName=.. yAxisName=.. baseFontSize=..>
    <set name=.. value=.. />
    <set name=.. value=.. />
    <set name=.. value=.. />
  </chart>
 "}
     wr3.clj.chart)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.n)
(use 'hiccup.core)

;;;;--------------------------------------------------------------------- 静态png图形生成
(import '(wr3.web Chart))

(defn c2j
  "把Clojure的[[1 2 3] [4 5 6]]转为Java的{ArrayList(1,2,3), ArrayList(4,5,6)}"
  [data]
  (into-array (map #(java.util.ArrayList. %) data)))

(defmacro uri
  "根据chartype类型画1～n条图。"
  ([chartype]
    `(.. (Chart/create) ~chartype url))
  ([chartype & data]
    `(.. (Chart/create ) ~chartype (data (c2j (list ~@data))) url))) ; data url 为java方法

(defmacro img
  "根据chartype类型画1～n条图。"
  ([chartype]
    `(html [:img {:src (str "/chart?" (.. (Chart/create) ~chartype url))}]))
  ([chartype & data]
    `(html [:img {:src (str "/chart?" (.. (Chart/create ) ~chartype (data (c2j (list ~@data))) url))} ])))

;;;;--------------------------------------------------------------------- Flash 图形生成

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
    (let [label2 (into {} (for [[k v] label] (cond (= k :x) [:xAxisName v]
                                                   (= k :y) [:yAxisName v]
                                                   (= k :title) [:caption v]
                                                   :else [k v])))
          data2 (dataset data)]
      (html 
        [:chart 
         (merge {:showLegend 1 :showNames 1 :decimalPrecision 0 :formatNumberScale 0 :baseFontSize 12} label2)
         data2]) ))
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
  
(defn embed 
  "生成<embed ..> html代码段，参数：
  ctype: str类型，可为 bar line pie circle bars lines stack area barline tower funnel cup panel 
  data: 有|无标注的一组|多组数据; 注意：bars,lines只能接受多组数据；barline只能接受2组数据；其他只能接受一组数据
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
                             :type "application/x-shockwave-flash" :quality "high" :wmode "opaque"
                             :flashVars "_flashVars_" } ])
        ]
    (replace-first html2 "_flashVars_" (str "dataXML=" dataXML)) ))

(defn- is-1? 
  "判断是否1组值（有或者没有label）"
  [data] (= \1 (first (name (data-type data)))))
  

;;;---------- 常用Flash线柱饼环图形, 区别于png图形，后面都带上f
(defn barf 
  "1条bar或者多条bar"
  [data label & m]
  (if (is-1? data) 
    (apply embed "bar" data label m)
    (apply embed "bars" data label m) ))

(defn linef 
  "1条line或者多条line"
  [data label & m]
  (if (is-1? data)
    (apply embed "line" data label m)
    (apply embed "lines" data label m) ))

(defn pief 
  "1个pie"
  [data label & m] 
  (apply embed "pie" data label m))

(defn circlef
  "1个circle环"
  [data label & m] 
  (apply embed "circle" data label m))

(defn stackf
  "堆切的bar"
  [data label & m] 
  (apply embed "stack" data label m))

(defn areaf 
  "堆切的area"
  [data label & m] 
  (apply embed "area" data label m))

(defn barlinef
  "组合bar和line"
  [data label & m] 
  (apply embed "barline" data label m))

;;;---------- 其他Flash图形
(defn towerf
  "金字塔图"
  [data label & m] 
  (apply embed "tower" data label m))

(defn funnelf
  "漏斗图：用于筛选"
  [data label & m] 
  (apply embed "funnel" data label m))
  
(defn cupf
  "水杯图：用于完成百分比"
  [data label & m] 
  (apply embed "cup" data label m))

(defn panelf
  "仪表盘"
  [data label & m] 
  (apply embed "panel" data label m))


;; 测试
;(.. (Chart/create) (data (into-array (map #(java.util.ArrayList. %) [[1 2 3 4] [5 6 7 8]]))) url)
;(wr3.clj.chart/tojava '([1 2 3] [4 5 6]))
;(println (str "localhost/chart?"
;           (uri pie [1 3 5 7] [2 4 6 8] [0 5 10 15])))

