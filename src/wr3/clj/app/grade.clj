(ns ^{:doc "
  评分系统。
  上市公司综合评价系统，对应数据库为mongodo的grade"
      :author "jamesqiu"}
     wr3.clj.app.grade)

(use 'wr3.clj.web 'wr3.clj.u 'wr3.clj.n 'wr3.clj.s 'wr3.clj.db 'wr3.clj.nosql 'wr3.clj.tb)
(use 'hiccup.core)
(use 'somnium.congomongo)
(use '[wr3.clj.chart :only (panelf cupf barf linef pief)])

(require '[wr3.clj.app.auth :as au])
(defn auth
  "该函数被 CljServlet 调用 "
  [request fname & args]
  (cond
    (session request "wr3user") true
    :else false))

(defn exit
  "service: 退出重新登录"
  [request]
  (session-close request))
  
(def periods ["2011-10" "2011-11" "2011-12" "2012-1" "2012-2"]) ; 评价期数
;{"深市" "000,001", "中小板" "002", "深市B股" "2", "创业板" "3", "沪市" "6", "沪市B股" "9"}
(def boards ["上海主板" "深圳主板" "中小企业板" "创业板" "上海B股" "深圳B股"])
  
(defn- to-point4
  "四舍五入到小数点后4位
  @d 浮点数如3.14159
  @return 3.1416M "
  [d]
  (* 0.0001M (Math/round (* d 10000))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; 从mongodb读取配置、数据
(defn- dd
  "得到dict表中字典内容，usage：(dd :industry) ; 返回 {:A '农、林、牧、渔业', :B '采掘业', ..} "
  [code]
  (with-mdb2 "grade"
    (:v (mdb-get-one :dict :where {:code (name code)}))))
  
(defn- get-indic
  "得到indic表的内容：title或者indic配置。
  @id 'title' | 'head' | 'indic' 
  @return (indic 'title') 返回名称字符串，
  (get-indic 'head') 返回['..' '..' ..]
  (get-indic 'indic') 返回配置[{..} {..} ..] 
  (get-indic 'rank') 返回配置array-map {:AAA '>= 90', :AA '>= 85', ..} 
  "
  [id]
  (with-mdb2 "grade"
    (:v (mdb-get-id :indic (name id)))))  

(defn- get-advice
  "指定上市公司和年月，得到专家委员会意见的各项修正因子.
  @param corp-code 如 '000001' 
  @param year 年 eg.'2012' 
  @param month 月 eg. '1' 
  @return [{:_id #<ObjectId ..> :code '000001' :year '2012' :month '1' :201 0.95 :225 1.2 } {..} ..] "
  [corp-code year month]
  (with-mdb2 "grade"
    (mdb-get-one :advice :where {:code corp-code :year year :month month})))
  
(defn- get-advice-reason
  "指定上市公司和年月，得到专家委员会意见的解释原因.
  @param corp-code 如 '000001' 
  @param year 年 eg.'2012' 
  @param month 月 eg. '1' 
  @return [{:_id #<ObjectId ..> :code '000001' :year '2012' :month '1' :202 '原因..' } {..} ..] "
  [corp-code year month]
  (with-mdb2 "grade"
    (mdb-get-one :advice-reason :where {:code corp-code :year year :month month})))
  
(defn- get-corp-name
  "得到指定的上市公司信息如：'深发展A'
  @corp-code 上市公司代码如 '000001' "
  [corp-code]
  (with-mdb2 "grade"
    (:name (mdb-get-one :corp :where {:code corp-code})))) 
  
(defn- get-data
  "得到一条指定评估数据
  @usage: (get-data '000001' '2012' '2') 
  @return {:year '2012' :month '2' :code '000001', :222 88, :229 83, ..} "
  [corp-code year month]
  (with-mdb2 "grade"
    (mdb-get-one :data :where {:code corp-code :year year :month month})))

(defn- rank
  "根据分值算出评级"
  ([score ranks]
    (let [ranks (or ranks (get-indic "rank"))
          rt (find-first 
               (fn [[k v]] (let [[v1 v2] (split v " ")] (eval (read-string (format "(%s %s %s)" v1 score v2)))))
               ranks)]
      (if rt (name (key rt)) "末级")))
  ([score] (rank score nil)))

(defn- get-score2
  "得到一个二级指标的最终评分
  @param e2 二级指标eg. {:code 201, :name '所有权结构及其影响', :weight 20, :factor 1.1}
  @param data {:code '000001' :year '2012' :month '1' :201 85 :202 75 ..}
  @param advice {:code '000001', :year '2012', :month '1', :201 0.95, :202 1.2} 
  @return 7.1280 "
  [e2 w1 data advice]
  (let [code (-> e2 :code str keyword)
        w2 (:weight e2)
        f2 (:factor e2)
        v (get data code)
        a (or (get advice code) 1.0) ]
    (to-point4 (* v 0.01 w1 0.01 w2 f2 a))))

(defn- get-score1
  "得到一个一级指标的最终评分
  @param e 一级级指标eg. {:code 11, :name '上市公司治理评价', :weight 20, :child [{:code 201 ..} ..] }
  @param data {:code '000001' :year '2012' :month '1' :201 85 :202 75 ..}
  @param advice {:code '000001', :year '2012', :month '1', :201 0.95, :202 1.2} 
  @return eg. 19.2615 "
  [e data advice]
  (let [w1 (:weight e)]
    (sum (for [e2 (:child e)] (get-score2 e2 w1 data advice)))))

(defn- get-score0
  "得到一个上市公司指定月份的修正指标分值
  @usage: (get-score0 '000001' '2012' '2')
  @return 如：91.0225M "
  ([corp-code year month indic data advice]
    (let [indic (or indic (get-indic "indic"))
          data (or data (get-data corp-code year month))
          advice (or advice (get-advice corp-code year month))]
      (sum (for [e indic] (get-score1 e data advice))) ))
  ([corp-code year month] (get-score0 corp-code year month nil nil nil)))
  
(defn- set-scores
  "为mongodb的data中所有数据设置分数及评级"
  []
  (with-mdb2 "grade"
    (let [indic (get-indic "indic")
          ranks (get-indic "rank")]
      (vec 
        (doseq [data (fetch :data)]
          (let [corp-code (:code data)
                year (:year data)
                month (:month data)
                advice (get-advice corp-code year month)
                score0 (get-score0 corp-code year month indic data advice) ]
            (when (.endsWith corp-code "0") (println corp-code year month score0))
            (mdb-upd :data data (merge data {:score (double score0) :rank (rank score0 ranks)})) ))))))
  
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; layout
(defn- app-top
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id "layout_north" :style "height: 80px; padding: 10px;" }
    [:span {:class "layout_title"} "上市公司综合评价——演示版"]
    [:div {:style "position: absolute; right: 10px; top: 8px; color: gray"} "当前用户: " 
     [:span#wr3user {:style "color:red; font-weight:bold"} ".."] (space 3)
     [:a {:href "#" :onclick "grade_exit()"} "退出"]]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher "grade_search1" :style "width: 250px;" :value "000001" :title "上市公司代码模糊查询"} 
       [:div {:name "范围A" :iconCls "icon-ok" :style "margin: 0px"} "上市公司代码"]
       [:div {:name "范围B" :iconCls "icon-tip" :style "margin: 0px"} "局部搜索"])]          
    ))

(defn- app-left
  "layout.west"
  []
  (eui-region 
    "west" 
    {:title "快捷导航" :style "width: 210px"}
    [:div#corp {:code "000001" :style "margin: 10px"} "当前被评估的是：" 
     [:span#corp2 {:style "font-family:微软雅黑; font-weight:bold" :title "在下面的“评价工作”中选择上市公司"} "深发展Ａ"] [:br]
     "评价期 : " [:b [:span#year "2012"] "年" [:span#month "2"] "月"] "&nbsp;"
     (eui-button {:id "year_month_bt" :plain "true" :iconCls "icon-arrow" :title "更改评价期"} "") ]
    (eui-context-menu 
      "year_month_cm" {} 
      (html (for [ym (reverse periods)]
              (let [[y m] (split ym "-")]
                [:div {:iconCls "icon-save" :onclick (format "grade_year_month('%s','%s')" y m)} (format "%s年%s月" y m)] ))))

    (eui-accord 
      {:id "accord1" :style "" }
      (eui-accord- 
        {:iconCls "icon-pen"} "评价工作"
        (eui-button {:id "indic0_bt" :plain "true" :iconCls "icon-sum"} "上市公司综合评价") [:br]
        (eui-button {:id "hs300_bt" :plain "true" :iconCls "icon-list"} "沪深300公司") [:br]
        (eui-button {:id "corp_bt" :plain "true" :iconCls "icon-list"} "所有上市公司") [:br]
        (eui-button {:id "report1_bt" :plain "true" :iconCls "icon-bar" } "各评价期得分") [:br]
        (eui-button {:id "report2_bt" :plain "true" :iconCls "icon-bar" } "各级别公司数目") [:br]
        (eui-button {:id "report3_bt" :plain "true" :iconCls "icon-pie" } "分行业统计") [:br]
        (eui-button {:id "report4_bt" :plain "true" :iconCls "icon-bar" } "分地域统计") [:br]
        (eui-button {:id "report5_bt" :plain "true" :iconCls "icon-pie" } "分板块统计") [:br]
        )    
      (eui-accord- 
        {:iconCls "icon-search"} "其他（网站样式等）"
        (eui-button {:id "site_bt" :plain "true" :iconCls "icon-search" } "网站样式") [:br]
        (eui-button {:id "help_bt" :plain "true" :iconCls "icon-help" } "使用帮助") [:br]
        )
      )))

(defn- app-main
  "layout.center"
  []
  (eui-region 
    "center" 
    {:border "false" :style "padding: 2px"} 
    [:img {:src "/img/grade/grade-help.png" :style "margin-top: 30px"}] ))

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
    (eui-foot-region) ))

(defn- corp-score-sum
  "综合评分及等级，驾驶仪表板示意图，用于indic函数"
  [score rank-advice]
  (let [ranks (get-indic "rank")
        rank (rank score ranks)]
    (html
      [:a {:name "rank_result"}]
      [:h2 
       (format "修正指标分值：<span style='color:red'>%.4f</span>（评级参考：<span id='rank_ref' style='color:red'>%s</span>）" 
               score rank) (space 3)
       [:span#rank_advice0 {:value (or rank-advice rank)} "人工评级结果：" ]
       (eui-combo {:id "rank_advice" :style "width:80px" } 
                  (apply array-map (mapcat #(let [k (-> % key name)] [k k]) ranks)))]
      (panelf {:value score 
              ; range = [30, 50 55 60 65 70 75 80 85 90, 100]
              :range (reduce into [[30] (reverse (map #(-> % val (right " ") to-int) ranks)) [100]]) }
             {:lowerLimit 30 :upperLimit 100} 500 250) )))

(defn- corp-score-detail 
  "各一级评价指标分值cup图显示，用于indic函数"
  [indic data advice]
  (eui-panel 
    {:id "panel1" :closed "true" :title "各一级评价指标得分 : " :style "color: blue; width: 900px; padding: 5px" }
    (for [e indic]
      (let [name1 (:name e) ; 一级指标名称
            w1 (:weight e) ; 一级指标权重
            score1 (get-score1 e data advice)]
        [:div {:style "float:left; margin: 10px"}
         [:h2 name1] "满分值 : " w1 [:br] "得分值 : " score1 [:br]
         (cupf (double score1) {:lowerLimit 0 :upperLimit w1} 150 250)] )) ))

(defn indic
  "app: 显示整个指标体系
  @ids 第一个参数为上市公司代码如'000001'，第二个参数为年月如'2012-1' "
  [ids]
  (let [corp-code (or (first ids) "000001")
        corp-name (get-corp-name corp-code)
        [year month] (split (or (second ids) "2012-1") "-")
        data (get-data corp-code year month) ; {:201 82 :202 98 ..}
        advice (get-advice corp-code year month) ; {:201 1.1 :229 0.95} 
        reason (get-advice-reason corp-code year month) ; {:201 1.1 :229 0.95} 
        [caption title head indic] (map get-indic ["caption" "title" "head" "indic"])
        rowspan (sum (map #(count (:child %)) indic)) ; 25 = (+ 4 5 5 5 6)
        css1 "vertical-align:middle; text-align:center; width:110px"
        css2 "vertical-align:middle; text-align:left" 
        css3 "color: blue"
        fmt (fn [e] (format "%s : %s%%" (:name e) (:weight e)))]
    (html
      [:a {:name "top"}]
      [:h1 (format "%s : %s &nbsp; （评价期：%s年%s月）" corp-code corp-name year month)]
      (eui-button {:href "#rank_result"} "评级结果参考") (space 3)
      (eui-button {:href "#rank_detail" :onclick "grade_rank_detail()"} "评级结果细项图示") (space 6)      
      (eui-button {:id "score_save" :onclick "grade_score_save()" :iconCls "icon-save" :disabled true} " 保存修改分值")      
      ; 指标体系大表
      [:table {:class "wr3table" :border 1 :style "border:1px solid black"}
       [:caption caption]
       [:tbody
        [:tr 
         [:td {:rowspan (+ 1 rowspan) :style css1} title]
         (map #(html [:th %]) head) ]
        (for [e indic] ; e  {:code 11, :name "上..", :weight 20, :child [{:code 201, :name "所..", :weight 20} ..]}, ..] } 
          (let [w1 (:weight e) ; 一级指标权重，如：20
                f-td-score2 (fn [e2] ; “评价基本指标”和“修正指标”的html片段
                              (let [code (-> e2 :code str keyword)
                                    v (get data code)]
                                (html
                                  [:td {:align "right" :group "score" :title "点击更改" :code (name code) :style css3 } v]; 评价指标值 eg. 85
                                  [:td {:align "right"} (:factor e2)]; 修正指标值 eg. 1.1 
                                  [:td {:group "advice" :title "点击更改" :code (name code) :style css3} 
                                   (or (code advice) 1.0) " " (when-let [r (code reason)] (format "（%s）" r))] ; 专家委员会意见 eg. 1.1
                                  ))) ]
            (html [:tr 
                   [:td {:rowspan (count (:child e)) :style css2} 
                    (fmt e) [:br][:br] [:font {:color "red"} (get-score1 e data advice)] " 分"] ; 一级指标 eg. "上市公司治理评价 : 20%"
                   (let [e2 (-> e :child first)] ; e2是[第1个]二级指标eg. {:code 201, :name "所有权结构及其影响", :weight 20 :factor}
                     (html [:td (fmt e2)] 
                           (f-td-score2 e2) ))]
                  (for [e2 (-> e :child rest)] ; e2是[其他]二级指标（不含第一个）如：'{:code 202, :name "金融相关者关系", :weight 25}
                    [:tr
                     [:td (fmt e2)] ; rest 二级指标 eg. 金融相关者关系
                     (f-td-score2 e2) ]) )))]]
      ; 综合评分及等级，驾驶仪表板示意图
      (corp-score-sum (get-score0 corp-code year month indic data advice) (:rank advice)) [:br]
      (eui-button {:href "#top" :style "margin: 3px" :iconCls "icon-redo"} "回到上面") (space 3)
      (eui-button {:onclick "grade_rank_detail()"} "评级结果细项图示") [:br]
      ; 各一级评价指标分值cup图显示
      [:a {:name "rank_detail"}]
      (corp-score-detail indic data advice) )))

(defn- corps-list
  "所有上市公司列表 {'000001' '深发展Ａ' '000002' '万科Ａ' '000004' '国农科技' .. }  "
  []
  (with-mdb2 "grade"
    (let [corps (fetch :corp :sort {:code 1}) ]
      (apply array-map (flatten (for [corp corps] [(:code corp) (:name corp)])))) ))

(defn- hs300-list
  "沪深300公司列表"
  []
  (with-mdb2 "grade"
    (let [ls300 (:v (mdb-get-one :dict :where {:code "hs300"}))
          corps (fetch :corp :where {:code {:$in ls300}} :sort {:code 1}) ]
      (apply array-map (flatten (for [corp corps] [(:code corp) (:name corp)])))) ))


(defn- rank-list
  "得到某年某月某评级的上市公司列表, eg. (rank-list '2012' '1' 'AA') 
  @return {'000001' '大名城B', .. } "
  [year month rank]
  (with-mdb2 "grade"
    (let [datas (fetch :data :only [:code :score] :where {:year year :month month :rank rank})
          codes (map :code datas)
          corps (fetch :corp :only [:code :name] :where {:code {:$in codes}})]
      (apply array-map (flatten (for [corp corps] [(:code corp) (:name corp)]))))))

(defn- corps-like-list
  "符合代码模糊查询条件的公司列表"
  [pattern]
  (with-mdb2 "grade"
    (let [corps (fetch :corp :where {:code (re-pattern pattern)} :sort {:code 1})]
      (apply array-map (flatten (for [corp corps] [(:code corp) (:name corp)])))) ))

(defn- app-corp-list
  [corps title]
  (html
    [:h1 (format "%s（%s 家）：" title (count corps))]
    (for [[code nam] corps] 
      (eui-button {:plain "true" :onclick (format "grade_corp('%s','%s')" code nam)} 
                  (format "%s: %s" code nam)) )))

(defn corp
  "app: 列出所有上市公司"
  [] (app-corp-list (corps-list) "上市公司列表"))

(defn hs300
  "app: 列出沪深300公司"
  [] (app-corp-list (hs300-list) "沪深300公司"))

(defn corp-like
  "app: 列出符合代码模糊查询条件的公司"
  [id] 
  (let [pattern (or id "000001")]
    (app-corp-list (corps-like-list pattern) (format "代码中含%s的上市公司" pattern)) ))
  
(defn save-score-advice
  "service: 保存更改的评价分数，专家建议，并将自动计算的评级结果保存入库；
  @param ids ['000001' '2012' '1' '201 98.5 205 89.0 229 69.8' '201 \"1.0 合理\" 207 \"0.95\" '] 
  保存到如下 data, advice, advice-reason "
  [ids]
  (let [corp-code (or (first ids) "000001")
        year (or (second ids) "2012")
        month (or (nth ids 2) "1")
        scores (nth ids 3)
        advices (nth ids 4)
        ; scores 保存到 data
        data (get-data corp-code year month) ; 原来data
        score-map (eval (read-string (format "{%s}" scores)))
        data2 (merge data (into {} (for [[k v] score-map] [(-> k str keyword) v]))) ; 修改了原始分值
        score0 (double (get-score0 corp-code year month nil data2 nil))
        rank (rank score0)
        data3 (merge data2 {:score score0 :rank rank}) ; 修改最后分值以及评级
        ; advices 保存到 advice 和 advice-reason
        advice-map (eval (read-string (format "{%s}" advices))) ; {201 "1.0 合理", 207 "0.95"}
        advice (get-advice corp-code year month) ; 原来advice
        advice-reason (get-advice-reason corp-code year month) ; 原来advice-reason
        advice2 (merge advice (into {:code corp-code :year year :month month} 
                                    (for [[k v] advice-map] [(-> k str keyword) (to-double (first (split v " ")))])))
        advice-reason2 (merge advice-reason (into {:code corp-code :year year :month month} 
                                                  (for [[k v] advice-map] [(-> k str keyword) (second (split v " "))])))
        ]
    (println (format "debug: scores=[%s], advices=[%s], advice-map=%s" scores advices advice-map))
    (with-mdb2 "grade"
      (update! :data data data3) 
      (if advice
        (update! :advice advice advice2)
        (insert! :advice advice2))
      (if advice-reason
        (update! :advice-reason advice-reason advice-reason2)
        (insert! :advice-reason advice-reason2))
      )))

(defn save-rank-advice
  "service: 保存评级人工建议结果于advice
  调用url: '/save-rank-advice/000001/2012/1/AA'
  @ids '('000001' '2012' '1' 'AA') "
  [ids]
  (let [corp-code (first ids)
        [year month] (rest ids)
        rank-advice (last ids)
        advice (get-advice corp-code year month)]
    (with-mdb2 "grade"
      (if advice
        (update! :advice advice (merge advice {:rank rank-advice}))
        (insert! :advice {:code corp-code :year year :month month :rank rank-advice}) ) )))

(defn report-score
  "app: 分值分析, 一个企业各月份的分值分析"
  [id]
  (html
    (let [corp-code (or id "000001")
          ranks (get-indic "rank")
          m (into {} (for [p periods]
                       (let [[year month] (split p "-")
                             data (get-data corp-code year month)
                             v (:score data)]
                         [(str p ": " (rank v ranks)) v])))]
      (barf m {:title (format "[%s %s] 各评价期得分情况" corp-code (get-corp-name corp-code)) 
              :x "评价期: 评级" :y "分值"}))))

(defn- rank-count
  "得到某年某月某评级的上市公司数量, eg. (rank-count '2012' '1' 'AA') "
  [year month rank]
  (with-mdb2 "grade"
    (fetch-count :data :where {:year year :month month :rank rank})))

(defn- ranks-count
  "某年月所有企业各个评级的数量
  @return {'AAA' 135 'AA' 89 'A' 32 .. '末级' 2} "
  [year month]
  (let [ranks (get-indic "rank")
        rank-keys (concat (map name (keys ranks)) ["末级"])] ; ("AAA" "AA" .. "C") 
    (apply array-map (flatten (for [k rank-keys]
                                [k (rank-count year month k)] )))))

(defn report-ranks
  "app: 某年月所有企业评级分析，维度：评级，指标：数目
  @param id '2012-1' 
  @return {'AAA' 169, 'AA' 131, .. '末级' 1} "
  [id]
  (let [[year month] (or (split id "-") ["2012" "1"])
        m (ranks-count year month)]
    (html
      (barf m {:title (format "%s年%s月各级别数量" year month) :x "评分级别" :y "上市公司数量"}) [:br]
      [:h2 "点击如下参考详情："] [:br]
      (for [[rank n] m]
        (eui-button {:style "margin: 3px" :onclick (format "grade_report_ranks('%s')" rank)} 
                    (format "%s: %s家" rank n)) ))))

(defn corp-of-rank
  "app: 列出某个rank的所有上市公司"
  [ids]
  (let [year (or (first ids) "2012")
        month (or (second ids) "1")
        rank (or (nth ids 2) "AAA") 
        corps (rank-list year month rank) ]
    (app-corp-list corps (format "评级为%s的公司" rank)) ))
  
;;;------------------------------------------------ 用于后台运行一次计算出报表结果进行保存的函数，<开始
(defn- data-count-
  "计算data表中指定：年、月、rank和公司代码集合的记录数量"
  [year month rank corps]
  (with-mdb2 "grade"
    (fetch-count :data :where {:year year :month month :rank rank :code {:$in (map :code corps)}})))
  
(defn- industry-count
  "得到某年月某行业某级别的企业个数 (corp-of-industry '2012' '1' 'A' 'AAA') "
  [year month incode rank]
  (let [corps (fetch :corp :only [:code] :where {:incode (name incode)}) ]
    (data-count- corps)))

(defn- province-count
  "得到某年月某省份某级别的企业个数 (province-count '2012' '1' '北京' 'AAA') "
  [year month province rank]
    (let [corps (fetch :corp :only [:code] :where {:province province}) ]
      (data-count- corps)))

(defn- board-count
  "得到某年月某板块某级别的企业个数 (corp-of-industry '2012' '1' 'A' 'AAA') "
  [year month board rank]
  (let [pattern (case board
                  "深圳主板" #"^00[01]"
                  "中小企业板" #"^002"
                  "深圳B股" #"^2"
                  "创业板" #"^3"
                  "上海主板" #"^6"
                  "上海B股" #"^9"
                  nil )]
    (let [corps (fetch :corp :only [:code] :where {:code pattern}) ]
      (data-count- corps))))

(defn- report-of-
  "@param type-name 如 'industry' 'province' 'board'
  @param f-count 统计函数如 industry-count, province-count, board-count "
  [year month type-name f-count dims]
  (let [ranks (get-indic "rank") ;  {:AAA ">= 90", :AA ">= 85", .. }
        rank-keys  (concat (map name (keys ranks)) ["末级"]) ]; ("AAA" "AA" .. "C" "末级")
    {:type type-name :year year :month month
     :count (vec (for [dim dims rank rank-keys]
                   [dim rank (f-count year month dim rank)]))} ))
  
(defn- industry-of
  "得到某年月不同行业不同级别的企业数目, 维度：行业+评级，指标：count
  @param id 年月如'2012-1' "
  [year month]
  (let [dd-industry (dd "industry") ; 行业大类字典  {:A "农、林、牧、渔业", :B "采掘业", .. }
        incodes (map name (keys dd-industry))] ; ("A" "B" .. "C2")
    (report-of- year month "industry" industry-count incodes)))

(defn- province-of
  "得到某年月不同省份不同级别的企业数目, 维度：省份+评级，指标：count "
  [year month]
  (let [provinces (concat (dd "provinces") [nil])]
    (report-of- year month "province" province-count provinces)))

(defn- board-of
  "得到某年月不同板块不同级别的企业数目, 维度：省份+评级，指标：count "
  [year month]
  (report-of- year month "board" board-count boards))

(defn- report-save-
  "@param f-of 函数如 industry-of, province-of, board-of "
  [f-of]
  (doseq [p periods]
    (let [[year month] (split p "-")]
      (with-mdb2 "grade"
        (insert! :report (f-of year month))))))

(defn- industry-report-save
  "生成所有年月的行业评级分析报表保存到mongodb的report表中"
  [] (report-save- industry-of))

(defn- province-report-save
  "生成所有年月的省份评级分析报表保存到mongodb的report表中"
  [] (report-save- province-of))

(defn- board-report-save
  "生成所有年月的板块评级分析报表保存到mongodb的report表中"
  [] (report-save- board-of))
;;;------------------------------------------------ 用于后台运行一次计算出报表结果进行保存的函数，结束>

(defn- get-report
  [year month report-type]
  (with-mdb2 "grade"
    (:count (mdb-get-one :report :where {:type report-type :year year :month month}))))

(defn- get-type- [type-name]
  (case type-name "industry" "行业" "province" "省份" "board" "板块" "未知"))
  
; (report-chart "2012" "1" "industry" "AA" nil)
(defn report-chart
  "app: 生成报表中一列或者一行的图形显示；目前只做了行业分析的
  @param report-type 如：'industry', 'province', 'board' 
  @param dim-top ''或者 'AAA', .., '末级' 
  @param dim-left ''或者 'C5: 电子', .., '上海主板: 上海主板' "
  [year month report-type dim-top dim-left]
  (let [top? (nullity? dim-left)
        data (sort-by #(first %) (get-report year month report-type))
        title (if top? 
                (format "评级为“%s”的各行业上市公司数量" dim-top) 
                (format "“%s”的各评级上市公司数量" dim-left))
        x (if top? (get-type- report-type) "评级")
        dict (case report-type
               "industry" (into {} (for [[k v] (dd "industry")] [(name k) v])) ; {:A "农、林、牧、渔业", :B "采掘业", .. }
               "province" (into {"未知" "未知"} (for [e (dd "provinces")] [e e]))
               "board" (into {} (for [e boards] [e e]))
               nil)
        v (if top?
            (for [[dim rank c] data :when (= rank dim-top)] [(dict dim) c])
            (for [[dim rank c] data :when (= dim (left dim-left ":"))] [rank c]))
        m (apply array-map (flatten v)) ]
    (barf m {:title title :x x :y "上市公司数量"}) ))

(defn report-
  "@param type-name 如：'industry', 'province', 'board' 
  @param dims 如 {'A' '农、林、牧、渔业' 'B' '采掘业' .. 'M' '综合类' }
  [['北京' '北京'] ['上海' '上海'] .. ['广东' '广东']] 
  (('上海A股' '上海A股') () ) "
  [year month type-name dims]
  (let [year (or year "2012")
        month (or month "1")
        data (sort-by #(first %) (get-report year month type-name))
        type-cn (get-type- type-name)
        dict (into {} dims)
        ]
    (html
      [:h1 {:align "center" :type type-name} (format "分%s统计（%s年%s月）" type-cn year month) ]
      (cross-table data {:caption (format "维度：%s、评级； 指标：上市公司数量" type-cn)
                         :dim-top-name "评级"
                         :dim-left-name type-cn
                         :f-dim-top (fn [dim] [:a {:href "#chart"} dim])
                         :f-dim-left (fn [dim] [:a {:href "#chart"} [:font {:color "gray"} dim] ": " (dict dim)])
                         }) [:br]
      [:a {:name "chart"}]
      [:div#chart {:style "height: 400px"} "图形（请点击列头或者行头）"] )))
  
(defn report-industry
  "app: 生成行业分析表"
  [ids]
  (let [[year month] (take 2 ids)
        dims (for [[k v] (dd "industry")] [(name k) v]) ]
    (report- year month "industry" dims) ))
  
(defn report-province
  "app: 生成省份分析表"
  [ids]
  (let [[year month] (take 2 ids)
        dims (for [e (dd "provinces")] [e e]) ]
    (report- year month "province" dims) ))

(defn report-board
  "app: 生成板块分析表"
  [ids]
  (let [[year month] (take 2 ids)
        dims (for [e boards] [e e]) ]
    (report- year month "board" dims) ))

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
      (:v (mdb-get-one :dict :where {:code "industry"}))
      )))

(defn test3 
  "初始化mongodb的指标表indic。所有项自己都是100分制，按照权证算出在整个系统中的分值。"
  []
  (let [indic-name "上市公司综合评价（时间跨度：三年）"
        indic [
               {:code 11 :name "上市公司治理评价" :weight 20 :note "参考标普"
                :child [
                        {:code 201 :name "所有权结构及其影响" :weight 40}
                        {:code 202 :name "金融相关者关系" :weight 20}
                        {:code 203 :name "财务透明与信息披露" :weight 20}
                        {:code 204 :name "董事会的结构与运作" :weight 20}
                        ]}
               {:code 12 :name "上市公司业绩评价" :weight 25 :note "参考中联评估"
                :child [
                        {:code 206 :name "财务效益状况" :weight 25}
                        {:code 207 :name "资产质量状况" :weight 25}
                        {:code 208 :name "偿债风险状况" :weight 20}
                        {:code 209 :name "发展能力状况" :weight 15}
                        {:code 210 :name "市场表现状况" :weight 15}
                        ]}
               {:code 13 :name "上市公司内控规范评价" :weight 20 :note "参考财政部内控规范"
                :child [
                        {:code 212 :name "内部环境" :weight 30}
                        {:code 213 :name "风险评估" :weight 25}
                        {:code 214 :name "控制活动" :weight 15}
                        {:code 215 :name "信息与沟通" :weight 15}
                        {:code 216 :name "内部监督" :weight 15}
                  ]}
               {:code 14 :name "上市公司诚信评价" :weight 20 :note ""
                :child [
                        {:code 218 :name "财务诚信" :weight 15}
                        {:code 219 :name "金融诚信" :weight 15}
                        {:code 220 :name "合同诚信" :weight 15}
                        {:code 221 :name "质量诚信" :weight 15}
                        {:code 222 :name "信用记录" :weight 40}
                        ]}
               {:code 15 :name "上市公司社会责任评价" :weight 15 :note "参考深交所"
                :child [
                        {:code 223 :name "股东和债权人权益保护" :weight 20}
                        {:code 224 :name "职工权益保护" :weight 15}
                        {:code 225 :name "供应商、客户和消费者权益保护" :weight 15}
                        {:code 226 :name "对国家政府承担的责任" :weight 15}
                        {:code 227 :name "环境保护和社会公益保护" :weight 15}
                        {:code 228 :name "综合指标" :weight 20}
                        ]}
               ]
               grade (array-map :AAA ">= 90" :AA ">= 85" :A ">= 80" :BBB ">= 75" :BB ">= 70" :B ">= 65" :CCC ">= 60" :CC ">= 55" :C ">= 50" )
               frandom (fn [] (+ 0.9 (* 0.1 (random 2))))
               ]
    (let [indic-new (for [e indic] (assoc e :child (for [e1 (:child e)] (into e1 {:factor (frandom)}))))]
      (with-mdb "grade"
;      (mdb-add :indic {:_id "rank" :v grade}) 
;      (mdb-add :indic {:_id "caption" :v "上市公司综合评价指标体系"})
;      (mdb-add :indic {:_id "head" :v ["评价一级指标"	"评价二级指标"	"评价基本指标"	"修正指标"]})
;      (mdb-add :indic {:_id "title" :v indic-name})
       (mdb-add :indic {:_id "indic" :v indic-new})
       ))))

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

(defn test5-province
  "设置corp表的:province属性, 164家B股或沪市新股没有设置地域"
  []
  (let [provinces '(北京 上海 广东 江苏 陕西 山东 新疆 湖南 黑龙江 湖北 安徽 浙江 四川 贵州 甘肃 福建 辽宁 重庆 天津 广西 吉林 海南 河北 河南 内蒙古 山西 西藏 青海 江西 云南 宁夏)
        ]
    (with-mdb2 "grade"
      (let [corp (fetch :corp :where {:province {:$exists false}})]
        (doseq [o corp]
          (println (select-keys o [:code :name :province]))))
       )))

;(with-mdb2 "grade"
;  (vec (map :code (fetch :corp :only [:code] :where {:code #"^9"}))))

