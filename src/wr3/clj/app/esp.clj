(ns ^{:doc 
      "企业安全生产标准化管理系统 Enterprise Safety Production Standardization。
  后来的文档中有这么叫的“安全生产标准化（work safety standardization）” 
  pn：考评员person，en：企业enterprise，org：考评机构organization，mot：交通部Ministry Of Transport
  pot：省级交通厅/委/局 Pronvince department Of Transport
  暂定的企业达标证书格式：2012-type-grade-admin-%05d "
      :todo 
      "1、pn、en、org的标识：pn可以用uid，en、org必须用en、org表的oid; 或者把en、org所有用户的uid都设为一样，
  再用ukey号来标识其他的区别； 2、增加主管机关自己初始化下属单位的功能。" }
     wr3.clj.app.esp)

(use 'wr3.clj.app.espc :reload) ; 发布时去除reload
(use 'wr3.clj.app.espconf :reload) ; 发布时去除reload
(use 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.s 'wr3.clj.n 'wr3.clj.chart 'wr3.clj.u 'wr3.clj.nosql 'wr3.clj.chart)
(use 'hiccup.core 'somnium.congomongo)
(import 'wr3.util.Charsetx)
(require '[wr3.clj.app.auth :as au])

;;;-------------------------------------------------------------------------------------------------------- 公共函数
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
        (and (= id "pot") (= role "pot")) true
        :else false)
      ; 其他页面注册用户都能访问
      (cond 
        uid true
        (.startsWith fname "hot") true ; 热线投诉匿名用户即可访问   
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

(defn- apply-nav-
  "共用函数：各种证书申请导航页
  @type :pn :en :org "
  [type request]
  [request]
  (let [uid (wr3user request)
        tb type
        tb-apply (-> tb name (str "-apply") keyword)
        r1 (first (with-uid- tb uid)) ; 保存的最后录入信息
        rs2 (with-uid- tb-apply uid)] ; 提交过的所有信息
    (html
      [:h1 (format "录入过的申请信息 %s 条" (if r1 1 0))]
      (eui-button {:href "#" :onclick (format "layout_load_center('/c/esp/%s-input')" (name type))}  
                  (if r1 "查看已录入的申请信息" "初次申请考评证书"))
      (when rs2
        (html [:h1 (format "已提交过%s次申请：" (count rs2))]
              (result-html- rs2 '[申请时间 处理状态 查看] [:date :respdate :_id] 
                            {:form (format "docv/%s-apply" (name type))}) )) )))

(defn- apply-input-
  "共用函数：pn-input, org-input, en-input的录入表单。
  把缺省value设为用户最后录入的数据记录中的值，以便减少录入量。
  @type :pn :en :org "
  [request type]
  (let [uid (wr3user request)
        ntype (name type)
        cfg0 ({:pn cfg-apply-pn :en cfg-apply-en :org cfg-apply-org} type)
        nam (dd-cert type)
        r (first (with-uid- type uid))
        cfg (if r 
              (for [[n id m] cfg0] [n id (merge m (if (id r) {:v (id r)} {}))])
              cfg0)]
    (html
      (input-form 
        cfg 
        {:title (format "<font color=blue>%s</font> 申请表" nam)
         :buttons (html
                    (eui-button {:onclick (format "esp_input_save('%s')" ntype)} " 保 存 ") (space 5)
                    (eui-button {:onclick (format "esp_input_submit('%s')" ntype)} "提交申请") (space 5)
                    (eui-button {:onclick "$('form').get(0).reset()"} " 取 消 ")) })
      (fileupload-dialog)) ))

(defn- get-stand-
  "达标标准指标
  @tb 'en-stand1' 'en-stand2' 改为 'indic1' 'indic2' 'indic3' 
  @type2 11 12 "
  [tb type2]
  (with-mdb2 "esp"
    (vec (fetch (keyword tb) :where {:type2 type2} :sort {:i 1 :j 1 :k 1}))))  

(defn- stand-
  "企业、考评机构安全生产达标标准的自评、考评
  @type2 细类, 如11，51
  @enid 被评估的企业的uid
  @m 含各字段缺省值的hash-map，如：{:grade '2' :sum '998' :report '..' :f_1_1_1 '5'}  "
  ([type2 enid m]
    (let [;type2 (to-int (or id 11))
          is-type2-42? (= 42 type2)
          [rt1 rt2 rt3] (map #(get-stand- (str "indic" %) type2) [1 2 3]) ; "en-stand" -> "indic"
          fsetv (fn [k v0] (if-let [v (get m k)] (to-int v) v0)) ; k：字段名称如:grade,:sum； v0：字段缺省值
          f3 (fn [r] ; r:  某个3级考核指标，如“①制定企业安全生产方针、”
               (let [[score star] [(:score r) (:star r)]
                     [i j k] [(:i r) (:j r) (:k r)]
                     s1 (html [:td {:style "width: 800px"} (:name r)] 
                              [:td {:align "right"} score] 
                              [:td (format "<font color=red>%s</font>" (apply str (repeat star "★")))]
                              [:td {:align "right"} (let [fname (format "f_%d_%d_%d" i j k)]
                                                      (eui-numberspin 
                                                        {:min 0 :max score :increment 1 :style "width:40px"
                                                         :name fname :i i :j j :k k
                                                         :group "score" :score score :star star
                                                         :value (fsetv (keyword fname) score) }))])
                     s2 (if (= 1 (:k r)) s1 (html [:tr s1]))] s2))
          f2 (fn [r] ; r:  某个2级考核指标，如“1.安全工作方针与目标”
               (let [rt3i (filter #(and (= (:i r) (:i %)) (= (:j r) (:j %))) rt3) ;该2级指标对应的所有三级指标
                     s1 (html [:td {:rowspan (count rt3i)} (str (:j r) ". " (:name r))]
                              (f3 (first rt3i)) 
                              (for [r (rest rt3i)] (f3 r)))
                     s2 (if (= 1 (:j r)) s1 (html [:tr s1])) ] s2))
          f1 (fn [r] ; r： 某个1级考核指标，如“一、安全目标 35分 ”
               (let [rt2i (filter #(= (:i r) (:i %)) rt2) ;该1级指标对应的所有二级指标
                     rt3i (filter #(= (:i r) (:i %)) rt3)] ;该1级指标对应的所有三级指标
                 (html [:tr [:td {:rowspan (count rt3i)} (:name r)]
                        (f2 (first rt2i))] ; 产生后面2、3级指标的多个[:tr]html片段 
                       (for [r (rest rt2i)] (f2 r))))) ]
      (html-body
        [:form#fm1 {:method "POST" :action "/c/esp/stand-save"}
         (when enid (let [r (first (with-uid- :en enid))]
                      (html [:center [:h1 "被考评企业：" (:name r)]]
                            [:input {:name "enid" :type "hidden" :value enid}])))
         [:h2 [:label "请选择申请达标级别："] (eui-combo {:id "grade" :name "grade"  :onchange "esp_stand_grade()"
                                                :value (fsetv :grade 1) }
                                               (if is-type2-42? (dissoc dd-grade 3) dd-grade))] 
         (eui-tip [:span#tip (dd-stand-tip (fsetv :grade 1))
                   (when is-type2-42? "<br/>注：城市轨道交通运输企业由于安全要求高，只能申请一二级达标，不能申请三级达标。")])
         [:table.wr3table {:border 1}
          [:caption (format "%s企业安全生产达标标准" (dd-type2 type2))]
          [:thead [:tr [:th "考核内容"] [:th {:colspan 2} "考核要点"] [:th {:nowrap "1"} "分数"] [:th "星级"] [:th "自评分"] ]]
          [:tbody (for [r rt1] (f1 r))]
          [:tfoot [:tr [:td {:colspan 6}
                        [:h2 "注：" "“<font color=red>★</font>”为一级必备条件；" "“<font color=red>★★</font>”为二级必备条件；"
                         "“<font color=red>★★★</font>”为三级必备条件。" "即：" [:br][:br](space 8)
                         (join (vals dd-stand-tip) "；") "。" ]]]] ] [:br]
         (if enid "评估总分：" "自评总分：") 
         [:input#sum {:name "sum" :style "width:70px; font-size:16px; color:green" :readonly "true" 
                              :value (fsetv :sum "0")}] (space 5) 
         (eui-button {:href "javascript:esp_get_score()"} "计算分数") (space 10)
         (if enid "评估报告：" "自评报告：") 
         (fileupload-field "自评报告" "report" (:report m) {}) (space 10)
         (eui-button {:onclick (format "esp_stand_save(%s)" type2)} "提 交") ] 
        (fileupload-dialog) (repeat 10 [:br]) )))
  ([type2] (stand- type2 nil {})))

(defn stand
  "app: 企业、考评机构安全生产达标标准的自评、考评
  @id type2细类, 如'11'，'51' 
  @enid 被考评企业的object-id "
  [id enid]
  (let [type2 (to-int (or id 11))
        enid (or enid "en1")]
    (stand- type2 enid {})))
    
(defn stand-save
  "service: 保存评分结果。
  企业自评后保存在en-stand中并简要记录在en-apply中；
  考评机构评审后也保存在en-stand中并简要记录在en-apply中
  @id 细类 '11' '12' "
  [id request]
  (let [type2 (to-int id)
        uid (wr3user request) ; 当前评分人uid如'en1'（自评），'org1'（考评）
        vars (query-vars2 request)
        enid (:enid vars)
        sum (:sum vars)]
    (with-mdb2 "esp"
      (insert! :en-stand {:uid uid :enid enid :date (datetime) :type2 type2
                          :stand (dissoc vars :enid)}))
    (println "-- is role en: " (wr3role? request "en")) 
    (cond 
      (wr3role? request "en")  (update- :en-apply {:uid uid  :type2 id} (fn [r] {:score0 sum}))
      (wr3role? request "org") (update- :en-apply {:uid enid :type2 id} (fn [r] {:score1 sum}))
      :else nil )
    "已经保存"))
  
(defn cert-renew
  "service：考评员、考评机构、企业的证书换证
  @id 'pn' 'org' 'en' 转换为:pn :org :en "
  [id request]
  (let [tb (keyword id)
        tb-apply (keyword (str id "-apply"))
        uid (wr3user request)
        rs (with-esp- (fetch tb-apply :where {:uid uid :cid {:$exists true}}))
        y (case tb :pn 5 :org 5 :en 3 1) ; 有效期年份
        cname (dd-cert tb)]
    (html
      [:h1 "申请换证"]
      (if (empty? rs)
        (eui-tip (format "目前还没有%s证书，对于已有证书系统会自动提醒换证（%s年到期前3个月）。" cname y))
        (html
          [:h2 (format "目前已有的 《%s》：" cname)]
          (result-html- rs [] [:cid :cdate :_cdate-end :_id] {:type tb :form (str "cert-renew-doc/" id)}) )))))

(defn cert-renew-doc
  "app: pn、en、org显示换证操作
  @ids ids[0]: 'pn','org','en'; ids[1]: object-id "
  [ids]
  (let [[id oid] ids
        tb (keyword (str id "-apply"))]
  (doc- tb oid 
        {:after (html [:br](eui-tip "请注意填写标注与换证相关的内容！") [:br]
                      (eui-button {:href (format "/c/esp/cert-renew-input/%s" id)} "申请换证") )})))

(defn cert-renew-input
  "采用申请表单，但录入一些换证特定的字段"
  [id request]
  (html-body (apply-input- request (keyword id))))

(defn cert-renew-resp
  "service: 主管机关对考评员、考评机构、企业的换证申请受理
  @id pn或org或en "
  [id]
  (let [cname (dd-form (keyword id))]
    (html
      [:h1 (format "%s换证申请受理" cname)]
      (eui-tip "暂无换证申请"))))

(defn cert-resp
  "service: 用户：org和mot；mot制发考评员、考评机构证书，org制发企业证书。
  @id 'pn','en','org' "
  [id]
  (let [title (format "%s制发" (dd-cert (keyword id)))]
    (html
      [:h1 title]
      (eui-tip "系统可直接套打A3纸，也可生成电子证书。")
      [:h2 "审核通过的申请："]
      (case id
        "en" (let [rs (with-esp- (fetch :en-apply :where {:resp-review "yes"}))]
               (result-html- rs [] [:name :resp :resp-eval :resp-review :respdate-review :_id :_issue] 
                             {:form "docv/en-apply" :issue "en-apply"}))
        "org" (let [rs (with-esp- (fetch :org-apply :where {:resp "yes"}))]
                (result-html- rs [] [:name :resp :respdate :_id :_issue] 
                              {:form "docv/org-apply" :issue "org-apply"}))
        "pn" (let [rs (with-esp- (fetch :pn-apply :where {:resp "yes"}))]
               (result-html- rs [] [:name :resp :respdate :_id :_issue]
                             {:form "docv/pn-apply" :issue "pn-apply"}))
        nil) )))

(defn- has-digit4- [n] (some #(= \4 %) (str n)))
(defn- cert-sid-max 
  "得到保存在:cert表中的最大证书号。
  @id :pn :en :org 
  @admin 主管机关代码如：'01' '02' '34' "
  [id admin]
  (if-let [r (with-mdb2 "esp" (fetch-one :cert :where {:sid (name id) :admin admin}))]
    (:max r) 0))
  
(defn- cert-sid-next 
  "得到pn、en、org证书下一个序号，根据目前最后一个序列号，得到下一个不含'4'的序列号（6位左补零）。 例子：
  (cert-sid-next 444444) ; 500000
  (take 1000 (iterate cert-sid-next 1)) ; (1 2 3 5 6 7 8 9 10 11 12 13 15 ..) "
  [max] (let [sid (find-first #(not (has-digit4- %)) (iterate inc (inc max)))]
          (if (<= sid 999999) sid (cert-sid-next 0))))

(defn- cert-id
  "生成en、org的达标证书和资质证书号，形如yyyy-ta-xxxxxx；pn的资格证，形如yyyy-c-ta-xxxxxx。
  @m {:year 2005 :admin '01' :max 0 :type 1} 
    其中:year 发证年份，:admin 主管机关，:max 当前最大号，:type证书类型参数只有pn才有，表明业务大类型"
  ([m]
    (let [year (or (:year m) (year))
          admin (or (:admin m) "01")
          max (or (:max m) 0)
          sid (cert-sid-next max)]
      (if-let [type (:type m)]
        (format "%s-%s-%s-%06d" year type admin sid)
        (format "%s-%s-%06d" year admin sid))))
  ([] (cert-id nil)))

(defn cert-issue-pn
  "pn制发证书
  @id xx-apply表中文档的object-id字符串 "
  [id request]
  (let [r (with-oid- :pn-apply id)
        uid (:uid r)
        max (cert-sid-max :pn (:admin r))
        cfg [["姓 名" :name {:v (:name r)}]
             ["身份证号" :pid {:v (:pid r)}]
             ["证书号" :cid {:v (cert-id {:admin (:admin r) :type (:type r) :max max})}]
             ["有效期开始日期" :date {:v (date)}] ]] 
    (html-body
      (input-form cfg {:action (str "/c/esp/cert-print-pn/" id)
                       :title (format "【%s】内容" (dd-cert :pn))
                       :buttons (html [:input {:name "uid" :type "hidden" :value uid}]
                                      (eui-button {:onclick "$('#fm1')[0].reset()"} "重置") (space 10)
                                      (eui-button {:onclick "$('#fm1').submit()" :iconCls "icon-ok"} "确定发放证书"))}) )))

(defn cert-print-pn
  "打印/保存证书"
  [id request]
  (let [vars (query-vars2 request)
        cid (:cid vars)
        date (:date vars)
        [year type admin sid] (split cid "-")]
    (with-mdb2 "esp" (update! :cert {:sid "pn" :admin admin} {:$set {:max (to-int sid)}})) ; 更新max值
    (update- :pn-apply {:_id (object-id id)} (fn [r] {:cid cid :cdate date})) 
    (html-body
      [:h1 "已发证书：" [:font {:color "blue"} cid]][:br][:br]
      (eui-button {:href "#" :onclick "window.close();"} "关闭") )))

(defn cert-issue
  "en, org制发证书
  @ids ids[0] typ 'en-apply' 'org-apply' ; ids[1] en-apply或者org-apply表中文档的object-id字符串 "
  [ids request]
  (let [[apply-type oid] ids
        tb (keyword apply-type) ; :en-apply :org-apply 
        r (with-oid- tb oid)
        max (cert-sid-max (left apply-type "-") (:admin r))
        uid (:uid r)
        type-dd (if (= tb :en-apply) dd-type2 dd-type)
        type-value (if (= tb :en-apply) (:type2 r) (:type r)) 
        cfg [["证书编号" :cid {:v (cert-id {:admin (:admin r) :max max})}]
             ["有效期开始日期" :date {:v (date)}]
             ["名称" :name {:v (:name r)}]
             ["类型类别" :type {:v (get type-dd (to-int type-value))}]
             ["达标/资质等级" "grade" {:v (dd-grade (to-int (:grade r)))}]
             ["正本/副本选择" :copy {:t {"正本" "—— 打印正本 ——" "副本" "—— 打印副本 ——"}}]] ]
    (html-body 
      (eui-tip "提示：1、请核查内容是否有误；2、打印一正三副；3、请在打印机设置中调整至A3、合适的页边距后保存设置。")
      (input-form cfg {:action (str "/c/esp/cert-print/" oid)
                       :title (format "【%s】证书内容" (dd-form tb))
                       :buttons (html [:input {:name "apply-type" :type "hidden" :value apply-type}]
                                      (eui-button {:onclick "$('#fm1')[0].reset()"} "重置") (space 10)
                                      (eui-button {:onclick "$('#fm1').submit()"} "查看、打印证书") ) }) )))

(defn cert-print
  "查看打印操作"
  [id request]
  (let [vars (query-vars2 request)
        apply-type (:apply-type vars) ; "en-apply" "org-apply
        en-org (left apply-type "-") ; "en" "org"
        cid (:cid vars)
        date (:date vars)
        [year admin sid] (split cid "-")
        date-end (date-add date 3 0 0)
        copy (:copy vars)
        css1 "position:absolute;left:%d;top:%d;font-size:19pt"
        css2 "position:absolute;left:%d;top:%d;font-family:黑体;font-size:21pt;width:450px"
        css3 "position:absolute;left:%d;top:%d;font-size:20pt;width:230px"
        css4 "position:absolute;left:%d;top:%d;font-size:51pt;color:#9fa0a0"
        f (fn [css x y] {:style (format css x y)}) ]
    (with-mdb2 "esp" (update! :cert {:sid en-org :admin admin} {:$set {:max (to-int sid)}})) ; 更新max值
    (update- (keyword apply-type) {:_id (object-id id)} (fn [r] {:cid cid :cdate date})) 
    (html
      [:body {:style "margin:0px; font-family: 宋体; font-size:21pt"}
       [:div {:style (format "background:url('/img/esp/cert-%s.jpg');border:1px solid red;width:1654px;height:1169px"
                             (left apply-type "-"))} ]
       [:div (f css1 250  785) (:cid vars)]
       [:div (f css1 250  832) (format-date-cert- date) " 至 " (format-date-cert- date-end)]
       [:div (f css2 1080 285) (:name vars)]
       [:div (f css2 1080 407) (:type vars)]
       [:div (f css2 1080 530) (:grade vars)]
       [:div (f css3 1262 926) (format-date-cert- date)] 
       [:div (f css4 1105 635) [:b "（" [:span {:style "font-family:黑体"} copy] "）"]] ])))

(defn report
  "service: 考评机构、企业的年度工作报告；
  @type 类型 :org :en "
  [id request]
  (let [type (keyword id)
        uid (wr3user request)
        tb-report (-> type name (str "-report")) ; "org-report"  "en-report"
        rs (with-esp- (fetch (keyword tb-report) :where {:uid uid}))
        r (first (with-esp- (fetch (keyword tb-report) :where {:uid uid :year (year)})))
        sid "freport"]
    (html
      (when-not r (eui-tip "还没有本年度工作记录。"))
      [:h1 (year) " 年度工作报告"]
      [:form {:method "POST" :action "/c/esp/report-save"}
       [:label "年度报告："]
       (fileupload-field "年度工作报告" sid (:freport r) {})
       [:br][:br]
       (eui-button {:onclick (format "esp_report_save('%s')" tb-report)} "保 存") (space 5) ]
      (fileupload-dialog)
      [:h2 "已经上传的所有年度工作报告："]
      (result-html- rs '["年度" "上报时间" "查看"] [:year :date :freport]) )))

(defn report-save
  "service: 共用函数，org、en年度报告保存，被 year-report- 函数ajax调用
  @id form名称如 'en' 'org' "
  [id request]
  (let [vars (query-vars request)
        uid (wr3user request)
        tb (keyword id) ; :en-report :org-report
        yyyy (year)
        m (into vars {:uid uid :year yyyy :date (date)})]
    (with-mdb "esp" 
      (update! tb {:uid uid :year yyyy} m))
    (str "已保存 " yyyy " 年度报告。")))

(defn apply-resp
  "service: 主管机关对考评员、考评机构、企业的申请受理. 
  @id 代表申请来源的字符串：pn,org,en "
  [id request]
  (let [tb (keyword (str id "-apply")) ; :pn-apply :org-apply :en-apply
        cname (dd-cert (keyword id))
        rs (with-esp- (fetch tb))
        form (cond 
               (= id "pn") "mot-pn-apply"
               (= id "org") "mot-org-apply" 
               (and (= id "en") (wr3role? request "mot")) "mot-en-apply"
               (and (= id "en") (wr3role? request "org")) "org-en-apply" )
        tip (case id 
              "pn" (str "同意则颁发资格证书；<br/>注：直接从事交通运输安全生产行政管理工作10年以上，"
                        "熟悉掌握交通运输安全生产相关法规和企业安全生产标准化规定者，身体健康，经本人申请、所在单位推荐、"
                        "发证主管机关核准，可直接办理考评员资格证。")
              "org" "主管机构处理：（同意/不同意）+意见"
              "en" "企业申请处理：主管机关（同意+指派考评机构）/（不同意+意见）——> 考评机构（同意）/（不同意+意见）——> 主管机关审核")]
    (html
      [:h1 (format "受理%s申请" cname)]
      (eui-tip tip)
      (result-html- rs []
                    [:name (if (= id "en") :type2 :type) :date :resp :_id :_select] 
                    {:form form}) [:br]
      (when (= id "pn") 
        (eui-button {:onclick "esp_pn_apply_resp()"} "资格证书制发")) )))

(defn backup
  "service: 考评机构、企业变更申请录入表单
  @id 'org'或'en'，分别代表考评机构、企业 "
  [id request]
  (let [tb (keyword (str id "-backup"))
        form (format "docv/%s-backup" id)
        rs (with-esp- (fetch tb :where {:uid (wr3user request)}))
        reason (if (= id "org") dd-org-backup dd-en-backup)
        n (count rs)] 
    (html
      [:h1 "申请变更备案"]
      (when (pos? n)
        (html 
          (eui-tip (format "已提交的 %s 条变更备案申请：" n))
          (result-html- rs '["变更原因" "备案时间" "查看"] [:reason :date :_id] {:form form :id id}))) [:br][:br]
      [:form {:id "fm1"}     
       [:table {:style "font-family: 微软雅黑; font-size: 16px"}
        [:caption "提交新的变更备案申请"]
        [:tbody
         [:tr [:td [:label "选择变更原因："]] [:td (eui-combo {:name "reason"} reason)]]
         [:tr [:td [:label "变更内容描述："]] [:td (eui-textarea {:name "content" :style "width:350px"} "")]] ]
        [:tfoot [:tr [:td {:colspan 2 :align "right" :style "padding:10px"} 
                      (eui-button {:href "#" :onclick (format "esp_save_backup('%s')" id)} "提交申请")]]]] ] )))

(defn backup-save
  "service: 机构变更申请表单提交保存.
  @id 'org'或'en'，分别代表考评机构、企业 "
  [id request]
  (let [tb (keyword (str id "-backup")) ; org-backup, en-backup
        vars (query-vars request)]
    (do
      (with-mdb "esp"
        (insert! tb (into vars {:uid (wr3user request) :date (datetime)})))
      "提交完毕！请点击左边菜单刷新。")))

(defn backup-resp
  "主管机关受理考评机构、企业变更备案.
  @id 'en' 'org' "
  [id request]
  (let [tb (keyword (str id "-backup"))
        rs (with-esp- (fetch tb ))]
    (html
      [:h1 (format "%s 变更备案受理" (dd-form (keyword id)))]
      (eui-tip "主管机关处理：（同意/不同意）+意见")
      (result-html- rs []
                    [:reason :content :date :respdate :_id] 
                    {:form (format "backup-resp-doc/%s" id) :id id}) 
      [:br] )))

(defn backup-resp-doc
  "app: 主管机关受理考评机构、企业变更记录
  @ids ids[0]: 'org', 'en'; ids[1]: object-id "
  [ids]
  (let [tb (keyword (str (first ids) "-backup"))
        oid (second ids)]
  (doc- tb oid 
        {:after (html
                  [:br] [:label "处理意见："] (eui-textarea {:id "advice" :style "width:600px"} )[:br][:br]
                  (eui-button {:onclick (format "esp_backup('%s','%s')" (name tb) oid)} "提交") )})))

(defn backup-resp-save
  "service: mot保存考评机构、企业变更记录 "
  [ids request]
  (let [[tb oid] ids
        vars (query-vars2 request)]
    (update- (keyword tb) {:_id (object-id oid)} 
             (fn [r] {:advice (:advice vars) :respdate (datetime)}) )
    "已经保存"))
  
(defn- cert-cancel
  "service: 主管机关对pn、org、en证书的撤销
  @id 'pn' 'org' 'en' "
  [id]
  (let [type (keyword id)
        nam (dd-form type)
        v0 (type {:pn "2011-2-022-20789" :org "2012-D-00-丙7585" :en "2012-2-1-0551-00844"})]
    (html
      [:h1 (format "%s资格撤销工作" nam)]
      [:form {:id "fm1" :action (format "/c/esp/cert-cancel-doc/%s" (name type)) :method "get" :target "_blank"}
       [:label (format "请输入要撤销资质的%s证书号：" nam)]
       (eui-text {:id "cid" :name "cid" :value v0}) (space 3) 
       (eui-button {:onclick "$('#fm1').submit()"} "查询")] )))

(defn cert-cancel-doc
  "service: 查询指定证书号，显示信息并撤销
  @id 'pn' 'org' 'en', 
  @cid cid证书号
  @type :pn :org "
  [id cid]
  (let [tb (keyword id)
        r (first (with-esp- (fetch tb :where {:cid cid})))
        id (str (:_id r))]
    (if r
      (doc- tb id 
            {:after (html
                      [:br] (eui-button {} "撤销（并填写意见）") (space 3) (eui-textarea {} ) [:br][:br] )})
      (html-body (eui-tip "未找到该证书号！" )) )))
  
(defn report-view
  "service: mot查看org、en的年报
  @id :org :en "
  [id]
  (let [tb (get {"org" :org-report "en" :en-report} id)
        nam (dd-form (keyword id))
        rs (data- tb)]
    (html
      [:h1 (format "%s年度工作报告" nam)]
      (result-html- rs ["年度" (format "%s名称" nam) "报送日期" "文件"]
                    [:year :uid :date :freport]))))

(defn- yes-no-advice-
  "显示意见填写+同意/不同意按钮。 
  @m 客户化配置如：{:label ['意见' '同意' '不同意'] :onclick ['esp_yes()' 'esp_no()']} "
  [m]
  (let [br2 (html [:br][:br])
        [s-advice s-yes s-no] (or (:label m) ["填写意见：" "同  意" "不同意"])
        [f-yes f-no] (or (:onclick m) [])]
    (html
      br2 [:label s-advice] (eui-textarea {:id "advice" :style "width:600px"} ) br2
      (eui-button {:onclick f-yes :iconCls "icon-ok"} s-yes) (space 10)
      (eui-button {:onclick f-no :iconCls "icon-cancel"} s-no) br2 br2 )))

;;;-------------------------------------------------------------------------------------------------------- pn  考评员
(def pn---------- nil)

(defn pn-apply
  "service: 考评员申请导航页"
  [request]
  (apply-nav- :pn request))

(defn pn-input
  "service：评审人员在线申请"
  [request]
  (apply-input- request :pn))

(defn pn-list
  "service: 考评员列表
  @id name的pattern如'张' "
  [id]
  (list- id :pn [] [:name :org :from :_id]))

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

;;;-------------------------------------------------------------------------------------------------------- org 考评机构
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
  [id]
  (list- id :org [] [:name :province :_id]))

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
       (eui-tip "已聘用的考评员可以点击详情查看后进行解聘。")
       [:label "聘用考评员，请输入资质证书号："]
       (eui-text {:id "cid"}) (space 3)
       (eui-button {:onclick "esp_org_hire()"} "查询聘用") ]
      (result-html- rt '[姓名 证书类别 证书编号 专兼职 聘用日期 解聘日期 详情] 
                    [:name :type :cid :fulltime :contract0 :contract1 :_id] {:form "org-hire-view"}))))

(defn org-refine
  []
  (html (eui-tip "暂无整改通知")))

(defn org-hire-view
  "app: 考评员情况，能否聘用
  @如果有id，如果有cid，先通过cid查出文档:_id"
  [id cid]
  (let [;cid (or cid "2011-2-0471-07959")
        ;r (first (with-esp- (fetch :pn :where {:cid cid})))
        r (if id 
            (with-oid- :pn id)
            (first (with-esp- (fetch :pn :where {:cid cid}))))
        oid (:_id r)
        cid (:cid r)
        c0 (:contract0 r)
        c1 (:contract1 r)
        belong (:belong r)]
    (if r
      (doc- :pn (str oid) 
            {:before (if (and c0 (not c1)) 
                       (html
                         (eui-tip "该考评员目前已经聘用。") [:br]
                         (eui-button {:onclick (format "esp_fire('%s')" cid)} 
                                     (str "解聘考评员" (:name r))) [:br][:br])
                       (html (eui-combo {:id "fulltime" :value 1} {1 "专职" 0 "兼职"} ) (space 5)
                             (eui-button {:onclick (format "esp_hire('%s')" cid)} 
                                         (str "聘用考评员" (:name r))) [:br][:br]))})
      "无效证书号")))

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
  @id object-id
  @todo 1、保存数据；2、传入企业uid"
  [id]
  (let [f-yes (format "esp_org_en_apply('yes', '%s')" id) 
        f-no  (format "esp_org_en_apply('no',  '%s')" id)]
    (doc- :en-apply id 
          {:after (fn [rt] (html
                             [:br] [:label "第一步："] 
                             (eui-button {:href (format "/c/esp/stand/%s?enid=%s" (:type2 rt) (:uid rt)) 
                                        :target "_blank"} "考评打分") [:br][:br]
                             [:label "第二步："] (yes-no-advice- {:onclick [f-yes f-no]}) ))})))

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
  "service: 考评机构的企业档案管理"
  []
  (list- "山西" :en [] [:province :name :type :grade :_id]))
 
(defn org-en-eval
  "service: org查看en的考评情况汇总"
  []
  (html
    [:h1 "企业考评情况汇总表"]
    (eui-tip "暂无记录")))

(defn org-stand-view
  "org, mot查看org对en达标评估的结果
  @ids ids[0]:enid被评估企业的uid； ids[1]:达标结果文档的object-id"
  [ids]
  (let [[enid oid _] ids
        doc (with-oid- :en-stand oid)
        type2 (:type2 doc)
        m (:stand doc)] 
    (stand- type2 enid m)))
  
;;;-------------------------------------------------------------------------------------------------------- en  企业
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
  [id]
  (list- id :en [] [:province :name :type :grade :_id]))

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
  [id]
  (let [doc (with-oid- :en-stand id)
        type2 (:type2 doc)
        m (:stand doc)] 
    (stand- type2 nil m)))
  
(defn en-select-org
  "app: 企业选择考评机构"
  [request]
  (let [uid (wr3user request)
        admin (:admin (first (with-uid- :en uid)))
        rs (with-esp- (fetch :org :where {:admin admin}))
        r (first (with-esp- (fetch :en-apply :where {:uid uid} :sort {:date -1}))) ; 第一条申请
        ] 
    (html
      [:h1 (format "%s主管的考评机构（%s 名）" (dd-admin admin) (count rs))]
      (eui-tip "请在如下的考评机构列表中自行选择两个。")
      (result-html- rs [] [:name :admin :_id :_select] {:form "docv/org"}) [:br]
      (eui-button {:onclick "esp_en_select_org()"} "提 交")
      [:script (format "esp_en_selected('%s')" (join (:orgid r) ","))] )))

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

;;;-------------------------------------------------------------------------------------------------------- mot 主管机关
(def mot---------- nil)

(defn mot-pn-olap
  "service: 主管机关-考评员分析"
  []
  (let [rs (data- :pn)
        region (for [r rs] (-> r :from (subs 0 2)))
        rt (apply array-map (flatten (for [[v vv] (group-by #(str %) region)] [v (count vv)])))
        rt1 (frequencies (map :type (with-esp- (fetch :pn :only [:type] :sort {:type 1}))))
        rt2 (into {} (for [[k v] rt1] [(dd-type k) v]))
        rt3 (frequencies (map :edu (with-esp- (fetch :pn :only [:edu] :sort {:edu 1}))))]
    (html
      (eui-tip "提示：可点击右上角搜索框，按考评员姓名或者证书号模糊查询。")
      (barf rt {:title "各地域考评员分布情况" :x "省份" :y "考评员人数"})
      (pief rt2 {:title "各专业类型考评员数量统计" :x "专业类型" :y "考评员人数"}) 
      (linef rt3 {:title "考评员学历统计" :x "学历" :y "考评员人数"}) )))

(defn mot-en-olap
  "service: 企业统计分析"
  []
  (let [rt (data- :en)
        rt1 (group-by :province rt)
        m (for [[p ls] rt1] [p (count ls)])
        m1 (sort-by #(- (second %)) m)]
    (html
      [:h1 "各省一级企业数量分析"]
      (barf (apply array-map (flatten m1)) {:x "省份" :y "一级企业数量"})
      (pief (apply array-map (flatten m1)) {}) )))

;;-- mot-(pn en org)-apply[-resp]
(defn- mot-apply-
  "主管机关对pn、org、en的申请回应。
  @type 即 :pn :org :en
  @oid 申请文档的object-id "
  [type oid]
  (let [tb (-> type name (str "-apply") keyword) 
        f-click (fn [y-n] (format "esp_mot_apply('%s','%s','%s')" (name type) oid y-n))]
    (doc- tb oid
          {:after
           (fn [rt]
             (html
               (case type
                 :pn (let [uid (:uid rt) t (:type rt)
                           rs (with-esp- (fetch :pn-train :where {:uid uid :type t}))]
                       (html [:h2 "该考评员“" (dd-type t) "”类型的培训、考试记录："]
                             (result-html- rs [] [:name :type :train-id :exam-date :exam-score]) [:br][:br]
                             (eui-tip "直接从事交通运输安全生产行政管理工作10年以上，熟悉掌握交通运输安全生产相关法规和企业安全生产标准化规定者：")
                             (eui-text {:id "pass-direct" :type "checkbox"}) (space 2) "直接颁发" ))
                 :org ""
                 :en (eui-tip "注意：请指派一个考评机构对该企业进行考评！")
                 nil)
               (yes-no-advice- {:onclick [(f-click "yes") (f-click "no")]}) ))})))

(defn mot-pn-apply [id] (mot-apply- :pn id))
(defn mot-org-apply [id] (mot-apply- :org id))
(defn mot-en-apply [id] (mot-apply- :en id))

(defn mot-apply-resp
  "主管机关保存pn、org、en的申请处理意见
  @id 类型'pn' 'org' 'en' "
  [id request]
  (let [tb (keyword (str id "-apply")) ; :pn-apply :org-apply :en-apply
        vars (query-vars2 request) 
        oid (:oid vars)]
    (update- tb {:_id (object-id oid)}
             (fn [r] (merge vars {:respdate (datetime)})))
    "申请已处理"))

(defn mot-en-review
  "主管机关对org评审过的企业考评结论审核"
  []
  (let [rs (with-esp- (fetch :en-apply ))]
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
      [:h2 "公示期满可发证企业一览："] (eui-tip "无公示期满的企业") )))

(defn mot-en-review-doc
  "app: 受理企业考评结论审核
  @id 企业申请文档的object-id "
  [id]
  (doc- :en-apply id 
        {:after 
         (fn [rs]
           (let [uid (:uid rs)
                 rs1 (with-uid- :en-stand uid)
                 rs2 (with-esp- (fetch :en-stand :where {:enid uid}))
                 f-yes (format "esp_mot_review('yes', '%s')" id)
                 f-no (format "esp_mot_review('no', '%s')" id) ]
             (html 
               [:h2 "自评报告："]
               (result-html- rs1 [] [:date :type2 :_id] {:form "en-stand-view"})
               [:h2 "评估报告："]
               (result-html- rs2 [] [:date :type2 :uid :_id] {:form (str "org-stand-view/" uid)}) 
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
          (eui-button {:href "#" :onclick "window.close();"} "关闭") )))))
                     
(defn mot-pn-train
  "省级主管机关管理的考评员培训
  @see pn-learn 考评员自己的培训视图"
  [request]
  (let [count (with-mdb2 "esp" (fetch-count :pn-train))
        rt2 (with-esp- (fetch :pn-train :sort {:train-start 1}))
        yyyymm (frequencies (map #(-> % :train-start (leftback "-")) rt2))]
    (html
      [:h1 "考评员培训工作"]
      [:h2 "培训时间，培训学时（不少于24个学时），培训类别，培训合格证号"]
      [:div "注：由省级交通运输主管机关、长江和珠江航务管理局按管辖范围负责组织实施培训、考试工作。"]
      [:br]
      (barf yyyymm {:title "各月份培训的考评员数量" :x "月份" :y "考评员人数"}) 
      [:h2 "尚无培训、考试记录的考评员："]
      (eui-tip "请点击查看考评员详情并录入培训、考试资料。") ; todo: 增加excel导入功能
      (result-html- (data- :pn-apply) [] [:name :type :mobile :_id] {:form "mot-pn-train-doc"})
      [:h2 (format "已录入的考评员培训、考试记录（%s 条）：" count)]
      (result-html- rt2 
                    ["姓名" "培训合格证" "开始日期" "结束日期" "学时" "类型"] 
                    [:name :train-id :train-start :train-end :train-hour :type]))))

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
                        [:td [:label (dd-meta2 f) "："]] 
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
      [:h1 "考评员考试工作"]
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
      [:label "请输入企业证书号："]
      (eui-text {:id "cid" :name "cid" :value "2012-2-1-0551-00844" :style "width:200px"}) [:br][:br] 
      [:label "附加考评原因："] (eui-combo {} dd-recheck) [:br][:br]
      [:label "指定附加考评机构："] (eui-combo {} r) [:br][:br]
      [:label "意 见： "] (eui-textarea {} "") [:br][:br]
      (eui-button {} "提 交") )))

(defn cross-data
  "生成cross-table所需的data，如：[[left1 top1 v1] [left2 top2 v2] ...]
  @rs: 数据库查询结果集
  @fs: 字段列表，如[:admin :grade]或者 [:admin :grade :value]
  @count? 对没有value的2列数据进行计数统计 "
  [rs fs]
  (if (= 2 (count fs)) 
    (for [r rs] [((first fs) r) ((second fs) r) 1])
    (for [r rs] (vec (map #(get r %) fs)))))

(defn mot-olap
  "service: 主管机关对下级机关的综合分析"
  []
  (let [rt1 (with-esp- (fetch :en :only [:admin :grade]))
        rt2 (sort (for [e (frequencies (for [{admin :admin grade :grade} rt1] [admin grade]))] 
                    [(ffirst e) (-> e first second str) (second e)]))
        data (cross-data rt1 [:admin :grade])]
    (html
      [:h1 "各级交通管理部门管辖企业分析"]
      (cross-table data {:caption "主管机构各级企业数量统计表"
                        :dim-top-name "企业级别"
                        :dim-left-name "主管机构"
                        :f-dim-left (fn [v] (dd-admin v))
                        :f-dim-top (fn [v] (str (dd-grade (to-int v)) "企业"))}) )))

(defn mot-admin
  "service: 管理本主管机关直接下级的主管机关
  @todo "
  []
  (html
    (eui-tip "管理本主管机关的直接下级主管机关。")))

(defn mot-give
  "service: 主管机关向下级主管机关委托代办工作
  @todo "
  []
  (html
    (eui-tip "向下级主管机关委托代办审核工作。")))

(defn mot-org-refine
  []
  (html
    (eui-tip "Todo：选择考评机构下发整改通知。")))
  
;;;-------------------------------------------------------------------------------------------------------- hot 热线举报投诉
(def hot---------- nil)

(defn hot
  "app: 举报热线"
  []
  (let [sid "fhot"]
    (html-body
      [:h1 "实名举报"]
      (eui-tip "任何单位和个人对考评机构的考评行为，有权向主管机关进行实名举报，主管机关会及时受理、组织调查处理，并为举报人保密。") [:br][:br]
      [:form {:id "fm1" :action "/c/esp/hot-save" :method "post"}
       [:label [:b "举报人信息等："]] (eui-textarea {:name "info"} "姓 名：\n身份证号：\n联系方式：\n\n其 他：\n") [:br][:br]
       [:label [:b "填写举报内容："]] (eui-textarea {:name "content" :style "width: 500px; height: 300px"} "") [:br][:br]
       [:label [:b "选择主管机关："]] (eui-combo {:name "admin"} dd-admin) [:br][:br]
       (eui-button {:onclick "esp_hot_submit()"} "提 交")] 
      (set-title "举报热线"))))
  
(defn hot-save
  "app: "
  [request]
  (let [vars (query-vars request)]
    (with-mdb2 "esp"
      (insert! :hot (into vars {:date (datetime)})))
    "举报信息已经保存"))
  
;;------------------------------------------------- test
;--- 注：文件大小不能大于64k字节，否则报错
;(with-esp- (fetch :user :where {:uid {:$in ["en1" "en2" "org1" "org2" "pn1" "pn2" "mot1" "mot2"]}}))
;(with-mdb2 "esp" (destroy! :user {:role "pot"}))
;(update- :en {:admin "14 "} {:admin "14"})
;(insert- :user  {:name "岳传志0628", :pid "110104198001010116", :role "pn", :mobile "13581601845", :uid "pn-110104198001010116"})

;(let [tb :pn-train
;      field :type
;      kk (sort < (with-mdb2 "esp" (distinct-values tb (name field) )))]
;  (for [k kk] {field k :count (with-mdb2 "esp" (fetch-count tb :where {field k}))}) )
