(ns ^{:doc "企业安全生产标准化管理系统 Enterprise Safety Production Standardization "
      :todo "生成数据（考评员、考评机构、企业）；文件上传"} 
     wr3.clj.app.esp)

(use 'hiccup.core)
(use 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.s 'wr3.clj.n 'wr3.clj.chart 'wr3.clj.u)
(use 'somnium.congomongo 'wr3.clj.nosql 'wr3.clj.chart)
(import 'wr3.upload.FileUpload 'wr3.upload.File 'wr3.util.Charsetx)

(require '[wr3.clj.app.auth :as au])
(defn auth
  "该函数被 CljServlet 调用 "
  [request fname ids & args]
  (let [uid (wr3user request) 
        role (wr3role request)
        id (first ids)]
    (if (= fname "index")
      (cond
        (nil? id) true ; index 页面所有用户都能访问
        (and (= id "pn") (= role "pn")) true
        (and (= id "en") (= role "en")) true
        (and (= id "org") (= role "org")) true
        (and (= id "mot") (= role "mot")) true
        (and (= id "mct") (= role "mct")) true
        :else false)
      (cond ; 其他页面注册用户都能访问
        uid true
        :else false))))

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
   :nav [
         ["待办事宜" "icon-pen" ; title id
          ["考评机构申请受理" "icon-list"    "pn-list"] ; title icon id 
          ["考评机构变更备案受理" "icon-list"    "pn-list"] ; title icon id 
          ["考评机构换证受理" "icon-list"    "pn-list"] ; title icon id 
          ["企业初次申请受理" "icon-list"    "pn-olap"] ; title icon id 
          ["企业考评结论审核" "icon-list"    "pn-olap"] ; title icon id 
          ["企业换证申请受理" "icon-list"    "pn-olap"] ; title icon id 
          ["企业变更申请受理" "icon-list"    "pn-olap"] ; title icon id 
          ]
         ["考评员" "icon-pen" ; title id
          ["考评员列表" "icon-list"    "pn-list"] ; title icon id 
          ["考评员管理"          "icon-list"    "pn-olap"] ; title icon id 
          ]
         ["考评机构" "icon-pen" ; title id
          ["考评机构列表" "icon-list"  "org-list"] ; title icon id 
          ["资格证书制发" "icon-list"    "org-input"] ; title icon id 
          ["资格撤销" "icon-list"    "org-input"] ; title icon id 
          ["年度工作报告"          "icon-list"    "indic0_bt"] ; title icon id 
          ["考评情况汇总表"          "icon-list"    "indic0_bt"] ; title icon id 
          ]
         ["交通运输企业"           "icon-pen" ; title id
          ["企业列表" "icon-list"  "en-list"] ; title icon id 
          ["资格证书制发" "icon-list"    "org-input"] ; title icon id 
          ["附加考评" "icon-list"    "org-input"] ; title icon id 
          ["资格撤销" "icon-list"    "org-input"] ; title icon id 
          ["年度工作报告"          "icon-list"    "en-analysis"] ; title icon id 
          ]
         ["交通运输管理部门"           "icon-pen" ; title id
          ["统计分析"          "icon-list"    "indic0_bt"] ; title icon id 
          ]
         ["系统管理及帮助"     "icon-search"
          ["网站样式"          "icon-search" "site_bt"]
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ]   
   :menu_del [["企业安全生产标准化申报系统" 
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
          ["申请考评证书" "icon-list"    "pn-apply"] ; title icon id 
          ["培训、考试情况" "icon-list"  "pn-learn"] 
          ["换证申请" "icon-list"  "pn-renew"] 
          ["使用帮助" "icon-help" "pn-help"]
          ]
         ] 
   :frame-main (html [:script "layout_load_center('/static/esp/about-pn.html')"])
   })

(def frame-cfg-org
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——考评机构管理系统（试行）"
   :nav [
         ["&nbsp;考评机构" "icon-arrow" ; title id
          ["申请资质证书" "icon-list"    "org-input"] ; title icon id 
          ["申请变更备案" "icon-list"    "org-backup"] ; title icon id 
          ["申请换证" "icon-list"    "org-renew"] 
          ["已认定的资质证书" "icon-list"    "org-view"] 
          ["年度工作报告" "icon-list" "org-report"] ; title icon id 
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ["&nbsp;管理考评员" "icon-user" ; title id
          ["本机构考评员列表" "icon-list"    "org-pn"] ; title icon id 
          ["考评员档案管理" "icon-list"    "pn-archive"] 
          ]
         ["&nbsp;企业考评管理" "icon-pen" ; title id
          ["企业考评待办工作" "icon-list" "en-input"] ; title icon id 
          ["企业资格证书制发" "icon-list" "en-input"] ; title icon id 
          ["工作进度查询" "icon-list" "org-stat"] 
          ["考评企业档案管理" "icon-list" "en-archive"] 
          ["考评情况汇总表" "icon-list" "en-report"] 
          ]
         ]   
   })

(def frame-cfg-en
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——企业在线填报管理系统（试行）"
   :nav [
         ["企业申请" "icon-pen" ; title id
          ["1、在线填报" "icon-list"    "en-input"] ; title icon id 
          ["2、达标自评" "icon-list"    "stand-list"] ; title icon id 
          ["3、选择考评机构" "icon-list" "org-list"] 
          ["进度查询" "icon-list"    "en-input"] 
          ["企业年度工作报告" "icon-list"    "en-input"] 
          ["换证申请" "icon-list"    "en-input"] 
          ["变更申请" "icon-list"    "en-backup"] 
          ]
         ]   
   })

; 子系统名称
(def cfg-subsys
  [
   ["考评员管理系统" "pn" "考评人员在线申请"]
   ["考评机构管理系统" "org" "考评机构在线申请、评定管理"]
   ["企业在线填报管理系统" "en" "企业在线填报管理"]
   ["交通部管理系统" "mot" "交通运算管理部门（交通部）内部管理"]
   ["省级交通主管部门/长江、珠江航管局管理系统" "mot" "省级交通运输主管部门/长江航务管理局、珠江航务管理局内部管理"]
   ["<small>实名举报</small>" "hot" "任何单位和个人对考评机构的考评行为，有权向主管机关进行实名举报，主管机关会及时受理、组织调查处理，并为举报人保密。"]
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

(defn main-pn
  []
  (html [:h1 "hello"]))

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
    "d1" "道路旅客运输"
    "d2" "道路危险货物运输"
    "d3" "汽车客运站"
    "d4" "道路运输普通货运"
    "d5" "道路货物运输场站"
    "d6" "机动车维修"
    "s1" "水路旅客运输"
    "s2" "水路危险货物运输"
    "s3" "水路散货运输"
    "g1" "港口客运（滚装、渡船渡口）码头"
    "g2" "危险货物码头运输"
    "g3" "港口散杂货（集装箱）码头"
    "c1" "城市公共汽车客运"
    "c2" "城市轨道交通运输"
    "c3" "出租汽车"
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
   :from "地域"
   :type "类型"
   :type2 "细类"
   :grade "级别"
   :sex "性别"
   :org "单位组织"
   :workdate "参加工作日期"
   :birth "出生日期"
   :title "职称"
   :major "专业"
   :cid "证书号"
   :ctype "证书类型"
   :tel "电话"
   :date "日期"
   :death "死亡人数"
   :yyyy "年份"
   :scroe "分数"
   :edu "学历"
   :pcode "邮编"
   :mobile "手机"
   :pid "证件号"
   :uid "用户ID"
   :admin "主管机关"
   :legalp "法人代表"
   })
(def dd-province
  (let [ls (map str '(北京 上海 广东 江苏 陕西 山东 新疆 湖南 黑龙江 湖北 安徽 浙江 四川 贵州 甘肃 福建 辽宁 重庆 天津 广西 吉林 
                         海南 河北 河南 内蒙古 山西 西藏 青海 江西 云南 宁夏))]
    (apply array-map (flatten (for [e ls] [e e])))))

; 上级主管部门列表
(def dd-moc 
 (array-map
   0 "00-交通运输部"
   1 "01-北京交委"
   2 "02-天津公路局"
   3 "03-天津交通局"
   4 "04-河北交通厅"
   5 "05-山西交通厅"
   6 "06-内蒙古交通厅"
   7 "07-辽宁交通厅"
   8 "08-吉林交通厅 "
   9 "09-黑龙江交通厅"
   10 "10-上海交委"
   11 "11-江苏交通厅"
   12 "12-浙江交通厅"
   13 "13-安徽交通厅"
   14 "14-福建交通厅 "
   15 "15-江西交通厅"
   16 "16-山东交通厅"
   17 "17-河南交通厅"
   18 "18-湖北交通厅"
   19 "19-湖南交通厅"
   20 "20-广东交通厅 "
   21 "21-广西交通厅"
   22 "22-海南交通厅"
   23 "23-重庆交委"
   24 "24-四川交通厅"
   25 "25-贵州交通厅"
   26 "26-云南交通厅"
   27 "27-西藏交通厅"
   28 "28-陕西交通厅"
   29 "29-甘肃交通厅"
   30 "30-青海交通厅"
   31 "31-宁夏交通厅"
   32 "32-新疆交通厅"
   33 "33-新疆兵团交通局"
   34 "34-长江航务管理局"
   35 "35-珠江航务管理局"             
   ))

(def dd-edu 
  {
  "中专" "中专" 
  "大专" "大专" 
  "本科" "本科" 
  "研究生" "研究生" 
  "博士生" "博士生" 
  })

; 考评员申请表
(def cfg-apply-pn ; [name id {:t :title ..}] :t类型,不写时为text 
  [
   ["姓名" :name {:require true}]
   ["性别" :sex {:t ["男" "女"]}]
   ["出生年月" :birth {:t 'date :title "注意：年龄原则上不得超过65周岁；格式如1980-5"}]
   ["常住地" :from {:t dd-province}]
   ["照片" :photo {:t 'file :title "照片要求：……"}]
   ["身份证号" :pid {:t 'pid :title "15位或18位身份证"}]
   ["工作单位" :org]
   ["职称" :title]
   ["通讯地址" :address]
   ["邮  编" :pcode {:t 'pcode}]
   ["联系电话" :tel]
   ["传真号码" :fax]
   ["手机号码" :mobile]
   ["电子邮箱" :email {:t 'email}]
   ["文化程度" :edu {:t dd-edu :title "按国标（中专以上）"}]
   ["所学专业" :major {:title "注意：必须具备交通运输相关专业大学专科以上学历"}]
   ["现从事专业" :prof]
   ["相关专业从业时间" :begindate {:v 5}]   
   ["申请类别" :type {:t dd-type :v "s" :title "考评员申请的专业类型不得多于二种"}] ; 最多两类
   ["主要学习（培<br/>训）经历" :train {:t 'textarea}]
   ["主要工作简历" :resume {:t 'textarea}]
   ["专业工作业绩" :perf {:t 'file}]
   ["相关证明文件" :proof {:t 'file :title "包括身份证明、学历证明以及其他资格证的电子文档等（pdf, doc或者jpg格式）"}]
   ["主管机关" :admin {:t dd-moc :title "自选"}]
   ])
; 考评机构申请表  
(def cfg-apply-org 
  [["单位名称" :name {:require true :v "" :title "一般为：学校/交通相关学会/协会/研究所"}]
   ["法人代表" :legalp {:require true}]
   ["评审机构资质" :qual {:t dd-org-grade :v "甲类"}]
   ["专业范围" :type {:t dd-type :v "d" :title "todo: 改为可以多选，或者每个专业申请一次"}]
   ["专职考评员人数" :pnumber {:v 15}]
   ["高级技术职称考评员人数" :pnumber {:v 6}]
   ["开始从事相应业务年份" :start {:v 2005}]
   ["主管机关" :admin {:t dd-moc}]
   ["办公地址" :address]
   ["邮  编" :pcode {:t 'pcode}]
   ["单位电话" :tel]
   ["传真号码" :fax]
   ["联系人" :contact]
   ["联系人手机" :mobile]
   ["联系人邮箱" :email]
   ["单位相关证明材料" :met {:t 'file}]
   ["考评员相关材料" :pns {:t 'file :title "文件10M以内大小"}]
   ])
; 企业申请表
(def cfg-apply-en ; en-input-form 
  [
   ["企业名称" :name {:require true :v "xxx"}]
   ["申请等级" :grade {:t [:一级 :二级 :三级] :v :一级}]
   ["法人代表" :legalp]
   ["生产经营类别" :type2 {:t dd-type2 :v "s1" :title "可多选"}]
   ["主管机关" :admin {:t dd-moc :title "一级不用选（部）；二级选34个机构；三级选二级、三级机构；每个类型对应自己的主管机关"}]
   ["企业办公地址" :address {:title "选择GIS坐标"}]
   ["企业电话" :tel]
   ["企业传真" :tax]
   ["联系人" :contact]
   ["联系电话" :contact-tel]
   ["安全生产组织架构" :safe {:t 'file}]
   ["企业法人资格证件" :qual {:t 'file}] 
   ["经营许可证" :license {:t 'file}]
   ["企业安全生产工作报告" :report {:t 'file :title "Word文档"}]
   ])

(defn- input-form-
  "统一录入表单
  @cfg 表单录入项的配置
  @m 含 {:title .. :form ..} 的其他定制化参数, :title为标题，:form 为区别标识如 'en', 'pn', 'org' "
  [cfg m]
  (let [css-label "font-family:微软雅黑; font-size:14px; vertical-align:center; height: 35px; border-bottom:1px dotted gray"]
    (html
      [:form {:method "POST" :action (format "/c/esp/input-save") :enctype "multipart/form-data"}
       [:table {:align "left" :style "margin-left: 30px"}
        [:caption {:style "padding: 5px"} [:h1 (m :title)]]
        (for [[nam id {t :t v :v require :require title :title}] cfg]
          (let [sid (name id)
                m {:id sid :name sid :value v :title title}]
            [:tr 
             [:td {:style css-label} [:label (str nam "：")]]
             [:td {:style "border-bottom:1px dotted gray"}
              (cond 
                (true? require)  (eui-text     (into m {:required "true" :style "width: 250px"}))
                (= t 'textarea)  (eui-textarea m v)
                (= t 'file)      (html 
                                   (eui-text (into m {:type "hidden" :value ""}))
                                   [:span (when v (html
                                                    [:a {:href v :target "_blank"} "查看"]
                                                    (space 3)))]
                                   (eui-button {:onclick (format "fileupload('%s', '%s')" (str nam) sid)} "上传文件"))
                (= t 'email)     (eui-email    m)
                (map? t)         (eui-combo    m t)
                (vector? t)      (eui-combo    m (apply array-map (flatten (for [e t] [e e]))))
                :else            (eui-text     (into m {:style "width: 250px"})))
              ]]))
        [:tfoot [:tr [:td {:colspan 2 :align "center" :style "padding: 15px"} 
                      (eui-button {:onclick (format "esp_input_save('%s')" (m :form))} " 保 存 ") (space 5)
                      (eui-button {:onclick (format "esp_input_submit('%s')" (m :form))} "提交申请") (space 5)
                      (eui-button {:onclick "$('form').get(0).reset()"} " 取 消 ") ]]]]]
      (eui-dialog "fileupload" 
                  {:closed "true" :href "/c/esp/fileupload"} 
                  )
      [:script "fileupload_bt()"]
      )))
       
(defn fileupload
  "文件上传弹出窗口的内容
  @id 弹出窗口对应的父按钮前文件字段的id或者name"
  []
  (html 
    [:form {:name "fm_fileupload" :id "fm_fileupload" :method "POST" :action (format "/c/esp/filesave") 
            :enctype "multipart/form-data" :target "ifrm_fileupload"}
     (eui-text {:name "f_fileupload" :id "f_fileupload" :type "file"}) 
     (eui-tip "选择好文件后请按确定进行上传")
     ] 
    [:iframe {:name "ifrm_fileupload" :style "display:none"}] ))
   
(defn filesave
  "文件上传form提交后的处理：1、保存文件；2、保存文件后调用js函数"
  [request]
  (let [fu (doto (FileUpload.) (.initialize request) .upload)
        myfile (.getFile (.getFiles fu) 0)
        fname0 (.getFileName myfile)
        fsize (.getSize myfile)
        wr3user (wr3user request)
        fname1 (format "/file/%s-%s.%s" wr3user (System/currentTimeMillis) (rightback fname0 "."))
        ]
    (when (not (.isMissing myfile))
      (do
        (.saveAs myfile (.getRealPath request fname1)) 
        (html-body
          [:h1 (format "文件名: %s (大小：%s)" fname0 fsize)]
          "文件上载完成"
          [:script (format "parent.fileupload_ok('%s')" fname1)])))))

(defn en-input
  "service：企业在线填报"
  []
  (input-form- cfg-apply-en {:form "en" :title "交通运输企业安全生产标准化<font color=blue>企业</font>申请表"}))

(defn- pn-of-
  [uid]
  (with-mdb2 "esp"
    (fetch-one :pn :where {:uid uid})))

(defn- pn-apply-of-
  [uid]
  (with-mdb2 "esp"
    (vec (fetch :pn-apply :where {:uid uid}))))

(defn pn-apply
  "service: 考评员申请导航页"
  [request]
  (let [uid (wr3user request)
        r1 (pn-of- uid)
        rs2 (pn-apply-of- uid)]
    (html
      [:h1 (format "录入过的申请信息 %s 条" (if r1 1 0))]
      (if r1
        (eui-button {:href "#" :onclick "layout_load_center('/c/esp/pn-input')"}  "查看已录入的申请信息")
        (eui-button {:href "#" :onclick "layout_load_center('/c/esp/pn-input')"} "初次申请考评证书"))
      (when rs2
        (html
          [:h1 (format "已提交过%s次申请：" (count rs2))]
          (for [r rs2] 
            (html 
              (eui-button {:href (format "/c/esp/pn-apply-view/%s" (:_id r)) :target "_blank"} (format "查看%s的申请" (:date r))) (space 5))))))))

(defn pn-input
  "service：评审人员在线申请"
  [request]
  (let [r (pn-of- (wr3user request))
        cfg (if r
              (for [[n id m] cfg-apply-pn] [n id (merge {:v (id r)} m)])
              cfg-apply-pn)]
    (input-form- cfg {:form "pn" :title "交通运输企业安全生产标准化<font color=blue>考评员</font>申请表"})))

(defn org-input
  "service：评审机构在线申请"
  []
  (input-form- cfg-apply-org {:form "org" :title "交通运输企业安全生产标准化<font color=blue>考评机构</font>申请表"}))

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

(defn input-save
  "service: 考评员，考评机构，企业申请表保存
  @id form名称如'pn' 'en' 'org' "
  [id request]
  (let [vars (query-vars request)
        uid (wr3user request)
        tb (keyword id)
        m (into {:uid uid} (for [[k v] vars :when (not (nullity? v))] [k v]))]
    (with-mdb "esp" 
      (update! tb {:uid uid} m))
    (str "已保存 " (vars "name") " 的申请。")))

(defn input-submit
  "service: 考评员，考评机构，企业申请表提交"
  [id request]
  (let [vars (query-vars request)
        uid (wr3user request)
        tb1 (keyword id)
        tb2 (keyword (str id "-apply"))
        m (into {:uid uid} (for [[k v] vars :when (not (nullity? v))] [k v]))]
    (with-mdb "esp" 
      (update! tb1 {:uid uid} m) ; 保存到基本信息表
      (insert! tb2 (into m {:date (datetime)}))) ; 保存到申请表
    (str "已提交 " (vars "name") " 的申请。")))


(defn- data-
  "取出数据表所有记录
  @tb :pn | :en | :org"
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
  "service: 考评机构列表"
  [id]
  (let [tb :org
        rt (if id (search- tb id) (data- tb))]
    (html
      [:h1 (format "考评机构列表（%s 名）" (count rt))]
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
                   :admin (or (dd-moc v) v)
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
  
(defn pn-process
  "service: 申请进度查询"
  [request]
  (let [wr3user (session request "wr3user")
        r (with-mdb2 "esp" (fetch-one :pn :where {:uid "pn1"}))
        oid (:_id r)]
    (if (= wr3user "pn1")
      (eui-button {:href (str "/c/esp/pn-form/" (str oid)) :target "_blank"} "查看已有申请记录")
      "尚无申请记录" )))

(defn pn-learn
  "service: 考评员培训、考试查询"
  []
  (html
    [:h1 "尚无培训记录和考试成绩。"]
    [:h2 "1、首次培训情况（时间不少于24个学时）；"]
    [:h2 "2、年度继续教育情况（时间不少于8个学时）；"]
    ))

(defn pn-renew
  "service: 换证申请"
  []
  (html
    [:h1 "换证申请原因："]
    [:h2 "1、考评员资格证5年有效期慢提前3个月申请换证"] (space 6) 
    (eui-button {:href "#" :onclick "layout_load_center('/c/esp/pn-input')"} "申请")
    [:h2 "2、跨管辖范围流动申请换发新证书"] (space 6) 
    (eui-button {:href "#" :onclick "layout_load_center('/c/esp/pn-input')"} "申请")
    ))
  
(defn pn-olap
  "service: 考评员分析"
  []
  (let [rs (data- :pn)
        region (for [r rs] (-> r :from (subs 0 2)))
        rt (apply array-map (flatten (for [[v vv] (group-by #(str %) region)] [v (count vv)])))]
    (barf rt {:title "考评员地域分布情况" :x "省份" :y "考评员人数"})
    ))

(defn pn-archive
  "service: 考评机构的考评员档案管理"
  []
  (html
    [:h1 "考评员档案管理"]
    (eui-button {:plain "true" :iconCls "icon-sum"} "安全生产标准化考评员汇总表、登记表") [:br]
    (eui-button {:plain "true" :iconCls "icon-list"} "考评员学历和专业技术能力证明汇总表") [:br]
    (eui-button {:plain "true" :iconCls "icon-file"} "考评员培训合格证书汇总表") [:br]
    (eui-button {:plain "true" :iconCls "icon-file"} "考评员培训情况汇总表") [:br]
    ))

(defn pn-apply-view
  "app: 查看申请记录
  @id object-id"
  [id]
  (form- :pn-apply id "考评员"))

(defn en-backup
  "service: 企业变更申请"
  []
  (html
    [:h1 "管理办法规定："]
    [:h2 "企业法人代表、名称、地址等变更的，应在变更后1个月内，向相应的主管机关提供有关材料，"
     "申请对企业安全生产标准化达标证书的变更。"]
    (eui-button {:href "#" :onclick "layout_load_center('/c/esp/en-input')"} "填写变更信息")))

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
      (html
      [:table.wr3table {:border 1}
       [:caption (format "%s企业安全生产达标标准" (dd-type2 type2))]
       [:thead 
        [:tr [:th "考核内容"] [:th {:colspan 2} "考核要点"] [:th "分数"] [:th "自评分"] ]]
       [:tbody 
        (for [r rt1] (f1 r))]
       [:tfoot
        [:tr [:td {:colspan 5} [:h2 "注：打 “ <font color=red>★</font> ” 的为必备项，必须完全满足。"]]]]
       ] 
      [:br]
      "自评总分：" [:input {:value 900}]
      "自评报告："[:input {:type "file"}]))))

(defn org-backup
  "service: "
  []
  (html
    [:h1 "申请变更备案"]
    [:h2 "（一）法定代表人变更"]
    [:h2 "（二）停业、破产或有其他原因终止业务"]
    [:h2 "（三）考评员发生重大变化"]))
  
(defn org-pn
  "service: 本考评机构机构的所有考评员"
  [request]
  (let [uid (wr3user request)
        rt (with-mdb2 "esp"
             (vec (fetch :pn :where {:belong uid})))]
    (html
      [:h1 (format "本机构当前专兼职考评人员 %s 名（改成：姓名、证书类别、证书编号、专兼职、已解聘）" (count rt))]
      [:h2 "说明：在职的排在前面，解聘的放后面；详情中增加聘用、解聘时间"]
      (result-html- rt '[姓名 单位 属地 详情] [:name :org :from :_id] {:form "pn-form"}))))

(defn t10
  ""
  [request]
;  (println "hello world") ; (.getRealPath request "/"))
  (html
    "hello"
    [:pre (.getRealPath request (format "/file/%s.txt" (System/currentTimeMillis)))]))

;;-------- test
(def m [
])

(defn- t1
  "造org表的数据字段" []
  (with-mdb2 "esp"
    (let [rs (fetch :org)
          f3x (str "王李张刘陈杨黄赵吴周徐孙马朱胡郭何高林罗郑梁谢宋唐许韩冯邓曹彭曾肖"
                   "田董袁潘于蒋蔡余杜叶程苏魏吕丁任沈姚卢姜崔鍾谭陆汪范金石廖贾夏韦傅方白邹孟熊秦邱江尹薛闫段雷侯龙"
                   "史陶黎贺顾毛郝龚邵万钱严覃武戴莫孔向汤")
          name3 (fn [] (str (rand-nth f3x) (rand-nth f3x) (when (zero? (rand-int 2)) (rand-nth f3x))))
          ]
      (doseq [r rs]
        (let [m {:_id ""
                 :name ""
                 :legalp (name3)
                 :admin 13
                 :mobile (format "13%s" (apply str (random-n 9 9)))
                 }]
          (update! :org r (dissoc (into m r) :legelp))
          ))) ))
  
;;;; test
;(with-mdb2 "esp"
;  (destroy! :org {:name #"分局"})
;  (let [rs (fetch :org :where {:name #"分局"})]
;    )
