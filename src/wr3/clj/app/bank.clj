(ns ^{:doc "商业银行存贷款分析Demo"} 
     wr3.clj.app.bank)

(use 'hiccup.core)
(use 'wr3.clj.web 'wr3.clj.db 'wr3.clj.tb 'wr3.clj.u 'wr3.clj.s 'wr3.clj.n)
(use 'wr3.clj.chart)

(require '[wr3.clj.app.auth :as au])
(defn auth
  "该函数被 CljServlet 调用 "
  [request fname & args]
  (cond
    (session request "wr3user") true
    :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; layout

;; app-conf 说明：
;; :id              应用的标识，用于?
;; style            上方色调，["layout_north" "layout_title"]（蓝色）或 ["layout_north1" "layout_title1"]（金色）
;; :title           应用名称，位于上方
;; :search-title    搜索框的label，位于右上方
;; :nav             导航条，位于左边
;; :menu            2层菜单条，位于上方
(def app-conf
  {:name "bank"
   :style (map #(str % "1") ["layout_north" "layout_title"]) 
   :title "IDP行业应用——银行经营分析系统"
   :searcher ["红塔证券股份"
              ["客户智能搜索" "cust" "icon-find"]
              ["客户经理搜索" "mng" "icon-user"]]
   :nav [["业务工作"            "icon-pen" ; title id
          ["综合业务"          "icon-sum"    "indic0_bt"] ; title icon id 
          ["列表查看"          "icon-list"   "hs300_bt" ]
          ["统计报表"          "icon-pie"    "report3_bt"]]
         
         ["系统管理及帮助"      "icon-search"
          ["网站样式"          "icon-search" "site_bt"]
          ["使用帮助"          "icon-help"   "help_bt"]]
         ]   
   :menu [["业务系统数据查询" 
           ["客户信息"     "11-cust"] 
           ["客户经理"     "12-mng"]
           ["分支网点"     "13-org"]
           ["其他"         "14-other"]]
          
          ["分析报表"
           ["活期存款分析"    "21-hq"]     
           ["定期存款分析"    "22-dq"]     
           ["存款分析"        "23-depo"]
           ["贷款分析"        "24-loan"] ]
   
          ["时间序列分析"
           ["进场登记分析*" "31-enter"] 
           ["交易流水分析" "32-trade"] 
           ["综合分析*" "33-other"]]
          
          ["其他"
           ["首页"     "41-home"]
           ["系统管理" "41-admin"] 
           ["帮助"     "42-help"]]
          ] 
   })

(def top-height (if (:menu app-conf) 135 85))

(defn- top-menu
  []
  (eui-tabs
    {:style "height: 65px; position: absolute; left:0px; bottom: 0px;"}
    (for [[title & m2] (:menu app-conf)]
      (eui-tab1
        (html [:span {:style "font-size: 14px; padding-left: 10px; padding-right: 10px"} title]) 
        {:closable "false" :style "padding: 4px"} 
        (for [[title id] m2] 
          (eui-button {:id id :plain "true" :iconCls "icon-arrow" :group2 "menu2"} title)) ))))
  
(defn- app-top
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id (first (:style app-conf)) :style (format "height: %spx; padding: 10px;" top-height) }
    [:span {:class (second (:style app-conf))} (:title app-conf)]
    [:div {:style "position: absolute; right: 10px; top: 8px; color: gray"} "当前用户: " 
     [:span#wr3user {:style "color:red; font-weight:bold"} ".."] (space 3)
     [:script "app_user()"]
     [:a {:href "#" :onclick "app_exit('/c/demo/layout1')"} "退出"]]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher (str (:name app-conf) "_search") :style "width: 300px;" :value (first (:searcher app-conf))} 
       (for [[label nam icon] (rest (:searcher app-conf))]
         [:div {:name nam :iconCls icon :style "margin: 0px"} label] ))]
    ; 1、2级导航条
    (when (:menu app-conf) (top-menu)) ))

(defn- app-left
  "layout.west"
  []
  (eui-region 
    "west" 
    {:title "快捷导航" :style "width: 210px"}
    [:div {:style "margin: 10px"} "全局信息提示内容……" ]
    
    (eui-accord 
      {:id "accord1" :style "" }
      (for [[title icon & nav2] (:nav app-conf)]
        (eui-accord- 
          {:iconCls icon} title
          (for [[title icon id] nav2]
            (html
              (eui-button {:id id :plain "true" :iconCls icon} title) [:br] ) ))) )))

(defn- app-main
  "layout.center"
  []
  (eui-region 
    "center" 
    {:border "false" :style "padding: 10px"} 
    [:h2 "主显示页面"] ))

(defn index
  []
  (eui-layout
    {:id "layout1" :onload (str (:name app-conf) "_onload()")}
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

(defn- org-map
  "得到org_id和org_name的hash-map, 
  @deprecated 一般用org-dict即可，除非数据库更改了"
  []
  (let [sql "select org_id,org_name from sys_org order by org_id"
        rt (select-all "bank" sql)]
    (into {} (for [{id :org_id nam :org_name} rt] [id nam]))))

(def org-dict 
  {"132121" "护国支行", "132033" "部门033", "132055" "部门055", "132111" "江岸支行", "132001" "分行营业部", 
   "132101" "第三支行", "132002" "部门002", "132003" "双龙支行", "132006" "个人营业部", "132008" "金碧支行", 
   "132901" "办公室", "132902" "会计结算部", "132903" "人力资源部", "132904" "中间业务部", "132905" "公司银行部", 
   "132906" "保卫部", "132091" "部门091", "132092" "国际业务部", "132071" "第一支行", "132061" "人民中路支行",
   "132161" "北京路支行", "132051" "玉溪支行", "132151" "华龙支行", "132052" "部门052", "132031" "曲靖支行", 
   "132053" "部门053", "132131" "国贸支行", "132021" "第二支行", "132032" "部门032"})
  
(def hb-dict {"1" "个人客户" "2" "企业客户"})
(def currency-dict {"1" "人民币", "12" "英镑", "13" "港元", "14" "美元", "15" "瑞土法朗", "16" "德国马克", 
                    "17" "法国法郎", "18" "新加坡元", "20" "荷兰盾", "21" "瑞典克朗", "22" "丹麦克朗", 
                    "23" "挪威克朗", "24" "奥地利先令", "25" "比利时法朗", "26" "意大利里拉", "27" "日元", 
                    "28" "加拿大元", "29" "澳大利亚元", "31" "西班牙比塞塔", "32" "马来西亚林吉特", 
                    "33" "欧洲贷币单位", "42" "芬兰马克", "81" "澳门元", "84" " 泰国铢", "87" "新西兰元", 
                    "95" "清算瑞士法朗", "96" "欧元"})
  
(defn- cust-search
  "得到模糊查询客户的sql结果
  @pattern 为中文时查姓名，非中文时查pid"
  [pattern]
  (let [field (if (wr3.util.Charsetx/isChinese pattern) "name" "cust_pid")
        sql (format "select top 100 * from in_cust where %s like '%%%s%%' order by org_id" field pattern)]
    (select-all "bank" sql)))

(defn- f-join
  "把一行数据中几个key的内容进行合并"
  [row & keys] 
  (join (remove nullity? (unique (map #(trim (row %)) keys))) " ， "))

(defn- cust-list
  "得到in_cust客户列表的html片段
  @rt clojure.sql结果集"
  [rt]  
  (let [m (org-map)]
    (html 
      [:h1 (let [n (count rt)] (format "符合查询条件的客户%s个" (if (= n 100) "可能已超过100" n)))]
      (result-html rt {:f-head (fn [thead] 
                                 (map (fn [th] [:th th]) '[序号 客户名称 类型 开户网点 证件号码 联系方式 地址 账户]))
                       :f-row (fn [row-index row]
                                [:tr (bgcolor-css row-index)
                                 [:td {:align "right" :style "color: lightgray"} row-index]
                                 [:td (row :name)]
                                 [:td (hb-dict (row :hb))]
                                 [:td (m (row :org_id))]
                                 [:td (f-join row :cust_pid :en_pid)]
                                 [:td (f-join row :tel :phone)] 
                                 [:td (f-join row :address :addr :zip)] 
                                 [:td {:align "center"} 
                                  [:a {:href (format "/c/bank/acct?org=%s&cif=%s" (row :org_id) (row :cif_key))
                                       :target "_blank"} "查看"]] ]) 
                       } ))))

(defn cust
  "service: 模糊查找客户的结果列表
  @id 模糊查询的字符串"
  [id]
  (let [rt (cust-search (or id ""))] (cust-list rt)))

(defn- cust-count
  "按网点、类型统计客户数目
  @return 如：[['132071' '1' 18456] ['132001' '1' 16640] ..] "
  []
  (let [sql (str "select org_id, hb, count(*) c from in_cust"
                 " where hb in ('1', '2')"
                 " group by org_id,hb order by hb,count(org_id) desc")
        rt (select-all "bank" sql)]
    (rest (result-table rt))))

(defn- mng-count
  []
  (let [sql (str "select org_id,count(*) as n from mng_base group by org_id order by count(*) desc")
        rt (select-all "bank" sql)]
    rt))
  
(defn custs
  "service: 客户概况图、表"
  []
  (let [sql "select hb, count(*) c from in_cust where hb in ('1','2') group by hb order by hb"
        rt (select-all "bank" sql)
        [n1 n2] (map :c rt)
        sum (+ n1 n2)
        orgs (org-map)
        data (cust-count) ]
    (html
      [:h1 {:align "center"} "客户概况图表"]
      (eui-tip (format (str "以下 %s 个分支网点共有<font color='red'> %s（约%s）</font>个客户。"
                        "其中<font color='red'> %.1f%% 计 %s </font>为个人客户，"
                        "<font color='red'> %.1f%% 计 %s </font>为企业客户。")
                       (count (distinct (map first data))) sum (wr3.bank.Currency/about sum) 
                       (/ n1 sum 0.01) n1 
                       (/ n2 sum 0.01) n2))
      [:div {:style "float: left"}
       (cross-table data {:caption "客户类型、网点-数量统计表"
                          :dim-top-name "客户类型"
                          :dim-left-name "分支网点"
                          :f-dim-top (fn [v] (hb-dict v))
                          :f-dim-left (fn [v] (orgs v))
                          } ) ]
      [:div {:style "float: left; margin-left: 20px"}
       (let [codes (distinct (map first data))
             m1 (into {} (for [[org hb n] data :when (and (in? org codes) (= hb "1"))] [org n]))
             m2 (into {} (for [[org hb n] data :when (and (in? org codes) (= hb "2"))] [org n]))
             cust1 (map #(if-let [v (m1 %)] v 0) codes)
             cust2 (map #(if-let [v (m2 %)] v 0) codes) ]
         (stackf {"category" (vec (map orgs codes)) "个人客户" (vec cust1) "企业客户" (vec cust2) }
                 {:title "图示如下" :x "分支网点" :y "客户数量"} 700 500) ) ] )))

(defn mngs
  "service: 客户经理概况图表"
  []
  (let [orgs (org-map)
        rt (mng-count)]
    (html
      [:h1 {:align "center"} "客户经理概况图表"]
      (eui-tip (format "以下 %s 个分支网点共有<font color='red'> %s </font>个客户经理。"
                       (count rt) (sum (for [row rt] (:n row))) ))
      [:div {:style "float: left"}
       (result-html rt {:f-head (fn [thead] [:tr [:th "分支网点"] [:th "数量"]])
                        :f-td (fn [r c [k v] row] 
                                [:td (td-align v) 
                                 (if (zero? c)
                                   [:a {:href (format "/c/bank/mng?org=%s" v) :target "_blank"} 
                                    (orgs v)] 
                                   v)]) })]
      [:div {:style "float: left; margin-left: 20px"} 
       (pief (apply array-map (flatten (for [{org :org_id n :n} rt] [(orgs org) n]))) {} 600 500)] 
      [:div {:style "float: left; margin-left: 20px"} 
       (barf (apply array-map (flatten (for [{org :org_id n :n} rt] [(orgs org) n]))) {} 600 500)] 
      )))

(defn mng
  "app: 客户经理列表"
  [org]
  (let [org (or org "132051")
        sql (format "select * from mng_base where org_id='%s'" org)
        rt (select-all "bank" sql)
        orgs (org-map)]
    (html-body 
      [:h1 (let [n (count rt)] (format "%s （客户经理 %s 位）" (orgs org) (count rt)))]
      (result-html rt {:f-head (fn [thead]
                                 (map (fn [th] [:th th]) '[序号 姓名 性别 职务 职称 学历 证件号码 生日 手机 电话 负责客户]))
                       :f-row (fn [row-index row]
                                [:tr (bgcolor-css row-index)
                                 [:td {:align "right" :style "color: lightgray"} row-index]
                                 (for [col [:name :sex :job :job_title :education :pid :birth :mobile :tel]] [:td (row col)])  
                                 [:td {:align "center"} 
                                  [:a {:href (format "/c/bank/mng-cust?org=%s&mng=%s" org (row :pid))} "查看"]]
                                 ]) } ))))

(defn mng-cust
  "app: 指定客户经理负责的所有客户
  @org 机构id
  @mng 客户经理的pid"
  [org mng]
  (let [orgs (org-map)
        mng (or mng "510105198007151834")
        sql (format "select * from mng_cust where mng_pid='%s'" mng)
        rt (select-all "bank" sql)]
    (html-body 
      [:h1 [:a {:href (format "/c/bank/mng?org=%s" org) :title "查看所有客户经理"} (format "%s客户经理" (orgs org))] 
       (format " %s 所负责的 %s 位客户" (:mng_name(first rt)) (count rt))]
      (result-html rt {:f-head (fn [thead]
                                 (map (fn [th] [:th th]) '[序号 客户名 客户证件号 负责占比 账户信息]))
                       :f-row (fn [row-index row]
                                [:tr (bgcolor-css row-index)
                                 [:td {:align "right" :style "color: lightgray"} row-index]
                                 [:td (f-join row :cust_name :en_name)]
                                 [:td (f-join row :cust_pid :en_pid)]
                                 [:td (row :ratio)]
                                 [:td {:align "center"} 
                                  [:a {:href 
                                       (let [[cpid epid cname ename] (map #(trim (row %)) 
                                                                          [:cust_pid :en_pid :cust_name :en_name]) ]
                                         (cond 
                                           (not= cpid "") (format "/c/bank/acct/%s" cpid)
                                           (not= epid "") (format "/c/bank/acct/%s" epid)
                                           :else (format "/c/bank/cust/%s" (str cname ename))))
                                       } "查看"]]
                                 ]) } ))))

(defn- cust-search2
  "根据org、cif查询客户"
  [org cif]
  (let [sql (format "select * from in_cust where org_id='%s' and cif_key='%s'" org cif)]
    (select-all "bank" sql)))

(defn acct-search
  "根据org、cif查询账户"
  [org cif]
  (let [sql (format "select * from acct where org_id='%s' and cif_key='%s' " org cif)]
    (select-all "bank" sql)))

(defn- acct-list
  "得到acct账户列表的html片段
  @rt clojure.sql结果集"
  [rt]  
  (html 
    [:h1 (format "有%s个账户" (count rt))]
    (result-html rt {:f-head (fn [thead] 
                               (map (fn [th] [:th th]) '[序号 业务类型 币种 开户日期]))
                     :f-row (fn [row-index row]
                              [:tr (bgcolor-css row-index)
                               [:td {:align "right" :style "color: lightgray"} row-index]
                               [:td (row :yw_type)]
                               [:td (currency-dict (trim (row :bz)))]
                               [:td (-> (row :khrq) str (subs 0 10))]
                               ]) 
                     } )))

(defn acct
  "app: 根据（1、指定网点、客户号）或者（2、客户pid）查出账户信息
  @id 有id时进行客户模糊搜索，id为中文时按客户名称搜索，id为非中文时按照pid进行搜索，有多个结果时显示第一个客户的帐号
  @org 网点号
  @cif "
  [id org cif]
  (let [cust-rt (if id (cust-search id) (cust-search2 org cif))]
    (html-body 
      (if (or (nil? cust-rt) (empty? cust-rt))
        (eui-tip "未找到指定客户或该客户没有任何账户")
        (let [cust (first cust-rt)
              acct-rt (acct-search org cif)]
          (html
            (cust-list cust-rt)
            (eui-tip (format "<b>%s</b> 在 <b>%s</b> 的账户情况如下：" (cust :name) ((org-map) (cust :org_id))))
            (acct-list acct-rt)
            [:h1 "帐户详情："]
            (eui-tabs 
              {:style "width:1230px; height:1000px;"}
              (for [row acct-rt]
                (eui-tab1 
                  (str "帐号" (:zh row)) 
                  {:href (apply format "/c/bank/acct-detail?org=%s&cif=%s&bz=%s&yw=%s&zh=%s" 
                                (map #(-> % row trim) [:org_id :cif_key :bz :yw_type :zh]) )} "11" ))) ))) )))

(defn- result-html-
  "对没有特殊要求的结果进行列表展示
  @rt Clojure.sql结果集 [{:c1 v :c2 v ..} ..]
  @thead 表头名称 [活期余额 业务类型 币种 日期] 
  @cols 列名称 [:ye :yw_type :bz :_created] 
  @m 客户化定制 {} "
  [rt head cols]
  (result-html rt {:f-head (fn [thead] (for [th (cons "序号" head)] [:th th]))
                   :f-row (fn [row-index row]
                            [:tr (bgcolor-css row-index)
                             [:td {:align "right" :style "color: lightgray"} row-index]
                             (for [col cols] (let [v0 (-> col row)
                                                   v (-> v0 str trim)
                                                   v (if (.endsWith v "00:00:00.0") (subs v 0 10) v)]
                                             [:td (td-align v0) 
                                              (case col
                                                :bz (currency-dict v)
                                                :org_id (org-dict v)
                                                v)])) ])
                   } ))  
  
(defn acct-detail
  "service: 账户存贷款明细"
  [org cif yw bz zh]
  (let [sql-hq (format "select * from in_depo_hq where org_id='%s' and yw_type='%s' and bz='%s' and zh='%s' "
                       org yw bz zh)
        sql-dq (format "select * from in_depo_dq where org_id='%s' and bz='%s' and cif_key='%s' order by khrq "
                       org bz cif)
        sql-loan-main (format "select * from in_loan_main where org_id='%s'  and bz='1' and zh='%s' order by khrq"
                              org zh)
        sql-loan-detail (format "select * from in_loan_detail where org_id='%s' and bz='%s' and zh='%s' order by rq"
                                org bz zh)
        sql-daybook (format "select * from in_daybook where org_id='%s'  and yw_type='%s' and bz='%s' and zh='%s' "
                            org yw bz zh)
        [hq dq loan loan2 daybook] (for [sql [sql-hq sql-dq sql-loan-main sql-loan-detail sql-daybook]] 
                                     (select-all "bank" sql))
        css {:style "float:left; margin: 5px"}]
    (html
      [:h2 "存款总额：" (currency-dict bz) " <font color=red>" (sum (map :ye (into hq dq))) "</font> 圆"]
      [:div css [:h2 "活期存款："] 
       (result-html- hq '[余额 币种 日期] [:ye :bz :rq])]
      [:div css [:h2 "定期存款："]
       (result-html- dq '[余额 币种 开户日期 到期日期] [:ye :bz :khrq :dqrq])]
      [:div css [:h2 "贷款情况："]
       (result-html- loan '[贷款额 币种 利率 开户日期 到期日期] [:cdje :bz :ll :khrq :dqrq])]
      [:div css [:h2 "贷款明细："]
       (result-html- loan2 '[贷款余额 金额 币种 利率 日期] [:ye :je :bz :ll :rq])]
      (when (not (empty? daybook))
        [:div css [:h2 "账户余额变化："]
         (linef {:category (vec (map #(-> % :rq str (subs 0 10)) daybook)) 
                 :余额 (vec (map #(-> % :ye) daybook)) 
                 :金额 (vec (map #(-> % :je) daybook))}
                {} 1200 300)])
      [:div css [:h2 "账户交易明细："]
       (result-html- daybook '[交易金额 余额 币种 交易日期] [:je :ye :bz :rq])]
      )))

; &org_id=132001&yw_type=100&bz=1%20&zh=82082%20%20&cif_key=13237  
;(acct-detail "132001" "13237" "100" "1" "82082")
;(currency-dict (str 1))