(ns wr3.clj.app.espconf)

(use 'hiccup.core)
(use 'wr3.clj.web)
(require '[wr3.clj.app.espreg :as espreg])

;; 交通运输主管部门界面配置
(def cfg-frame-mot
  {:name "esp"
   :style (map #(str % "") ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——主管机关管理系统（试行）"
   :searcher ["张" ; default-value
              ["考评员搜索" "range_pn" "icon-user"] ; label name icon
              ["考评机构搜索" "range_org" "icon-earth"]
              ["企业搜索" "range_en" "icon-star"] ]
   :nav [
         ["待办事宜" "icon-arrow" ; title icon
          ["待办事宜概况一览" "icon-sum" "mot-resp-sum"]
          ["考评员申请受理" "icon-user"     "apply-resp/pn"] ; title icon id 
          ["考评员换证申请受理" "icon-user" "cert-renew-resp/pn"]  
          ["考评机构申请受理" "icon-earth"    "apply-resp/org"]  
          ["考评机构变更备案受理" "icon-earth" "backup-resp/org"] 
          ["考评机构换证申请受理" "icon-earth"    "cert-renew-resp/org"] 
          ["企业初次申请受理" "icon-star"    "apply-resp/en"]  
          ["企业考评结论审核" "icon-star"    "mot-en-review"]  
          ["企业变更备案申请受理" "icon-star"    "backup-resp/en"]  
          ["企业换证申请受理" "icon-star"    "cert-renew-resp/en"]  
          ["投诉举报受理" "icon-tip"    "mot-hot"]  
          ]
         ["考评员" "icon-user" ; title id
          ["考评员列表" "icon-list"    "pn-list"] ; title icon id 
          ["资格证书制发" "icon-list"    "cert-resp/pn"] ; title icon id 
          ["考评员培训、考试" "icon-list"    "mot-pn-train"] ; title icon id 
          ["考评员资格撤销" "icon-list"    "cert-cancel/pn"] ; title icon id 
          ["考评员统计查询" "icon-bar"    "mot-olap/pn"] ; title icon id 
          ["考评员考试统计" "icon-list"    "mot-pn-exam"] ; title icon id 
          ]
         ["考评机构" "icon-earth" ; title id
          ["考评机构列表" "icon-list"  "org-list"] ; title icon id 
          ["资质证书制发" "icon-list"    "cert-resp/org"] ; title icon id 
          ["问题整改" "icon-list"    "mot-org-refine"] ; title icon id 
          ["资质撤销" "icon-list"    "cert-cancel/org"] ; title icon id 
          ["年度工作报告"          "icon-list"    "report-view/org"] ; title icon id 
          ["考评机构统计查询" "icon-bar"    "mot-olap/org"] ; title icon id 
          ["考评情况汇总表"          "icon-list"    "mot-org-eval"] ; title icon id 
          ]
         ["交通运输企业"           "icon-star" ; title id
          ["企业列表" "icon-list"  "en-list"] ; title icon id 
          ["已达标企业" "icon-list"  "mot-en-passed"] ; title icon id 
          ["企业统计查询" "icon-bar"  "mot-olap/en"] ; title icon id 
          ["附加考评" "icon-list"    "mot-en-recheck"] ; title icon id 
          ["达标证书撤销" "icon-list"    "cert-cancel/en"] ; title icon id 
          ["年度工作报告"          "icon-list"    "report-view/en"] ; title icon id 
          ]
         ["下级机构管理"           "icon-pen" ; title id
          ["统计分析"          "icon-list"    "mot-sub-olap"] ; title icon id 
          ["机构维护"          "icon-list"    "mot-admin"] ; title icon id 
          ["委托代办"          "icon-list"    "mot-give"] ; title icon id 
          ]
         ["系统管理及帮助"     "icon-search"
          ["装载更新配置"     "icon-search" "reload" "/c/console/reload/app.espconf"]
          ["网站样式"          "icon-search" "site_bt"]
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ]   
   :frame-main (html [:script "layout_load_center('/c/esp/mot-resp-sum')"]
                     espreg/bjca-on-change
                     (set-title "主管机关管理系统（试行）"))
   })

;; 考评员界面配置
(def cfg-frame-pn
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——考评员在线申请系统（试行）"
   :nav [
         ["考评员" "icon-user" ; title id
          ["申请考评证书" "icon-list"    "pn-apply"] ; title icon id url 
          ["培训、考试情况" "icon-list"  "pn-learn"] 
          ["换证申请" "icon-list"  "cert-renew/pn"] 
          ["使用帮助" "icon-help" "pn-help" "/static/esp/about-pn.html"]
          ]
         ] 
   :frame-main (html [:script "layout_load_center('/static/esp/about-pn.html')"]
                     espreg/bjca-on-change
                     (set-title "考评员在线申请系统（试行）"))
   })

;; 考评机构界面配置
(def cfg-frame-org
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——考评机构管理系统（试行）"
   :nav [
         ["考评机构" "icon-earth" ; title id
          ["申请资质证书" "icon-list"    "org-apply"] ; title icon id 
          ["申请变更备案" "icon-list"    "backup/org"] ; title icon id 
          ["申请换证" "icon-list"    "cert-renew/org"] 
          ["已认定的资质证书" "icon-list"    "org-cert"] 
          ["年度工作报告" "icon-list" "report/org"] ; title icon id 
          ["问题整改" "icon-list" "org-refine"] ; title icon id 
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ["管理考评员" "icon-user" ; title id
          ["本机构考评员列表" "icon-list"    "org-pn"] ; title icon id 
          ["本机构考评员培训考试" "icon-list"    "org-pn-train"] 
          ["考评员档案管理" "icon-list"    "org-pn-archive"] 
          ]
         ["企业考评管理" "icon-pen" ; title id
          ["企业考评待办工作" "icon-list" "apply-resp/en"] ; title icon id 
          ["企业达标等级证书制发" "icon-list" "cert-resp/en"] ; title icon id 
          ["工作进度查询" "icon-list" "org-en-process"] 
          ["考评企业档案管理" "icon-list" "org-en-archive"] 
          ["考评情况汇总表" "icon-list" "org-en-eval"] 
          ]
         ]   
   :frame-main (html [:h2 "考评机构用户主界面"]
                     espreg/bjca-on-change
                     (set-title "考评机构管理系统（试行）"))
   })

;; 交通运输企业界面配置
(def cfg-frame-en
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——企业在线填报管理系统（试行）"
   :nav [
         ["企业申请" "icon-star" ; title id
          ["1、达标自评" "icon-list"    "en-stand"] 
          ["2、在线填报" "icon-list"    "en-apply"] 
          ["3、选择考评机构" "icon-list" "en-select-org"] 
          ["进度查询" "icon-list"    "en-process"] 
          ["企业年度工作报告" "icon-list"  "report/en"] 
          ["申请变更" "icon-list"    "backup/en"] 
          ["申请换证" "icon-list"    "cert-renew/en"] 
          ]
         ]   
   :frame-main (html [:h2 "交通运输企业用户主界面"]
                     espreg/bjca-on-change
                     (set-title "企业在线填报管理系统（试行）"))
   })

; 主界面子系统菜单
(def cfg-subsys
  [
   ["考评员管理系统" "pn" "考评人员在线申请"]
   ["考评机构管理系统" "org" "考评机构在线申请、评定管理"]
   ["企业在线填报管理系统" "en" "企业在线填报管理"]
   ["交通运输主管机关管理系统" "mot" "交通运输管理部门（交通部、厅委局）内部管理"]
   ])

;;------------------- 
; 申请类别 1d 道路运输、2s 水路运输、3g 港口码头、4c 城市客运、5j 交通运输工程建设
(def dd-type-map {"d" 1 "s" 2 "g" 3 "c" 4 "j" 5}) 
; 业务大类
(def dd-type 
  {
   1 "道路运输" 
   2 "水路运输" 
   3 "港口码头" 
   4 "城市客运" 
   5 "交通运输工程建设"
   6 "其他" ; add jiayuan's req (2012-8-13)
   })
; 业务细类
(def dd-type2 
  (array-map
    11 "道路旅客运输"
    12 "道路危险货物运输"
    13 "汽车客运站"
    14 "道路运输普通货运"
    15 "道路货物运输场站"
    16 "机动车维修"
    21 "水路旅客运输"
    22 "水路危险货物运输"
    23 "水路散货运输"
    31 "港口客运（滚装、渡船渡口）码头"
    32 "危险货物码头运输"
    33 "港口散杂货（集装箱）码头"
    41 "城市公共汽车客运"
    42 "城市轨道交通运输"
    43 "出租汽车"
    51 "交通运输建筑施工"
   ))
; 撤销考核员资格原因
(def dd-reason
  {
   1 "（一）隐瞒企业重大安全问题的；"
   2 "（二）考评结论严重失实的；"
   3 "（三）泄露企业技术和商业秘密的；"
   4 "（四）收受企业财物或者为企业谋取不正当利益的；"
   5 "（五）不服从主管机关监督管理的；"
   6 "（六）资格证逾期不申请换证的；"
   7 "（七）其他不能胜任考评工作的。" 
   })
; 考评员换证原因
(def dd-renew
  {
   1 "1、考评员资格证书到期"
   2 "2、户籍所在地或常住地发生省际间变更"
   })
; 对企业附加考评的原因
(def dd-recheck 
  {
   1 "（一）企业发生重大及以上安全责任事故；"
   2 "（二）企业一年内连续发生二次及以上较大安全责任事故；"
   3 "（三）企业被举报并经核实其安全生产管理存在重大安全问题；"
   4 "（四）企业发生其他可能影响其安全生产管理的重大事件或主管机关认为确实必要的。" 
   })
; 考评机构变更备案
(def dd-org-backup
  {
   1 "（一）机构名称和法定代表人变更的"
   2 "（二）停业、破产或有其他原因终止业务的"
   3 "（三）从事专职管理和考评工作的人员发生重大变化的"
   })
; 企业变更备案
(def dd-en-backup
  {
   1 "企业法人代表变更的"
   2 "企业名称变更的"
   3 "企业地址变更的"
   })
; 企业申请达标级别对应的分数要求
(def dd-score
  {
   "一级" 900 ; >=900
   "二级" 700 ; >=700
   "三级" 600 ; >=600
   })
; 企业达标自评提示
(def dd-stand-tip
  {
   1 "一级企业必须完全满足所有标<font color=red>★、★★、★★★</font>的项"
   2 "二级企业必须完全满足所有标<font color=red>★★、★★★</font>的项"
	 3 "三级企业必须完全满足所有标<font color=red>★★★</font>的项"
  })
; 企业达标等级、考评机构资格资格
(def dd-grade
  {
   1 "一级"
   2 "二级"
   3 "三级"
   })
; 证书名称
(def dd-cert
  {
   :pn "考评员资格证"
   :org "考评机构资质证书"
   :en "企业达标等级证书"
   })
; 表单名称，用于doc-标题等
(def dd-form
  {
   :pn "考评员"
   :org "考评机构"
   :en "交通运输企业"
   :pn-apply "考评员申请"
   :org-apply "考评机构申请"
   :en-apply "交通运输企业申请"
   :org-backup "考评机构变更申请"
   :en-backup "企业变更申请"
   :pn-train "考评员培训考试记录"
   :hot "实名举报信息"
   })
; 证书年限
(def dd-cert-year
  {
   :pn 5 
   :org 5 
   :en 3
   })
; 各省名称，@deprecated
(def dd-province
  (let [ls (map str '(北京 上海 广东 江苏 陕西 山东 新疆 湖南 黑龙江 湖北 安徽 浙江 四川 贵州 甘肃 福建 辽宁 重庆 天津 广西 吉林 
                         海南 河北 河南 内蒙古 山西 西藏 青海 江西 云南 宁夏))]
    (apply array-map (flatten (for [e ls] [e e])))))
; 交通运输主管部门
(def dd-admin 
  (array-map
    "01" "交通运输部"
    "02" "北京市"
    "03" "天津市"
    "04" "河北省"
    "05" "山西省"
    "06" "内蒙古自治区"
    "07" "辽宁省"
    "08" "吉林省"
    "09" "黑龙江省"
    "10" "上海市"
    "11" "江苏省"
    "12" "浙江省"
    "13" "安徽省"
    "14" "福建省"
    "15" "江西省"
    "16" "山东省"
    "17" "河南省"
    "18" "湖北省"
    "19" "湖南省"
    "20" "广东省"
    "21" "海南省"
    "22" "广西壮族自治区"
    "23" "重庆市"
    "24" "四川省"
    "25" "贵州省"
    "26" "云南省"
    "27" "西藏自治区"
    "28" "陕西省"
    "29" "甘肃省"
    "30" "青海省"
    "31" "宁夏回族自治区"
    "32" "新疆维吾尔自治区"
    "33" "新疆生产建设兵团"
    "34" "长江航务管理局"
    "35" "珠江航务管理局"
    ))
; 学历
(def dd-edu 
  {
  "中专" "中专" 
  "大专" "大专" 
  "本科" "本科" 
  "研究生" "研究生" 
  "博士生" "博士生" 
  })

;; 考评员资格证书、考评机构资质证书、企业达标证书申请表格
; 考评员申请表
(def cfg-apply-pn ; [name id {:t :title ..}] :t类型,不写时为text 
  [
   ["姓名" :name {:require true}]
   ["身份证号" :pid {:t 'pid :require true :title "15位或18位身份证"}]
   ["常住地" :from {:t dd-province :v "福建" :require true}]
   ["照片" :photo {:t 'file :title "照片文件请勿超过10M大小" :require true}]
   ["工作单位" :org {:require true}]
   ["职称" :title]
   ["通讯地址" :address {:require true}]
   ["邮编" :pcode {:t 'pcode :require true}]
   ["联系电话" :tel]
   ["传真号码" :fax]
   ["手机号码" :mobile {:require true}]
   ["电子邮箱" :email {:t 'email :require true}]
   ["文化程度" :edu {:t dd-edu :title "按国标（中专以上）" :require true}]
   ["所学专业" :major {:title "注意：必须具备交通运输相关专业大学专科以上学历" :require true}]
   ["现从事专业" :prof {:require true}]
   ["相关专业从业年份" :begindate {:v 5 :require true}]   
   ["申请专业类型（不得多于两种）" :type {:t dd-type :v 1 :title "考评员申请的专业类型不得多于二种"}] ; 最多两类
   ["主要学习（培训）经历" :train {:t 'textarea :require true}]
   ["主要工作简历" :resume {:t 'textarea :require true}]
   ["专业工作业绩" :perf {:t 'textarea :require true :title "不得少于10个字"}]
   ["专业工作业绩附件" :perf2 {:t 'file}]
   ["相关证明文件（身份证）" :proof {:t 'file :title "二代身份证正反面（pdf, doc或者jpg格式）" :require true}]
   ["相关证明文件（学历证书）" :proof2 {:t 'file :title "学历证书（pdf, doc或者jpg格式）" :require true}]
   ["相关证明文件（其他证书）" :proof3 {:t 'file :title "其他各类培训合格证明的照片、编号页、发证机关印章页（pdf, doc或者jpg格式）"}]
   ["主管机关" :admin {:t (dissoc dd-admin "01") :title "请自选主管机关"}]
   ["换证原因<b>（仅换证申请）</b>" :renew {:t dd-renew}]
   ["继续教育证明<b>（仅换证申请）</b>" :edu2 {:t 'file}]
   ["工作业绩证明<b>（仅换证申请）</b>" :orgproof {:t 'file :title "由所在考评机构出具的工作业绩证明。"}]
   ])
; 考评机构申请表  
(def cfg-apply-org 
  [
   ["单位名称" :name {:require true :v "" :title "一般为：学校/交通相关学会/协会/研究所"}]
   ["组织机构代码" :orgcode {:require true}]
   ["法人代表" :legalp {:require true}]
   ["资质等级" :grade {:t dd-grade :v 1}]
   ["专业范围" :type {:t dd-type :v 1 :title "todo: 改为可以多选，或者每个专业申请一次"}]
   ["专职考评员人数" :pnumber {:v 7 :title "一级≥7名；二级≥5名；三级≥3名。"}]
   ["高级技术职称考评员人数" :pnumber2 {:v 3 :title "一级≥3名；二级≥2名；三级≥1名。"}]
   ["开始从事相应业务年份" :start {:v 2005}]
   ["主管机关" :admin {:t dd-admin}]
   ["办公地址" :address]
   ["邮编" :pcode {:t 'pcode}]
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
   ["申请等级" :grade {:t dd-grade :v 1}]
   ["法人代表" :legalp]
   ["生产经营类型" :type {:t dd-type :v 1}]
   ["生产经营类别" :type2 {:t dd-type2 :v 11}]
   ["主管机关" :admin {:t dd-admin :title "一级不用选（部）；二级选34个机构；三级选二级、三级机构；每个类型对应自己的主管机关"}]
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
;---------------- end

(defn- cfg-meta [cfg] (into {} (for [[v k & _] cfg] [k v])))
; 数据项英文、中文对照
(def ^{:doc "减少dd-meta和cfg-apply-pn|org|en中的重复定义"} 
      dd-meta
  (merge
    (cfg-meta cfg-apply-pn)
    (cfg-meta cfg-apply-org)
    (cfg-meta cfg-apply-en)
    {
     :_cdate-end "证书到期日"
     :_id "详情"
     :_issue "证书"
     :_select "选择"
     :advice "处理意见"
     :advice-eval "考评意见"
     :advice-review "审核意见"
     :belong "所属考评机构"
     :birth "出生日期"
     :cdate "发证时间"
     :cid "证书号"
     :content "内容"
     :contract0 "聘用日期"
     :contract1 "解聘日期"
     :ctype "证书类型"
     :date "日期"
     :death "死亡人数"
     :enid "企业ID"
     :exam-date "考试日期"
     :exam-score "考试分数"
     :fulltime "专兼职"
     :grade "等级"
     :info "举报人信息"
     :mobile "手机"
     :name "名称"
     :org "单位组织"
     :orgid "选择的2个考评机构" ; en预选
     :orgid1 "指定的考评机构" ; mot指定
     :pass-direct "直接颁发"
     :pn "考评员"
     :pnids "选择的考评员"
     :province "省份"
     :reason "原因"
     :resp "受理结果"
     :resp-eval "考评结果"
     :resp-review "审核结果"
     :respdate "受理日期"
     :respdate-eval "考评日期"
     :respdate-review "审核日期"
     :score "分数"
     :score0 "自评分数"
     :score1 "考评分数"
     :sex "性别"
     :stand "达标评估"
     :stop "终止业务"
     :tel "电话"
     :train-end "培训结束日期"
     :train-hour "培训学时"
     :train-id "培训合格证号"
     :train-start "培训开始日期"
     :type "业务类型"
     :type2 "业务类别"
     :uid "用户ID"
     :workdate "参加工作日期"
     :yyyy "年份"
     ;-- espfj
     :titlefile "职称证明文件"
     :beginfile "相关专业从业年份证明文件"
     :proofmobile "证明人联系电话"
     :proofname "证明人"
     :prooforg "证明单位"
     }))

;(use 'wr3.clj.s)
;(count dd-meta) ; 76