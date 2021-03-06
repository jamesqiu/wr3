(ns ^{:doc "企业安全生产标准化管理系统 Enterprise Safety Production Standardization。db说明见package.html" }
     wr3.clj.app.esp)

;; 注：use 后的函数可从/c/esp/foo 调用
(use 'wr3.clj.app.espc 'wr3.clj.app.espc2 'wr3.clj.app.espconf); :reload)
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
        (in? fname ["pview" "plist"]) true ; pview在主页，不用登录就可以访问
        (= fname "mot-user-check-save") true ; ajax调用
        (in? fname ["verify" "verify-input" "verify-search"]) true ; 公众查验en/pn/org
        (.startsWith fname "rsql") (wr3role? request "mot") ; 只有mot用户能后台更改字段
        (= fname "clean") (wr3role? request "root")
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

(def pn---------- nil) ; 考评员

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
    
(defn- where- 
  "为pn-list en-list org-list 生成用户限制相关的where条件 "
  [request]
  (let [admin (user-admin request)]
    (where-admin (where-cid {:resp "yes"}) admin)))

(defn pn-list
  "service: 考评员列表
  @id name的pattern如'张' "
  [id skip request]
  (let [where (where- request)]
    (list- id :pn [] [:admin :name :org :type :cid :cdate :_id] {:skip (to-int skip 0) :where where})))

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
          (result-html- rs '["培训证书" "培训类型" "培训日期" "培训学时" "考试日期" "考试分数" "详情"]
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

(def org---------- nil) ; 考评机构

(defn org-apply
  "service: 考评机构申请导航"
  [request]
  (apply-nav- :org request))

(defn org-main
  "ajax: 考评机构首页导航：申请 + 考评待办" 
  [request]
  (html
    (org-apply request) [:hr]
    (apply-resp "en" request)))

(defn org-input
  "service：评审机构在线申请"
  [request]
  (apply-input- request :org))

(defn org-list
  "service: 考评机构列表
  @id name的pattern如'学校' "
  [id skip request]
  (list- id :org [] [:admin :name :type :grade :cid :cdate :_id] 
         {:skip (to-int skip 0) :where (where- request)})) 

(defn org-pn-archive
  "service: 考评机构-考评员档案管理"
  [request]
  (let [uid (wr3user request)
        where1 {:fulltime true :contract1 nil}
        where2 {:fulltime false :contract1 nil}
        count1 (with-mdb2 "esp" (fetch-count :pn :where (merge {:belong uid} where1)))
        count2 (with-mdb2 "esp" (fetch-count :pn :where (merge {:belong uid} where2)))
        rt-edu (sort-by :count > (with-esp- (mdb-group :pn [:edu])))
        rt-exam (sort-by :exam-date (with-esp- (mdb-group :pn-train [:exam-date])))
        ]
    (html
      [:h1 "考评员档案管理"]
      (eui-button {:plain "true" :iconCls "icon-sum"} "安全生产标准化考评员汇总表、登记表") [:br]
      (eui-button {:plain "true" :iconCls "icon-list"} "考评员学历和专业技术能力证明汇总表") [:br]
      (eui-button {:plain "true" :iconCls "icon-file"} "考评员培训情况汇总表（何时培训，多长时间，取得何种培训合格证书）") [:br] 
      [:h2 "本机构专兼职考评员："]
      (result-html- [{:fulltime true :count count1} {:fulltime false :count count2}] 
                    ["专兼职" "数量小计"] [:fulltime :count] {}) 
      [:h2 "考评员学历一览："]
      (result-html- rt-edu ["学历" "数量小计"] [:edu :count] {})
      [:h2 "考评员培训考试："]
      (result-html- rt-exam ["考试时间" "数量小计"] [:exam-date :count] {})
      
      )))

(defn org-pn
  "service: 本考评机构机构的所有考评员 "
  [request]
  (let [uid (wr3user request)
        rt (with-mdb2 "esp"
             (vec (fetch :pn :sort {:contract1 1 :fulltime -1} :where {:belong uid} )))]
    (html
      [:h1 (format "本机构当前在职考评人员 %s 名" (count (filter #(nil? (:contract1 %)) rt)))]
      [:div#hire {:style "margin:15px"}
       (eui-tip "受聘考评员需提供其登录认证U盘；已聘用的考评员可以点击详情查看后进行解聘。")
       "1、" (eui-button {:onclick "esp_org_pn_ukey()"} "读取受聘考评员登录认证U盘") [:br][:br]
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
  @id cid或pid或oid
  @type 'cid' 'pid' 为空则通过id查 "
  [id type request]
  (let [where (case  type "cid" {:cid id} "pid" {:pid id} {:_id (object-id id)}) 
        r (with-mdb2 "esp" (fetch-one :pn :where where))
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
        rt2 (with-esp- (fetch :pn-train :where {:_id {:$in (map #(object-id %) ids)}})) ]
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
        rs (with-esp- (fetch :org-apply :where (where-cid {:uid uid})))]
    (html
      [:h1 "考评机构资质证书"]
      (if (empty? rs)
        (eui-tip "还没有资质证书。")
        (html
          (result-html- rs '["证书号" "证书类型" "证书等级" "发证日期" "证书状态" "查看"] 
                        [:cid :type :grade :cdate :cstate :_id] {:form "docv/org-apply"}))))))

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
      {:js "app-esp.js"}
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
  [request]
  (let [uid (wr3user request)
        rs (with-esp- (fetch :en-apply :where {:orgid1 uid}))]
    (html
      [:h1 "企业考评情况汇总表"]
      [:h2 (count rs) "条考评记录"]
      (result-html- rs [] [:name :date :type2 :grade :score0 :score1] {})
      )))

(defn org-stand-view
  "org, mot查看org对en达标评估的结果
  @ids ids[0]:enid被评估企业的uid； ids[1]:达标结果文档的object-id"
  [ids request]
  (let [[enid oid _] ids
        doc (with-oid- :en-stand oid)
        type2 (:type2 doc)
        m (merge (:stand doc) {:role (wr3role request)})] 
    (stand- type2 enid m)))
  
(def en---------- nil) ; 企业

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
  [id skip request]
  (list- id :en [] [:admin :name :type2 :grade :cid :cdate :_id] 
         {:skip (to-int skip 0) :where (where- request)}))

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
        admin (or (user-admin request) "01")
        ;rs (with-esp- (fetch :user :where {:role "org" :admin admin :usable {:$ne "0"} :del {:$ne "0"} :pid {:$exists true}})) ; 共同主管机关的所有考评机构
        rs (with-esp- (fetch :org-apply :where (where-cid {:uid {:$exists true} :resp "yes"}) 
                             :sort {:grade 1 :cid 1}))
        r (first (with-esp- (fetch :en-apply :where {:uid uid} :sort {:date -1}))) ; 最近一次申请记录
        ] 
    (html
      [:h1 (format "%s主管的考评机构（%s 名）" (dd+ dd-admin admin) (count rs))]
      (eui-tip "请在如下的考评机构列表中自行选择两个。")
      (result-html- rs [] [:admin :name :type :grade :cid :contact :tel :_select] {}) [:br]
      (eui-button {:onclick "esp_en_select_org()"} "提 交")
      [:script (format "esp_mark_selected('%s')" (join (:orgid r) ","))] )))
           
(defn en-select-org-save
  "service: 保存企业所选2个考评机构的uid
  @id en的uid "
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

(def mot---------- nil) ; 主管机关

(defn- mot-subs
  "获取下一级主管机关列表
  @uppser 上级的代码如'02' 
  @with-upper? 包含上级及所有下级 "
  [upper & with-upper?]
  (let [where (if with-upper? {:code (re-pattern (str "^" upper))} {:upper upper})]
    (map (juxt :code :name) (with-esp- (fetch :mot :where where :sort {:code 1})))))

(defn mot-olap
  "公共service: 主管机关进行pn、org、en的查询搜索和统计分析
  @id 'pn' 'org' 'en' "
  [id request]
  (let [tb (keyword id) ; :pn :org :en
        nam (dd-form tb) ; 考评员 考评机构 交通运输企业
        admin (user-admin request)
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
       [:label "名称或者证书号："] (eui-text {:name "s" 
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
  (let [tb (apply-tb id) ; :pn :org :en 
        vars (query-vars2 request)
        filters (case tb :pn-apply [:admin :type] (:org-apply :en-apply) [:admin :type :grade])
        col-ids (case tb :pn-apply [:name :type :cid :admin :_id] (:org-apply :en-apply) [:name :type :grade :cid :admin :_id])
        s (:s vars)
        where (into {(if (wr3.util.Charsetx/hasChinese s) :name :cid) (re-pattern (:s vars))} 
                    (map #(if (= "0" (% vars)) nil [% (% vars)]) filters))
        where (where-cid where)
        rt (with-esp- (fetch tb :limit 100 :where where))
        count (count rt)]
    (html
      (eui-tip (if (= 100 count) "提示：结果太多（≥100），请适当增加搜索条件。" 
                 (format "搜索到 %s 条符合条件的结果。" count)))
      (result-html- rt [] col-ids {:form (str "docv/" id "-apply")}))))

(defn- mot-dim-group-
  [tb dim]
  (let [where (where-cid {})
        rs (with-esp- (distinct-values tb (name dim) :where where))]
    (for [e (sort rs)] {dim e :count (tb-count tb {:where (into {dim e} where)})})))

(defn mot-dim
  "service: mot对pn、en、org的维度统计图表
  @id 'pn' 'org' 'en' 
  @dim 维度如'admin' 'grade' 'type' 'edu' "
  [id dim request]
  (let [tb (apply-tb id)
        nam (dd-form tb)
        dim (keyword dim)
        dim-nam (dd-meta dim)
        rt (mot-dim-group- tb dim);(with-esp- (mdb-group tb [dim]))
        dd (case dim :admin dd-admin :grade dd-grade :type dd-type :edu str)
        m (for [r rt] (let [v0 (dim r)
                            v (case dim (:admin :edu) v0 (:grade :type) (to-int v0))] 
                        [(dd v) (:count r)])) 
        ]
    (html
      [:h3 (format "【%s】-【%s数量】分析图表：" dim-nam nam)]
      (if (empty? m) [:h2 "没有数据。"]
        (html
          (barf (apply array-map (flatten m)) {:x (str dim-nam) :y (str nam "数量")})
          (pief (apply array-map (flatten m)) {})
          (result-html- rt [dim-nam "数量小计"] [dim :count] {}))))))

;;-- mot-(pn en org)-apply[-resp]
(defn- mot-apply-
  "主管机关对pn、org、en的申请回应。
  @type 即 :pn :org :en
  @oid 申请文档的object-id
  @orgid-as-select 标识把:orgid显示为可选列表（2选1） "
  [typ oid & orgid-as-select]
  (let [tb (apply-tb (name typ)) 
        rs (with-oid- tb oid)
        deleted (or (:del rs) "0") ; '1': 此申请已经标记为删除, '0': 正常
        f-click (fn [y-n] (format "esp_mot_apply('%s','%s','%s')" (name typ) oid y-n)) 
        js (format "ajax_post('/c/esp/apply-resp-del/%s/%s/%s', function(){window.location.reload()} )" (name typ) oid deleted)]
    (doc- tb oid
          {:rs rs
           :before (html [:h1 {:align "center"} (dd-cert typ) "申请审批"]
                         (if (= deleted "1")
                           (eui-button {:onclick js :iconCls "icon-undo" :style "margin:10px"}
                                       "恢复此记录")
                           (eui-button {:onclick js :iconCls "icon-cancel" :style "margin:10px"} 
                                       "删除此申请记录（放入垃圾箱）") ))
           :after
           (fn [rt]
             (html
               (case typ
                 :pn (let [pid (:pid rt) t (:type rt)
                           rs (with-esp- (fetch :pn-train :where {:pid pid :type t}))]
                       (html [:h2 "该考评员“" (dd-type (to-int t 0)) "”类型的培训、考试记录："]
                             (result-html- rs [] [:name :type :train-id :train-hour :exam-date :exam-score]) [:br][:br]
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
           :orders (map second ({:pn cfg-apply-pn :org cfg-apply-org :en cfg-apply-en} typ))
           })))

(defn mot-pn-direct
  "app: 填写直接颁发的信息"
  []
  (let []
    (html-body
      {:js "app-esp.js"}
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
  (let [tb (apply-tb id) ; :pn-apply :org-apply :en-apply
        vars (query-vars2 request) 
        oid (:oid vars)]
    (update- tb {:_id (object-id oid)}
             (fn [r] (merge (dissoc vars :oid) {:respdate (datetime)}))) ; :oid和:_id值相同，没必要保存
    "申请已处理"))

(defn mot-en-review
  "主管机关对org评审过的企业考评结论审核"
  [request]
  (let [admin (user-admin request)
        where (where-apply  {:resp-eval {:$ne nil}})
        where (where-admin where admin)        
        rs (with-esp- (fetch :en-apply :where where))
        rs2 (filter #(> (days (:respdate-review %)) 7) 
                    (with-esp- (fetch :en-apply :where (merge {:resp-review "yes"} where))))]
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
  (let [rs (with-esp- (fetch :en-apply :where (where-cid {})))]
    (html
      [:h2 "已达标企业："] 
      (eui-button {:href "/c/esp/mot-en-export/exp-达标企业列表.csv?content-type=text/csv&charset=GBK" 
                   :target "_blank" :style "margin: 10px"} "导出电子表格")
      (if (empty? rs)
        (eui-tip "无已达标企业") 
        (result-html- 
          rs ["企业名称" "达标类别" "达标等级" "指定的考评机构" "详情"] 
          [:name :type2 :grade :orgid1 :_id] {:form "mot-en-review-doc"}))
      ))) 

(defn mot-en-export
  "导出列表"
  []
  (let [rs (with-esp- (fetch :en-apply :where (where-cid {})))
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
  [skip request]
  (let [admin (user-admin request)
        skip (to-int skip 0)
        where (where-admin {:pid {:$exists true}} admin)
        count1 (tb-count :pn-train {:where where})
        rt2 (with-esp- (fetch :pn-train :skip skip :limit 100 :sort {:train-start 1} :where where))]
    (html
      [:div#pn_train_list
       [:h2 (format "已录入的考评员培训、考试记录（%s 条）：" count1)]
       (pager-html count1 skip "esp_pager('/c/esp/mot-pn-train-list', $('#pn_train_list'))")
       (result-html- rt2 
                     ["主管机关" "姓名" "培训合格证" "开始日期" "结束日期" "学时" "类型" "考试日期" "考试成绩"] 
                     [:admin :name :train-id :train-start :train-end :train-hour :type :exam-date :exam-score])])))

(defn mot-pn-train
  "省级主管机关管理的考评员培训
  @see pn-learn 考评员自己的培训视图"
  [request]
  (let [rt2 (with-esp- (fetch :pn-train :only [:train-start] :sort {:train-start 1}))
        yyyymm (frequencies (map #(-> % :train-start (leftback "-")) rt2))
        admin (user-admin request)]
    (html
      [:h1 "考评员培训、考试管理"]
      [:h2 "培训时间，培训学时（不少于24个学时），培训类别，培训合格证号"]
      [:div "注：由省级交通运输主管机关、长江和珠江航务管理局按管辖范围负责组织实施培训、考试工作。"]
      [:br]
      (if (empty? rt2) [:h2 "没有数据。"]
        (barf yyyymm {:title "各月份培训的考评员数量" :x "月份" :y "考评员人数"})) 
      [:h2 "尚无培训、考试记录的考评员："]
      (eui-tip "请点击查看考评员详情并录入培训、考试资料。") ; todo: 增加excel导入功能
      (result-html- (data- :pn-apply {:where (where-apply (where-admin {} admin))}) [] 
                    [:admin :name :type :mobile :_id] {:form "mot-pn-train-doc"})[:br]
      (mot-pn-train-list 0 request) )))

(defn mot-pn-train-doc
  "mot录入考评员培训及考试结果"
  [id]
  (doc- :pn-apply id
        {:after 
         (let [fields [:name :type :admin :train-start :train-end :train-hour :train-id :exam-date :exam-score]
               r0 (with-oid- :pn-apply id)
               {pid :pid typ :type} r0
               r (with-mdb2 "esp" (fetch-one :pn-train :where {:pid pid :type (or typ "1")}))]
           (html [:h2 "录入考评员培训及考试结果："]
                 [:form#fm1 {} 
                  [:table 
                   (for [f fields]
                     (let [attr {:name (name f) :value (get (case f (:name :type :admin) r0 r) f)}
                           f-number (fn [n0] (eui-numberspin 
                                               (merge attr {:min 0 :max 100 :increment 1 :value (or (:value attr) n0)})))]
                       [:tr 
                        [:td [:label (dd-meta f) "："]] 
                        [:td (case f
                               :type (eui-combo attr dd-type)
                               :admin (eui-combo attr dd-admin)
                               (:train-start :train-end :exam-date) (eui-datebox attr)
                               :train-hour (f-number 24)
                               :exam-score (f-number 85)
                               (eui-text attr))]]))]]
                 (eui-button {:onclick (format "esp_pn_train_save('%s')" pid)} "保存") ))}))

(defn pn-train-save
  "考评员培训、考试信息表单保存
  @id 考评员uid "
  [id request]
  (let [pid id
        vars (query-vars2 request)
        vars2 (for [[k v] vars] [k (case k (:train-hour :exam-score) (to-int v) v)])]
    (insert- :pn-train (into {:pid pid} vars2))
    "已保存，请关闭"))

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
      (if (empty? rt2) [:h2 "没有数据。"]
        (linef scores {:title "各分数值的考评员数量" :x "分数" :y "考评员人数"} 1000) ))))
  
(defn mot-org-eval
  "service: mot查看org的考评情况汇总"
  []
  (let [rs (with-esp- (fetch :en-apply :where {:orgid1 {:$exists true}} :sort {:orgid1 1} :limit 100))]
    (html
      [:h1 "考评机构考评情况汇总表"]
      [:h2 (format "%s 条考评记录：" (count rs))]
      (result-html- rs [] [:orgid1 :name :type2 :grade :date :admin] {})
      )))
  
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
        rt1 (with-esp- (mdb-group :en [:admin typ]))
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
  (let [admin (user-admin request)
        mot (with-mdb2 "esp" (fetch-one :mot :where {:code admin}))
        rs (with-esp- (fetch :mot :where {:upper admin} :sort {:code 1}))]
    (html
      (eui-tip "管理本主管机关的直接下级主管机关。") 
      (when (= admin "01") (eui-button {:href "/c/esp/mot-admin2" :target "_blank"} "查看全部主管机关（含地市级）"))
      [:h1 (format "【%s】的直接下级主管机关：" (:name mot))]
      (when-not (= admin "01")
        (eui-button {:href "/c/esp/mot-admin-doc" :target "_blank" :style "margin:10px"} "新增下级主管机关"))
      (result-html- rs
                    ["代码编号" "名称" "详情"]
                    [:code :name :_id]
                    {:form "mot-admin-doc"}) [:br]
      (eui-button {:href "/c/esp/mot-admin-doc" :target "_blank"} "新增下级主管机关"))))

(defn mot-admin-doc 
  "@id id是用户oid字符串，没有id则表示新增用户操作"
  [id request]
  (let [rs (when id (with-mdb2 "esp" (fetch-by-id :mot (object-id id)))) ; 本机构 
        upper (user-admin request)
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
           (eui-button {:onclick "$('#fm1').attr('action','/c/esp/mot-admin-crud/add').submit()"} "添加主管机关") (space 5))
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
  "app: mot得到选定某主管机关下的所有用户. todo: 只列出没有停用的
  @id :admin代码如'01' "
  [id request]
  (let [rs (when id (with-esp- (fetch :user :where {:admin id :del {:$ne "1"} :usable {:$ne "0"} :role "mot" 
                                                    :pid {:$exists true}} :limit 1000)))]
    (if (empty? rs)
      "（该下级主管机关尚无审核通过的注册用户。）" 
      (html
        [:h2 (format "用户列表（%s 名）：" (count rs))]
        (result-html- rs [] [:admin :name :uid :_id] 
                      {:show-uid? true :form "mot-fn-doc"}) ))))

(defn mot-fn
  "service: mot向下级主管机关用户委托代办工作。
  @todo 为下级用户配置管理菜单（工作量大的条目如考评员、企业审批） " 
  [request]
  (let [admin (user-admin request)
        dd (if (= admin "01") dd-admin (mot-subs admin true))
        js "ajax_load($('#users'), '/c/esp/mot-user-list/'+$('#admins').val())"]
    (html
      (eui-tip "请选择某个主管机关用户后，进行工作内容委托。")
      [:h1 "下级主管机关用户列表"]
      [:label {:for "admins"} "选择主管机关："] 
      (eui-combo {:id "admins" :onchange js} dd) [:br]
      [:div#users]
      [:script js])))
  
(defn mot-fn-doc
  "调整一个主管机关用户的委托功能。 "
  [id request]
  (let [admin (user-admin request)
        dd dd-menu2
        button (eui-button-submit "fm1" {:ajax (format "/c/esp/doc-save/user/%s" id)})
        rs (with-oid- :user id)
        fn-join (fn [r f] (join (split (f r) "&") "," {:pre "[" :post "]" :quo "\""}))
        [menu-selected type-selected grade-selected] (map #(fn-join rs %) [:fnmenu :fntype :fngrade])
        ]        
    (doc- :user id 
          {:onload (format "esp_mot_menu_doc(%s, %s, %s)" menu-selected type-selected grade-selected)
           :rs rs
           :after (html 
                    [:input#fnMenuText {:type "hidden" :value ""}] ; 用于保存fnmenu选择的值                    
                    [:input#fnTypeText {:type "hidden" :value ""}] ; 用于保存fntype选择的值                    
                    [:input#fnGradeText {:type "hidden" :value ""}] ; 用于保存fngrade选择的值                    
                    (input-form [["委托管理的功能" :fnmenu {:t dd}]
                                 ["委托管理的业务" :fntype {:t dd-type}]
                                 ["委托管理的级别" :fngrade {:t dd-grade}]
                                 ] {:title "主管机关工作委托" :buttons button :require-hide? true})
                    (eui-tip "提交提示“完成更新”后，请关闭此页，并刷新列表查看更改结果。")) })))
  
(defn mot-user-admin
  "mot对注册用户进行管理：指定admin、启用、停用等. "
  [request]
  (let [js "ajax_load($('#ulist'), '/c/esp/users-list/'+$('#utype').val())"]
    (html
      (eui-tip "管理注册了登录认证U盘的用户：审批；停用/启用；调整主管机关（即：部分注册了登录认证U盘的用户申请时填写的省市或者主管机关可能有误，需要再次进行调整指定。）") [:br]
      [:h2 [:a {:href "/c/esp/mot-user-check" :target "_blank"} "待审批的用户"]] [:br]
      [:label "选择登录认证U盘用户类型："] 
      (eui-combo {:id "utype" :style "width:150px" :onchange js} dd-role) (space 10)
      (eui-button {:iconCls "icon-reload" :onclick js} "刷 新")
      [:div#ulist (users-list request "mot")])))

(defn mot-user-doc
  "调整一个用户的主管机关，启用/停用该用户
  @id 文档 object-id 字符串 "
  [id request]
  (let [admin (user-admin request) ; mot用户的主管机关号
        rs (with-oid- :user id)
        dd (case admin "01" dd-admin (apply array-map (reduce into (mot-subs admin true))))
        button (html (eui-button-submit "fm1" {:ajax (format "/c/esp/doc-save/user/%s" id)}) (space 5)
                     (eui-button-reset "fm1"))
        js (format "if(confirm('确定要删除？')){ ajax_post('/c/esp/mot-user-del/%s', function(){window.close()}) }" id)]
    (doc- :user id 
          {:rs rs
           :before (eui-button {:onclick js :iconCls "icon-cancel" :style "margin:10px"} 
                                       "删除此用户（放入垃圾箱）") 
           :after (html 
                    (input-form [["调整主管机关" :admin {:t dd :v (:admin rs)}]
                                 ["可用/停用" :usable {:t {"1" "&nbsp; 可用 &nbsp;" "0" "&nbsp; 停用 &nbsp;"} :v (:usable rs)}]
                                 ["操作/只读" :readonly {:t {"0" "正常操作" "1" "仅可查阅"} :v (:readonly rs)}] 
                                 ] {:title "设置管理该用户" :buttons button :require-hide? true})
                    (eui-tip "提交提示“完成更新”后，请关闭此页，并刷新列表查看更改结果。")) 
           :show-uid? true})))
  
(defn mot-user-del
  "service：删除:user表中一个登录认证U盘用户，标志del
  @id 文档 object-id 字符串 "
  [id]
  (do
    (update- :user {:_id (object-id id)} {:del "1"})
    "已删除该用户，关闭后请刷新"))

(require 'wr3.clj.db)
(require 'wr3.clj.app.espreg)

(defn mot-user-check
  "app: 列出已经注册发送给bjca，但未审批进入:user表的所用用户. "
  [request]
  (let [admin (user-admin request)]
    (if (or (not= admin "01") (user-readonly? request)) 
      (html-body (eui-tip "登录认证U盘用户目前由交通运输部管理员进行审批。")
                 (eui-button-close))
      (let [sql (str "select *,TradeGuid as _id from userregister "
                     " where sendflag='1' and (checkflag is null or checkflag<>'del') "
                     " order by TradeGuid desc ")
            rs (wr3.clj.db/select-all "espdev" sql)]
        (html-body
          [:h1 "已完成登录认证U盘注册申请，待主管机关审批的用户"]
          (eui-tip "用户拿到登录认证U盘后，还必须在此进行审核通过，才能登录系统。")
          [:p {:align "center"} (eui-button-reload) (space 5)
           (eui-button {:href "/c/esp/mot-user-check-all" :target "_blank" 
                        :iconCls "icon-list" :title "一键审核通过所有待处理登录认证U盘用户。"} "全部通过")]
          (result-html- rs '[类型 省 名称 证件号 电话号码 联系人 联系人手机 注册日期 审批标志 详情] 
                        [:usertype :province :commonname :paperid 
                         :telephonenumber :transname :transmobile 
                         :registerdate :checkflag :_id] 
                        {:form "mot-user-check-doc"
                         :admin dd-role}) 
          [:p {:align "center"} (eui-button-reload)] ))))) 

(defn mot-user-check-doc
  "app: 审批某一个申请过UKey的用户
  @id 注册rdb表中的TradeGuid字段内容 "
  [id]
  (let [fs '{commonname name usertype role paperid pid transname contact 
             telephonenumber tel transmobile mobile registerdate date PostalAddress address}
        sql (format "SELECT %s FROM userregister where TradeGuid='%s' " 
                    (join (for [[k v] fs] (str k " as " v)) ",") id)
        r (wr3.clj.db/select-row wr3.clj.app.espreg/db-reg sql)
        uid (str (:role r) "-" (:pid r))
        r1 (first (with-uid- :user uid))]
    (html-body 
      [:h2 "【登录认证U盘申请子系统】："]
      (doc2- :user nil {:rs r}) [:hr]
      (if r1
        (html [:h2 "【标准化主系统】已审批导入过持此证件号的用户："] 
              (doc2- :user nil {:rs r1 :show-uid? true}))
        (html [:h2 "【标准化主系统】中尚未发现持此证件号的用户："] [:br]
              (eui-button {:onclick (format "ajax_post('/c/esp/mot-user-check-del/%s', function(){window.close()}); " id) 
                           :iconCls "icon-cancel"} "删除该用户") (space 5)
              (eui-button {:onclick (format "ajax_post('/c/esp/mot-user-check-save/%s')" id) :iconCls "icon-ok"} 
                          "导入该用户"))) )))

(defn mot-user-check-del
  "service: 更新sqlserver的userregiester表某个注册用户的checkflag字段为del。
  @id 注册rdb表中的TradeGuid字段内容 "
  [id]
  (let [sql2 (format "UPDATE userregister set checkflag='del' where TradeGuid='%s' " id)]
    (do 
      (wr3.clj.db/update wr3.clj.app.espreg/db-reg sql2)
      (format "完成删除，请关闭后刷新。"))))
  
(defn mot-user-check-save
  "service: 更新sqlserver的userregiester表某个注册用户的checkflag字段为pass，并导入该用户到esp系统mdb中。
  @id 注册rdb表中的TradeGuid字段内容 "
  [id]
  (let [id (or id "")
        sql (format "SELECT * FROM userregister where TradeGuid='%s'" id)
        r (wr3.clj.db/select-row wr3.clj.app.espreg/db-reg sql) ; 无结果返回nil
        {nam :commonname pid :paperid role :usertype} r]
    (cond
      (nil? r) (format "err: 未找到TradeGuid=%s" id)
      (first (with-pid- :user pid)) (format "err: pid=%s的用户已存在" pid)
      :else (let [uid (str role "-" pid)
                  m {:name nam :pid pid :role role :uid uid :tel (:telephonenumber r) :address (:postaladdress r) 
                     :province (:province r) :date-import (datetime)}
                  admin (:admin (first (with-pid- (apply-tb role) pid))) ; 得到初次报名申请时填写的admin
                  m (case role 
                      "pn" (into m {:admin admin :mobile (:telephonenumber r)}) 
                      (into m {:admin admin :contact (:transname r) :legalp (:leagalperson r) :mobile (:transmobile r) })) 
                  sql2 (format "UPDATE userregister set checkflag='pass' where TradeGuid='%s' " id) ]
              (insert- :user m)
              (wr3.clj.db/update wr3.clj.app.espreg/db-reg sql2)
              (format "已经将 %s 导入并生效 " nam)))))

(defn mot-user-check-all
  "service: 更新sqlserver的userregiester表所有未处理注册用户的checkflag字段为pass，并导入这些用户到esp系统mdb中。"
  []
  (let [sql "select * from userregister where checkflag is null"
        rs (wr3.clj.db/select-all wr3.clj.app.espreg/db-reg sql)]
    (html-body
      [:h1 "审批通过下列登录认证U盘用户："] 
      (eui-tip "关闭该窗口后，刷新查看结果。")
      [:p {:align "center"} (eui-button-close)]
      (for [r rs] 
        (let [{nam :commonname pid :paperid role :usertype tradeguid :tradeguid} r
              uid (str role "-" pid)]
          (when (empty? (with-uid- :user uid)) ; 为空表示尚未导入
            (do (mot-user-check-save tradeguid)
              [:p "导入：" uid "，" nam]) ))) )))

(defn mot-org-refine
  "mot下发整改通知给org"
  []
  (refine :org))
  
(defn mot-en-refine
  "mot下发整改通知给en"
  []
  (refine :en))
  
(defn mot-resp-sum
  "service: 在主页面上显示待办事宜统计信息" 
  [request]
  (let [items (-> cfg-frame-mot :nav first rest rest rest)
        admin (user-admin request)
        sums [(apply map + (map #(count-reg % admin) ["pn" "en" "org" "mot"])) ; '(7 13)
              (count-apply "pn" admin) 
              (cert-renew-count "pn" admin) 
              (count-apply "org" admin) 
              (tb-count :org-backup) 
              (cert-renew-count "org" admin) 
              (count-apply "en" admin {:resp-review nil}) ; 还没被mot处理过的，org有可能处理过
              (count-apply "en" admin {:resp-eval {:$ne nil}}) ; 被org处理过的，mot有可能处理过
              (tb-count :en-backup) 
              (cert-renew-count "en" admin)
              (tb-count :hot)]
        f (fn [n] (format "<font color=%s>%s</font>" (if (zero? n) "lightgray" "red") n))]
    (html
      (eui-tip "待办事宜中个项目的数量提示：")
      [:ul 
       (for [i (range (count items)) :let [item (nth items i) title (first item) url (nth item 2) n (nth sums i)]]
         [:li [:a {:href "#" :onclick (format "layout_load_center('/c/esp/%s')" url)}
               [:h2 title (if (sequential? n) 
                            (format "（%s/%s）" (f (first n)) (last n))
                            (format "（%s）" n) )]]])] ) ))

(defn mot-portal-list
  "service: 列出所有的portal项目"
  []
  (let [rs (with-esp- (fetch :portal :limit 100 :where {:pdel {:$ne "1"}} :sort {:ptype 1 :pno 1 :date -1}))]
    (result-html- rs {:pno "排序号"} [:ptype :ptitle :date :pno :_id] {:form "mot-portal-doc"})))

(defn mot-portal-doc
  "app：显示一条首页栏目，修改保存或删除"
  [id]
  (let [r (with-oid- :portal id)
        js (format "ajax_post('/c/esp/doc-save/portal/%s?pdel=1', function(){ window.close() })" id)]
    (html-body
      (input-form (cfg-set-values- cfg-portal r)
                  {:title "首页栏目内容维护"
                   :buttons (html (eui-button-submit "fm1" {:ajax (str "/c/esp/doc-save/portal/" id)}) (space 5)
                                  (eui-button-reset "fm1") (space 5)
                                  (eui-button {:onclick js :iconCls "icon-cancel"} "删除本条内容") (space 5)
                                  (eui-button-close))})
      )))

(defn mot-portal
  "service: mot对首页进行维护的管理界面. "
  [request]
  (let [admin (user-admin request)
        js-update "ajax_post('/c/esp/mot-portal-update')"]
    (if (or (not= admin "01") (user-readonly? request)) 
      (html (eui-tip "首页内容目前由交通运输部安监处用户进行维护。"))
      (html
        (input-form cfg-portal
                    {:title "首页栏目内容维护"
                     :action "/c/esp/mot-portal-save"
                     :buttons (html (eui-button-submit "fm1" {:ajax "/c/esp/mot-portal-save"}) (space 5) 
                                    (eui-button-reset "fm1") ) })
        (fileupload-dialog) 
        [:hr]
        (eui-button {:style "margin:10px" :iconCls "icon-reload" 
                     :onclick "ajax_load($('#plist'), '/c/esp/mot-portal-list')"} "刷 新")
        (eui-button {:onclick js-update} "根据新栏目内容重新生成主页" ) 
        [:div#plist (mot-portal-list)]))))

(defn mot-portal-save
  "service: 保存portal的一条item"
  [request]
  (let [uid (wr3user request)
        vars (query-vars2 request)
        {rt :rt err :error} (input-check-require vars cfg-portal)]
    (html
      (if rt
        (do (insert- :portal (into {:uid uid :date (datetime)} vars))
          (str "成功提交\n\n“" (:ptitle vars) "”"))
        (str "如下必填字段尚未填写：\n\n" (join err "，"))))))

(require '[wr3.clj.file :as file])

(defn mot-portal-update
  "service: 备份并替换/esp/index.html文件"
  [request]
  (let [ffrom (real-path request "/esp/index.html")
        fto (real-path request (format "/esp/index-%s.html" (System/currentTimeMillis)))
        s (portal-gen)]
    (file/file-rename ffrom fto)
    (file/file-set-text ffrom s "UTF-8")
    "已生成主页。"))

(defn mot-log
  "app: 管理员查看系统日志 "  
  [request]
  (let [admin (user-admin request)]
    (if (or (not= admin "01") (user-readonly? request)) 
      (html (eui-tip "系统日志目前由交通运输部安监处用户进行管理。"))
      (let [rs (with-esp- (fetch :log :where {:type "login"} :limit 100 :sort {:date -1}))]
        (html
          [:h1 "系统日志："]
          [:p [:form#fm1 {}
               [:label "日志类型："] (eui-combo {:name "type"} dd-log) (space 5)
               [:label {:title "可按退格键清空"} "日期："] (eui-datebox {:name "date" :value "" :required false}) (space 5)
               [:label "证件号："] (eui-text {:name "pid" :title "可输入完整证件号或部分进行匹配"})]]
          [:p (eui-button {:onclick "ajax_load($('#logs'), '/c/esp/logs?'+$('#fm1').serialize())"
                           :iconCls "icon-reload"} "查询") (space 5) 
           (eui-button-reset "fm1") (space 5) 
           (eui-button {:href "/c/esp/logs-olap" :target "_blank" :iconCls "icon-bar"} "记录统计")]
          [:div#logs (logs "login" "" "")] )))))

