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
        name2 (case typ "pn" "姓名" "名称")]
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
    (chart/linef rt {:title "登录、审批操作日志" :x "日期" :y "操作数"} 1000 600) ))

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
(defn- portal-items
  "根据portal type从mdb库得到portal条目
  @portal-type dd-portal的key：1/2/3/4 "
  [portal-type]
  (let [limit (case portal-type (1 2 3) 7 12)
        rs (with-esp- (fetch :portal :limit limit :where {:ptype (str portal-type) :pdel {:$ne "1"}} 
                             :sort {:pno 1 :date -1}))]
    [:ul (for [{title :ptitle link :link content :content file :file id :_id pno :pno} rs] 
           [:li (cond 
                  (not-nullity? link) [:a {:href link :target "_blank"} title]
                  (not-nullity? content) [:a {:href (str "/c/esp/pview/" id) :target "_blank"} title]
                  :else title)
            (when (not-nullity? file) [:a {:href file} "（附件）"])
            (when (and (= portal-type 4) (= pno "0")) [:img {:src "/esp/img/new.gif"}]) ]) ] ))

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

;;------ 导入福建数据
(def espfj-files
  {:photo "照片"
   :titlefile "职称证明文件"
   :beginfile "相关专业从业年份证明文件"
   :perf2 "专业工作业绩附件"
   :proof "相关证明文件（身份证）"
   :proof2 "相关证明文件（学历证书）"
   :proof3 "相关证明文件（其他证书）"
   })

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


