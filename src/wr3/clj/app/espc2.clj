(ns wr3.clj.app.espc2)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.nosql 'wr3.clj.u 'wr3.clj.n 'wr3.clj.tb)
(use 'wr3.clj.app.espconf)
(use 'somnium.congomongo 'hiccup.core)
(use 'clojure.contrib.json)
(use 'wr3.clj.app.espc)

(defn- cert-input-
  [typ]
  (case typ "pn" (html (eui-text {:name "yyyy" :style "width:50px"} ) "—"
                       (eui-text {:name "c" :style "width:20px"} ) "—"
                       (eui-text {:name "ta" :style "width:30px"} ) "—"
                       (eui-text {:name "xxxxxx" :style "width:70px"} ) )
    (html (eui-text {:name "yyyy" :style "width:50px"} ) "—"
          (eui-text {:name "ta" :style "width:30px"} ) "—"
          (eui-text {:name "xxxxxx" :style "width:70px"} ))))

(defn who
  "ajax: 非mot用户（pn/en/org）查询其他用户的信息。注：证书形如pn的YYYY—C—TA—XXXXXX，en/org的YYYY—TA—XXXXXX
  @id 'pn'/'en'/'org' "
  [id request]
  (let [typ id
        type-name (dd-role typ)
        cert-name (dd-cert (keyword typ))
        name2 (name-label typ)]
    (html 
      [:h1 type-name "信息查询"] 
      (eui-tip (format "请输入%s%s 或者 %s编号进行查询" type-name name2 cert-name))
      [:form#fm1 {} 
        [:p [:label type-name name2 "："]  (eui-text {:name "name"})]
        [:p [:label cert-name "编号："]   (cert-input- typ) [:br]]
        [:p (eui-button {:onclick (format "ajax_load($('#result'), '/c/esp/who-search/%s?'+$('#fm1').serialize())" typ) 
                         :iconCls "icon-ok"} "查询") (space 5) (eui-button-reset "fm1")]
       ] 
      [:div#result])))

(defn who-search
  "ajax: 非mot用户（pn/en/org）查询其他用户的信息"
  [id request]
  (let [typ id
        {nam :name yyyy :yyyy c :c ta :ta xxxxxx :xxxxxx} (query-vars2 request)
        cid (case typ "pn" (format "%s-%s-%s-%s" yyyy c ta xxxxxx) (format "%s-%s-%s" yyyy ta xxxxxx))
        tb (apply-tb typ);(keyword typ)
        rt (with-esp- (fetch tb :where {:$or [{:name nam} {:cid cid}] :cid {:$exists true}}))
        n (count rt)
        role (wr3role request)
        fields (cond
                 (and (= role "org") (= typ "pn")) [:name :type :pid :date :admin :_id]
                 (and (= role "org") (= typ "en")) [:name :type2 :admin]
                 (and (= role "en") (= typ "pn")) [:name :photo :admin]
                 (and (= role "en") (= typ "org")) [:name :type :admin]
                 (and (= role "pn") (= typ "org")) [:name :type :admin]
                 (and (= role "pn") (= typ "en")) [:name :type2 :admin]
                 )]
    (html [:h2 "查询到" n "条记录"]
          (when (not= 0 n) (result-html- rt [] fields {:form "docv/pn"})))))

(defn logs
  "得到日志列表
  @type log类型
  @pid 操作员pid模式
  @date 日期时间的模式"
  [type pid date]
  (let [where {:type type :date1 (re-pattern date) :pid (re-pattern pid)}
        rs (with-esp- (fetch :log :limit 100 :where where :sort {:date -1}))]
    (result-html- rs {:pid "证件号" :no "U盘号" :msg "信息"} [:uid :role :pid :no :date :msg] 
                  {:admin dd-role}) ))

(require '[wr3.clj.chart :as chart])

(defn logs-olap
  "app: 登录、审批日志时间-数量图"
  []
  (let [rs (with-esp- (mdb-group :log [:date1]))
        rt (gen-array-map (for [{d :date1 c :count} rs] [d c]))]
    (chart/linef rt {:title "登录、审批操作日志" :x "日期" :y "操作数"} 1350 650) ))

(defn users-list
  "app: 根据mot用户的权限从:user表中得到不同的ukey用户列表；用于 esp/mot-user-admin
  @admin 主管机关代码，'01' 可以看到所有；其他只能看到自己属下及pn/org/en用户
  @id user-type 用户类型 'mot' | 'pn' | 'en' | 'org' "
  [request id]
  (let [admin (user-admin request)
        role id
        where (case admin "01" {} {:admin (re-pattern (str "^" admin))})
        where (into {:role role :pid {:$exists true} :del {:$ne "1"}} where)
        vars (query-vars2 request)
        skip (or (:skip vars) "0")
        onchange "ajax_load($('#ulist'), '/c/esp/users-list/'+$('#utype').val()+'?skip='+$('#pagers').val())"
        count1 (with-mdb2 "esp" (fetch-count :user :where where))
        rs (with-esp- (fetch :user :skip (to-int skip) :limit 100 :where where :sort {:admin 1 :_id -1 }))]
    (html
      [:h1 (format "登录认证U盘用户——%s用户调整" (dd-role role))]
      (pager-html count1 skip onchange) [:br]
      (result-html- rs {:pid "证件号" :contact "联系人" :mobile "联系人手机"} 
                    [:name :admin :contact :mobile :pid :uid :usable :_id] 
                    {:admin (merge dd-role dd-admin)
                     :form "mot-user-doc"
                     :show-uid? true
                     :readonly? (user-readonly? request)}))))

;;--- portal
(defn- portal-data-li-
  [typ limit]
  (let [rs (with-esp- (fetch :portal :limit limit :where {:ptype (str typ) :pdel {:$ne "1"}} :sort {:pno 1 :date -1}))]
    (html
      (for [{title :ptitle link :link content :content file :file id :_id pno :pno} rs] 
        [:li (cond 
               (not-nullity? link) [:a {:href link :target "_blank"} title]
               (not-nullity? content) [:a {:href (str "/c/esp/pview/" id) :target "_blank"} title]
               :else title)
         (when (not-nullity? file) [:a {:href file} "（附件）"])
         (when (and (= typ 4) (= pno "0")) [:img {:src "/esp/img/new.gif"}]) ]) )))

(defn- portal-items
  "根据portal type从mdb库得到portal条目；除了“相关下载”外的其他显示项
  @portal-type dd-portal的key：1/2/3/4 "
  [portal-type]
  (let [limit (case portal-type (1 2 3) 7 12) ]
    [:ul (portal-data-li- portal-type limit) ] ))

(defn- portal-downloads
  "比较特殊不带[:ul ]包括的“相关下载” 显示项"
  []
  (portal-data-li- "5" 4))

(defn- portal-links
  []
  (html
    [:div.links_item
     [:ul
      [:li [:a {:href "http://www.mot.gov.cn" :target "_blank"} "中华人民共和国交通运输部"]]
      [:li [:a {:href "http://www.mot.gov.cn/zizhan/siju/anquanjiandusi" :target "_blank"} "中华人民共和国交通运输部安全监督司"]]
      [:li [:a {:href "#"} "省级系统连接（暂无）"]]
      ]] ))

(defn pview
  "app: 显示一项有正文，无link（有link的直接点标题连到外部链接）的portal内容"
  [id] 
  (let [doc (if (= 24 (count id)) (with-oid- :portal id) nil)
        {title :ptitle content :content file :file} doc]
    (html-body
      [:center [:h1 title]]
      (for [p (split content "\n")] [:p {:style "text-indent:2em; font:20/1.5 仿宋"} p])
      (when (not-nullity? file) (format "<p><a href='%s'>附件</a></p>" file)) [:hr][:br]
      [:center (eui-button-close)])))
  
(defn- portal-div-moc-con-
  "生成portal页面"
  []
  [:div.moc_con
   [:div.moc_login
    (for [[url img] {:mot "j_r3_c3.jpg" :en  "j_r3_c4.jpg" :org "j_r3_c5.jpg" :pn  "j_r3_c6.jpg"}]
      [:div.login_a 
       [:a {:href (str "/c/esp/index/" (name url))} [:img {:src (str "img/" img) :border "0"}] ] ])
    [:div.login_b [:a {:href "/c/espc/hot"} [:img {:src "img/j_r8_c9.jpg"}]]]
    [:div.moc_list 
     [:div.moc_list_left 
      [:div.moc_list_left_news ; flash图片
       [:script {:src "js/config.js"}]
       [:script {:src "js/fun.js"}]
       [:div.links "相关链接：" (portal-links)]]
      [:div.moc_list_left_space ""]
      [:div.moc_list_left_news
       [:div.news_top
        [:h3 "政策法规"] ; portal-type: 1
        [:span [:a {:href "#"} "更多&gt;&gt;"]]]
       (portal-items 1) ]
      [:div.moc_list_left_news
       [:div.news_top
        [:h3 "图片新闻"] ; portal-type: 2
        [:span [:a {:href "#"} "更多&gt;&gt;"]]]
       (portal-items 2) ]
      [:div.moc_list_left_space "" ]
      [:div.moc_list_left_news
       [:div.news_top
        [:h3 "工作动态"] ; portal-type: 3
        [:span [:a {:href "#"} "更多&gt;&gt;"]] ]
       (portal-items 3) ] ]
     [:div.moc_list_right 
      [:div.moc_list_right_news
       [:div.right_top [:h3 "公告公示"]]  ; portal-type: 4
       (portal-items 4) 
       [:ul {:style "border-top:1px dashed gray"}
        [:li [:a {:href "/c/esp/verify" :target "_blank" :style "font-size:14px; font-weight:bold"} 
              [:img {:src "img/question.png"}] " 查验 企业/考评员/考评机构"]]
        [:li [:h3 {:style "font-size:15px; color:gray"} "相关下载："]]
        (portal-downloads)
;        [:li "连接1"]
;        [:li "连接2"]
;        [:li "连接3"]
;        [:li "连接4"]
        ] ] ] ]]])

(defn portal-gen
  "生成esp/index.html文件代码html "
  []
  (let [title "交通运输企业安全生产标准化系统（试行）"
        copyright (str "版权所有：<a href='http://www.mot.gov.cn' target='_blank'>中华人民共和国交通运输部</a> "
                       "信息维护：<a href='http://www.mot.gov.cn/zizhan/siju/anquanjiandusi' target='_blank'>安全监督司</a> "
                       "备案编号：京ICP备05046837号")] 
    (str ; DOCTYPE 这行是必须的，否则footer看不见 
      "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">"
      (html
        [:html
         [:head 
          [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"} ]
          [:title title]
          [:link {:href "esp.css" :rel "stylesheet" :type "text/css"}] ]
         [:body
          [:div {:id "moc_top"} [:img {:src "img/j_r1_c1.jpg"}]]
          [:div {:id "moc_main"}
           (portal-div-moc-con-)
           [:div.moc_footer copyright] ]]]))))


;(logs-olap)
;(with-esp- (fetch :pn-apply :where {:$or [{:name "test1"} {:cid ""}] :cid {:$exists false}}))
;(with-esp- (fetch :log :where {:type "login" :date #"2012-12-07" :pid #"998"}))

(defn verify-search
  "ajax: 非mot用户（pn/en/org）查询其他用户的信息"
  [id request]
  (let [typ id
        {nam :name yyyy :yyyy c :c ta :ta xxxxxx :xxxxxx} (query-vars2 request)
        cid (case typ "pn" (format "%s-%s-%s-%s" yyyy c ta xxxxxx) (format "%s-%s-%s" yyyy ta xxxxxx))
        tb (apply-tb typ);(keyword typ)
        rt (with-esp- (fetch tb :where {:$or [{:name nam} {:cid cid}] :cid {:$exists true}}))
        n (count rt) ]
    (html [:h2 "查询到" n "条记录"]
          (when (not= 0 n) (result-html- rt [] [:name :cid :admin] {})))))

(defn verify-input
  [typ]
  (let [cert-name (dd-cert (keyword typ))]
    (html
      [:form#fm1 {} 
       [:p [:label "姓名/名称："]  (eui-text {:name "name"})]
       [:p [:label cert-name "编号："] (cert-input- typ) [:br]]
       [:p (eui-button {:onclick (format "ajax_load($('#result'), '/c/esp/verify-search/%s?'+$('#fm1').serialize())" typ) 
                        :iconCls "icon-ok"} "查询") (space 5) (eui-button-reset "fm1")] ]
      [:div#result] )))

(defn verify 
  "app: 通过企业名称或者cid查验en/pn/org基本信息"
  []
  (html-body
    [:h1 "查验 企业/考评员/考评机构"]
    (eui-tip "请输入企业名称/考评员姓名/考评机构名称或者等级/资格/资质证书号查询其合法性。")
    (for [[typ s] {"en" "企业" "pn" "考评员" "org" "考评机构"}] 
      (eui-button {:onclick (format "ajax_load($('#input'), '/c/esp/verify-input?typ=%s')" typ) 
                   :style "margin-right:10px"} (str "查验" s)))
    [:div#input] [:hr]
    [:p (eui-button-close)]))

(defn apply-resp-change-admin
  "ajax: 调整主管机关"
  [ids admin request]
  (let [[typ oid] ids
        tb (apply-tb typ)]
    (update- tb {:_id (object-id oid)} {:admin admin})
    "已更改此报名申请的主管机关"))

(defn reg-olap
  "对所有报名情况按照主管机关进行统计 "
  []
  (let [reducefn (str "function(obj,prev){ "
                      " if(!obj.uid && obj.del!='1') prev.count++;"
                      "}")
        f (fn [tb] (with-mdb2 "esp" (group tb :key (reverse [:admin]) :initial {:count 0} :reducefn reducefn)))
        dim-admin (keys dd-admin)
        [dim-mot dim-pn dim-org dim-en] (map #(into {} (for [{admin :admin count :count} (f %)] [admin count])) 
                                             [:mot-apply :pn-apply :org-apply :en-apply])
        rs (for [k dim-admin] 
             {:admin k
              :mot (to-int (dim-mot k) 0) 
              :pn (to-int (dim-pn k) 0) 
              :org (to-int (dim-org k) 0) 
              :en (to-int (dim-en k) 0)}) ]
    (html-body 
      (wr3.clj.chart/barf 
        {:category (vec (map dd-admin dim-admin)) 
         "主管机关" (vec (map :mot rs)) 
         "考评员" (vec (map :pn rs))
         "考评机构" (vec (map :org rs))
         "企业" (vec (map :en rs)) }
        {:title "报名数统计图" :y "报名数"} 1200 600)
      (result-html- rs (into dd-form {:mot "主管机关"}) [:admin :mot :pn :org :en] {}) )))

(def filter-value-all- "<font color=gray>全部</font>")

(defn- filter-type-
  "@t 申请列表筛选类型'pn'/'en'/'mot'/'org' 
  @admin '01' 或其他 
  @reg? 是否u盘初次报名申请"
  [t request reg?]
  (let [admin (user-admin request)
        f-resp (if reg? "resp-reg" "resp") ; resp或resp-reg字段
        f-list (if reg? "reg-list" "apply-list") ; 显示链表的函数
        filter-combo {"" "--全部--" "admin" "主管机关" "name" "姓名/名称" "pid" "证件号" f-resp "受理结果"}
        filter-combo (if (= admin "01") filter-combo (dissoc filter-combo "admin"))
        filter-js (format "ajax_load($('#span1'), '/c/esp/filter-value/%s?isreg=%s&ftype='+$('#ftype').val())" t reg?) 
        uri "ftype='+$('#ftype').val()+'&fvalue='+$('#fvalue').val()"
        list-js (format "ajax_load($('#list'), encodeURI('/c/esp/%s/%s?%s) )" f-list t uri) ]
    (html
      [:h2 (format "进行%s申请的筛选" (dd-role t))]
      [:form#fm2
       [:label "筛选条件："] (eui-combo {:name "ftype" :id "ftype" :onchange filter-js} 
                                   (case t ("pn" "mot") filter-combo
                                     ("en" "org") (into filter-combo {"grade" "级别" "province" "所在省市"}))) (space 5)
       [:label "筛选内容："] [:span#span1 filter-value-all-] (space 5)
       (eui-button {:onclick list-js :iconCls "icon-reload"} "按筛选条件刷新列表")])))
        
(defn reg-filter-type
  "ajax: mot查看初次报名申请列表筛选类型'pn'/'en'/'mot'/'org' "
  [id request]
  (filter-type- id request true))

(defn apply-filter-type
  "ajax: mot查看证书报名申请列表筛选类型'pn'/'en'/'org' "
  [id request]
  (filter-type- id request false))

(defn filter-value
  "ajax：报名申请，根据筛选条件(@id ftype)，得到筛选内容 fvalue. 
  @ids 第一个参数为'pn'/'en'/'mot'/'org', 第二个参数为筛选字段 ftype 如'name'/'pid' "
  [id ftype isreg request]
  (let [admin (user-admin request)
        typ id
        ftype (or ftype "")
        tb (apply-tb typ)
        attr {:name "fvalue" :id "fvalue"}
        isreg? (= "true" isreg)
        where {:uid (if isreg? nil {:$ne nil}) :del {:$ne "1"}}
        where (where-admin where admin)
        f (fn [fd dd] ; 得到指定字段值的下拉选择项如： {"yes" "同意" "no" "不同意"}
            (gen-array-map (for [e (sort (with-esp- (distinct-values tb fd :where where)))] 
                             [(or e "") (dd+ dd e)])))]
    (case ftype
      "admin" (eui-combo attr (f "admin" dd-admin))
      "grade" (eui-combo attr (f "grade" dd-grade))
      "province" (eui-combo attr (f "province" dd-admin))
      ("name" "pid") (eui-text attr)
      ("resp-reg" "resp") (eui-combo attr (f ftype dd-resp))
      filter-value-all-)))

;(println 
;  (for [e (sort (with-esp- (distinct-values :pn-apply "resp" :where {:uid  {:$ne nil}})))] 
;    [(or e "") (dd+ dd-resp e)])
;  )

(defn mot-admin2-list
  "@id mot的代码 '01' ~ '35' "
  [id]
  (let [admin (or id "01")
        rs (with-esp- (fetch :mot :where {:upper admin} :sort {:code 1}))
        mot01? (= admin "01")
        rs (if (not mot01?) rs 
             (let [admin-count (with-esp- (mdb-group :mot [:upper]))
                   m (into {} (for [{upp :upper n :count} admin-count :when (not (nil? upp))] [upp (int n)]))]
               (for [r rs] (into r {:count (m (r :code))}))))
        fs [:code :name]
        fs (if mot01? (conj fs :count) fs)]
    (html
      [:h2 (if mot01? "省级" "地市级") "主管机关："]
      (result-html- rs {:code "代码" :count "下级机构数量"} fs {}))))

(defn mot-admin2
  "交通部mot用户查看省级和地市级主管机关"
  [request]
  (let [admin (user-admin request)]
    (if (= admin "01")
      (html-body
        [:h1 "直接下级主管机关："]
        (eui-combo {:id "mot2" :onchange "ajax_load($('#list'), '/c/esp/mot-admin2-list/'+$('#mot2').val())"} 
                   dd-admin)
        [:div#list (mot-admin2-list "01")])
      "仅交通部用户可查阅")))

(defn my-info
  "ajax: 当前各类用户查看自己的用户信息
  @id 'pn/en/org/mot' "
  [id request]
  (let [uid (wr3user request)
        rt (first (with-uid- :user uid))
        {pid :pid admin :admin} rt
        admin-name (:name (with-mdb2 "esp" (fetch-one :mot :where {:code admin})))
        tb-apply (apply-tb id)
        r0 (when pid (first (with-pid- tb-apply pid))) ; 初次申请记录        
        ]
    (html
      [:h1 "我的用户信息："]
      [:h3 "名称：" (:name rt)]
      [:h3 "证件号码：" pid]
      [:h3 "主管机关：" admin-name]
      (when-let [oid (:_id r0)]
        (eui-button {:href (str (case id "pn" ukey-reapply-url-pn ukey-reapply-url-en) pid) :target "_blank"} 
                    "再次申请登录认证U盘")) )))

;------ mot查看pn/org/en证书申请列表，可进行条件筛选。@see mot查看初次u盘报名申请
(defn- apply-resp-th-
  "资格/资质证书申请的列表显示列头
  @t pn/en/org/mot "
  [t]
  {:name (name-label t) 
   :resp (case t "en" "受理结果" "审核结果") ; en的申请由org受理，mot审核；其他的申请又mot直接审核
   :respdate (case t "en" "受理日期" "审核日期")
   })
  
(defn- apply-resp-fs-
  "资格/资质证书申请的列表显示字段
  @t pn/en/org "
  [t]
  (case t
    "pn" [:admin :name :pid :org :type :date :resp :_id]
    "en" [:admin :province :name :pid :type2 :grade :date :resp :resp-review :_id]
    [:admin :province :name :pid :type :grade :date :resp :_id] ))

(defn apply-list
  "service: 证书申请列表
  @id pn/en/org 
  @skip 分页skip值 "
  [id skip ftype fvalue request]
  (let [uid (wr3user request) 
        admin (user-admin request)
        mot-en? (and (wr3role? request "mot") (= id "en")) ; mot查看en
        org-en? (and (wr3role? request "org") (= id "en")) ; org查看en
        skip (or skip 0)
        ftype (or ftype "")
        fvalue (or fvalue "")
        uri "skip='+$('#pagers').val()+'&ftype='+$('#ftype').val()+'&fvalue='+$('#fvalue').val()"
        list-js (format "ajax_load($('#list'), encodeURI('/c/esp/apply-list/%s?%s) )" id uri)
        tb (apply-tb id)
        where (where-apply {})
        where (where-admin where admin)
        where (if (nullity? ftype) where 
                (into where {(keyword ftype) (case ftype
                                               ("name" "pid") (re-pattern fvalue)
                                               "resp" (case fvalue ("yes" "no") fvalue {:$nin ["yes" "no"]})
                                               fvalue)}))
        where (cond
                mot-en? (into where {:orgid {:$exists true}}) ; 已经选择了考评机构的企业
                org-en? (into where {:orgid {:$in [uid]}}) ; org只能查看由它进行考评的en 
                :else where) 
        form (cond 
               (= id "pn") "mot-pn-apply"
               (= id "org") "mot-org-apply" 
               mot-en? "mot-en-apply" ; mot对en的受理
               org-en? "org-en-apply" )  ; org对en的受理
        count1 (with-mdb2 "esp" (fetch-count tb :where where))
        rs (with-esp- (fetch tb :skip (to-int skip) :limit 100 :where where :sort {:date -1})) ]
    (html
      [:h2 (format "%s申请列表" (dd-cert (keyword id)))]
      (pager-html count1 skip list-js)
      (result-html- rs (apply-resp-th- id) (apply-resp-fs- id) 
                    {:form form
                     :readonly? (user-readonly? request)})
      (when (> count1 100) (eui-tip "有多页，请点击下拉框翻页。")))))

;;------ 导入福建数据（文件）
(def espfj-files
  {:photo "照片"
   :titlefile "职称证明文件"
   :beginfile "相关专业从业年份证明文件"
   :perf2 "专业工作业绩附件"
   :proof "相关证明文件（身份证）"
   :proof2 "相关证明文件（学历证书）"
   :proof3 "相关证明文件（其他证书）"
   })

;;------- 导入福建数据（pid）
; 导入条件：{:resp "yes" :del {:$ne "1"} :pid "upper后在pn-csv中"}
; from: "福建" 
; from2: from，下级代码对应的中文 dd-province-fj 
; admin: "14"
; admin2: admin 
; import-date: (datetime)

;(require 'wr3.clj.app.espfj)
;(defn tmp-import-espfj-pn-apply
;  []
;  (with-mdb2 "esp" (destroy! :pn-apply {:import-date {:$exists true}}))
;  (let [pids (map :pid (with-mdb2 "espfj" (vec (fetch :pn-csv :only [:pid] :where {:pid {:$exists true}}))))]
;    (with-mdb2 "espfj" 
;      (let [rs (fetch :pn-apply :where {:resp "yes" :del {:$ne "1"}})]
;        (doseq [r rs :let [{pid :pid from2 :from admin2 :admin} r
;                           not-imported? (empty? (with-esp- (fetch :pn-apply :where {:pid pid :import-date {:$exists true}})))]]
;          (when (and (in? (.toUpperCase pid) pids) not-imported?) 
;            (insert- :pn-apply (into (dissoc r :resp) 
;                                     {:from "福建" :from2 (wr3.clj.app.espfj/dd-province-fj from2)
;                                      :admin "14" :admin2 admin2 
;                                      :resp-reg "yes" :import-date (datetime)})) ) )) )
;    "福建考评员导入完毕"))

;(tmp-import-espfj-pn-apply)
;(filter #(> (val %) 1) (frequencies (map :pid (with-esp- (fetch :pn-apply :only [:pid] :where {:import-date {:$exists true}})))))
;(with-mdb2 "espfj" (vec (map :pid (fetch :pn-apply :only [:pid] :where {:resp "yes" :del {:$ne "1"}}))))
;(print-seq (with-mdb2 "espfj" (vec (fetch :pn-apply :where {:pid "350102196004180455"}))))
;(doseq [{n :name pid :pid a :address p :pcode} (with-esp- (fetch :pn-apply :where {:import-date {:$exists true}}))]
;  (println n pid a p))

;(require 'wr3.clj.file)
;(defn tmp-import-espfj
;  "从文本导入pid到espfj"
;  []
;  (let [f (fn [l] (let [[i nam pid] (split l ",")]
;                    (when pid 
;                      (with-mdb2 "espfj" (insert! :pn-csv {:i i :name (trim nam) :pid (.toUpperCase pid)}))))) ]
;    (wr3.clj.file/line-filter "f:/espfj.csv" f)))
;(tmp-import-espfj)
;(with-mdb2 "espfj" (drop-coll! :pn-csv))

;(let [rt (with-mdb2 "espfj" (vec (fetch :pn-apply :limit 5 :only (vec (keys espfj-files)) 
;                                        :where {:del {:$ne "1"} :resp "yes"})))]
;      (doseq [r rt] (print-seq (rest (map val r)))))

;(let [rt (with-mdb2 "espfj"
;           (vec (fetch :pn-apply :where {:$or [
;                                               {:photo "/file/fj-1343695373308.JPG"}
;                                               {:titlefile "/file/fj-1343695373308.JPG"}
;                                               {:beginfile "/file/fj-1343695373308.JPG"}
;                                               {:perf2 "/file/fj-1343695373308.JPG"}
;                                               {:proof "/file/fj-1343695373308.JPG"}
;                                               {:proof2 "/file/fj-1343695373308.JPG"}
;                                               {:proof3 "/file/fj-1343695373308.JPG"}
;                                               ] })))]
;  (print-seq rt))

;;------------------------------------------------- test
;(use 'wr3.clj.datagen)
;--- 注：文件大小不能大于64k（65536）字节，否则报错
;(with-esp- (fetch :user :where {:pid (re-pattern (str "(?i)" "52251616-X"))}))
;(with-mdb2 "esp" (destroy! :pn-apply {:pid "532525197003260011"}))
;(update- :portal {} (fn [r] (map-key-rename r [:title :type] [:ptitle :ptype])) :replace)
;(insert- :user  {:name "交通运输企业测试001", :pid "12345678-0", :role "en", :uid "en-12345678-0", :admin "01" :contact "岳传志"})
;(with-esp- (fetch :en-apply :where {:orgid ["4f8aebd175e0ae92833680f4" "4f8aebd175e0ae92833680ff"]}))
;(pager-options 201)
;(doseq [[k v] m] 
;  (insert- :user {:name k :pid v :role "mot" :uid (str "mot-" v) :admin "01"}))
;(with-mdb2 "esp" (mdb-group :pn-apply [:admin]))

;(print-seq (with-esp- (fetch :user :only [:name :admin :usable :del :uid] :where {:role "org" :usable "0"})))
;(update- :user {:role "org" :uid "org-X0009960-5"} {:usable "1"})
