;;;; 所有自有工具的web界面版本的 CljServlet 控制器
(ns wr3.clj.app.tool)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.web 'wr3.clj.chart) ; 如果以上类改变了不重启可使用 :reload
(use 'hiccup.core)
(import '(wr3.bank IDUtil) '(wr3.util Datetime) '(wr3.bank Areacode) '(tool Goodname))

(defn index
  "app: tool.*工具的web界面化主页"
  []
  (html-apps (map #(format "<h1><a href=\"%s/c/tool/%s\">%s</a></h1><p>%s</p>"
                     webapp (second %) (first %) (last %))
               [["身份证" "pid"   "身份证转换、分析"]
                ["分词"   "gname" "Tokenizer分词，及Goodname变量名"]
                ["标拼音" "pinyin"    "得到1+个汉字串的拼音"]
                ["算税率" "tax"   "计算个人所得税"]
                ["读金额" "money" "转换成大写金额；得到易读大约数"]
                ["翻译"   "dict"  "得到1+个中文词的简明英文解释"]
                ] )))

;;;---------------------------- pid
;; pid校验身份证的样例
(def usage "样例：
 532525810923001 --> 532525198109230018
 230121７５1021　00１ --> 230121197510210019
 11011420100213752 --> 11011420100213752X
 511102820125203 --> 511102198201252032")

;; 和 chinamap.gif 对应的热点设置和代码、名称
(def chinamap
  [["73,87,103,104"   "65" "新疆"] ["144,125,173,142" "63" "青海"] ["84,158,115,175"  "54" "西藏"]
   ["220,90,257,105"  "15" "内蒙"] ["346,68,371,83"   "22" "吉林"] ["323,84,351,100"  "21" "辽宁"]
   ["298,100,326,114" "12" "天津"] ["288,120,314,136" "37" "山东"] ["268,107,295,121" "13" "河北"]
   ["207,116,235,130" "64" "宁夏"] ["186,162,214,179" "51" "四川"] ["256,142,281,157" "41" "河南"]
   ["305,144,332,159" "32" "江苏"] ["320,162,342,177" "31" "上海"] ["216,168,243,185" "51" "重庆"]
   ["250,162,277,177" "42" "湖北"] ["283,161,308,177" "34" "安徽"] ["213,196,239,209" "52" "贵州"]
   ["243,183,269,198" "43" "湖南"] ["273,185,299,200" "36" "江西"] ["308,177,334,193" "33" "浙江"]
   ["179,210,204,225" "53" "云南"] ["227,214,252,228" "45" "广西"] ["231,248,257,263" "46" "海南"]
   ["292,200,319,214" "35" "福建"] ["259,219,287,231" "44" "广东"] ["272,90,302,107"  "11" "北京"]
   ["196,130,222,145" "62" "甘肃"] ["246,116,272,130" "14" "山西"] ["224,136,252,151" "61" "陕西"]
   ["244,231,270,246" ""   "澳门"] ["276,232,304,248" ""   "香港"] ["311,228,338,243" ""   "台湾"]
   ["348,45,389,61"   "23" "黑龙江"]])

;; 生成单个省份区域的 html 代码
(defn- html-area [area]
	[:area {:shape "rect"
         :coords (first area)
         :href (format "javascript:select_area('%s', '%s');" (second area) (last area))}])

;; 生成整个 chinamap 地图选择的 html 代码片段
(def html-chinamap
  (html
    [:div#chinamap
     [:img {:src (format "%s/img/chinamap.gif" webapp) :usemap "#Map"}]
     [:map {:name "Map" :class "maparea"} (map html-area chinamap)]
     ]))

;; 生成返回 tool 主页的html代码
(def home (html [:a {:href "./"} "Tools"]))

;; 生成身份证序号下拉选择列表html代码片段，options [1 501 2)为男、[2 502 2)为女
(defn- options
  [i0] (apply str (map #(format "<option value='%03d'>%03d</option>" % %) (range i0 (+ 500 i0) 2))))

(defn pid
  "app: 身份证应用：校验、生成界面。"
  []
  (html [:html head-set
         [:body {:onload "pid_onload();"}
          [:h1 (str home " / 身份证应用")][:br]
          ;------ 校验
          [:div
           	[:form.app
          		[:input {:type "text" :id "pid" :value "511102820125203" :title "输入待验证的身份证号码"}] "&nbsp;"
              [:input {:type "button" :id "check_pid" :value " 提交验证 "}]
              ]
           	[:pre usage][:br]]
          ;------ 生成
          [:div
           [:table.wr3table
            [:thead
             [:tr [:th {:colspan 5} "生成身份证"]]]
            [:tbody
             [:tr {:align "center"}
              [:td "地区码"] [:td "出生年月"] [:td "性别"] [:td "序号"] [:td ""]]
             [:tr
              [:td [:input#areacode {:type "text" :value "532525"}]]
							[:td [:input#datepicker {:type "text" :value "19810630"}]]
              [:td [:div#gender
                    [:input {:type "radio" :id "male"   :name "gender" :value "male" :checked "checked" }] "男"
                    [:input {:type "radio" :id "female" :name "gender" :value "female"}] "女"
               ]]
              [:td
               [:select#sid1 {:size "1"} (options 1)]
               [:select#sid2 {:size "1" :style "display:none"} (options 2)]]
              [:td [:button#gen_pid "提交生成"]]
              ]]]
					 [:table {:style "border:0px;"}
            [:tr 
             [:td {:style "vertical-align: top"} html-chinamap]
             [:td {:style "vertical-align: top"} [:h2#area1 "省份"] [:div#area2 "区县"]]
             ]]]
          ;------ 结果展示
          [:div.result "结果："]
          ]] ))


(defn photo
  "service: 带底图的身份证显示，传入身份证号，得到身份证头像的 html 片段"
  [id]
  (let [pid id
        f1 (fn [left top] {:style (format "position: relative; left: %d; top: %d" left top)})
        f2 (fn [sex age] (str
                           (if (= sex "男") "m" "f")
                           (cond (< age 0) "0" (>= age 80) "7" :else (quot age 10)) 0))
        f3x (str "王李张刘陈杨黄赵吴周徐孙马朱胡郭何高林罗郑梁谢宋唐许韩冯邓曹彭曾肖"
                 "田董袁潘于蒋蔡余杜叶程苏魏吕丁任沈姚卢姜崔鍾谭陆汪范金石廖贾夏韦傅方白邹孟熊秦邱江尹薛闫段雷侯龙"
                 "史陶黎贺顾毛郝龚邵万钱严覃武戴莫孔向汤")
        f3 (fn [] (str (rand-nth f3x) (rand-nth f3x) (when (zero? (rand-int 2)) (rand-nth f3x))))
        f4 (fn [] (rand-nth ["汉" "回" "畲" "塔塔尔" "阿昌" "哈萨克" "土家" "景颇" "哈尼" "土" "白" "维吾尔"
                             "保安" "赫哲" "乌孜别克" "基诺" "布依" "拉祜" "锡伯" "黎" "东乡" "蒙古" "仫佬"
                             "达斡尔" "藏" "毛南" "裕固" "俄罗斯" "德昂"  "僳僳"  "瑶" "朝鲜"  "布朗"  "满"
                             "彝"  "门巴"  "侗"  "苗" "佤"  "羌" "独龙"  "怒"  "珞巴"  "普米"  "傣" "纳西"
                             "高山" "壮" "额伦春"  "塔吉克"  "京"  "仡佬"  "鄂温克"  "撒拉"  "柯尔克孜" "水"]))
        infos (IDUtil/infos pid)
        area  (first infos)
        year  (subs pid 6 10)
        month (subs pid 10 12)
        day   (subs pid 12 14)
        sex   (last infos)
        age   (- 2011 (Integer/parseInt year))
        bgimg (format "background-image:url('%s/img/pid/pid2.jpg')" webapp)
        img (format "%s/img/pid/%s.jpg" webapp (f2 sex age))]
    (html [:div {:style (str "width:500; height:317; -moz-border-radius:18; -webkit-border-radius:18; "
                          "font-family:Arial,微软雅黑; font-size:18; font-weight:bold; " bgimg)}
           [:div (f1 90  43)   (f3)] ; 随机姓名
           [:div (f1 90  59)   sex]
           [:div (f1 200 37)   (f4)] ; 随机民族
           [:div (f1 90  53)   year]
           [:div (f1 170 32)   month]
           [:div (f1 217 10)   day]
           [:div (f1 90  28)   area]
           [:div (f1 170 106)  pid]
           [:div (f1 320 -130) [:img {:src img}]]
           ])))

;; 得到身份证显示的html结果
(defn- pid1 
  [id]
  (html
      [:h2 (format "结果：(%s)" (Datetime/datetime))]
      [:div {:id "dialog" :title "结果"} (photo id)]
;    [:p (IDUtil/info id)]
    ))

(defn pid2
  "service: pid查询的结果"
  [id]
  (pid1 (IDUtil/to18 id)))

;; 生成单个区县选择的 html 代码
(defn- f [kv]
  (let [k (key kv) v (val kv)]
  (html [:a {:href "#" :onclick (format "select_area2('%s','%s');" k v)} v])))

(defn- col3
  "把列表中的东西分三栏显示"
  [coll]
  (let [n (count coll)
        n3 (quot n 3)]
    (if (< n 7)
      (html [:ul (for [e coll] [:li e])])
      (html [:table
             [:tr {:valign "top"}
              [:td [:ul (for [e (slice coll 0 (dec n3))] [:li (f e)])]]
              [:td [:ul (for [e (slice coll n3 (dec (* 2 n3)))] [:li (f e)])]]
              [:td [:ul (for [e (slice coll (* 2 n3) -1)] [:li (f e)])]]
              ]]))))

(defn area2
  "service: 分3栏显示的二级行政区划列表，可选择; 传入地区代码，得到显示的html代码片段"
  [id]
  (col3 (into (sorted-map) (Areacode/areas2 id))))

(defn gen-pid
  "service: 17位生成18位pid, 传入17为pid，得到18位pid显示代码"
  [id]
  (pid1 (IDUtil/gen18 id)))

;;;------------------------ gname, dict, tax, pinyin, money 输入查询的共同模式抽象
(defn- input-submit-app
  "输入、回车或者按钮提交类型应用
id: 应用id;
info: 应用描述;
input0: 初始输入值
html2: 可选参数，其他需要补充在<body>最后的html内容
"
  [id info input0 & html2]
  (html
    [:html head-set
     [:body {:onload (format "isapp_onload('%s');" id)}
      [:h1 (str home " / " info)][:br]
      [:form.app
       [:input#in {:value input0}] " "
       [:input#ok {:type "button" :value " 提交处理 "}] " "][:br]
      [:div "结果："][:br]
      [:div#rt  (let [f (ns-resolve 'wr3.clj.app.tool (symbol (str id "2")))] (f {:id [input0]}))]
      (apply str html2)
      ]]))

;;;------------------------ 分词，good-name

;; hello-ClojureWorld -> '(hello Clojure World)
(defn- var-to-words [v]
  (let [s1 (apply str (map #(cond (Character/isUpperCase %) (str " " %)
                                  (or (= % \_) (= % \-)) (str " ")
                                  :else (str %)) v))]
    (filter #(not= % "") (split s1 " "))))

;; '(hello clojure world) -> helloClojureWorld 或 hello-clojure-world 或 hello_clojure_world
(defn- words-to-var
  ([words] (apply str (first words) (map capitalize+ (rest words))))
  ([words sep] (join words sep)))

;; hello-Clojure_world -> helloClojureWorld
(defn- format-var [s]
  (let [ww (var-to-words s)] (words-to-var ww)))

(defn gname2
  [id]
  (let [id0 (or id "helloClojureWorld")
        goodname (Goodname/instance)]
    (if (wr3.util.Charsetx/hasChinese id0)
      (let [s1 (join (.token goodname id0) " | ")
            en (.en goodname id0)
            s2 (join (var-to-words en) " ")]
        (join [s1 s2 en] "<br/>"))
      (let [s1 (format-var id0)
            cn (.cn goodname s1)
            s2 (join (var-to-words s1) " | ")]
        (join [s1 s2 cn] "<br/>")))))

(defn gname []
  (input-submit-app "gname" "分词、变量名解析翻译应用" "输入中文或者英文"))

;;;------------------------ 测试auto-complete
(defn- f2 [s term] (>= (.indexOf s term) 0))

(use 'clojure.contrib.json)
(defn auto
  [request]
  (let [term0 ((query-vars request) "term")
        term (if (nil? term0) "-" term0)
        area-map (read-string (slurp "f:/dev3/area-map.clj" :encoding "UTF-8"))]
    (json-str (filter #(f2 % term) area-map))))

;;
(defn complete
  "自动完成areacode"
  []
  (html
    [:html head-set
     [:body {:onload "area_complete();"}
      [:input {:id "complete"}]]]))

;;;------------------------ 测试dialog
(defn
  dialog
  "把一个div变成dialog进行显示。"
  []
  (html
    [:html head-set
     [:body {:onload "dialog();"}
      [:div {:id "dialog" :title "结果"} "对话框"]]]))

;;;------------------------ ce、ec字典及变量翻译
;;-------- 把字典装入内存太大，改从mongodb中查询
;(import tool.Dict)
;(def ec-dict (Dict/ec))
;(def ce-dict (Dict/ce))
;(defn- search
;  "模糊查询"
;  [s dict]
;  (for [[k v] (.getAll dict) :when (or (has? k s) (has? v s))]
;    (if (or (= k s) (= v s)) (print-str "<b>" k v "</b>") (print-str k v))))

(use 'somnium.congomongo)

(defn- search2
  "从mongodb模糊查询"
  [s typ]
  (let [db "wr3"
        tb1 "dict_ce"
        tb2 "dict_ec"
        tb (if (= (name typ) "ce") tb1 tb2)
        conn (make-connection db)]
    (with-mongo conn
      (let [rt (fetch tb :where {:k (re-pattern (format "%s" s))})]
        (for [e rt :let [k (:k e) v (:v e)]] (if (= k s)
                                               (print-str "<b>" k v "</b>")
                                               (print-str k v)))))))

(defn dict
  "app: 字典查询界面"
  []
  (input-submit-app "dict" "字典查询应用" "dict"))

(defn- isworld? [world] (re-matches #"[a-zA-Z]+" world))

(defn dict2
  "service: 给出中文或者英文，得到对应释义"
  [id]
  (let [id0 (.toLowerCase (or id "dict"))
        t0 (System/currentTimeMillis)
        ;rt (if (isworld? id0) (search id0 ec-dict) (search id0 ce-dict))
        rt (if (isworld? id0) (search2 id0 'ec) (search2 id0 'ce))
        t1 (System/currentTimeMillis)]
    (html
      [:div (join rt "<br/>")]
      [:h2 (count rt) "个结果, 用时" (- t1 t0) "ms(毫秒)"]
      )))

;;;------------------------ 拼音简拼、全拼
;(def py (tool.Pinyin.))

(defn pinyin
  "app: 查询汉字全拼简拼的录入显示界面"
  []
  (input-submit-app "pinyin" "汉字简拼、全拼应用" "汉字拼音"))

(defn pinyin2
  "service: 给出汉字，得到全拼和简拼"
  [id]
  (let [id0 (or id "汉字拼音")
        py (tool.Pinyin/instance)]
    (html
      [:div "全拼：" (.full py id0)]
      [:div "简拼：" (.jp py id0)])))

;;;------------------------- 个人所得税计算
(defn- tax-chart []
  (let [m0 (range 3000 22001 1000)
        tax1 (map #(tool.Tax/tax  %) m0)
        m1 (map - m0 tax1) ; 税后（老） 
        tax2 (map #(tool.Tax/taxNew %) m0)
        m2 (map - m0 tax2) ; 税后（新）
        ]
    (html
      [:br][:div "图表："][:br]
      [:img {:src (str "/chart?width=1200&height=600&" (uri line m0 m1 m2))}])))

(defn tax
  "app: 算个人所得税的输入显示界面"
  []
  (input-submit-app "tax"  "个人所得税利率(2011-9-1前)应用" "30000" (tax-chart)))

(defn tax2
  "service: 给出收入，得到所得税及余额"
  [id]
  (let [id0 (double (wr3.util.Numberx/toInt (or id "0") 30000))
        tax (tool.Tax/tax id0)]
    (html
      [:div (format "%.2f - %.2f = %.2f" id0 tax (- id0 tax))])))

;;;---------------------- 金额读取应用
(defn money2
  "service: 给出金额，得到大写读数及简读"
  [id]
  (let [id0 (or id "2468013570.14")
        id1 (Double/valueOf id0) ]
    (html [:div (str "金额 (圆)：" id0)][:br]
          [:div (str "精确读数：" (wr3.bank.Currency/asChinese id0))][:br]
          [:div (str "简略读数：" (wr3.bank.Currency/about id1))])))

(defn money
  "app: 展示金额解析的录入显示界面"
  []
  (input-submit-app "money" "读取金额大写应用" "21003.4"))



