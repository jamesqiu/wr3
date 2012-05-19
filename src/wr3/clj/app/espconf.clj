(ns wr3.clj.app.espconf)

(use 'hiccup.core)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; layout

;; cfg-frame-xxx 说明：
;; :name            应用名称，用于javascript函数的前缀，如 function demo_xxx()
;; :style           上方色调，["layout_north" "layout_title"]（蓝色）或 ["layout_north1" "layout_title1"]（金色）
;; :title           应用名称，位于上方
;; :searcher        搜索框的label，位于右上方
;; :nav             导航条，位于左边；第一级为标题（title icon），第二级为链接（title icon id [url]），url可为地址，或js函数(todo)
;; :menu            2层菜单条，位于上方
;; :left-or-right   "left" or "right"

;; 交通运输主管部门界面配置
(def cfg-frame-mot
  {:name "esp"
   :style (map #(str % "") ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——主管部门管理系统（试行）"
   :searcher ["张" ; default-value
              ["考评人员搜索" "range_pn" "icon-ok"] ; label name icon
              ["考评机构搜索" "range_org" "icon-ok"]
              ["交通运输企业搜索" "range_en" "icon-tip"] ]
   :nav [
         ["待办事宜" "icon-pen" ; title icon
          ["考评员申请受理" "icon-list"     "pn-apply-resp"] ; title icon id 
          ["考评员换证申请受理" "icon-list" "pn-renew-resp"]  
          ["考评机构申请受理" "icon-list"    "org-apply-resp"]  
          ["考评机构变更备案受理" "icon-list" "org-backup-resp"] 
          ["考评机构换证受理" "icon-list"    "org-renew-resp"] 
          ["企业初次申请受理" "icon-list"    "en-apply-resp"]  
          ["企业考评结论审核" "icon-list"    "en-review"]  
          ["企业换证申请受理" "icon-list"    "en-renew-resp"]  
          ["企业变更申请受理" "icon-list"    "en-backup-resp"]  
          ["投诉举报受理" "icon-list"    "mot-hot"]  
          ]
         ["考评员" "icon-pen" ; title id
          ["考评员列表" "icon-list"    "pn-list"] ; title icon id 
          ["考评员统计查询"          "icon-list"    "pn-olap"] ; title icon id 
          ["资格证书制发" "icon-list"    "pn-cert-resp"] ; title icon id 
          ["考评员培训"          "icon-list"    "pn-train-view"] ; title icon id 
          ["考评员考试"          "icon-list"    "pn-exam"] ; title icon id 
          ["考评员资格撤销"      "icon-list"    "pn-cancel"] ; title icon id 
          ]
         ["考评机构" "icon-pen" ; title id
          ["考评机构列表" "icon-list"  "org-list"] ; title icon id 
          ["资质证书制发" "icon-list"    "org-cert-resp"] ; title icon id 
          ["资质撤销" "icon-list"    "org-cancel"] ; title icon id 
          ["年度工作报告"          "icon-list"    "org-report-view"] ; title icon id 
          ["考评情况汇总表"          "icon-list"    "org-eval-report"] ; title icon id 
          ]
         ["交通运输企业"           "icon-pen" ; title id
          ["企业列表" "icon-list"  "en-list"] ; title icon id 
          ["附加考评" "icon-list"    "en-recheck"] ; title icon id 
          ["达标证书撤销" "icon-list"    "en-cancel"] ; title icon id 
          ["年度工作报告"          "icon-list"    "en-report-view"] ; title icon id 
          ]
         ["下级机构管理"           "icon-pen" ; title id
          ["统计分析"          "icon-list"    "mot-olap"] ; title icon id 
          ]
         ["系统管理及帮助"     "icon-search"
          ["网站样式"          "icon-search" "site_bt"]
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ]   
   })

;; 考评员界面配置
(def cfg-frame-pn
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——考评员在线申请系统（试行）"
   :nav [
         ["考评员" "icon-pen" ; title id
          ["申请考评证书" "icon-list"    "pn-apply"] ; title icon id url 
          ["培训、考试情况" "icon-list"  "pn-learn"] 
          ["换证申请" "icon-list"  "pn-renew"] 
          ["使用帮助" "icon-help" "pn-help" "/static/esp/about-pn.html"]
          ]
         ] 
   :frame-main (html [:script "layout_load_center('/static/esp/about-pn.html')"])
   })

;; 考评机构界面配置
(def cfg-frame-org
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——考评机构管理系统（试行）"
   :nav [
         ["&nbsp;考评机构" "icon-arrow" ; title id
          ["申请资质证书" "icon-list"    "org-apply"] ; title icon id 
          ["申请变更备案" "icon-list"    "org-backup"] ; title icon id 
          ["申请换证" "icon-list"    "org-renew"] 
          ["已认定的资质证书" "icon-list"    "org-cert"] 
          ["年度工作报告" "icon-list" "org-report"] ; title icon id 
          ["使用帮助"          "icon-help"   "help_bt"]
          ]
         ["&nbsp;管理考评员" "icon-user" ; title id
          ["本机构考评员列表" "icon-list"    "org-pn"] ; title icon id 
          ["考评员培训情况管理" "icon-list"    "org-pn-train"] 
          ["考评员档案管理" "icon-list"    "pn-archive"] 
          ]
         ["&nbsp;企业考评管理" "icon-pen" ; title id
          ["企业考评待办工作" "icon-list" "en-apply-resp"] ; title icon id 
          ["企业达标等级证书制发" "icon-list" "en-cert-resp"] ; title icon id 
          ["工作进度查询" "icon-list" "org-en-process"] 
          ["考评企业档案管理" "icon-list" "en-archive"] 
          ["考评情况汇总表" "icon-list" "org-eval-report"] 
          ]
         ]   
   })

;; 交通运输企业界面配置
(def cfg-frame-en
  {:name "esp"
   :style (map #(str % 1) ["layout_north" "layout_title"])  ; "" 或者 "1"
   :title "交通运输企业安全生产标准化——企业在线填报管理系统（试行）"
   :nav [
         ["企业申请" "icon-pen" ; title id
          ["1、在线填报" "icon-list"    "en-apply"] ; title icon id 
          ["2、达标自评" "icon-list"    "stand-list"] ; title icon id 
          ["3、选择考评机构" "icon-list" "en-select-org"] 
          ["进度查询" "icon-list"    "en-process"] 
          ["企业年度工作报告" "icon-list"  "en-report"] 
          ["申请变更" "icon-list"    "en-backup"] 
          ["申请换证" "icon-list"    "en-renew"] 
          ]
         ]   
   })

; 主界面子系统菜单
(def cfg-subsys
  [
   ["考评员管理系统" "pn" "考评人员在线申请"]
   ["考评机构管理系统" "org" "考评机构在线申请、评定管理"]
   ["企业在线填报管理系统" "en" "企业在线填报管理"]
   ["交通运输主管部门管理系统" "mot" "交通运输管理部门（交通部、厅委局）内部管理"]
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
   :name "单位名称变更"
   :legalp "法定代表人变更"
   :stop "停业、破产或有其他原因终止业务"
   :pn "考评员发生重大变化"
   })
; 企业申请达标级别对应的分数要求
(def dd-score
  {
   "一级" 900 ; >=900
   "二级" 700 ; >=700
   "三级" 600 ; >=600
   })
; 企业达标等级
(def dd-en-grade
  {
   1 "一级"
   2 "二级"
   3 "三级"
   })
; 考评机构的考评资格
(def dd-org-grade
  {
   1 "甲级"
   2 "乙级"
   3 "丙级"
   })
; 数据项英文、中文对照
(def dd-meta
  {
   :admin "主管机关"
   :advice "处理意见"
   :begindate "相关专业从业时间"
   :belong "所属考评机构"
   :birth "出生日期"
   :cdate "发证时间"
   :cid "证书号"
   :content "举报内容"
   :contract0 "聘用日期"
   :contract1 "解聘日期"
   :ctype "证书类型"
   :date "日期"
   :death "死亡人数"
   :edu "学历"
   :exam-date "考试日期"
   :exam-score "考试分数"
   :from "地域"
   :fulltime "专兼职"
   :grade "级别"
   :info "举报人信息"
   :legalp "法人代表"
   :major "专业"
   :mobile "手机"
   :name "名称"
   :org "单位组织"
   :orgid "选择的2个考评机构"
   :pcode "邮编"
   :pid "证件号"
   :pn "考评员"
   :pnumber "专职考评员人数"
   :pnumber2 "高级技术职称考评员人数"   
   :province "省份"
   :qual "评审机构资质" 
   :safe "安全生产组织架构"
   :scroe "分数"
   :sex "性别"
   :start "开始从事相应业务年份" 
   :stop "终止业务"
   :tel "电话"
   :title "职称"
   :train-end "培训结束日期"
   :train-hour "培训学时"
   :train-id "培训合格证"
   :train-start "培训开始日期"
   :type "类型"
   :type2 "细类"
   :uid "用户ID"
   :workdate "参加工作日期"
   :yyyy "年份"
   })
; 各省名称，@deprecated
(def dd-province
  (let [ls (map str '(北京 上海 广东 江苏 陕西 山东 新疆 湖南 黑龙江 湖北 安徽 浙江 四川 贵州 甘肃 福建 辽宁 重庆 天津 广西 吉林 
                         海南 河北 河南 内蒙古 山西 西藏 青海 江西 云南 宁夏))]
    (apply array-map (flatten (for [e ls] [e e])))))

; 上级主管部门列表，@deprecated
(def dd-pot-map 
  {1 "010" 2 "022" 3 "022" 4 "0311" 5 "0351" 6 "0471" 7 "024" 8 "0431" 9 "0451" 10 "021"
   11 "025" 12 "0571" 13 "0551" 14 "0591" 15 "0791" 16 "0531" 17 "0371" 18 "027" 19 "0731" 20 "020" 
   21 "0771" 22 "0898" 23 "023" 24 "028" 25 "0851" 26 "0871" 27 "0891" 28 "029" 29 "0931" 30 "0971"
   31 "0951" 32 "0991" 33 "032" 34 "033" 35 "034" })
; 交通运输主管部门
(def dd-pot 
 (array-map
   "0"    "交通运输部"
   "010"  "北京交委"
   "020"  "广东交通厅 "
   "021"  "上海交委"
   "022"  "天津交通局"
   "023"  "重庆交委"
   "024"  "辽宁交通厅"
   "025"  "江苏交通厅"
   "027"  "湖北交通厅"
   "028"  "四川交通厅"
   "029"  "陕西交通厅"
   "0311" "河北交通厅"
   "0351" "山西交通厅"
   "0371" "河南交通厅"
   "0431" "吉林交通厅 "
   "0451" "黑龙江交通厅"
   "0471" "内蒙古交通厅"
   "0531" "山东交通厅"
   "0551" "安徽交通厅"
   "0571" "浙江交通厅"
   "0591" "福建交通厅 "
   "0731" "湖南交通厅"
   "0771" "广西交通厅"
   "0791" "江西交通厅"
   "0851" "贵州交通厅"
   "0871" "云南交通厅"
   "0891" "西藏交通厅"
   "0898" "海南交通厅"
   "0931" "甘肃交通厅"
   "0951" "宁夏交通厅"
   "0971" "青海交通厅"
   "0991" "新疆交通厅"
   "032"  "新疆兵团交通局"
   "033"  "长江航务管理局"
   "034"  "珠江航务管理局"             
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
   ["申请类别" :type {:t dd-type :v 1 :title "考评员申请的专业类型不得多于二种"}] ; 最多两类
   ["主要学习（培<br/>训）经历" :train {:t 'textarea}]
   ["主要工作简历" :resume {:t 'textarea}]
   ["专业工作业绩" :perf {:t 'file}]
   ["相关证明文件" :proof {:t 'file :title "包括身份证明、学历证明以及其他资格证的电子文档等（pdf, doc或者jpg格式）"}]
   ["主管机关" :admin {:t (dissoc dd-pot "0") :title "自选"}]
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
   ["评审机构资质" :qual {:t dd-org-grade :v "甲类"}]
   ["专业范围" :type {:t dd-type :v 1 :title "todo: 改为可以多选，或者每个专业申请一次"}]
   ["专职考评员人数" :pnumber {:v 15}]
   ["高级技术职称考评员人数" :pnumber2 {:v 6}]
   ["开始从事相应业务年份" :start {:v 2005}]
   ["主管机关" :admin {:t dd-pot}]
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
   ["申请等级" :grade {:t dd-en-grade :v 1}]
   ["法人代表" :legalp]
   ["生产经营类别" :type2 {:t dd-type2 :v "s1" :title "可多选"}]
   ["主管机关" :admin {:t dd-pot :title "一级不用选（部）；二级选34个机构；三级选二级、三级机构；每个类型对应自己的主管机关"}]
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