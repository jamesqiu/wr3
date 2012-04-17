(ns ^{:doc "企业安全生产标准化管理系统 Enterprise Safety Production Standardization "
      :todo "生成数据（考评员、考评机构、企业）；文件上传"} 
     wr3.clj.app.esp)

(use 'hiccup.core)
(use 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.s 'wr3.clj.n 'wr3.clj.chart)
(use 'somnium.congomongo 'wr3.clj.nosql)

(require '[wr3.clj.app.auth :as au])
(defn auth
  "该函数被 CljServlet 调用 "
  [request fname & args]
  (cond
    (session request "wr3user") true
    :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; layout

;; frame-cfg 说明：
;; :name            应用名称，用于javascript函数的前缀，如 function demo_xxx()
;; :style           上方色调，["layout_north" "layout_title"]（蓝色）或 ["layout_north1" "layout_title1"]（金色）
;; :title           应用名称，位于上方
;; :searcher        搜索框的label，位于右上方
;; :nav             导航条，位于左边
;; :menu            2层菜单条，位于上方
;; :left-or-right   "left" or "right"
(def frame-cfg-mot
  {:name "esp"
   :style (map #(str % "") ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化管理系统（试行）"
   :searcher ["张" ; default-value
              ["考评人员搜索" "range_pn" "icon-ok"] ; label name icon
              ["考评机构搜索" "range_org" "icon-ok"]
              ["交通运输企业搜索" "range_en" "icon-tip"] ]
   :nav [["考评员" "icon-pen" ; title id
          ["考评员列表" "icon-list"    "pn-list"] ; title icon id 
          ["考评员管理"          "icon-list"    "indic0_bt"] ; title icon id 
          ]
         ["考评机构" "icon-pen" ; title id
          ["考评机构列表" "icon-list"  "org-list"] ; title icon id 
          ["达标考评" "icon-list"    "stand-list"] ; title icon id 
          ["资格撤销" "icon-list"    "org-input"] ; title icon id 
          ["考评机构管理"          "icon-list"    "indic0_bt"] ; title icon id 
          ]
         ["交通运输企业"           "icon-pen" ; title id
          ["企业列表" "icon-list"  "en-list"] ; title icon id 
          ["在线申请"          "icon-list"    "indic0_bt"] ; title icon id 
          ["达标标准自评"          "icon-list"    "indic0_bt"] ; title icon id 
          ["交通运输企业管理"          "icon-list"    "indic0_bt"] ; title icon id 
          ]
         ["交通运输管理部门"           "icon-pen" ; title id
          ["统计分析"          "icon-list"    "indic0_bt"] ; title icon id 
          ]
         ["系统管理及帮助"     "icon-search"
          ["网站样式"          "icon-search" "site_bt"]
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ]   
   :menu [["企业安全生产标准化申报系统" 
           ["企业在线填报子系统"     "11-input"] 
           ["填报管理子系统"       "12-mng"]
           ]
          
          ["评审机构及评审人员管理系统"
           ["考评员在线申请子系统"   "21-pn"]     
           ["考评机构在线申请子系统" "22-org"]     
           ["评定管理子系统"   "23-mng"]     
           ]     
          
          ["企业安全生产标准化评审系统"
           ["申报信息查询子系统" "31-search"] 
           ["评审过程管理子系统" "32-process"] 
           ["评审记录管理子系统" "33-mng"]
           ]
          
          ["重点企业跟踪监督管理系统"
           ["信息报送子系统" "41-input"] 
           ["信息复核子系统" "42-check"]
           ]
          
          ["统计分析管理系统"
           ["统计查询子系统" "51-search"] 
           ["统计分析子系统" "52-analysis"]
           ["报表管理子系统" "53-report"]
           ]
          
          ["政策法规管理系统"]
          
          ["信息发布系统"]
          ] 
   })

(def frame-cfg-pn
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——考评员在线申请系统（试行）"
   :nav [
         ["考评员" "icon-pen" ; title id
          ["在线申请考评证书" "icon-list"    "pn-input"] ; title icon id 
          ["在线申请进度查询" "icon-list"    "pn-process"] 
          ["培训、考试情况查询" "icon-list"    "pn-process"] 
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ]  
   })

(def frame-cfg-org
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——考评机构管理系统（试行）"
   :nav [
         ["企业考评" "icon-pen" ; title id
          ["企业考评受理" "icon-list"    "pn-input"] ; title icon id 
          ["企业换证及附加考评" "icon-list"    "pn-process"] 
          ["考评企业查询统计" "icon-list"    "pn-process"] 
          ["考评情况汇总表" "icon-list"    "pn-process"] 
          ]
         ["考评员" "icon-pen" ; title id
          ["在线申请考评证书" "icon-list"    "pn-input"] ; title icon id 
          ["在线申请进度查询" "icon-list"    "pn-process"] 
          ["考评员档案管理" "icon-list"    "pn-process"] 
          ]
         ["考评机构" "icon-pen" ; title id
          ["申请考评机构资质证书" "icon-list"    "org-input"] ; title icon id 
          ["申请考评机构变更备案" "icon-list"    "org-input"] ; title icon id 
          ["在线申请进度查询" "icon-list"    "pn-process"] 
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ]   
   })

(def frame-cfg-en
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——企业在线填报管理系统（试行）"
   :nav [
         ["企业申请" "icon-pen" ; title id
          ["1、在线填报" "icon-list"    "pn-input"] ; title icon id 
          ["2、达标自评" "icon-list"    "stand-list"] ; title icon id 
          ["3、选择考评机构" "icon-list"    "pn-process"] 
          ["企业年审" "icon-list"    "pn-process"] 
          ["变更申请" "icon-list"    "en-change"] 
          ]
         ]   
   })

; 子系统名称
(def cfg-subsys
  [
   ["考评员管理系统" "pn" "考评人员在线申请"]
   ["考评机构管理系统" "org" "考评机构在线申请、评定管理"]
   ["企业在线填报管理系统" "en" "企业在线填报管理"]
   ["安全生产标准化管理系统" "mot" "交通运算管理部门（交通部）内部管理"]
   ["实名举报考评机构" "hot" "任何单位和个人对考评机构的考评行为，有权向主管机关进行实名举报，主管机关会及时受理、组织调查处理，并为举报人保密。"]
   ])

(defn- index-all
  "app: 所有子系统的进入界面，临时，最后应该是一个不用登录即可访问的静态页面"
  []
  (html-body
    [:div {:style "text-align: center; margin: 100px; border: 1px solid blue; width:992px"}
     [:img {:src "/img/esp/esp.jpg"}]
     [:table {:align "center"}
      (for [[nam id meta] cfg-subsys ] 
        [:tr [:td [:h1 [:a {:href (format "%s/c/esp/index/%s" webapp id) :title meta}
                        (str "进入 " nam )]]]])]
     [:div {:style "width:100%; height:50px; margin-top:30px; background-color:#48f"}]]))
  
(defn index
  "@id 为nil时显示入口页面；为 pn,org,en,mot 时为子系统layout入口"
  [id]
  (if id 
    (frame-index (case id
                   "pn" frame-cfg-pn
                   "org" frame-cfg-org
                   "en" frame-cfg-en
                   "mot" frame-cfg-mot
                   "not-found"))
    (index-all)))

;;------------------- 
; 申请类别
(def dd-type 
  {"d" "道路运输" 
   "s" "水路运输" 
   "g" "港口码头" 
   "c" "城市客运" 
   "j" "交通运输工程建设"})
(def dd-type2 
  (array-map
    "d1" "道路运输普通货运"
    "d2" "机动车维修"
    "d3" "汽车客运站"
    "d4" "道路危险货物运输"
    "d5" "道路旅客运输"
    "d6" "道路货物运输场站"
    "s1" "水路危险货物运输"
    "s2" "水路散货运输"
    "s3" "水路旅客运输"
    "g1" "危险货物码头运输"
    "g2" "港口客运（滚装、渡船渡口）码头"
    "g3" "港口散杂货（集装箱）码头"
    "c1" "出租汽车"
    "c2" "城市公共汽车客运"
    "c3" "城市轨道交通运输"
    "j1" "交通运输建筑施工"
   ))
; 撤销考核员资格原因
(def dd-reason
  {1 "（一）隐瞒企业重大安全问题的；"
   2 "（二）考评结论严重失实的；"
   3 "（三）泄露企业技术和商业秘密的；"
   4 "（四）收受企业财物或者为企业谋取不正当利益的；"
   5 "（五）不服从主管机关监督管理的；"
   6 "（六）资格证逾期不申请换证的；"
   7 "（七）其他不能胜任考评工作的。" })
; 对企业附加考评的原因
(def dd-recheck
  {1 "（一）企业发生重大及以上安全责任事故；"
   2 "（二）企业一年内连续发生二次及以上较大安全责任事故；"
   3 "（三）企业被举报并经核实其安全生产管理存在重大安全问题；"
   4 "（四）企业发生其他可能影响其安全生产管理的重大事件或主管机关认为确实必要的。" })
; 企业申请达标级别对应的分数要求
(def dd-score
  {"一级" 900 ; >=900
   "二级" 700 ; >=700
   "三级" 600 ; >=600
   })
; 企业达标等级
(def dd-en-grade
  {1 "一级"
   2 "二级"
   3 "三级"})
; 考评机构的考评资格
(def dd-org-grade
  {1 "甲类"
   2 "乙类"
   3 "丙类"})
; 数据项英文、中文对照
(def dd-meta
  {:name "名称"
   :province "省份"
   :type "类型"
   :type2 "细类"
   :grade "级别"
   :sex "性别"
   :org "单位组织"
   :workdate "参加工作日期"
   :birth "出生日期"
   :title "职称"
   :major "专业"
   :from "地域"
   :cid "证书号"
   :ctype "证书类型"
   :tel "电话"
   :date "日期"
   :death "死亡人数"
   :yyyy "年份"
   :scroe "分数"
   :edu "学历"
   })

; 考评员申请表
(def cfg-apply-pn ; [name id {:t :title ..}] :t类型,不写时为text 
  [
   ["姓名" "name" {:require true}]
   ["性别" "sex" {:t ["男" "女"]}]
   ["出生年月" "birth" {:t 'date :title "注意：年龄原则上不得超过65周岁；格式如1980-5"}]
   ["照片" "photo" {:t 'file}]
   ["身份证号" "pid" {:t 'pid}]
   ["工作单位" "org"]
   ["职务/职称" "title"]
   ["通讯地址" "address"]
   ["邮  编" "pcode" {:t 'pcode}]
   ["联系电话" "tel"]
   ["传真号码" "fax"]
   ["手机号码" "mobile"]
   ["电子邮箱" "email" {:t 'email}]
   ["文化程度" "edu"]
   ["所学专业" "major" {:title "注意：必须具备交通运输相关专业大学专科以上学历"}]
   ["现从事专业" "prof"]
   ["申请专业" "type" {:t dd-type :title "考评员申请的专业类型不得多于二种"}] ; 最多两类
   ["申请级别" "grade" {:t dd-en-grade}]
   ["主要学习（培<br/>训）经历" "train" {:t 'textarea}]
   ["主要工作简历" "resume" {:t 'textarea}]
   ["专业工作业绩" "perf" {:t 'file}]
   ["相关证明文件" "proof" {:t 'file :title "包括身份证明、学历证明以及其他资格证的电子文档等（pdf, doc或者jpg格式）"}]
   ])
; 考评机构申请表  
(def cfg-apply-org 
  [["评审机构名称" "name" {:require true :v "xxx"}]
   ["评审机构资质" "qual" {:t dd-org-grade :v "甲类"}]
   ["评审人员姓名" "person" {:require true :v "张三"}]
   ["评审人员编码" "pcode" {:require true :v "007"}]
   ["评审人员资质" "pqual" {:t '[A级 B级 C级] :v 'C级}]
   ])
; 企业申请表
(def cfg-apply-en ; en-input-form 
  [["企业名称" "name" {:require true :v "xxx"}]
   ["行政区划" "region" {:t '[东北 华北 华南 西南 西北 华中 华东] :v '西南}]
   ["法人信息" "legel" {:t 'textarea :v "aaa\nbbbb\nccccc"}]
   ["企业类型" "type" {:t dd-type :v "s"}]
   ["企业具体分类" "type2" {:t dd-type2 :v "s1"}]
   ["企业地址" "address" "" {:title "选择GIS坐标"}]
   ["企业所在地描述" "belong" {:t 'textarea}]
   ["安全生产标准化等级" "grade" {:t [:一级 :二级 :三级] :v :一级}]
   ["联系人信息" "contact" ]
   ["安全生产组织架构" "safe" {:t 'file}]
   ["企业法人资格证件" "qual" {:t 'file}] 
   ["经营许可证" "license" {:t 'file}]
   ])

(defn- input-form-
  [form title]
  (let [css-label "font-family:微软雅黑; font-size:14px; vertical-align:center; height: 35px; border-bottom:1px dotted gray"]
    (html
      [:table 
       [:caption {:style "padding: 5px"} [:h1 title]]
       (for [[nam id {t :t v :v require :require title :title}] form]
         [:tr 
          [:td {:style css-label} [:label (str nam "：")]]
          [:td {:style "border-bottom:1px dotted gray"}
           (cond 
             (true? require)       (eui-text {:id id :required "true" :value v :title title})
             (= t 'textarea)       (eui-textarea {:id id} v)
             (= t 'file)           (eui-text {:id id :type "file"})
             (= t 'email)          (eui-email {:id id})
             (map? t)              (eui-combo {:id id :v v} t)
             (vector? t)           (eui-combo {:id id :v v} (apply array-map (flatten (for [e t] [e e]))))
             :else                 (eui-text {:id id :value v :title title}))
           ]])
       [:tfoot [:tr [:td {:colspan 2 :align "center" :style "padding: 15px"} 
                     (eui-button {} " 保 存 ") (space 5)
                     (eui-button {} " 取 消 ") ]]]] )))

(defn en-input
  "service：企业在线填报"
  []
  (input-form- cfg-apply-en "交通运输企业安全生产标准化<font color=blue>企业</font>申请表"))

(defn pn-input
  "service：评审人员在线申请"
  []
  (input-form- cfg-apply-pn "交通运输企业安全生产标准化<font color=blue>考评员</font>申请表"))

(defn org-input
  "service：评审机构在线申请"
  []
  (input-form- cfg-apply-org "交通运输企业安全生产标准化<font color=blue>考评机构</font>申请表"))

(defn- result-html-
  "对没有特殊要求的结果进行列表展示
  @rt Clojure.sql结果集 [{:c1 v :c2 v ..} ..]
  @head 表头名称 [活期余额 业务类型 币种 日期] 
  @cols 列名称 [:ye :yw_type :bz :_created] 
  @m 客户化定制 {} "
  ([rt head cols m]
    (result-html rt {:f-head (fn [thead] (for [th (cons "序号" head)] [:th th]))
                     :f-row (fn [row-index row]
                              [:tr (bgcolor-css row-index)
                               [:td {:align "right" :style "color: lightgray"} row-index]
                               (for [col cols] (let [v0 (-> col row)
                                                     v (-> v0 str trim) ]
                                                 [:td (td-align v0) 
                                                  (case col
                                                    :_id [:a {:href (format "/c/esp/%s/%s" (:form m) v) :target "_blank"} "查看"]
                                                    :type (or (dd-type v) v)
                                                    :type2 (or (dd-type2 v) v)
                                                    v)])) ]) } ))
  ([rt head cols] (result-html- rt head cols {})))
  
(defn- data-
  "@tb :pn | :en | :org"
  [tb]
  (with-mdb2 "esp" 
    (vec (fetch tb :limit 5000))))

(defn- search-
  "@tb :pn | :en | :org
  @s 名字字符串"
  [tb s]
  (with-mdb2 "esp" 
    (vec (fetch tb :limit 100 :where {:name (re-pattern (or s ""))}))))
  
(defn pn-list
  "service: 考评员列表"
  [id]
  (let [tb :pn
        rt (if id (search- tb id) (data- tb))]
    (html
      [:h1 (format "考评人员列表（%s 名）" (count rt))]
      (result-html- rt '[姓名 单位 属地 详情] [:name :org :from :_id] {:form "pn-form"}))))
  
(defn org-list
  "service: 企业列表"
  [id]
  (let [tb :org
        rt (if id (search- tb id) (data- tb))]
    (html
      [:h1 (format "交通运输企业列表（%s 名）" (count rt))]
      (result-html- rt '[机构名称 所属省份 详情] [:name :province :_id] {:form "org-form"}))))
  
(defn en-list
  "service: 企业列表"
  [id]
  (let [tb :en
        rt (if id (search- tb id) (data- tb))]
    (html
      [:h1 (format "交通运输企业列表（%s 名）" (count rt))]
      (result-html- rt '[所属省份 企业名称 企业类型 等级 详情] [:province :name :type :grade :_id] {:form "en-form"}))))

(defn form-
  "@tb 表名如 :pn :en :org 
  @id object-id字符串如 '4f8ad8ef75e0ae9283368075' "
  [tb id title]
  (with-mdb2 "esp"
    (let [rt (fetch-by-id tb (object-id id))]
      (html-body
        [:table.wr3table {:border 1}
         [:caption (format "%s <u>%s</u>" title (:name rt))]
         [:tbody
          (for [[k v] (dissoc rt :_id)]
            [:tr 
             [:th {:style "text-align: left"} (or (dd-meta k) k) "："] 
             [:td (case k
                   :type (or (dd-type v) v) 
                   :type2 (or (dd-type2 v) v) 
                   v)]])]
         [:tfoot 
          [:tr {:align "center" :height "50px"} 
           [:td {:colspan 2 } (eui-button {:href "#" :onclick "window.close();"} "关闭")]]]] ))))

(defn pn-form
  "service: 查看指定考评员的记录表单"
  [id]
  (form- :pn id "考评员"))
  
(defn org-form
  "service: 查看指定考评机构的记录表单"
  [id]
  (form- :org id "考评机构"))
  
(defn en-form
  "service: 查看指定企业的记录表单"
  [id]
  (form- :en id "交通运输企业"))
  
(defn en-analysis
  "service: 企业统计分析"
  []
  (let [rt (data- :en)
        rt1 (group-by :province rt)
        m (for [[p ls] rt1] [p (count ls)])
        m1 (sort-by #(- (second %)) m)]
    (html
      [:h1 "各省一级企业数量分析"]
      (barf (apply array-map (flatten m1)) {:x "省份" :y "一级企业数量"})
      (pief (apply array-map (flatten m1)) {})
      )))

(defn stand-list
  "service: 列出所有的达标标准"
  []
  (let []
    (html
      [:center [:h1 "达标标准考评（五大类16小类）"]]
      (for [[k1 v1] dd-type]
        (html 
          [:h2 v1 "："]
          (for [[k2 v2] (filter #(.startsWith (key %) k1) dd-type2)]
            (eui-button {:href (str "/c/esp/stand/" k2) :target "_blank" :style "margin: 5px"} 
                        v2)))
      ))))

(defn- get-stand-
  "@tb 'en-stand1' 'en-stand2' 
  @type2 'd1' 'd2' "
  [tb type2]
  (with-mdb2 "esp"
    (vec (fetch (keyword tb) :where {:type2 type2} :sort {:i 1 :j 1 :k 1}))))  

(defn stand
  "app: 企业安全生产达标标准——自评，机构考评"
  [id]
  (html-body
    (let [type2 (or id "d1")
          [rt1 rt2 rt3] (map #(get-stand- (str "en-stand" %) type2) [1 2 3])
          f3 (fn [r] ; r:  某个2级考核指标，如“1.安全工作方针与目标”
               (let [s1 (html [:td {:style "width: 800px"} (:name r)] 
                              [:td [:b (:score r)] (when (= 1 (:required r)) [:font {:color "red"} "★"])] 
                              [:td {:align "right"} (eui-numberspin {:min 0 :max (:score r) :increment 1 
                                                                     :value (:score r) :style "width:40px"})])
                     s2 (if (= 1 (:k r)) s1 (html [:tr s1]))]
                 s2))
          f2 (fn [r] ; r:  某个2级考核指标，如“1.安全工作方针与目标”
               (let [rt3i (filter #(and (= (:i r) (:i %)) (= (:j r) (:j %))) rt3) ;该2级指标对应的所有三级指标
                     s1 (html [:td {:rowspan (count rt3i)} (str (:j r) ". " (:name r))]
                              (f3 (first rt3i)) 
                              (for [r (rest rt3i)] (f3 r)))
                     s2 (if (= 1 (:j r)) s1 (html [:tr s1])) ]
                 s2))
          f1 (fn [r] ; r： 某个1级考核指标，如“一、安全目标 35分 ”
               (let [rt2i (filter #(= (:i r) (:i %)) rt2) ;该1级指标对应的所有二级指标
                     rt3i (filter #(= (:i r) (:i %)) rt3)] ;该1级指标对应的所有三级指标
                 (html [:tr
                        [:td {:rowspan (count rt3i)} (:name r) [:br][:br](space 10) (:score r) "分"]
                        (f2 (first rt2i))] ; 产生后面2、3级指标的多个[:tr]html片段 
                       (for [r (rest rt2i)] (f2 r)))))
          ]
      [:table.wr3table {:border 1}
       [:caption (format "%s企业安全生产达标标准" (dd-type2 type2))]
       [:thead 
        [:tr [:th "考核内容"] [:th {:colspan 2} "考核要点"] [:th "分数"] [:th "自评分"] ]]
       [:tbody 
        (for [r rt1] (f1 r))]
       [:tfoot
        [:tr [:td {:colspan 5} [:h2 "注：打 “ <font color=red>★</font> ” 的为必备项，必须完全满足。"]]]]
       ] )))

;;;------------------------ 测试文件上传
(defn upload
  []
  (html-body
    [:form {:name "form1" :method "post" :ENCTYPE "multipart/form-data" :action "/c/esp/file"}
     [:label "文件："]
     [:input {:type "file" :name "myfile"}]
     [:input {:type "submit"}] ] ))

(import 'wr3.upload.FileUpload 'wr3.upload.File 'wr3.util.Charsetx)

(defn file
  [request]
  (let [fu (doto (FileUpload.) (.initialize request) .upload)
        myfile (.getFile (.getFiles fu) 0)
        fname (.getFileName myfile)
        fsize (.getSize myfile)]
    (html-body
      (when (not (.isMissing myfile))
        (do
          (.saveAs myfile (str "f:/temp/" fname)) 
          [:h1 (format "文件名: %s (大小：%s)" fname fsize)]))
      "文件上载完成" )))

;;-------- test
(def m '[
[1	1	1	①制定企业安全生产方针、目标和不低于上级下达的安全考核指标；	2]
[1	1	2	②制定实现安全工作方针与目标的措施。	3]
[1	2	1	①制订和实施企业安全生产中长期规划和跨年度专项工作方案。	5]
[1	3	1	①根据中长期规划，制定年度计划和年度专项活动方案，并严格执行。	10]
[1	4	1	①将安全生产管理指标进行细化和分解，制定阶段性的安全生产控制指标；	3]
[1	4	2	②制定安全生产目标考核与奖惩办法；	2]
[1	4	3	③定期考核年度安全生产目标完成情况，并奖惩兑现。	10]
[2	1	1	①成立安全生产委员会或领导小组（安委会），下属各分支机构分别成立相应的领导机构。安委会职责明确，实行主要领导负责制；	10]
[2	1	2	②设置与企业规模相适应的安全生产管理机构；	"10★★"]
[2	1	3	③定期召开安委会会议。安全生产管理机构和下属各分支机构每月至少召开一次安全工作例会。	10]
[2	2	1	①按规定足额配备安全生产和应急管理人员。	★★★]
[3	1	1	①企业主要负责人、分管领导、全体员工安全职责明确，制定并落实安全生产责任制，层层签订安全生产责任书，并落实到位；	★★★]
[3	1	2	②法定代表人是安全生产第一责任人，按照安全生产法律法规赋予的职责，对安全生产负全面组织领导、管理责任和法律责任，并严格履行安全生产的责任和义务；	10]
[3	1	3	③分管安全生产的领导是安全生产的重要负责人，对安全生产负重要管理责任并落实到位；	10]
[3	1	4	④公司其他领导和全体员工实行“一岗双责”，对业务范围内的安全生产工作负责并落实到位；	10]
[3	1	5	⑤安全生产管理机构、各职能部门、生产基层单位的安全职责明确并落实到位；	10]
[3	2	1	①根据安全生产责任进行定期考核和奖惩，公告考核和奖惩情况。	15]
[4	1	1	①《道路运输经营许可证》、《企业法人营业执照》合法有效，经营范围符合要求。	★★★]
[4	2	1	①及时更新适用的安全生产法律法规、标准规范；	5]
[4	2	2	②将法规标准和相关要求及时转化为本单位的规章制度，贯彻到各项工作中；	5]
[4	2	3	③执行并落实安全生产法律法规、标准规范；	5]
[4	2	4	④将适用的安全生产法律、法规、标准及其他要求及时对从业人员进行宣传和培训。	5]
[4	3	1	①制定并及时修订安全生产管理制度，发放到岗位（职工），包括：1）安全生产责任制；2）安全例会制度；3）文件和档案管理制度；4）安全生产费用提取和使用管理制度；5）车辆、设施、设备安全管理制度；6）安全培训和教育学习制度；7）从业人员安全管理制度；8）内部交通管理制度；9）安全生产监督检查制度；10）事故统计报告制度；11）安全奖惩制度等；	10]
[4	3	2	②组织从业人员进行安全管理制度的学习和培训。督促从业人员严格执行本单位的安全管理制度。	5]
[4	4	1	①制定并及时修订各岗位的安全生产操作规程，并发放到岗位（职工）；	★★★]
[4	4	2	②对从业人员进行安全生产操作规程的学习和培训。从业人员严格执行本单位的安全生产操作规程。	10]
[4	5	1	①各级各部门各类人员贯彻落实国家有关安全生产的方针、政策、法规及本单位的安全管理制度和操作规程，依据行业特点，制定企业安全生产管理措施；	10]
[4	5	2	②每年至少一次对安全生产法律法规、标准规范、规章制度、操作规程的执行情况进行检查；	10]
[4	5	3	③建立和完善各类台帐和档案，并按要求及时报送有关资料和信息。	★★★]
[5	1	1	①按规定足额提取安全生产费用；	★★★]
[5	1	2	②安全生产经费专款专用，保证安全生产投入的有效实施；	"20★★"]
[5	1	3	③及时投入满足安全生产条件的所需资金；	10]
[5	1	4	④为员工参加工伤保险，并在有效期内。	3]
[5	2	1	①跟踪、监督安全生产专项经费使用情况；	15]
[5	2	2	②建立安全费用使用台账。	2]
[6	1	1	①具备满足安全生产需要的场地和设施设备；	10]
[6	1	2	②按规定配足有效的安全防护、环境保护、消防设备设施及器材，并按要求进行定期维护保养；	★★★]
[6	1	3	③从事危险货物运输车辆维修必须有与其作业内容相适应的专用维修车间、设备设施，并设置明显的指示标识；	★★★]
[6	1	4	④公司有专人负责安全设施、器材的管理，且管理规范；	10]
[6	1	5	⑤设有覆盖安全重点部位视频监控设备，并保持实时监控；	10]
[6	1	6	⑥应急通道、安全出口、消防车通道保证畅通。	10]
[6	2	1	①每天进行例检，记录点火延迟等现象；	10]
[6	2	2	②及时清理汽车喷烤漆房内的杂物，并定期清理汽车喷烤漆房烟道；	10]
[6	2	3	③按规定指定专人对汽车喷烤漆房进行管理，并在醒目位置安装负责人基本信息及永久性安全操作、保养文字标志。	10]
[6	3	1	①按要求建立严格的举升机操作规程和操作流程；	10]
[6	3	2	②定期对举升机进行例检，并按要求进行检查维护，保证技术状态良好。	10]
[6	4	1	①应按照《特种设备安全监察条例》、《特种设备质量监督与安全监察规定》及其特种设备相关的《检验规程》等，对特种设备及其安全附件进行定期检验和维护保养；	★★★]
[6	4	2	②按规定指定专人对特种设备进行管理；	10]
[6	4	3	③按要求规范建立特种设备台账。	5]
[6	5	1	①按照国家相关法律法规规范电气安全管理。	10]
[6	5	2	②电气装置周围应留有足够的安全通道和工作空间，应远离易燃、易爆和腐蚀性物品，不得被其他杂物遮盖。	10]
[7	1	1	①安全管理工作有创新的做法；	10]
[7	1	2	②应用现代科技手段，提升安全管理水平；	10]
[7	1	3	③优先采用安全可靠性强、节能、环保产品。	10]
[7	2	1	①设有安全生产管理系统；	10]
[7	2	2	②设有其它的安全监管信息系统。	5]
[8	1	1	①制定并实施年度及长期的安全培训计划，明确培训内容和年度培训时间。	10]
[8	2	1	①组织开展安全生产的法律、法规和安全生产知识的宣传、教育。	10]
[8	3	1	①企业主要负责人和管理人员具备相应安全知识和管理能力，并经行业主管部门培训合格；	★★★]
[8	3	2	②专（兼）职安全管理人员具备专业安全生产管理知识和经验，熟悉各岗位的安全生产业务操作规程，运用专业知识和规章制度开展安全生产管理工作，并保持安全生产管理人员的相对稳定。	20]
[8	4	1	①从业人员每年接受继续教育，提高从业人员的素质和能力，年度继续教育时间不得少于有关规定学时。未经安全生产培训合格的从业人员，不得上岗作业；	"15★★"]
[8	4	2	②新技术、新设备投入使用前，对管理和操作人员进行专项培训。	15]
[8	5	1	①建立健全安全宣传教育培训档案；	10]
[8	5	2	②对培训教育效果进行评估，改进提高培训教育质量。	10]
[9	1	1	①严格执行操作规程和安全生产作业规定，严禁违章指挥、违章操作、违反劳动纪律，不得占用道路进行车辆维修作业。	15]
[9	1	2	②在下达生产任务的同时，布置安全生产工作要求；	15]
[9	1	3	③工位划分清楚，特殊作业场所（钣金、涂漆等）单独设置，厂区出入口分开设置，若场地条件不允许，应设专人指挥车辆进出；	10]
[9	1	4	④有符合规定的专业技术人员，特种作业人员和从事危险作人员须具备相应资质，并取得相关资格证书；	★★★]
[9	1	5	⑤指定专人对危险作业进行现场管理，严格执行巡回检查制度，严禁无关人员进入作业区域；	10]
[9	1	6	⑥制定至少包括下列危险作业的安全监督管理制度，明确责任部门、人员、许可范围、审批程序、许可签发人员等：危险区域动火作业，进入受限空间作业，高处作业，其他危险生产作业；	10]
[9	1	7	⑦设施设备、生产物料堆放和存储符合相关安全规范和技术要求，易燃易爆有毒物品（如：油漆）单独存放。	5]
[9	2	1	①制定并落实安全生产值班计划和值班制度，重要时期实行领导到岗带班，有值班记录。	5]
[9	3	1	①两个或两个以上单位共用同一设施设备进行生产经营的现场安全生产管理职责明确，并落实到位；	10]
[9	3	2	②与外来施工（作业）方签订安全协议，明确双方各自的安全责任；	5]
[9	3	3	③对短期合同工、临时用工、实习人员、外来参观人员、客户及其车辆等进入作业现场有相应的安全管理制度和措施。	5]
[9	4	1	①应在室内进行喷涂作业；	5]
[9	4	2	②应设置独立的调漆间；	5]
[9	4	3	③涂漆作业区应设有专用的废水排放及处理设施，采用干打磨工艺的，有粉尘收集装置和除尘设备，并设有通风设备。	10]
[9	5	1	①调试工位应设置汽车尾气收集净化装置。	10]
[9	6	1	①动用明火作业时，必须办理动火证，并做好动火记录；	10]
[9	6	2	②气瓶不得置于受阳光暴晒、热源辐射及可能受到电击的地方，必须距离实际焊接或切割作业点足够远（一般为5m以上）；	5]
[9	6	3	③乙炔瓶和氧气瓶必须分开存放。	5]
[9	7	1	①在存在一定危险因素的作业场所和设备设施，设置明显的安全警示标志，相关场所按交通法律要求设置交通安全标志。	10]
[10	1	1	①开展本单位危险设施或场所危险源的辨识和确定工作；	10]
[10	1	2	②对重大危险源及时采取措施，并按规定报有关部门备案。	"15★★"]
[10	2	1	①及时对作业活动和设备设施进行危险、有害因素识别；	5]
[10	2	2	②向进入工作区域人员如实告知存在的危险因素、防范措施以及事故应急措施；	10]
[10	2	3	③对危险源进行建档，重大危险源单独建档管理。	5]
[11	1	1	①制定隐患排查工作方案，明确排查的目的、范围，选择合适的排查方法；	5]
[11	1	2	②每月至少开展一次安全自查自纠工作，及时发现安全管理缺陷和漏洞，消除安全隐患。检查及处理情况应当记录在案；	★★★]
[11	1	3	③对各种安全检查所查出的隐患进行原因分析，制定针对性控制对策。	10]
[11	2	1	①制定完善的隐患治理方案，包括目标和任务、方法和措施、经费和物资、机构和人员、时限和要求；	10]
[11	2	2	②对上级检查指出或自我检查发现的一般安全隐患，严格落实防范和整改措施，并组织整改到位；	10]
[11	2	3	③重大安全隐患报相关部门备案，做到整改措施、责任、资金、时限和预案“五到位”；	"15★★"]
[11	2	4	④建立隐患治理台帐和档案，有相关记录；	5]
[11	2	5	⑤按规定对隐患排查和治理情况进行统计分析，并向有关部门报送书面统计分析表。	10]
[12	1	1	①设置或指定职业健康管理机构，配备专（兼）职管理人员；按规定对员工进行职业健康检查。	10]
[12	2	1	①对从业人员进行职业健康宣传培训。使其了解其作业场所和工作岗位存在的危险因素和职业危害、防范措施和应急处理措施，降低或消除危害后果的事项。	15]
[12	3	1	①为从业人员提供符合职业健康要求的工作环境和条件，配备与职业健康保护相适应的设施、工具；	5]
[12	3	2	②对工作中可能遇到的职业危害进行调查、统计并建档，作业场所职业危害因素应符合GBZ2.1、GBZ2.2规定；	5]
[12	3	3	③按规定为员工参加工伤保险。	5]
[13	1	1	①设立安全文化廊、安全角、黑板报、宣传栏等员工安全文化阵地，每月至少更换一次内容；	5]
[13	1	2	②公开安全生产举报电话号码、通信地址或者电子邮件信箱。对接到的安全生产举报和投诉及时予以调查和处理。	5]
[13	2	1	①开展安全承诺活动；	5]
[13	2	2	②编制旅客运输安全知识手册，并发放到职工；	5]
[13	2	3	③组织开展安全生产月活动、安全生产竞赛活动，有方案、有总结；	5]
[13	2	4	④对在安全工作中做出显著成绩的集体、个人给予表彰、奖励，并与其经济利益挂钩；	5]
[13	2	5	⑤对安全生产进行检查、评比、考核，总结和交流经验，推广安全生产先进管理方法。	5]
[14	1	1	①制定相应的突发事件应急预案，有相应的应急保障措施和必备的应急物资；	★★★]
[14	1	2	②结合实际将应急预案分为综合应急预案、专项应急预案和现场处置方案；	"5★★"]
[14	1	3	③应急预案与当地政府预案保持衔接，报有关部门备案，通报有关协作单位；	2]
[14	1	4	④定期评估应急预案，并根据评估结果或实际情况的变化进行修订和完善，修订情况有记录并归档。	5]
[14	2	1	①开展应急预案的宣传教育，普及安全生产事故预防、避险、自救和互救知识；	5]
[14	2	2	②开展应急预案培训活动，使有关人员了解应急预案内容，熟悉应急职责、应急程序和应急处置方案；	5]
[14	2	3	③发生事故后，及时启动应急预案，组织有关力量进行救援，并按照规定将事故信息及应急预案启动情况报告有关部门。	10]
[14	3	1	①建立与本单位安全生产特点相适应的专兼职应急救援队伍，或指定专兼职应急救援人员；	10]
[14	3	2	②组织应急救援人员日常训练和演练。	8]
[14	4	1	①按照应急预案的要求配备相应的应急物资及装备；	10]
[14	4	2	②建立应急装备使用状况档案，定期进行检测和维护，使其处于良好状态。。	10]
[14	5	1	①制定应急预案演练计划，按照有关规定组织开展应急预案演练；	"10★★"]
[14	5	2	②应急预案演练结束后，对应急预案演练效果进行评估，撰写应急预案演练评估报告，分析存在的问题，并对应急预案提出修订意见。	10]
[15	1	1	①发生事故按相关规定及时、如实向有关部门报告，及时进行事故现场处置，按相关规定进行事故处理，并建立事故档案和事故管理台帐；	★★★]
[15	1	2	②准确、及时填报安全生产责任事故统计报表，没有隐瞒不报、谎报、拖延不报情况。	"10★★"]
[15	2	1	①接到事故报告后，迅速采取有效措施，组织抢救，防止事故扩大，减少人员伤亡和财产损失；	10]
[15	2	2	②发生事故后，按规定成立事故调查组，积极配合各级人民政府组织的事故调查，随时接受事故调查组的询问，如实提供有关情况；	5]
[15	2	3	③按时提交事故调查报告，剖析事故原因，落实整改措施；	5]
[15	2	4	④发生事故后，及时召开安全生产分析通报会，对事故当事人的聘用、培训、考核、上岗以及安全管理等情况进行责任倒查；	5]
[15	2	5	⑤按“四不放过”原则严肃查处事故，严肃查处安全生产事故，严格追究责任领导和相关责任人。处理结果报有关部门备案。	"15★"]
[16	1	1	①每年至少一次对本单位安全生产标准化的实施情况进行评定，对安全生产工作目标、指标的完成情况进行综合考核。	5]
[16	2	1	①提出进一步完善安全标准化的计划和措施，对安全生产目标、指标、管理制度、操作规程等进行修改完善。	5]
[16	3	1	①根据企业生产经营实际，建立相应的安全管理体系，规范安全生产管理，形成长效机制。	"20★"]
        ])

;;;; test
;(with-mdb2 "esp"
;  (let [rs (fetch :en-stand1 :where {:type2 "d2"})]
;    (doseq [r rs]
;      (update! :en-stand1 r (update-in r [:i] inc)))
;    (mass-insert! :en-stand3 rs) 
;    )
;  )

