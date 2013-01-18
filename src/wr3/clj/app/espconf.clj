(ns wr3.clj.app.espconf)

(use 'hiccup.core)
(use 'wr3.clj.web)

;; 登录模式： :ca-local ca本地认证 :ca-server ca服务器认证 :user-pass 用户名密码认证 
(def login-mode (second [:ca-local :ca-server :user-pass]))

; 证书申请应用的url. @deprecated 该url以后不用了，从esprep/ca的主页面上移除
(def userregister-url "http://219.141.223.141:8080/userregister/firstpage.html")

; bjca证书验证返回值代表的含义
(def dd-retValue {-1 "登录证书的根不被信任"
                  -2 "登录证书超过有效期（<a href='http://help.bjca.org.cn/support/client/jtb/index.htm' target='_blank'>更新</a>）"
                  -3 "登录证书为作废证书" 
                  -4 "登录证书被临时冻结" })

;; bjca新版所需2个.js文件
(def bjca-js2 ["bjca-date.js" "bjca-XTXApp.js"])
;; frame所需的所有.js文件
(def frame-js (into ["app-esp.js"] bjca-js2))

; ie浏览器会调用该内容；非ie浏览器解释不了如下内容，直接执行 bjca-XTXAPP.js 中的OnUsbKeyChange函数，但也是同样的内容。
(def bjca-onchange
  "<SCRIPT LANGUAGE=javascript event=OnUsbKeyChange for=XTXAPP> OnUsbKeyChange() </SCRIPT>")

;; 第一行用于ie浏览器，第二行用于非ie浏览器。用于各子系统主页
(def bjca-onpull
  "<SCRIPT LANGUAGE=javascript event=OnUsbKeyChange for=XTXAPP> esp_bjca_onpull(); </SCRIPT>
   <script language=javascript> function OnUsbKeyChange() { esp_bjca_onpull() } </script> ")

; 登录页面的提示
(def bjca-prompt "提示：请插入本系统专用登录认证U盘。" )

;; 交通运输主管部门界面配置
(def cfg-frame-mot
  {:name "esp"
   ; :style (map #(str % "") ["layout_north" "layout_title"])  ; "" 或者 "1"
   :style ["layout_north_mot" "layout_title_esp"]
   :title "交通运输企业安全生产标准化——主管机关管理系统（试行）"
   :searcher ["张" ; default-value
              ["考评员搜索" "range_pn" "icon-user"] ; label name icon
              ["考评机构搜索" "range_org" "icon-earth"]
              ["企业搜索" "range_en" "icon-star"] ]
   :nav [
         ["待办事宜" "icon-arrow" ; title icon
          ["待办事宜概况一览"     "icon-sum"   "mot-resp-sum"]
          ["报名申请初审"         "icon-edit"  "reg-resp"]
          ["考评员申请受理"       "icon-user"  "apply-resp/pn"        nil "m01"] ; title icon id url menu-id 
          ["考评员换证申请受理"   "icon-user"  "cert-renew-resp/pn"   ]  
          ["考评机构申请受理"     "icon-earth" "apply-resp/org"       nil "m03"]  
          ["考评机构变更备案受理" "icon-earth" "backup-resp/org"      ] 
          ["考评机构换证申请受理" "icon-earth" "cert-renew-resp/org"  ] 
          ["企业初次申请受理"     "icon-star"  "apply-resp/en"        nil "m06"]  
          ["企业考评结论审核"     "icon-star"  "mot-en-review"        ]  
          ["企业变更备案申请受理" "icon-star"  "backup-resp/en"]  
          ["企业换证申请受理"     "icon-star"  "cert-renew-resp/en"]  
          ["投诉举报受理"         "icon-tip"   "mot-hot"              nil "m0a"]  
          ]
         ["考评员管理" "icon-user" ; title id
          ["考评员列表" "icon-list"    "pn-list"] ; title icon id 
          ["资格证书制发" "icon-list"    "cert-resp/pn"] ; title icon id 
          ["考评员培训、考试" "icon-list"    "mot-pn-train"] ; title icon id 
          ["考评员资格撤销" "icon-list"    "cert-cancel/pn"] ; title icon id 
          ["考评员统计查询" "icon-bar"    "mot-olap/pn"] ; title icon id 
          ["考评员考试统计" "icon-list"    "mot-pn-exam"] ; title icon id 
          ]
         ["考评机构管理" "icon-earth" ; title id
          ["考评机构列表" "icon-list"  "org-list"] ; title icon id 
          ["资质证书制发" "icon-list"    "cert-resp/org"] ; title icon id 
          ["问题整改" "icon-list"    "mot-org-refine"] ; title icon id 
          ["资质撤销" "icon-list"    "cert-cancel/org"] ; title icon id 
          ["年度工作报告"          "icon-list"    "report-view/org"] ; title icon id 
          ["考评机构统计查询" "icon-bar"    "mot-olap/org"] ; title icon id 
          ["考评情况汇总表"          "icon-list"    "mot-org-eval"] ; title icon id 
          ]
         ["交通运输企业管理"           "icon-star" ; title id
          ["企业列表" "icon-list"  "en-list"] ; title icon id 
          ["已达标企业" "icon-list"  "mot-en-passed"] ; title icon id 
          ["企业统计查询" "icon-bar"  "mot-olap/en"] ; title icon id 
          ["问题整改" "icon-list"    "mot-en-refine"] ; title icon id 
          ["附加考评" "icon-list"    "mot-en-recheck"] ; title icon id 
          ["达标证书撤销" "icon-list"    "cert-cancel/en"] ; title icon id 
          ["年度工作报告"          "icon-list"    "report-view/en"] ; title icon id 
          ]
         ["下级机构管理"           "icon-list" ; title id
          ["机构维护"          "icon-list"    "mot-admin"] ; title icon id 
          ["工作委托"          "icon-list"    "mot-fn"] ; title icon id 
          ["下级机构统计分析"          "icon-list"    "mot-sub-olap"] ; title icon id 
          ]
         ["查询及统计分析"           "icon-pie" ; title id
          ["企业统计查询" "icon-bar"  "mot-olap/en"] ; title icon id 
          ["考评机构统计查询" "icon-bar"    "mot-olap/org"] ; title icon id 
          ["考评员统计查询" "icon-bar"    "mot-olap/pn"] ; title icon id 
          ["下级机构统计分析"          "icon-list"    "mot-sub-olap"] ; title icon id 
          ]
         ["系统管理"     "icon-setting"
          ["首页内容维护"     "icon-file" "mot-portal"]
          ["登录日志查询"     "icon-text" "mot-log"]
          ["密钥用户管理"     "icon-user" "mot-user-admin"]
          ["当前用户信息"     "icon-info" "whoami/mot"]
          ]
         ]   
   :frame-main (html [:script "layout_load_center('/c/esp/mot-resp-sum')"]
                     (set-title "主管机关管理系统（试行）"))
   :js frame-js
   :after bjca-onpull
   })

;; 考评员界面配置
(def cfg-frame-pn
  {:name "esp"
   ; :style (map #(str % "") ["layout_north" "layout_title"])  ; "" 或者 "1"
   :style ["layout_north_pn" "layout_title_esp"]
   :title "交通运输企业安全生产标准化——考评员在线申请系统（试行）"
   :nav [
         ["考评员" "icon-user" ; title id
          ["资格申请" "icon-list"    "pn-apply"] ; title icon id url 
          ["培训、考试情况" "icon-list"  "pn-learn"] 
          ["换证申请" "icon-list"  "cert-renew/pn"] 
          ["签约考评机构" "icon-list"  "pn-org"]
          ["使用帮助" "icon-help" "pn-help" "/static/esp/about-pn.html"]
          ]
         ["其他" "icon-setting" ; title id          
          ["考评机构基本信息查询"     "icon-info" "who/org"]
          ["企业基本信息查询"     "icon-info" "who/en"]
          ["当前用户信息"     "icon-info" "whoami/pn"]
          ]      
         ] 
   :frame-main (html [:h2 "考评员用户主界面"]
                     [:script "layout_load_center('/static/esp/about-pn.html')"]
                     (set-title "考评员在线申请系统（试行）"))
   :js frame-js
   :after bjca-onpull
   })

;; 考评机构界面配置
(def cfg-frame-org
  {:name "esp"
   ; :style (map #(str % "") ["layout_north" "layout_title"])  ; "" 或者 "1"
   :style ["layout_north_org" "layout_title_esp"]
   :title "交通运输企业安全生产标准化——考评机构管理系统（试行）"
   :nav [
         ["待办事宜" "icon-arrow" ; title icon
          ["企业考评待办工作" "icon-list" "apply-resp/en"] ; title icon id 
          ["整改报告" "icon-list" "refine-resp"] ; title icon id 
          ]
         ["考评机构工作" "icon-earth" ; title id
          ["资质申请" "icon-list"    "org-apply"] ; title icon id 
          ["变更申请" "icon-list"    "backup/org"] ; title icon id 
          ["换证申请" "icon-list"    "cert-renew/org"] 
          ["资质证书信息" "icon-list"    "org-cert"] 
          ["年度工作报告" "icon-list" "report/org"] ; title icon id 
          ]
         ["考评员管理" "icon-user" ; title id
          ["考评员列表" "icon-list"    "org-pn"] ; title icon id 
          ["考评员培训考试" "icon-list"    "org-pn-train"] 
          ["考评员档案管理" "icon-list"    "org-pn-archive"] 
          ]
         ["达标企业管理" "icon-pen" ; title id
          ["企业达标等级证书制发" "icon-list" "cert-resp/en"] ; title icon id 
          ["工作进度查询" "icon-list" "org-en-process"] 
          ["考评企业档案管理" "icon-list" "org-en-archive"] 
          ["考评情况汇总表" "icon-list" "org-en-eval"] 
          ]
         ["其他" "icon-setting" ; title id          
          ["考评员信息查询"     "icon-info" "who/pn"]
          ["企业基本信息查询"     "icon-info" "who/en"]
          ["当前用户信息"     "icon-info" "whoami/org"]
          ]      
         ]   
   :frame-main (html [:h2 "考评机构用户主界面"]
                     [:script "layout_load_center('/c/esp/org-main')"] ; '/c/esp/apply-resp/en'
                     (set-title "考评机构管理系统（试行）"))
   :js frame-js
   :after bjca-onpull
   })

;; 交通运输企业界面配置
(def cfg-frame-en
  {:name "esp"
   ; :style (map #(str % "") ["layout_north" "layout_title"])  ; "" 或者 "1"
   :style ["layout_north_en" "layout_title_esp"]
   :title "交通运输企业安全生产标准化——企业在线填报管理系统（试行）"
   :nav [
         ["企业申请" "icon-star" ; title id
          ["1、达标自评" "icon-list"    "en-stand"] 
          ["2、等级申请" "icon-list"    "en-apply"] 
          ["3、选择考评机构（拟去除）" "icon-list" "en-select-org"] 
          ["进度查询" "icon-list"    "en-process"] 
          ["企业年度工作报告" "icon-list"  "report/en"] 
          ["变更申请" "icon-list"    "backup/en"] 
          ["换证申请" "icon-list"    "cert-renew/en"] 
          ["整改报告" "icon-list" "refine-resp"] ; title icon id 
          ]
         ["其他" "icon-setting" ; title id
          ["考评员基本信息查询"     "icon-info" "who/pn"]
          ["考评机构基本信息查询"     "icon-info" "who/org"]
          ["当前用户信息"     "icon-info" "whoami/en"]
          ]      
         ]   
   :frame-main (html [:h2 "交通运输企业用户主界面"]
                     (set-title "企业在线填报管理系统（试行）"))
   :js frame-js
   :after bjca-onpull
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
; 业务大类
(def dd-type 
  {
   1 "道路运输" 
   2 "水路运输" 
   3 "港口运营" 
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
   :pn "考评员资格证"
   :org "考评机构资质证书"
   :en "企业达标等级证书"

(def dd-renew
  {
   0 "-- 无 --"
   1 "1、考评员：资格证书到期"
   2 "2、考评员：户籍所在地或常住地发生省际间变更"
   3 "3、企业：达标等级证书到期"
   4 "4、考评机构：资质证书到期"
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
   :mot-apply "主管机关申请" ; 报名信息初次申请
   :org-backup "考评机构变更申请"
   :en-backup "企业变更申请"
   :pn-train "考评员培训考试记录"
   :hot "实名举报信息"
   :refine "整改通知"
   :user "登录认证U盘用户"
   :portal "首页内容"
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
(def dd-admin0 
  (array-map
    "00" "--请选择--"
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
(def dd-admin (dissoc dd-admin0 "00"))

(def dd-admin-province0 (dissoc dd-admin0 "01" "34" "35")) ; 有“ "00" "--请选择--" ”项
(def dd-admin-province (dissoc dd-admin-province0 "00")) ; 无“ "00" "--请选择--" ”项

; 学历
(def dd-edu 
  {
   "中专" "中专" 
   "大专" "大专" 
   "本科" "本科" 
   "研究生" "研究生" 
   "博士生" "博士生" 
   })
; 门户模块
(def dd-portal
  {
   "1" "政策法规"
   "2" "图片新闻"
   "3" "工作动态"
   "4" "公告公示"
   "5" "相关下载"
   })
; 登录认证U盘用户表role类型
(def dd-role
  (array-map
    "mot" "主管机关" ; 密钥用户管理缺省先看mot的
    "pn" "考评员"
    "org" "考评机构" 
    "en" "企业" 
    ))
; 委托功能列表。 如： [[考评员申请受理 icon-user apply-resp/pn m01] [..] [..] .. [投诉举报受理 icon-tip mot-hot m0a]]
(def dd-menu
  (filter #(= 5 (count %)) (-> cfg-frame-mot :nav first (subvec 3))))
; 委托功能array-map。如： {m01 考评员申请受理, m03 考评机构申请受理, m06 企业初次申请受理, m0a 投诉举报受理}
(def dd-menu2
  (wr3.clj.u/gen-array-map (map (juxt last first) dd-menu)))
;(println (into (array-map) (for [[label icon link url mid] dd-menu] [mid label])))

;; bjca注册的province、city字典
(def dd-bjca-province
  {"001"	"北京" "002"	"安徽" "003"	"重庆" "004"	"福建" "005"	"甘肃" "006"	"广东" "008"	"广西"
   "007"	"贵州" "009"	"海南" "010"	"河北" "011"	"黑龙江" "012"	"河南" "013"	"香港" "014"	"湖北"
   "015"	"湖南" "016"	"江苏" "017"	"江西" "018"	"吉林" "019"	"辽宁" "020"	"澳门" "021"	"内蒙古"
   "022"	"青海" "023"	"山东" "024"	"上海" "025"	"山西" "026"	"陕西" "027"	"四川" "028"	"台湾"
   "029"	"天津" "030"	"新疆" "031"	"西藏" "032"	"云南" "033"	"浙江" "034"	"宁夏" "035"	"兵团"
   "036"	"长航局" "037"	"珠航局" })
; 登录类型
(def dd-log
  {"login" "登录" 
   "resp-reg" "报名申请审批"})
; 处理结果
(def dd-resp
  {"yes" "同 意" "no" "不同意" "" "尚未处理"})

; 首页portal项目表单
(def cfg-portal
  [
   ["类型" :ptype {:t dd-portal :require true}]
   ["标题" :ptitle {:require true :style "width:500px"}]
   ["连接" :link {:style "width:500px"}]
   ["正文" :content {:t 'textarea :style "width:500px;height:100px"}]
   ["附件" :file {:t 'file}]
   ["排序号" :pno {:title "缺省可不填，填1、2、3等，0表示置顶。"}]
   ])

;; 考评员资格证书、考评机构资质证书、企业达标证书申请表格
; 考评员申请表
(def cfg-apply-pn ; [name id {:t :title ..}] :t类型,不写时为text 
  [
   ["主管机关" :admin {:t (dissoc dd-admin0 "01") :title "请自选主管机关" :require true}]
   ["姓名" :name {:require true}]
   ["身份证号" :pid {:t 'pid :require true :title "15位或18位身份证"}]
   ["常住地" :from {:t dd-province :require true}]
   ["照片" :photo {:t 'file :title "照片文件请勿超过10M大小" :require true}]
   ["工作单位" :org {:require true :style "width:300px"}]
   ["职称" :title]
   ["通讯地址" :address {:require true :style "width:300px"}]
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
   ["专业技术职称及相关材料" :perf1 {:t 'file}]
   ["专业工作业绩附件" :perf2 {:t 'file}]
   ["身份证明文件" :proof {:t 'file :title "二代身份证、护照等的正面（pdf, doc或者jpg格式）" :require true}]
   ["身份证明文件（背面）" :proofb {:t 'file :title "二代身份证、护照等的反面（pdf, doc或者jpg格式）" :require true}]
   ["学历证明文件" :proof2 {:t 'file :title "学历证书（pdf, doc或者jpg格式）" :require true}]
   ["培训合格证明文件" :proof3 {:t 'file :title "其他各类培训合格证明的照片、编号页、发证机关印章页（pdf, doc或者jpg格式）"}]
   ["换证原因<b>（仅换证申请）</b>" :renew {:t dd-renew}]
   ["继续教育证明<b>（仅换证申请）</b>" :edu2 {:t 'file}]
   ["工作业绩证明<b>（仅换证申请）</b>" :orgproof {:t 'file :title "由所在考评机构出具的工作业绩证明。"}]
   ])
; 考评机构申请表  
(def cfg-apply-org 
  [
   ["主管机关" :admin {:t dd-admin0 :require true :title "提示：若须申请一级考评机构资质证书，请选择“交通运输部”"}]
   ["所在省市" :province {:t dd-admin-province0 :require true :title "注：除一级考评机构及长航局、珠航局考评机构外，请和上面主管机关选项一致"}]
   ["单位名称" :name {:require true :v "" :style "width:300px" :title "一般为：学校/交通相关学会/协会/研究所"}]
   ["组织机构代码" :pid {:require true :v ""}]
   ["法人代表" :legalp {:require true}]
   ["联系人姓名" :contact {:require true}]
   ["联系人手机" :mobile {:require true}]
   ["联系人邮箱" :email {:require true}]
   ["资质等级" :grade {:t dd-grade :v 1 :require true :title "报名申请时，填一个拟申请等级即可；\n报名申请通过后，可试情况申请多个等级的证书"}]
   ["专业范围" :type {:t dd-type :v 1 :require true :title "最多只可选择两种专业范围"}]
   ["专职考评员人数" :pnumber {:v 7 :require true :title "一级≥7名；二级≥5名；三级≥3名。"}]
   ["高级技术职称考评员人数" :pnumber2 {:v 3 :require true :title "一级≥3名；二级≥2名；三级≥1名。"}]
   ["开始从事相应业务年份" :start {:v 2005 :require true}]
   ["通讯地址" :address {:style "width:300px"}]
   ["邮编" :pcode {:t 'pcode}]
   ["联系电话" :tel {:require true}]
   ["传真号码" :fax {:require true}]
   ["单位基本情况相关材料" :met {:t 'file :require true}]
   ["专职考评员聘用<br/>证明与职称证明" :pns {:t 'file :require true :title "文件10M以内大小"}]
   ["换证原因<b>（仅换证申请）</b>" :renew {:t dd-renew}]
   ])
; 企业申请表
(def cfg-apply-en ; en-input-form 
  [
   ["主管机关" :admin {:t dd-admin0 :require true :title "提示：若须申请一级企业达标等级证书，请选择“交通运输部”"}]
   ["所在省市" :province {:t dd-admin-province0 :require true :title "注：除一级企业及长航局、珠航局企业外，请和上面主管机关选项一致"}]
   ["企业名称" :name {:require true :v "" :style "width:300px"}]
   ["组织机构代码" :pid {:require true :v ""}]
   ["法人代表" :legalp {:require true }]
   ["联系人姓名" :contact {:require true}]
   ["联系人手机" :mobile {:require true}]
   ["联系人邮箱" :email {:require true}]
   ["申请等级" :grade {:require true :t dd-grade :v 1 :title "报名申请时，填一个拟申请等级即可；\n报名申请通过后，可试情况申请多个等级的证书"}]
   ["生产经营类型" :type {:t dd-type :require true :v 1}]
   ["生产经营类别" :type2 {:t dd-type2 :require true :v 11}]
   ["通讯地址" :address {:require true :style "width:300px" :title "请填写清楚通讯地址"}]
   ["联系电话" :tel {:require true }]
   ["传真" :tax {:require true }]
   ["安全生产组织架构" :safe {:t 'file :require true }]
   ["企业法人资格证件" :qual {:t 'file :require true }] 
   ["经营许可证" :license {:t 'file :require true }]
   ["标准化达标自评报告" :report {:t 'file :require true :title "即：企业安全生产工作报告"}] 
   ["换证原因<b>（仅换证申请）</b>" :renew {:t dd-renew}]
   ])
; 主管机关申请表
(def cfg-apply-mot ; en-input-form 
  [
   ["上级主管机关" :admin {:t dd-admin0 :require true :title "省级交通厅、交委请选择“交通运输部”"}]
   ["单位名称" :name {:require true :v "" :style "width:300px"}]
   ["组织机构代码" :pid {:require true :v ""}]
   ["法人代表" :legalp {:require true }]
   ["联系人姓名" :contact {:require true}]
   ["联系人手机" :mobile {:require true}]
   ["联系人邮箱" :email {:require true}]
   ["通讯地址" :address {:require true :style "width:300px" :title "选择GIS坐标"}]
   ["联系电话" :tel {:require true }]
   ["单位传真" :tax {:require true }]
   ])
;---------------- end
(def dd-cfg-apply {:pn cfg-apply-pn :en cfg-apply-en :org cfg-apply-org :mot cfg-apply-mot})

(defn- cfg-meta [cfg] (into {} (for [[v k & _] cfg] [k v])))
; 数据项英文、中文对照
(def ^{:doc "减少dd-meta和cfg-apply-pn|org|en中的重复定义"} 
      dd-meta
  (merge
    (cfg-meta cfg-portal)
    (cfg-meta cfg-apply-pn)
    (cfg-meta cfg-apply-org)
    (cfg-meta cfg-apply-en)
    {
     :_cdate-end "证书到期日"
     :_id "详情"
     :_issue "证书"
     :_select "选择"
     :admin-uid "主管机关用户"
     :advice "处理意见"
     :advice-eval "考评意见"
     :advice-refine "整改意见"
     :advice-reg "初审意见"
     :advice-review "审核意见"
     :belong "所属考评机构"
     :birth "出生日期"
     :cdate "发证时间"
     :cid "证书号"
     :comment "备注"
     :content "内容"
     :contract0 "聘用日期"
     :contract1 "解聘日期"
     :ctype "证书类型"
     :date "日期"
     :date-import "审批通过时间"
     :death "死亡人数"
     :del "删除标志"
     :direct-name "签发人姓名" ; 用于pn证书直接颁发
     :direct-title "签发人职务" ; 用于pn证书直接颁发
     :enid "企业ID"
     :exam-date "考试日期"
     :exam-score "考试分数"
     :fngrade "委托如下等级"
     :fnmenu "委托如下功能"
     :fntype "委托如下业务"
     :fulltime "专兼职"
     :grade "等级"
     :info "举报人信息"
     :mobile "手机"
     :name "名称"
     :org "单位组织"
     :orgid "选择的2个考评机构" ; en预选
     :orgid1 "指定的考评机构" ; mot指定
     :otype "举报对象类型"
     :pass-direct "直接颁发"
     :pid "证件号"
     :pn "考评员"
     :pnids "选择的考评员"
     :pwd "密码"
     :readonly "操作权限"
     :reason "原因"
     :refine-doc "整改报告"
     :resp "受理结果"
     :resp-eval "考评结果"
     :resp-reg "初审结果"
     :resp-review "审核结果"
     :respdate "受理日期"
     :respdate-eval "考评日期"
     :respdate-refine "整改报告日期"
     :respdate-review "审核日期"
     :role "用户类型"
     :score "分数"
     :score0 "自评分数"
     :score1 "考评分数"
     :sex "性别"
     :stand "达标评估"
     :stop "终止业务"
     :tel "联系电话"
     :train-end "培训结束日期"
     :train-hour "培训学时"
     :train-id "培训合格证号"
     :train-start "培训开始日期"
     :type "业务类型"
     :type2 "业务类别"
     :uid "用户ID"
     :usable "可用/停用"
     :workdate "参加工作日期"
     :yyyy "年份"
     ;-- espfj
     :titlefile "职称证明文件"
     :beginfile "相关专业从业年份证明文件"
     :proofmobile "证明人联系电话"
     :proofname "证明人"
     :prooforg "证明单位"
     }))

(defn dd+
  "无论dd的key是1还是“1”，都能取到值"
  [dd k] (let [rt (if (string? (key (first dd))) (dd (str k))
                    (dd (wr3.clj.n/to-int k k)))]
           (or rt k)))

;(dd+ dd-portal 0)
;(use 'wr3.clj.s)
;(count dd-meta) ; 