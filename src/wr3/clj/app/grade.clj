(ns ^{:doc "
  评分系统。
  上市公司综合评价系统，对应数据库为mongodo的grade"
      :author "jamesqiu"}
     wr3.clj.app.grade)

(use 'wr3.clj.web 'wr3.clj.u 'wr3.clj.n 'wr3.clj.s 'wr3.clj.db 'wr3.clj.nosql)
(use 'hiccup.core)
(use 'somnium.congomongo)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 从mongodb读取配置、数据
(defn- dd
  "得到dict表中字典内容，usage：(dd :industry) ; 返回 {:A '农、林、牧、渔业', :B '采掘业', ..} "
  [code]
  (with-mdb "grade"
    (:v (mdb-get-one :dict :where {:code (name code)}))))
  
(defn- get-indic
  "得到indic表的内容：title或者indic配置。
  @id 'title' | 'head' | 'indic' 
  @return (indic 'title') 返回名称字符串，
  (indic 'head') 返回['..' '..' ..]
  (indic 'indic') 返回配置[{..} {..} ..] "
  [id]
  (with-mdb "grade"
    (:v (mdb-get-id :indic (name id)))))  

(defn- get-indic-weight
  "返回：{11 {201 0.04, 202 0.04, ..} 12 {..} ..}"
  []
  (into 
    {} (for [e (get-indic "indic")]
         [(:code e) 
          (into {} (for [e2 (:child e)] [(:code e2) (* 0.0001 (:weight e) (:weight e2) )]))])))

(defn- get-indic2-weight
  "得到所有2级评价指标的最终加权如：{201 0.04, 202 0.04, .. , 229 0.0075} "
  []
  (let [indic (get-indic "indic")
        rt (for [e indic e1 (:child e)]
             [(:code e1) (* 0.0001 (:weight e) (:weight e1))])]
    (into {} rt) ))
  
(defn- get-corp-name
  "得到指定的上市公司信息
  @id 上市公司代码"
  [corp-code]
  (with-mdb "grade"
    (:name (mdb-get-one :corp :where {:code (or corp-code "000001")})))) 
  
(defn- get-data
  "得到一条指定评估数据
  @usage: (get-data '000001' '2012' '2') 
  @return {:year '2012' :month '2' :code '000001', :222 88, :229 83, ..} "
  [corp-code year month]
  (with-mdb "grade"
    (mdb-get-one :data :where {:code corp-code :year year :month month})))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; layout
(defn- app-top
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id "layout_north" :style "height: 80px; padding: 10px;" }
    [:span {:class "layout_title"} "上市公司综合评价——演示版"]
    [:div {:style "float: right"} "当前用户: " [:span ".."]]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher "eui_search1" :style "width: 250px;"} 
       [:div {:name "范围A" :iconCls "icon-ok" :style "margin: 0px"} "公司搜索"]
       [:div {:name "范围B" :iconCls "icon-tip" :style "margin: 0px"} "局部搜索"])]          
    ))

