(ns ^{:doc "企业安全生产标准化管理系统 Enterprise Safety Production Standardization。
  “安全生产标准化（work safety standardization）” 
  pn：考评员person，en：企业enterprise，org：考评机构organization，mot：交通部Ministry Of Transport "
      :todo "" }
     wr3.clj.app.esp)

(use 'wr3.clj.app.espc 'wr3.clj.app.espconf); :reload)
(use 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.s 'wr3.clj.n 'wr3.clj.chart 'wr3.clj.u 'wr3.clj.nosql 'wr3.clj.chart)
(use 'hiccup.core 'somnium.congomongo)
(use 'clojure.contrib.json)
(import 'wr3.util.Charsetx)
(require '[wr3.clj.app.auth :as au])

;;;--------------------------------------------------------------------------------------------------- 公共函数
(def auth-login-url (case login-mode :user-pass "/login.html" "/c/espreg/ca")) ; 本ns的登录页面：可选缺省认证或者bjca认证
;  (first ["/login.html" "/c/espreg/ca"])) 
(defn auth
  "该 CljServlet 调用，用于本应用各函数的权限控制 "
  [request fname ids & args]
  (let [uid (wr3user request) 
        role (wr3role request)
        id (first ids)]
    (if (= fname "index")
      (cond
        (nil? id) true ; index 页面匿名用户即可访问
        (and (= id "pn") (= role "pn")) true
        (and (= id "en") (= role "en")) true
        (and (= id "org") (= role "org")) true
        (and (= id "mot") (= role "mot")) true
        :else false)
      ; 其他页面注册用户都能访问
      (cond 
        uid true
        ; (.startsWith fname "hot") true ; 实名举报不登录可访问   
        :else false))))

(defn- index-all
  "@deprecated: by /esp/index.html"
  [] (html [:script "window.location.href = '/esp/' "]))
  
(defn index
  "@id 为nil时显示入口页面；为 pn,org,en,mot 时为子系统layout入口"
  [id]
  (if id 
    (frame-index (case id
                   "pn" cfg-frame-pn
                   "org" cfg-frame-org
                   "en" cfg-frame-en
                   "mot" cfg-frame-mot
                   "not-found"))
    (index-all)))

;;;--------------------------------------------------------------------------------------------------- pn  考评员
(def pn---------- nil)

(defn pn-apply
  "service: 考评员申请导航页"
  [request]
  (apply-nav- :pn request))

(defn pn-input
  "service：评审人员在线申请"
  [request]
  (apply-input- request :pn))

(defn pn-input-test
  [request]
  (html-body (pn-input request)))
    
(defn pn-list
  "service: 考评员列表
  @id name的pattern如'张' "
  [id skip]
  (list- id :pn [] [:name :type :org :from :_id] {:skip (to-int skip 0)}))

(defn pn-learn
  "service: 考评员培训、考试查询
  @see pn-train 主管机构的考评员培训视图"
  [request]
  (let [uid (wr3user request)
        rs (with-esp- (fetch :pn-train :where {:uid uid}))]
    (html
      [:h1 "考评员培训、考试情况"]
      (eui-tip "1、首次培训时间不少于24个学时；2、年度继续教育时间不少于8个学时；")
      (if (empty? rs) 
        [:h3 "尚无培训和考试记录。"]
        (html
          [:h3 (format "有 %s 次培训和考试记录" (count rs))]
          (result-html- rs '["培训证书" "培训类型" "培训日期" "培训学时" "考试日期" "考试分数" "查看"]
                        [:train-id :type :train-start :train-hour :exam-date :exam-score :_id] {:form "docv/pn-train"})) ) )))

(defn- pn-history
  "指定oid的pn参与考评en的历史记录"
  [oid]
  (let [rs (with-oid- :pn oid)
        rt (with-esp- (fetch :en-apply :where {:pnids {:$in [(:uid rs)]}}))]
    (html 
      [:h2 (format "【%s】参与的考评情况记录（%s条）：" (:name rs) (count rt))]
      (result-html- rt [] [:name :type2 :grade :date :respdate :resp :pnids ] {}) )))
  
(defn pn-doc
  "service: 根据 pn 的 object-id 来显示文档，含参与考评企业的历史记录。
  @id object-id字符串，如 '4f8aeb2a75e0ae92833680eb' "
  [id]
  (doc- :pn id {:after (pn-history id)}))

(defn pn-org
  "service: pn本人受聘org的情况"
  [request]
  (let [uid (wr3user request)
        rs (with-esp- (fetch :pn :where {:uid uid :belong {:$exists true}}))]
    (html (if (empty? rs) [:h1 "尚未受聘于考评机构"] 
            (html [:h1 "已受聘于如下考评机构："]
                  (result-html- rs [] [:name :belong :fulltime :contract0 :contract1] {}) )))))

;;;--------------------------------------------------------------------------------------------------- org 考评机构
(def org---------- nil)

(defn org-apply
  "service: 考评机构申请导航"
  [request]
  (apply-nav- :org request))

(defn org-input
  "service：评审机构在线申请"
  [request]
  (apply-input- request :org))

(defn org-list
  "service: 考评机构列表
  @id name的pattern如'学校' "
  [id skip]
  (list- id :org [] [:name :grade :province :_id] {:skip (to-int skip 0)}))

(defn org-pn-archive
  "service: 考评机构-考评员档案管理"
  []
  (html
    [:h1 "考评员档案管理"]
    (eui-button {:plain "true" :iconCls "icon-sum"} "安全生产标准化考评员汇总表、登记表") [:br]
    (eui-button {:plain "true" :iconCls "icon-list"} "考评员学历和专业技术能力证明汇总表") [:br]
    (eui-button {:plain "true" :iconCls "icon-file"} "考评员培训情况汇总表（何时培训，多长时间，取得何种培训合格证书）") [:br] ))

(defn org-pn
  "service: 本考评机构机构的所有考评员 "
  [request]
  (let [uid (wr3user request)
        rt (with-mdb2 "esp"
             (vec (fetch :pn :sort {:contract1 1 :fulltime -1} :where {:belong uid} )))]
    (html
      [:h1 (format "本机构当前在职考评人员 %s 名" (count (filter #(nil? (:contract1 %)) rt)))]
      [:div#hire {:style "margin:15px"}
       (eui-tip "受聘考评员需提供其证书U盘；已聘用的考评员可以点击详情查看后进行解聘。")
       "1、" (eui-button {:onclick "esp_org_pn_ukey()"} "读取受聘考评员证书U盘") [:br][:br]
       "2、" (eui-text {:id "pid" :name "pid" :disabled "true" :style "width: 200px"}) (space 5) 
       (eui-button {:onclick "esp_org_hire('pid')"} "继续") [:br][:br]
       [:label "或通过资质证书号聘用考评员（<font color=red>注：此功能将被移除</font>）："]
       (eui-text {:id "cid"}) (space 3)
       (eui-button {:onclick "esp_org_hire('cid')"} "查询聘用") ]
      (result-html- rt '[姓名 证书类别 证书编号 专兼职 聘用日期 解聘日期 详情] 
                    [:name :type :cid :fulltime :contract0 :contract1 :_id] {:form "org-hire-view"})
      [:br][:br])))

(defn org-hire-view
  "app: 考评员情况，能否聘用
  @如果有id，如果有cid，先通过cid查出文档:_id"
  [id type request]
  (let [field (if (= type "cid") :cid :pid) 
        r (with-mdb2 "esp" (fetch-one :pn :where {field id}))
        oid (:_id r)
        cid (:cid r)
        c0 (:contract0 r) ; 合同开始日
        c1 (:contract1 r) ; 合同到期日
        belong (:belong r)
        fulltime? (:fulltime r)
        uid (wr3user request)]
    (if r
      (doc- :pn (str oid) 
            {:before (if (and fulltime? c0 (not c1)) 
                       (html ; 未到期的专职考评员，可解聘
                         (eui-tip (format "该考评员目前已经聘用于【%s】。" (wr3user-name belong))) [:br]
                         (when (= belong uid)
                           (eui-button {:onclick (format "esp_fire('%s')" cid) :style "margin:10px"} (str "解聘考评员" (:name r)))) )
                       (html ; 到期的专职考评员，或兼职考评员，可聘用
                         (eui-combo {:id "fulltime" :value 1} {1 "专职" 0 "兼职"} ) (space 5)
                         (eui-button {:onclick (format "esp_hire('%s')" cid)} 
                                     (str "聘用考评员" (:name r))) [:br][:br]))})
      "未查到相关考评员")))

(defn org-hire
  "service: 考评机构后台聘用指定证书号的考评员
  @id 证书号cid
  @fulltime 专职'1'或兼职 "
  [id fulltime request]
  (let [cid id
        ft (if (= fulltime "1") true false)
        uid (wr3user request) ]
    (with-mdb2 "esp"
      (let [r (fetch-one :pn :where {:cid cid})]
        (do
          (update! :pn r (into r {:fulltime ft :contract0 (date) :contract1 nil :belong uid}))
          "聘用成功")))))

(defn org-fire
  "service: 考评机构后台解聘指定证书号的考评员
  @id 证书号cid "
  [id request]
  (let [cid id]
    (do (update- :pn {:cid cid} (fn [r] {:contract1 (date)}))
      "已解聘")))

(defn org-pn-train
  "省级主管机关查看属于自己的考评员的培训、考试情况
  @see pn-learn 考评员自己的培训视图"
  [request]
  (let [uid (wr3user request)
        rt (with-esp- (fetch :pn :only ["_id"] :where {:belong uid :contract1 nil} ))
        ids (vec (map #(-> % :_id str) rt))
        rt2 (with-esp- (fetch :pn-train :where {:_id {:$in (map #(object-id %) ids)}}))]
    (html
      [:h1 "本机构考评员培训、考试情况一览"]
      [:h2 "培训时间，培训学时（不少于24个学时），培训类别，培训合格证号"]
      [:div "注：由省级交通运输主管机关、长江和珠江航务管理局按管辖范围负责组织实施培训、考试工作。"]
      [:br]
      (result-html- rt2 
                    ["姓名" "培训合格证" "开始日期" "结束日期" "学时" "类型"] 
                    [:name :train-id :train-start :train-end :train-hour :type]))))

(defn org-cert
  "service: 考评机构目前证书"
  [request]
  (let [uid (wr3user request)
        rs (with-esp- (fetch :org :where {:uid uid :cid {:$exists true}}))]
    (html
      [:h1 "考评机构资质证书"]
      (if (empty? rs)
        (eui-tip "还没有资质证书。")
        (html
          (result-html- rs '["证书号" "证书类型" "发证日期" "证书状态" "查看"] 
                        [:cid :type :cdate :cstate :_id] {:form "docv/org"}))))))

(defn org-en-apply
  "app: 考评机构受理企业申请记录
  @id object-id"
  [id]
  (let [f-yes (format "esp_org_en_apply('yes', '%s')" id) 
        f-no  (format "esp_org_en_apply('no',  '%s')" id)]
    (doc- :en-apply id 
          {:after (fn [rt] (html
                             [:br] [:label "第一步："] 
                             (eui-button {:href (format "/c/esp/org-select-pn/%s" id) :target "_blank"} "考评员选择") (space 5)
                             (eui-button {:href (format "/c/esp/stand/%s?enid=%s" (:type2 rt) (:uid rt)) 
                                        :target "_blank"} "企业考评打分") [:br][:br]
                             [:label "第二步："] (yes-no-advice- {:onclick [f-yes f-no]}) ))})))

(defn org-select-pn
  "app: org选择负责企业考评的考评员
  @id :en-apply文档的object-id"
  [id request]
  (let [r (with-oid- :en-apply id) ; 申请记录
        uid (wr3user request)
        rt (with-esp- (fetch :pn :sort {:fulltime -1} :where {:belong uid :contract1 nil} ))
        pnids (:pnids r)]
    (html-body
      [:h1 (format "为【%s】选择考评员：" (:name r))]
      (when-not (zero? (count pnids)) (eui-tip "之前已经选择过考评员，可更改。"))
      (result-html- rt '[姓名 证书类别 证书编号 专兼职 聘用日期 详情 选择] 
                    [:name :type :cid :fulltime :contract0 :_id :_select] {:form "docv/pn"})
      [:br](eui-button {:onclick (format "esp_org_select_pn('%s')" id) :iconCls "icon-ok"} "选择考评员") (space 5)
      (eui-button-close)
      [:script (format "esp_mark_selected('%s')" (join pnids ","))] )))

(defn org-select-pn-save
  "service: 保存选中的几个考评员
  @id :en-apply文档的object-id
  @sids 空格分隔的考评员oid列表 "
  [id sids request]
  (let [pnids (vec (split sids " "))]
    (update- :en-apply {:_id (object-id id)} {:pnids pnids})
    (format "已选择 %s 个考评员。请关闭此窗口！" (count pnids))))

(defn org-en-apply-save
  "service: 保存org对en的考评结果
  @id :en-apply表文档的object-id"
  [id request]
  (let [vars (query-vars2 request)]
    (update- :en-apply {:_id (object-id id)} (fn [r] (merge {:respdate-eval (datetime)} vars)))
    "审核结果已经保存 " ))

(defn org-en-process
  "service: 考评机构查询企业申请处理进度
  @todo"
  []
  (let []
    (html
      [:h1 "企业考评及证书制发工作进度"]
      [:h2 "待处理的企业申请考评"]
      (eui-tip "目前暂无")
      [:h2 "已处理待主管机关审核的企业申请"]
      (eui-tip "目前暂无")
      [:h2 "待发证的企业"]
      (eui-tip "目前暂无") )))

(defn org-en-archive
  "service: 考评机构的企业档案管理
  todo: 只列出admin自己管的企业"
  [request]
  (let [uid (wr3user request)
        rs (with-esp- (fetch :en-apply :where {:orgid1 uid} :limit 1000))]
    (html
      [:h1 (format "本考评机构考评过的企业档案（%s条）" (count rs))]
      (result-html- rs [] [:name :type2 :grade :date :resp :respdate :_id] {:form "docv/en-apply"})
      )))

(defn org-en-eval
  "service: org查看en的考评情况汇总"
  []
  (html
    [:h1 "企业考评情况汇总表"]
    (eui-tip "暂无记录")))

(defn org-stand-view
  "org, mot查看org对en达标评估的结果
  @ids ids[0]:enid被评估企业的uid； ids[1]:达标结果文档的object-id"
  [ids request]
  (let [[enid oid _] ids
        doc (with-oid- :en-stand oid)
        type2 (:type2 doc)
        m (merge (:stand doc) {:role (wr3role request)})] 
    (stand- type2 enid m)))
  
;;;--------------------------------------------------------------------------------------------------- en  企业
(def en---------- nil)

(defn en-apply
  "service: 企业申请导航页"
  [request]
  (apply-nav- :en request))
  
(defn en-input
  "service：企业在线填报"
  [request]
  (apply-input- request :en))

(defn en-list
  "service: 企业列表
  @id name的pattern如'安徽' "
  [id skip]
  (list- id :en [] [:name :type :grade :province :_id] {:skip (to-int skip 0)}))

(defn en-stand
  "service: 列出所有的达标标准"
  [request]
  (let [uid (wr3user request)
        rt (with-esp- (fetch :en-stand :where {:uid uid}))]
    (html
      [:center [:h1 "达标标准考评（五大类16小类）"]]
      [:h2 "已经提交过的达标自评："]
      (result-html- rt [] [:date :type2 :stand :_id] {:form "en-stand-view"}) [:br]
      (eui-tip "注意：企业根据交通运输部颁布的下列某项标准进行达标自评打分！")
      (for [[k1 v1] dd-type]
        (html 
          [:h2 v1 "："]
          (for [[k2 v2] (filter #(= (int (/ (key %) 10)) k1) dd-type2)]
            (eui-button {:href (str "/c/esp/stand/" k2) :target "_blank" :style "margin: 5px"} v2))) ))))

(defn en-stand-view
  "显示en之前达标自评的结果
  @id 达标自评结果文档的object-id"
  [id request]
  (let [doc (with-oid- :en-stand id)
        type2 (:type2 doc)
        role (wr3role request)
        m (merge (:stand doc) {:role role})] 
    (stand- type2 nil m)))
  
(defn en-select-org
  "app: 企业选择考评机构"
  [request]
  (let [uid (wr3user request)
        admin (:admin (first (with-uid- :user uid)))
        rs (with-esp- (fetch :org :where {:admin admin}))
        r (first (with-esp- (fetch :en-apply :where {:uid uid} :sort {:date -1}))) ; 第一条申请
        ] 
    (html
      [:h1 (format "%s主管的考评机构（%s 名）" (dd-admin admin) (count rs))]
      (eui-tip "请在如下的考评机构列表中自行选择两个。")
      (result-html- rs [] [:name :admin :_id :_select] {:form "docv/org"}) [:br]
      (eui-button {:onclick "esp_en_select_org()"} "提 交")
      [:script (format "esp_mark_selected('%s')" (join (:orgid r) ","))] )))

(defn en-select-org-save
  "service: 保存企业所选2个考评机构的object-id "
  [id request]
  (with-mdb2 "esp"
    (let [sid (vec (split id " "))
          uid (wr3user request)
          rs (fetch :en-apply :where {:uid uid})]
      (doseq [r rs]
        (update! :en-apply r (into r {:orgid sid})))
      "已保存" )))

(defn en-process
  "service: 企业申请进度查询"
  [request]
  (let [uid (wr3user request)
        rs (with-uid- :en-apply uid)]
    (html
      [:h1 "申请进度查询"]
      (when rs
        (html [:h2 (format "已提交过%s次申请：" (count rs))]
              (result-html- rs [] [:date :grade :type2 :admin :_id] {:form "docv/en-apply"}) )))))

;;;--------------------------------------------------------------------------------------------------- mot 主管机关
(def mot---------- nil)

(defn- mot-role
  "获取mot角色用户的admin代号，如'01' '14' ；没找到该用户或者不是mot角色则返回nil。"
  [request]
  (let [uid (wr3user request)
        r (with-mdb2 "esp" (fetch-one :user :where {:uid uid}))]
    (when r (:admin r))))

(defn- mot-subs
  "获取下一级主管机关列表"
  [upper]
  (map (juxt :code :name) (with-esp- (fetch :mot :where {:upper upper} :sort {:code 1}))))

(defn mot-olap
  "公共service: 主管机关进行pn、org、en的查询搜索和统计分析
  @id 'pn' 'org' 'en' "
  [id request]
  (let [tb (keyword id) ; :pn :org :en
        nam (dd-form tb) ; 考评员 考评机构 交通运输企业
        admin (mot-role request)
        dims (case tb :pn [:type :admin :edu] (:org :en) [:type :admin :grade])
        option0 {"0" "-- 所有 --"}]
    (html
      [:h1 nam "统计分析及查询搜索"]
      [:a {:name "search"}] 
      [:h2 "条件搜索：" [:a {:href "#dim"} "（跳转到：维度指标分析）"]]
      [:form#fm1
       [:label "主管机关："] (eui-combo {:name "admin"}
                                   (sort (into option0 (if (= admin "01") dd-admin (mot-subs admin))))) (space 5)
       [:label "业务类型："] (eui-combo {:name "type"} (into option0 dd-type)) (space 5)
       (when (not= tb :pn) 
         (html [:label ({:org "资质" :en "达标"} tb) "等级："] (eui-combo {:name "grade"} (into option0 dd-grade))))
       [:br][:br]
       [:label "姓名或者证书号："] (eui-text {:name "s" 
                                      :onkeypress "if(event.keyCode==13||event.which==13){return false;}"}) (space 5)
       (eui-button {:onclick (format "ajax_load($('#result'), '/c/esp/mot-search/%s?'+$('#fm1').serialize())" id) 
                    :iconCls "icon-ok"} " 搜 索 ") (space 5) 
       (eui-button {:onclick "$('#fm1')[0].reset()" :iconCls "icon-cancel"} " 取 消 ") ]
      [:div#result] [:hr]
      [:a {:name "dim"}] 
      [:h2 nam "维度指标分析：" [:a {:href "#search"} "（跳转到：条件搜索）"]] [:label "点击如下维度进行统计："]
      (for [dim dims] 
        (html (eui-button {:onclick (format "ajax_load($('#result2'), '/c/esp/mot-dim/%s?dim=%s')" id (name dim))} 
                          (dd-meta dim)) (space 5)))
      [:div#result2 {:style "min-height:400px"}][:br][:br] )))

(defn mot-search
  "service: mot对pn、org、en的复杂条件搜索
  @id 'pn' 'org' 'en' "
  [id request]
  (let [tb (keyword id) ; :pn :org :en
        vars (query-vars2 request)
        filters (case tb :pn [:admin :type] (:org :en) [:admin :type :grade])
        col-ids (case tb :pn [:name :type :cid :admin :_id] (:org :en) [:name :type :grade :cid :admin :_id])
        s (:s vars)
        where (into {(if (wr3.util.Charsetx/hasChinese s) :name :cid) (re-pattern (:s vars))} 
                    (map #(if (= "0" (% vars)) nil [% (% vars)]) filters))
        rt (with-esp- (fetch tb :limit 100 :where where))
        count (count rt)]
    (html
      (eui-tip (if (= 100 count) "提示：结果太多（≥100），请适当增加搜索条件。" 
                 (format "搜索到 %s 条符合条件的结果。" count)))
      (result-html- rt [] col-ids {:form (str "docv/" id)}))))

(defn mot-dim
  "service: mot对pn、en、org的维度统计图表
  @id 'pn' 'org' 'en' 
  @dim 维度如'admin' 'grade' 'type' 'edu' "
  [id dim request]
  (let [tb (keyword id)
        nam (dd-form tb)
        dim (keyword dim)
        dim-nam (dd-meta dim)
        rt (with-esp- (mdb-group tb [dim]))
        dd (case dim :admin dd-admin :grade dd-grade :type dd-type :edu str)
        m (for [r rt] (let [v0 (dim r)
                            v (case dim (:admin :edu) v0 (:grade :type) (to-int v0))] 
                        [(dd v) (:count r)])) ]
    (html
      [:h3 (format "【%s】-【%s数量】分析图表：" dim-nam nam)]
      (barf (apply array-map (flatten m)) {:x (str dim-nam) :y (str nam "数量")})
      (pief (apply array-map (flatten m)) {})
      (result-html- rt [dim-nam "数量小计"] [dim :count] {}))))

;;-- mot-(pn en org)-apply[-resp]
(defn- mot-apply-
  "主管机关对pn、org、en的申请回应。
  @type 即 :pn :org :en
  @oid 申请文档的object-id
  @orgid-as-select 标识把:orgid显示为可选列表（2选1） "
  [type oid & orgid-as-select]
  (let [tb (-> type name (str "-apply") keyword) 
        f-click (fn [y-n] (format "esp_mot_apply('%s','%s','%s')" (name type) oid y-n))]
    (doc- tb oid
          {:after
           (fn [rt]
             (html
               (case type
                 :pn (let [uid (:uid rt) t (:type rt)
                           rs (with-esp- (fetch :pn-train :where {:uid uid :type t}))]
                       (html [:h2 "该考评员“" (dd-type (to-int t 0)) "”类型的培训、考试记录："]
                             (result-html- rs [] [:name :type :train-id :exam-date :exam-score]) [:br][:br]
                             [:div {:style "border:1px solid lightgray; padding: 10px"}
                              (eui-tip "直接从事交通运输安全生产行政管理工作10年以上，熟悉掌握交通运输安全生产相关法规和企业安全生产标准化规定者：") [:br]
                              (eui-button {:href "/c/esp/mot-pn-direct" :target "_blank"} "直接颁发操作") [:br][:br]
                              "操作结果：（ "
                              (eui-text {:id "pass-direct" :type "checkbox" :disabled "true"}) (space 2) "直接颁发" (space 5)
                              "签发人姓名：" (eui-text {:id "direct-name" :disabled "true" :value ""}) (space 5) 
                              "签发人职务：" (eui-text {:id "direct-title" :disabled "true" :value ""}) " ）" ] ))
                 :org ""
                 :en (eui-tip "注意：请指派一个考评机构对该企业进行考评！")
                 nil)
               (yes-no-advice- {:onclick [(f-click "yes") (f-click "no")]}) ))
           :orgid-as-select (when orgid-as-select true)
           :orders (map second ({:pn cfg-apply-pn :org cfg-apply-org :en cfg-apply-en} type))
           })))

(defn mot-pn-direct
  "app: 填写直接颁发的信息"
  []
  (let []
    (html-body
      [:h1 (dd-cert :pn) "直接颁发"] 
      (eui-tip "提示：直接颁发则签发人姓名和职务必填。修改后请点击 “确定” 按钮；若要放弃修改请直接点击 “关闭” 按钮。")
      (eui-text {:id "pass-direct" :type "checkbox"}) (space 2) "直接颁发" [:br][:br]
      [:label {} "签发人姓名："](eui-text {:id "direct-name"}) [:br]
      [:label {} "签发人职务："](eui-text {:id "direct-title"}) [:br][:br]
      (eui-button {:onclick "esp_mot_pn_direct()"} "确定") (space 5) (eui-button-close) )))

(defn mot-pn-apply [id] (mot-apply- :pn id))
(defn mot-org-apply [id] (mot-apply- :org id))
(defn mot-en-apply [id request] (mot-apply- :en id true))

(defn mot-apply-resp
  "主管机关保存pn、org、en的申请处理意见
  @id 类型'pn' 'org' 'en' "
  [id request]
  (let [tb (keyword (str id "-apply")) ; :pn-apply :org-apply :en-apply
        vars (query-vars2 request) 
        oid (:oid vars)]
    (update- tb {:_id (object-id oid)}
             (fn [r] (merge (dissoc vars :oid) {:respdate (datetime)}))) ; :oid和:_id值相同，没必要保存
    "申请已处理"))

(defn mot-en-review
  "主管机关对org评审过的企业考评结论审核"
  []
  (let [rs (with-esp- (fetch :en-apply ))
        rs2 (filter #(> (days (:respdate-review %)) 7) (with-esp- (fetch :en-apply :where {:resp-review "yes"})))]
    (html
      [:h1 "企业考评结论审核"]
      (eui-tip (str "企业考评结论受理：" 
                    "（同意并在本系统实时公示，7天到期自动通过可发证）" 
                    "/（同意并在本系统实时公示，7天到期有举报且情况属实，退到考评机构）" 
                    "/（不同意+意见退到考评机构和企业）"))
      [:h2 "待审核企业："]
      (result-html- 
        rs []
        [:name :type2 :date :score0 :score1 :resp :resp-eval :resp-review :_id :_select]  ; todo: 考评机构评分score字段
        {:form "mot-en-review-doc"}) [:br]
      (eui-button {:onclick "esp_en_apply_resp()"} "同意，进行公示") [:br][:br]
      [:h2 "公示期满可发证企业一览："] 
      (if (empty? rs2)
        (eui-tip "无公示期满的企业") 
        (result-html- 
          rs2 [] 
          [:name :type2 :date :score0 :score1 :resp :resp-eval :resp-review :_id :_select] {:form "mot-en-review-doc"}))
      )))

(defn mot-en-passed
  "已达标企业列表（全国或本月）"
  []
  (let [rs (with-esp- (fetch :en-apply :where {:cid {:$exists true}}))]
    (html
      [:h2 "已达标企业："] 
      (eui-button {:href "/c/esp/mot-en-export/exp-达标企业列表.csv?content-type=text/csv&charset=GBK" 
                   :target "_blank" :style "margin: 10px"} "导出电子表格")
      (if (empty? rs)
        (eui-tip "无已达标企业") 
        (result-html- 
          rs ["企业名称" "达标类别" "达标等级" "实施考评的考评机构" "详情"] 
          [:name :type2 :grade :orgid1 :_id] {:form "mot-en-review-doc"}))
      ))) 

(defn mot-en-export
  "导出列表"
  []
  (let [rs (with-esp- (fetch :en-apply :where {:cid {:$exists true}}))
        frow (fn [r] (join (map #(wr3.util.Csv/toCsv %)
                                [(:name r)
                                 (dd-type2 (to-int (:type2 r)))
                                 (dd-grade (to-int (:grade r)))
                                 (format-orgid1 (:orgid1 r))]) 
                           ","))]
    (str
      (join (map #(wr3.util.Csv/toCsv %) ["企业名称" "达标类别" "达标等级" "实施考评的考评机构"]) ",") "\r\n"
      (join (for [r rs] (frow r) ) "\r\n"))))

(defn mot-en-review-doc
  "app: 受理企业考评结论审核
  @id 企业申请文档的object-id "
  [id]
  (doc- :en-apply id 
        {:after 
         (fn [rs]
           (let [uid (:uid rs) ; 企业uid，如'en1'
                 rs1 (with-uid- :en-stand uid)
                 rs2 (with-esp- (fetch :en-stand :where {:enid uid})) 
                 f-yes (format "esp_mot_review('yes', '%s')" id)
                 f-no (format "esp_mot_review('no', '%s')" id) ]
             (html 
               [:h2 "自评报告："]
               (result-html- rs1 [] [:type2 :date :stand :_id] {:form "en-stand-view"})
               [:h2 "评估报告："]
               (result-html- rs2 [] [:type2 :date :stand :uid :_id] {:form (str "org-stand-view/" uid)}) 
               (yes-no-advice- {:label ["填写意见：" "同 意（并进行公示）" "不同意（并填写意见）"]
                                :onclick [f-yes f-no]} ) )))}))

(defn mot-en-review-save
  "service: 保存mot对en的最后审核
  @id :en-apply表文档的object-id"
  [id request]
  (let [vars (query-vars2 request)]
    (update- :en-apply {:_id (object-id id)} (fn [r] (merge {:respdate-review (datetime)} vars)))
    "审核结果已经保存 " ))

(defn mot-hot
  "service: 主管机关受理实名投诉举报"
  []
  (let [rs (with-esp- (fetch :hot :sort {:date 1}))]
    (html
      [:h1 "实名投诉举报申请受理"]
      (if (empty? rs)
        (eui-tip "暂无投诉举报")
        (result-html- rs ["举报时间" "举报人信息" "主管机关" "详情"] [:date :info :admin :_id] 
                      {:form "mot-hot-doc"}) ))))

(defn mot-hot-doc
  "app: 主管机关查看并受理举报信息"
  [id]
  (let [r (with-oid- :hot id)]
    (doc- :hot id
          {:after (html
                    [:form#fm1 {:action "/c/esp/mot-hot-resp" :method "post"}
                     [:input {:name "oid" :type "hidden" :value (:_id r)}]
                     [:p [:label "处理意见："] (eui-textarea {:name "advice"})]
                     [:p [:label "转发至主管机关："] (eui-combo {:id "admin" :name "admin" :value (:admin r)} dd-admin)]
                     [:p (eui-button {:onclick "$('#fm1').submit()"} "提 交")]] )})))
  
(defn mot-hot-resp
  "app: 主管机构保存处理后的举报意见"
  [request]
  (with-mdb2 "esp"
    (let [vars (query-vars request)
          r (fetch-by-id :hot (object-id (vars "oid")))]
      (do
        (update! :hot r (into r (dissoc vars "oid")))
        (html-body 
          (eui-tip "已保存" )
          (eui-button-close) )))))

(defn mot-pn-train-list
  "service: 列出考评员培训考试列表"
  [skip]
  (let [count1 (tb-count :pn-train)
        skip (to-int skip 0)
        rt2 (with-esp- (fetch :pn-train :skip skip :limit 100 :sort {:train-start 1}))] 
    (html
      [:div#pn_train_list
       [:h2 (format "已录入的考评员培训、考试记录（%s 条）：" count1)]
       (pager-html count1 skip "esp_pn_train_list('/c/esp/mot-pn-train-list')")
       (result-html- rt2 
                     ["姓名" "培训合格证" "开始日期" "结束日期" "学时" "类型" "考试日期" "考试成绩"] 
                     [:name :train-id :train-start :train-end :train-hour :type :exam-date :exam-score])])))

(defn mot-pn-train
  "省级主管机关管理的考评员培训
  @see pn-learn 考评员自己的培训视图"
  [request]
  (let [
;        count (with-mdb2 "esp" (fetch-count :pn-train))
        rt2 (with-esp- (fetch :pn-train :only [:train-start] :sort {:train-start 1}))
        yyyymm (frequencies (map #(-> % :train-start (leftback "-")) rt2))]
    (html
      [:h1 "考评员培训、考试管理"]
      [:h2 "培训时间，培训学时（不少于24个学时），培训类别，培训合格证号"]
      [:div "注：由省级交通运输主管机关、长江和珠江航务管理局按管辖范围负责组织实施培训、考试工作。"]
      [:br]
      (barf yyyymm {:title "各月份培训的考评员数量" :x "月份" :y "考评员人数"}) 
      [:h2 "尚无培训、考试记录的考评员："]
      (eui-tip "请点击查看考评员详情并录入培训、考试资料。") ; todo: 增加excel导入功能
      (result-html- (data- :pn-apply) [] [:name :type :mobile :_id] {:form "mot-pn-train-doc"})[:br]

;      [:h2 (format "已录入的考评员培训、考试记录（%s 条）：" count)]
;      (result-html- rt2 
;                    ["姓名" "培训合格证" "开始日期" "结束日期" "学时" "类型" "考试日期" "考试成绩"] 
;                    [:name :train-id :train-start :train-end :train-hour :type :exam-date :exam-score])
      
      (mot-pn-train-list 100)
      )))

(defn mot-pn-train-doc
  "mot录入考评员培训及考试结果"
  [id]
  (doc- :pn-apply id
        {:after 
         (let [fields [:name :type :admin :train-start :train-end :train-hour :train-id :exam-date :exam-score]
               r (with-oid- :pn-apply id)
               uid (:uid r)]
           (html [:h2 "录入考评员培训及考试结果："]
                 [:form#fm1 {} 
                  [:table 
                   (for [f fields]
                     (let [attr {:name (name f) :value (get r f)}]
                       [:tr 
                        [:td [:label (dd-meta f) "："]] 
                        [:td (case f
                               :type (eui-combo attr dd-type)
                               :admin (eui-combo attr dd-admin)
                               (:train-start :train-end :exam-date) (eui-datebox attr)
                               :train-hour (eui-numberspin (merge attr {:min 0 :max 100 :value 24}) )
                               :exam-score (eui-numberspin (merge attr {:min 0 :max 100 :value 85}) )
                               (eui-text attr))]]))]]
                 (eui-button {:onclick (format "esp_pn_train_save('%s')" uid)} "保存") ))}))

(defn pn-train-save
  "考评员培训、考试信息表单保存
  @id 考评员uid "
  [id request]
  (let [pn-uid id
        vars (query-vars2 request)
        vars2 (for [[k v] vars] [k (case k (:type :train-hour :exam-score) (to-int v) v)])]
    (insert- :pn-train (into {:uid pn-uid} vars2))
    "已保存"))

(defn mot-pn-exam
  "省级主管机关管理的考评员考试"
  []
  (let [rt2 (with-esp- (fetch :pn-train :only [:exam-score] :sort {:exam-score 1}))
        scores (into (sorted-map) (frequencies (map :exam-score rt2)))]
    (html
      [:h1 "考评员考试情况统计"]
      [:h2 "考试时间，考试成绩，是否合格，考试类别，"]
      [:div "注：交通运输部负责组织编写培训教材和考试大纲。"
       "省级交通运输主管机关、长江和珠江航务管理局按管辖范围负责组织实施培训、考试工作"] [:br]
      (linef scores {:title "各分数值的考评员数量" :x "分数" :y "考评员人数"} 1000) )))
  
(defn mot-org-eval
  "service: mot查看org的考评情况汇总"
  []
  (html
    [:h1 "考评机构考评情况汇总表"]
    (eui-tip "暂无记录")))
  
(defn mot-en-recheck
  "service：主管机关对企业进行附加考评"
  []
  (let [rs (with-esp- (fetch :org :only [:name]))
        r (apply array-map (flatten (for [r rs] [(:_id r) (:name r)])))]
    (html
      [:h1 "企业附加考评"]
      (name-cid-search-input :en) [:br][:br]
;      [:label "请输入企业证书号："]
;      (eui-text {:id "cid" :name "cid" :value "2012-2-1-0551-00844" :style "width:200px"}) [:br][:br] 
      [:label "附加考评原因："] (eui-combo {} dd-recheck) [:br][:br]
      [:label "指定附加考评机构："] (eui-combo {} r) [:br][:br]
      [:label "意 见： "] (eui-textarea {} "") [:br][:br]
      (eui-button {} "提 交") )))

(defn mot-sub-olap
  "service: 主管机关对下级机关的综合分析
  @type 'grade' 'type' "
  [type]
  (let [type (or type "grade")
        url (str "/c/esp/mot-sub-chart?type=" type)
        typ (keyword type)
        type-name ({:grade "达标等级" :type "业务类型"} typ)
        dd ({:grade dd-grade :type dd-type} typ)
        rt1 (with-mdb2 "esp" (mdb-group :en [:admin typ]))
        data (cross-data rt1 [:admin typ :count])]
    (html
      (eui-button {:onclick "layout_load_center('/c/esp/mot-sub-olap?type=grade')"} "主管机关——企业达标等级") (space 5)
      (eui-button {:onclick "layout_load_center('/c/esp/mot-sub-olap?type=type')"} "主管机关——企业业务类型")
      (eui-tip "请点击数据行或者数据列可得到该行或者该列的图形分析。")
      (cross-table data {:caption (format "各%s企业数量统计表" type-name)
                         :dim-top-name type-name
                         :dim-left-name "主管机关"
                         :f-dim-left (fn [v] (format "<font color=gray>%s</font>: %s" v (dd-admin v)))
                         :f-dim-top (fn [v] (format "<font color=gray>%s</font>: %s" v (dd (to-int v))))
                         :f-value (fn [v] (to-int v))
                         :sort? true }) 
      [:div#chart ]
      (html-js (format "cross_table_chart('%s')" url)) )))

(defn- mot-sub-chart-data
  " (mot-sub-chart-data :admin '02') 画:北京3个等级的数量图，(mot-sub-chart-data :grade '2') 画所有地区2级企业的数量图
  @type :grade :type
  @dim 点击的维度 :grade :type :admin
  @where 该维度的值，用字符串 "
  [type dim where]
  (let [dims [:admin type] 
        dim2 (if (= dim :admin) type :admin) ; 统计的维度
        dd (case dim2
             :admin (rest dd-admin) 
             :grade dd-grade
             :type dd-type)
        rs (with-mdb2 "esp" (mdb-group :en dims))
        rt (filter #(= where (dim %)) rs)
        m (into {} (for [{d dim2 c :count} rt] [d c])) ]
    (for [[k v] dd] [v (or (m (str k)) 0)])))

(defn mot-sub-chart
  "绘制一行或者一列的图表。
  @type 除了:admin之外的维度，可以是 'grade' 'type'
  @dim-top @dim-left 空或者形如 '01: 北京' '1: 一级企业' "
  [type dim-top dim-left]
  (let [type (keyword type)
        type-name ({:grade "达标等级" :type "业务类型"} type)
        dim-top? (nullity? dim-left)
        dim (if dim-top? type :admin)
        where (if dim-top? (left dim-top ":") (left dim-left ":"))
        data (mot-sub-chart-data type dim where)
        title (format "%s %s 企业数量" 
                      (if dim-top? "各主管机关" (right dim-left ": ")) 
                      (if dim-top? (right dim-top ": ") (str "各" type-name))) 
        x (if dim-top? "主管机关" type-name) ]
    (html 
      [:h1 "图表显示"]
      (barf (apply array-map (flatten data)) {:title title :x x :y "企业数量"}) )))

(defn mot-admin
  "service: 管理本主管机关直接下级的主管机关
  @todo "
  [request]
  (let [admin (mot-role request)
        mot (with-mdb2 "esp" (fetch-one :mot :where {:code admin}))
        rs (with-esp- (fetch :mot :where {:upper admin} :sort {:code 1}))]
    (html
      (eui-tip "管理本主管机关的直接下级主管机关。") 
      [:h1 (format "【%s】的直接下级主管机关：" (:name mot))]
      (when-not (= admin "01")
        (eui-button {:href "/c/esp/mot-admin-doc" :target "_blank" :style "margin:10px"} "新增下级主管机关"))
      (result-html- rs
                    ["代码编号" "名称" "详情"]
                    [:code :name :_id]
                    {:form "mot-admin-doc"}))))

(defn mot-admin-doc 
  "@id id是用户oid字符串，没有id则表示新增用户操作"
  [id request]
  (let [rs (when id (with-mdb2 "esp" (fetch-by-id :mot (object-id id)))) ; 本机构 
        upper (mot-role request)
        mot (with-mdb2 "esp" (fetch-one :mot :where {:code upper}))] ; 上级机构
    (html-body
      [:h1 (fmt "主管机关管理（%s）" (or (:name rs) "增加新主管机关"))]
      [:form#fm1 {:action "/c/esp/mot-admin-crud" :method "POST"}
       [:input {:name "oid" :type "hidden" :value id}]
       [:label {:for "upper"} "上级主管机关："]  
       [:input {:name "upper" :value (or (:upper rs) upper) :readonly true :style "background-color:#e5e5e5"}] 
       [:span (format " （注：%s-%s）" (:code mot) (:name mot))][:br]
       [:label {:for "code"} "主管机关代码："] 
       [:input {:name "code" :value (or (:code rs) (if (= upper "01") "" (str upper "xx")))}] 
       [:span (format " （注：省级主管机关代码为两位数；地市级主管机关代码以省级主管机关代码开头，后接两位数编号。）" upper)][:br]
       [:label {:for "name"} "主管机关名称："] 
       [:input {:name "name" :value (or (:name rs) "")}][:br]
       [:br]
       (if (nil? id)
         (html 
           (eui-button {:onclick "$('#fm1').attr('action','/c/esp/mot-admin-crud/add').submit()"} "新加主管机关") (space 5))
         (html
           (eui-button {:onclick "$('#fm1').attr('action','/c/esp/mot-admin-crud/update').submit()"} "更新主管机关") (space 5) 
           (eui-button {:onclick "$('#fm1').attr('action','/c/esp/mot-admin-crud/del').submit()"} "删除主管机关") (space 5) ))
       (eui-button-close) ] )))

(defn mot-admin-crud
  "mot下级主管机关的crud.
  @crud 'add' 'del' 'update' "
  [id request]
  (with-mdb2 "esp"
    (let [{oid :oid n :name code :code upper :upper} (query-vars2 request)
          mot (fetch-by-id :mot (when-not (nullity? oid) (object-id oid)))
          mot-new {:name n :code code :upper upper}]
      (case id
        "add" (if (fetch-one :mot :where {:code code})
                (format "代码为 %s 的下级主管机关已经存在，请填写不同的代码" code)
                (do (insert! :mot mot-new) (str "已增加新主管机关" n)))
        "del" (do (destroy! :mot mot) (str "已删除该主管机关" n))
        "update" (do (update! :mot mot mot-new) (str "已保存该主管机关" n))
        "未知动作" ))))

(defn mot-user-list
  "得到mot指定admin的用户
  @id :admin代码如'01' "
  [id request]
  (let [rs (when id (with-esp- (fetch :user :where {:admin id})))]
    (if (empty? rs)
      "（该下级主管机关尚无审核通过的注册用户。）" 
      (html
        (result-html- rs [] [:name :uid :_select] {:mot-user true}) ))))

(defn mot-user
  "service: 主管机关向下级主管机关委托代办工作
  @todo "
  [request]
  (let [admin (mot-role request)
        rs (with-esp- (fetch :mot :where {:upper admin}))]
    (html
      [:h1 "下级主管机关用户列表"]
      [:label {:for "admins"} "选择主管机关："] 
      (eui-combo {:id "admins" :onchange "ajax_load($('#users'), '/c/esp/mot-user-list/'+$('#admins').val())"} 
                 (map #(vector (:code %) (:name %)) rs)) [:br]
      [:label {} "用户列表："][:br][:br]
      [:div#users (mot-user-list (:code (first rs)) request)])))
  
(defn mot-org-refine
  []
  (refine :org))
  
(defn mot-en-refine
  []
  (refine :en))
  
(defn mot-resp-sum
  "service: 在主页面上显示待办事宜统计信息"
  []
  (let [items (-> cfg-frame-mot :nav first rest rest rest)
        sums [(tb-count :pn-apply) 0 
              (tb-count :org-apply) (tb-count :org-backup) 0 
              (tb-count :en-apply) (tb-count :en-apply) (tb-count :en-backup) 0
              (tb-count :hot)]]
    (html
      (eui-tip "待办事宜中个项目的数量提示：")
      [:ul 
       (for [i (range (count items)) :let [item (nth items i) title (first item) url (last item) sum (nth sums i)]]
         [:li {:style ""} 
          [:a {:href "#" :onclick (format "layout_load_center('/c/esp/%s')" url)}
           [:h2  
            (format "%s （<font color=%s>%s</font>）" title (if (zero? sum ) "lightgray" "red") sum)]]])] ) ))

(defn mot-portal
  "service: mot对首页进行维护. todo: 改为ajax提交 "
  [request]
  (html
    (input-form cfg-portal 
                {:title "首页栏目内容维护"
                 :action "/c/esp/mot-portal-save"
                 :buttons (html (eui-button-submit "fm1" {:ajax "/c/esp/mot-portal-save"}) (space 5) 
                                (eui-button-reset "fm1") ) })
    (fileupload-dialog) ))

(defn mot-portal-save
  [request]
  (let [vars (query-vars2 request)
        {rt :rt err :error} (input-check-require vars cfg-portal)]
    (html
      (if rt
        (str "成功提交：\n\n" vars) ; todo: 完成保存
        (str "如下必填字段尚未填写：\n\n" (join err "，"))))))