(defn- app-left
  "layout.west"
  []
  (eui-region 
    "west" 
    {:title "快捷导航" :style "width: 210px"}
    [:div#corp {:code "000001" :style "margin: 10px"} "当前被评估的是：" 
     [:span#corp2 {:style "font-family:微软雅黑; font-weight:bold" :title "在下面的“评价工作”中更改"} "深发展Ａ"] [:br]
     "评价期 : " [:b [:span#year "2012"] "年" [:span#month "1"] "月"] "&nbsp;"
     (eui-button {:id "year_month_bt" :plain "true" :iconCls "icon-arrow" :title "更改评价期"} "") ]
    (eui-accord 
      {:id "accord1" :style "" }
      (eui-accord- 
        {:iconCls "icon-search"} "评价工作"
        (eui-button {:id "hs300_bt" :plain "true" :iconCls "icon-search"} "沪深300公司") [:br]
        (eui-button {:id "corp_bt" :plain "true" :iconCls "icon-search"} "所有上市公司") [:br]
        (eui-button {:id "indic0_bt" :plain "true" :iconCls "icon-sum"} "上市公司综合评价") [:br]
        (map-indexed 
          (fn [i e] 
            (html 
              (eui-button {:group "indic1_bt" :code (:code e) :plain "true" :iconCls "icon-arrow" } 
                          (format "%d、%s" (inc i) (:name e))) 
              [:br]))
          (get-indic "indic"))
        (eui-button {:id "grade_bt" :plain "true" :iconCls "icon-search" } "统计报表") [:br]
        )    
      (eui-accord- 
        {:iconCls "icon-search"} "管理维护"
        (eui-button {:id "grade_bt" :plain "true" :iconCls "icon-search" } "评估指标体系") [:br]
        (eui-button {:id "grade_bt" :plain "true" :iconCls "icon-search" } "上市公司") [:br]
        (eui-button {:id "grade_bt" :plain "true" :iconCls "icon-help" } "使用帮助") [:br]
        ))    
    ))

(defn- app-main
  "layout.center"
  []
  (eui-region 
    "center" 
    {:border "false" :style "padding: 2px"} 
    [:iframe {:id "ifrm1" :src "" 
              :width "99%" :height "99%" :style "border: 1px dotted gray"}] ))

(defn- app-foot
  "layout.south"
  []
  (eui-region 
    "south" 
    {:style "height: 30px; background: #cde; padding: 5px; text-align: center; color: #444"} 
    "版本信息: IDP集成数据平台系列——行业支撑平台 @" (year)))

(defn index
  "app: 常用应用框架"
  []
  (eui-layout
    {:id "layout1" :onload "grade_onload()"}
    ;----------------------- north
    (app-top)
    ;----------------------- west
    (app-left)
    ;----------------------- center
    (app-main)
    ;----------------------- east
;    (app-right)
    ;----------------------- south
    (app-foot)
    ))

(defn- fmt 
  ""
  [e] 
  (format "%s : %s%%" (:name e) (:weight e)))

(defn- sum-indic2
  "@rt-indic2: 某个2级指标体系的所有指标 [{:code 201, :name '所..', :weight 20} {:code 202, ..} ..] "
  [rt-indic2 corp-code year month]
  (sum (map #(let [v (get (get-data corp-code year month) (-> % :code str keyword))
                   w2 (:weight %) 
                   v2 (* v w2 0.01)] v2)
            rt-indic2)))

(defn- rank
  "根据分值算出评级"
  [score]
  (let [grade {:AAA ">= 90" :AA ">= 85" :A ">= 80" :BBB ">= 75" :BB ">= 70" :B ">= 65" :CCC ">= 60" :CC ">= 55" :C "< 55"} ]
    (cond
      (>= score 90) "AAA"
      (>= score 85) "AA"
      (>= score 80) "A"
      (>= score 75) "BBB"
      (>= score 70) "BB"
      (>= score 65) "B"
      (>= score 60) "CCC"
      (>= score 55) "CC"
      (>= score 50) "C"
      :else "末级"
    )))

(defn- to-point4
  "四舍五入到小数点后4位
  @d 浮点数如3.14159"
  [d]
  (* 0.0001M (Math/round (* d 10000))))

(defn- corp-score-sum
  "综合评分及等级，驾驶仪表板示意图"
  [corp-code year month rt-indic]
  (let [score (sum (map #(* (:weight %) (sum-indic2 (:child %) corp-code year month) 0.01) rt-indic))]
    (html
      [:a {:name "rank_result"}]
      [:h2 (format "修正指标分值：<span style='color:red'>%.4f</span>（评级参考：<span style='color:red'>%s</span>）" 
                   score (rank score))]
      (panel {:value (to-point4 score) 
              :range [30 50 55 60 65 70 75 80 85 90 100]} 
             {:lowerLimit 30 :upperLimit 100} 500 250) )))

(defn- corp-score-detail 
  "各一级评价指标分值cup图显示"
  [rt-indic data]
  (eui-panel 
    {:id "panel1" :closed "true" :title "各一级评价指标得分 : " :style "color: blue; width: 900px; padding: 5px" }
    (for [e rt-indic]
      (let [name1 (:name e) ; 一级指标名称
            w1 (:weight e) ; 一级指标权重
            indic2 (:child e) ; 二级指标
            ; 二级指标修正值列表
            all-v2 (for [e2 indic2] 
                     (let [w2 (:weight e2)
                           v (get data (-> e2 :code str keyword)) ]
                       (* v w1 w2 0.0001)))
            ; 一级指标修正分值
            score2 (to-point4 (sum all-v2))]
        [:div {:style "float:left; margin: 10px"}
         [:h2 name1] "满分值 : " w1 [:br] "得分值 : " score2 [:br]
         (cup (double score2) {:lowerLimit 0 :upperLimit w1} 150 250)] )) ))

(use '[wr3.clj.app.chartf :only (panel cup)])

(defn indic
  "app: 显示整个指标体系
  @ids 第一个参数为上市公司代码如'000001'，第二个参数为年月如'2012-1' "
  [ids]
  (let [corp-code (or (first ids) "000001")
        year-month (or (second ids) "2012-1")
        [year month] (split year-month "-")
        data (get-data corp-code year month) ; {:201 82 :202 98 ..}
        rt-caption (get-indic "caption")
        rt-title (get-indic "title")
        rt-head (get-indic "head")
        rt-indic (get-indic "indic")
        rowspan (map #(count (:child %)) rt-indic)
        css1 "vertical-align:middle; text-align:center; width:110px"
        css2 "vertical-align:middle; text-align:left; cursor: hand" ]
    (html
      [:a {:name "top"}]
      [:h1 corp-code " : " (get-corp-name corp-code)
       " &nbsp; " (format "（评价期：%s年%s月）" year month)]
      (eui-button {:href "#rank_result"} "评级结果参考") " &nbsp; "
      (eui-button {:href "#rank_detail" :onclick "grade_rank_detail()"} "评级结果细项图示") " &nbsp; "      
      ; 指标体系大表
      [:table {:class "wr3table" :border 1 :style "border:1px solid black"}
       [:caption rt-caption]
       [:tbody
        [:tr 
         [:td {:rowspan (+ 1 (sum rowspan)) :style css1} rt-title]
         (map #(html [:th %]) rt-head)]
        (map-indexed 
          (fn [i e] ; e  [{:code 11, :name "上..", :weight 20, :child [{:code 201, :name "所..", :weight 20} ..]}, ..] 
            (let [w1 (:weight e) ; 一级指标权重，如：20
                  f-td-score2 (fn [e2] ; “评价基本指标”和“修正指标”的html片段
                                (let [code (:code e2)
                                      v (get data (-> code str keyword))
                                      w2 (:weight e2)
                                      v2 (* v w1 w2 0.01 0.01)]
                                  (html
                                    [:td {:align "right" :group "score" :title "点击更改"} v]; 评价指标值 eg. 85
                                    [:td {:align "right" :title (format "%.2f (=%s*%s%%*%s%%)" v2 v w1 w2)} ; 修正指标值 eg. 85*20%*20%=3.4 
                                     (format "%.2f" v2)] ))) ]
              (html [:tr 
                     [:td {:rowspan (nth rowspan i) :style css2 :title "点击查看细该项" 
                           :onclick (format "grade_indic2('%s','%s','%s-%s')" (:code e) corp-code year month)} 
                      (fmt e)] ; 一级指标 eg. "上市公司治理评价 : 20%"
                     (let [e2 (-> e :child first)] ; e2是第1个二级指标eg. {:code 201, :name "所有权结构及其影响", :weight 20}
                       (html [:td (fmt e2)]
                             (f-td-score2 e2))) ]
                    (for [e2 (-> e :child rest)] ; e2是其他二级指标（不含第一个）如：'{:code 202, :name "金融相关者关系", :weight 25}
                      [:tr
                       [:td (fmt e2)] ; rest 二级指标 eg. 金融相关者关系
                       (f-td-score2 e2) ]) )))
          rt-indic) ]]
      ; 综合评分及等级，驾驶仪表板示意图
      (corp-score-sum corp-code year month rt-indic) [:br]
      (eui-button {:href "#top" :style "margin: 3px" :iconCls "icon-redo"} "回到上面") [:br]
      ; 各一级评价指标分值cup图显示
      [:a {:name "rank_detail"}]
      (corp-score-detail rt-indic data) )))

(defn indic2
  "app: 显示1级指标下面的二级指标
  @ids 第一个参数为一级指标code，如11，12，13，14，15；
       第二个参数为上市公司code，如 '000001'；
       第三个参数为年月日，如'2012-1' "
  [ids]
  (let [code (or (first ids) "11")
        corp-code (or (second ids) "000001")
        year-month (or (get ids 3) "2012-1")
        [year month] (split year-month "-")
        rt-head (get-indic "head")
        rt-indic (find-first #(= code (str (:code %))) (get-indic "indic"))
        rt-indic2 (:child rt-indic)]
    (html
      [:h1 corp-code " : " (get-corp-name corp-code)
       " &nbsp; " (format "（评价期：%s年%s月）" year month)]
      (eui-button {:onclick (format "grade_indic('%s','%s')" corp-code year-month)} "返回综合评价")
      [:table {:class "wr3table" :border 1 :style "border:1px solid black"}
       [:caption (:name rt-indic)]
       [:thead
        [:tr (map #(html [:th %]) (rest rt-head))]]
       [:tbody
        (map
          #(let [v (get (get-data corp-code year month) (-> % :code str keyword))
                 w2 (-> % :weight)
                 v2 (* v w2 0.01)]
             (html [:tr 
                    [:td (fmt %)] 
                    [:td {:align "right"} (str v)] 
                    [:td {:align "right"} (format "%.2f" v2)] ]))
          rt-indic2) ]]
      [:h2 "修正指标分值：" 
       [:span {:style "color:red"} 
        (format "%.4f" (sum-indic2 rt-indic2 corp-code year month))]]
      )))

(defn corp
  "app: 列出所有上市公司"
  []
  (with-mdb "grade"
    (let [rt-corp (fetch :corp :sort {:code 1})
          n (fetch-count :corp)]
      (html
        [:h1 (format "上市公司列表（%s 家）：" n)]
        (map 
          #(eui-button {:plain "true" :onclick (format "grade_corp('%s','%s')" (:code %) (:name %))} 
                       (format "%s: %s" (:code %)(:name %)))
          rt-corp)
        ))))

(defn hs300
  "app: 列出沪深300公司"
  []
  (with-mdb "grade"
    (let [ls300 (:v (mdb-get-one :dict :where {:code "hs300"}))
          rt-corp (fetch :corp :where {:code {:$in ls300}} :sort {:code 1}) ]
      (html
        [:h1 (format "沪深300公司列表：")]
        (map 
          #(eui-button {:plain "true" :onclick (format "grade_corp('%s','%s')" (:code %) (:name %))} 
                       (format "%s: %s" (:code %)(:name %)))
          rt-corp)
        ))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; test
(defn test1 
  "把sqlserver里面的上市公司信息提到mongodb的corp"
  []
  (let [sql "select code,name,(case exchange when 'Shenzhen' then 'sz' else 'sh' end) exchange,incode,incode2 from stocks"
        rt (select-all "mssql" sql)
        hs300 (wr3.clj.s/split 
                "000001 000002 000009 000012 000021 000024 000027 000039 000059 000060 000061 000063 000069 000100 000157 000338 000401 000402 000422 000423 000425 000527 000528 000538 000559 000562 000568 000581 000623 000625 000629 000630 000651 000680 000686 000709 000718 000728 000729 000758 000768 000776 000778 000780 000783 000792 000800 000807 000825 000839 000858 000869 000876 000878 000895 000898 000933 000937 000960 000961 000968 000969 000983 000999 002001 002007 002024 002038 002069 002073 002092 002106 002122 002128 002142 002146 002155 002202 002244 002299 002304 002310 002344 002378 002385 002399 002405 002415 002422 002431 002493 002498 002500 002594 002603 600000 600005 600008 600009 600010 600015 600016 600019 600026 600028 600029 600030 600031 600036 600037 600048 600050 600058 600062 600066 600068 600085 600089 600096 600098 600100 600104 600108 600109 600111 600115 600118 600123 600125 600132 600143 600150 600151 600153 600160 600161 600166 600169 600170 600177 600183 600188 600196 600208 600216 600219 600221 600252 600256 600259 600266 600267 600271 600276 600307 600309 600316 600320 600331 600348 600352 600362 600369 600372 600376 600380 600383 600395 600406 600415 600418 600428 600432 600456 600481 600489 600497 600498 600500 600508 600516 600518 600519 600528 600535 600546 600547 600549 600550 600582 600583 600585 600588 600595 600598 600600 600635 600642 600649 600655 600660 600664 600674 600690 600694 600703 600718 600737 600739 600741 600770 600779 600783 600795 600804 600808 600809 600811 600812 600832 600837 600839 600859 600863 600873 600875 600879 600881 600887 600893 600895 600900 600970 600971 600997 600999 601001 601006 601009 601018 601088 601098 601099 601101 601106 601111 601117 601118 601158 601166 601168 601169 601179 601186 601216 601233 601258 601268 601288 601299 601318 601328 601333 601369 601377 601390 601398 601519 601558 601600 601601 601607 601618 601628 601666 601668 601688 601699 601717 601718 601727 601766 601788 601808 601818 601857 601866 601888 601898 601899 601918 601919 601933 601939 601958 601988 601989 601991 601992 601998"
                " ")
        ]
    (with-mdb "grade"
;      (mdb-add :dict {:code "hs300" :name "沪深300代码" :v hs300})
;      (mdb-del-coll :corp)
;      (doseq [r rt]
;        (mdb-add :corp r) )
      )))


(defn test2
  "把sqlserver里面的行业信息提到mongodb的dict"
   []
  (let [sql "select distinct incode2,inname2 from stocks"
        rt (select-all "mssql" sql)
        m (into {} (map #(vector (name (:incode2 %)) (:inname2 %)) rt))
        r {:code "industry2" :name "行业分类2" :v m}]    
    (with-mdb "grade"
      (:v (mdb-get-one :dict :where {:code "industry"}))) ))

(defn test3 
  "初始化mongodb的指标表indic。所有项自己都是100分制，按照权证算出在整个系统中的分值。"
  []
  (let [indic-name "上市公司综合评价（时间跨度：三年）"
        indic [
               {:code 11 :name "上市公司治理评价" :weight 20
                :child [
                        {:code 201 :name "所有权结构及其影响" :weight 20}
                        {:code 202 :name "金融相关者关系" :weight 20}
                        {:code 203 :name "财务透明与信息披露" :weight 20}
                        {:code 204 :name "董事会的结构与运作" :weight 10}
                        {:code 205 :name "（参考标普）" :weight 30}
                        ]}
               {:code 12 :name "上市公司业绩评价" :weight 25
                :child [
                        {:code 206 :name "财务效益状况" :weight 15}
                        {:code 207 :name "资产质量状况" :weight 15}
                        {:code 208 :name "偿债风险状况" :weight 15}
                  {:code 209 :name "发展能力状况" :weight 15}
                  {:code 210 :name "市场表现状况" :weight 15}
                  {:code 211 :name "（参考中联评估）" :weight 25}
                  ]}
               {:code 13 :name "上市公司内控规范评价" :weight 20
                :child [
                        {:code 212 :name "内部环境" :weight 15}
                        {:code 213 :name "风险评估" :weight 15}
                        {:code 214 :name "控制活动" :weight 15}
                  {:code 215 :name "信息与沟通" :weight 15}
                  {:code 216 :name "内部监督" :weight 15}
                  {:code 217 :name "（参考财政部内控规范）" :weight 25}
                  ]}
               {:code 14 :name "上市公司诚信评价" :weight 20
                :child [
                        {:code 218 :name "财务诚信" :weight 15}
                        {:code 219 :name "金融诚信" :weight 15}
                        {:code 220 :name "合同诚信" :weight 15}
                        {:code 221 :name "质量诚信" :weight 15}
                        {:code 222 :name "信用记录" :weight 40}
                        ]}
               {:code 15 :name "上市公司社会责任评价" :weight 15
                :child [
                        {:code 223 :name "股东和债权人权益保护" :weight 20}
                        {:code 224 :name "职工权益保护" :weight 15}
                        {:code 225 :name "供应商、客户和消费者权益保护" :weight 15}
                        {:code 226 :name "对国家政府承担的责任" :weight 15}
                        {:code 227 :name "环境保护和社会公益保护" :weight 15}
                        {:code 228 :name "综合指标" :weight 15}
                        {:code 229 :name "（参考深交所）" :weight 5}
                        ]}
               ]
        grade {:AAA ">= 90" :AA ">= 85" :A ">= 80" :BBB ">= 75" :BB ">= 70" :B ">= 65" :CCC ">= 60" :CC ">= 55" :C "< 55"}]
    (with-mdb "grade"
;      (mdb-add :corp 
;      (mdb-add :indic {:_id "caption" :v "上市公司综合评价指标体系"})
;      (mdb-add :indic {:_id "head" :v ["评价一级指标"	"评价二级指标"	"评价基本指标"	"修正指标"]})
;      (mdb-add :indic {:_id "title" :v indic-name})
;      (mdb-add :indic {:_id "indic" :v indic})
      )))

(defn test4
  "产生mongodb中data表的随机模拟评价数据"
  []
  (with-mdb "grade"
    (let [indic (vec (map #(str (:code %)) (mapcat #(-> % :child) (:v (fetch-by-id :indic "indic"))))) ; ('201' '202' .. '229')
          ls300 (vec (:v (mdb-get-one :dict :where {:code "hs300"}))) ;  ["000001" "000002" ..]
          rt-corp (vec (map :code (fetch :corp :only [:code] :sort {:code 1}))) ; ("000001" "000002" ..)
          frand (fn [base] (zipmap indic (map #(+ base %) (random-n (count indic) (- 100 base))))) ; [20 100], (frand 20) {201 20, 202 100, ..}
          ]
      (doseq [r rt-corp]
        (if (in? r ls300) 
           (insert! :data (merge {:code r :year "2011" :month "12"} (frand 80)))
           (insert! :data (merge {:code r :year "2011" :month "12"} (frand 30))))
        )
    )))

;(test4)
;(dissoc (get-data "600109" "2012" "2") :_id)
