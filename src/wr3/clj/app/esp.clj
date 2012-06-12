(ns ^{:doc "企业安全生产标准化管理系统 Enterprise Safety Production Standardization。 
  pn：考评员person，en：企业enterprise，org：考评机构organization，mot：交通部Ministry Of Transport
  pot：省级交通厅/委/局 Pronvince department Of Transport
  暂定的企业达标证书格式：2012-type-grade-admin-%05d "
      :todo "pn、en、org的标识：pn可以用uid，en、org必须用en、org表的oid; 或者把en、org所有用户的uid都设为一样，再用ukey号来标识其他的区别 "
      } 
     wr3.clj.app.esp)

(use 'wr3.clj.app.espconf :reload)
(use 'hiccup.core)
(use 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.s 'wr3.clj.n 'wr3.clj.chart 'wr3.clj.u)
(use 'somnium.congomongo 'wr3.clj.nosql 'wr3.clj.chart)
(import 'wr3.upload.FileUpload 'wr3.upload.File 'wr3.util.Charsetx)

(require '[wr3.clj.app.auth :as au])

;;;-------------------------------------------------------------------------------------------------------- 公共函数
(defn auth
  "该函数被 CljServlet 调用，用于本应用各函数的权限控制 "
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
  "app: 所有子系统的进入界面，临时，最后应该被一个不用登录即可访问的静态页面替代"
  []
  (html-body
    [:div {:style "text-align: center; margin: 100px; border: 1px solid blue; width:992px"}
     [:img {:src "/img/esp/esp.jpg"}]
     [:table {:align "center"}
      (for [[nam id meta] cfg-subsys ] 
        [:tr [:td [:h1 [:a {:href (format "%s/c/esp/index/%s" webapp id) :title meta}
                        (str "进入 " nam )]]]])] [:br]
     [:div {:align "left"}
      (eui-tip "任何单位和个人对考评机构的考评行为，有权向主管机关进行实名举报，主管机关会及时受理、组织调查处理，并为举报人保密。")]
     (eui-button {:href "/c/esp/hot" :target "_blank" :title ""} "实名举报")
     [:div {:style "width:100%; height:50px; margin-top:30px; background-color:#48f"}]]))
  
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

(defn- fileupload-field-
  "文件上传相关的2个字段：（1）一个hidden字段，上传成功文件后显示文件链接；（2）一个上传按钮，弹出真正的文件选取上传； 
  @nam 字段名称如'简历' 
  @sid 字段id、name如'resume'
  @v 初始赋值 
  @m 字段的其他属性如 {:id .. :name .. :title ..}  "
  [nam sid v m] 
  (html 
    (eui-text (merge m {:id sid :name sid :type "hidden" :value (or v "")}))
    [:span (when (not (nullity? v))
             (html [:a {:href v :target "_blank"} "查看"] (space 3)))]
    (eui-button {:onclick (format "fileupload('%s', '%s')" (str nam) sid)} "上传文件..")))

(defn- fileupload-dialog-
  "文件上传的dialog及其js事件函数"
  []
  (html (eui-dialog "fileupload" {:closed "true" :href "/c/esp/fileupload"})
        [:script "fileupload_bt()"]))

;;; 文件上传的设计：
;;;   js函数fileupload(name,sid)弹出对话框（传入input hidden字段中文名及对应id或name），
;;;   保存在 /file 下文件名为 uid-[timestamp].xx ，把该文件名传给自定义name的input hidden字段
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
  "文件上传form提交后的处理：1、保存文件；2、保存文件后调用js函数fileupload_ok()"
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

(defn- input-form-
  "统一录入表单，用于 pn-input, en-input, org-input 函数
  @cfg 表单录入项的配置
  @m 含 {:title .. :form ..} 的其他定制化参数, :title为标题，:form 为区别标识如 'en', 'pn', 'org' "
  [cfg m]
  (let [css-label "font-family:微软雅黑; font-size:14px; vertical-align:center; height: 35px; border-bottom:1px dotted gray"]
    (html
      [:form {:method "POST" :action "/c/esp/input-save"}
       [:table {:align "left" :style "margin-left: 30px"}
        [:caption {:style "padding: 5px"} [:h1 (m :title)]]
        (for [[nam id {t :t v :v require :require title :title}] cfg]
          (let [sid (name id)
                attr {:id sid :name sid :value v :title title}]
            [:tr 
             [:td {:style css-label} [:label (str nam "：")]]
             [:td {:style "border-bottom:1px dotted gray"}
              (cond 
                (true? require)  (eui-text     (into attr {:required "true" :style "width: 250px"}))
                (= t 'textarea)  (eui-textarea attr v)
                (= t 'file)      (fileupload-field- nam sid v attr)
                (= t 'email)     (eui-email    attr)
                (map? t)         (eui-combo    attr t)
                (vector? t)      (eui-combo    attr (apply array-map (flatten (for [e t] [e e]))))
                :else            (eui-text     (into attr {:style "width: 250px"})))
              ]]))
        [:tfoot [:tr [:td {:colspan 2 :align "center" :style "padding: 15px"} 
                      (eui-button {:onclick (format "esp_input_save('%s')" (m :form))} " 保 存 ") (space 5)
                      (eui-button {:onclick (format "esp_input_submit('%s')" (m :form))} "提交申请") (space 5)
                      (eui-button {:onclick "$('form').get(0).reset()"} " 取 消 ") ]]]]]
      (fileupload-dialog-) )))

(defn- input-save-submit-
  "service: 共用函数，考评员、考评机构、企业申请表提交保存或更新
  被 input-form- 进行ajax调用
  @id form名称如'pn' 'en' 'org' "
  [id request submit?]
  (let [vars (query-vars request)
        uid (wr3user request)
        tb1 (keyword id)
        tb2 (keyword (str id "-apply"))
        m (into {:uid uid} (for [[k v] vars :when (not (nullity? v))] [k v]))]
    (with-mdb "esp" 
      (update! tb1 {:uid uid} m) ; 保存到基本信息表
      (when submit?  ; 保存到申请表 
        (insert! tb2 (into m {:date (datetime)})))
      )
    (format "已%s %s 的申请表。" (if submit? "提交" "保存") (vars "name") )))

(defn input-save
  "service: 考评员，考评机构，企业申请表保存；"
  [id request]
  (input-save-submit- id request false))

(defn input-submit
  "service: 共用函数，考评员、考评机构、企业申请表提交保存"
  [id request]
  (input-save-submit- id request true))

(defmacro with-esp-
  "共用函数：查询出esp的序列化数据"
  [fetch-op]
  `(with-mdb2 "esp" (vec ~fetch-op)))

(defn- with-oid-
  "通过object-id来查询文档
  @oid object-id的字符串"
  [tb oid]
  (with-mdb2 "esp" (fetch-by-id tb (object-id oid))))
  
(defn- with-uid-
  "共用函数：得到序列化的数据表中指定uid的记录
  @tb 数据表如 :pn :pn-apply "
  [tb uid]
  (with-esp- (fetch tb :where {:uid uid})))

(defn- data-
  "取出数据表所有记录并持久化。
  @tb :pn | :en | :org
  @todo 超过100条增加分页 "
  [tb]
  (with-esp- (fetch tb :limit 1000)))

(defn- format-date-
  "共用函数：格式化日期，2011-5-4 -> 2011-05-04 便于显示和文本排序"
  [s]
  (let [[yyyy m d] (split s "-")]
    (format "%s-%02d-%02d" yyyy (to-int m) (to-int d))))

(defn- resp-format-
  "格式化'yes' 'no'的显示，用于函数(result-html- ..)， (doc- ..)
  @yes-or-no 'yes' or 'no' "
  [yes-or-no]
  (case yes-or-no 
    "yes" "<font color=green>同 意</font>" 
    "no" "<font color=red>不同意</font>" 
    "<font color=gray>尚未受理</font>"))

(defn- result-html-
  "共用函数：对没有特殊要求的结果进行列表展示
  @rt Clojure.sql结果集 [{:c1 v :c2 v ..} ..]
  @head 表头名称 [活期余额 业务类型 币种 日期]，如果(empty? head)如[] nil，则使用dd-meta自动从cols参数得到head
  @cols 列名称 [:ye :yw_type :bz :_created] 
  @m 客户化定制 {} 设置 :form 表示cols中含 :_id 时文档显示所使用的form "
  ([rt head cols m]
    (let [head (if (empty? head) (for [c cols] (or (dd-meta c) c)) head)]
      (result-html rt {:f-head (fn [thead] (for [th (cons "序号" head)] [:th th]))
                       :f-row (fn [row-index row]
                                [:tr (bgcolor-css row-index)
                                 [:td {:align "right" :style "color: lightgray"} row-index]
                                 (for [col cols] 
                                   (let [v0 (-> col row)
                                         v (-> v0 str trim) ]
                                     [:td (td-align v0) 
                                      (case col
                                        :_id [:a {:href (format "/c/esp/%s/%s" (:form m) v) :target "_blank"} "查看"]
                                        :_select [:input {:type "checkbox" :group "select" :sid (:_id row)}]
                                        :type (or (dd-type (to-int v0)) v)
                                        :type2 (or (dd-type2 (to-int v0)) v)
                                        :grade (or (dd-en-grade (to-int v0)) v)
                                        :fulltime (if v0 "专职" "<font color=gray>兼职</font>")
                                        :contract0 (format-date- v)
                                        :contract1 (if v0 (format-date- v0) "<b>目前在职</b>")
                                        :uid (:name (au/users v0))
                                        :freport [:a {:href v0 :target "_blank"} "查看"]
                                        :info (replace-all v "\r\n" "<br/>")
                                        :admin (or (dd-pot v) v)
                                        :respdate (if (nil? v0) "<font color=gray>尚未处理</font>" 
                                                    (format "已于%s处理" v))
                                        :cstate (if (nil? v0) "正常" "撤销") ; 考评机构证书状态
                                        :reason (or (get (case (:id m) 
                                                           "org" dd-org-backup 
                                                           "en" dd-en-backup) 
                                                         (to-int v0)) v)
                                        :content (if (> (count v) 20) (str (subs v 0 20) " ……") v)
                                        :stand (html "自评分：" [:b (:sum v0)] "；" 
                                                     "等级：" (when-let [grd (:grade v0)] 
                                                             [:b (dd-en-grade (to-int grd))]) "；"
                                                     (when-let [report (:report v0)] [:a {:href report} "自评报告"])) ; 企业达标自评
                                        (:resp :resp-review) (resp-format- v0)
                                        v)])) ]) } )))
  ([rt head cols] (result-html- rt head cols {})))

(defn- insert-
  "共用函数：保存新纪录到esp的tb表中，不需要用with-mdb2包围
  @tb 如:pn-train 
  @m {:name .. :scroe ..} "
  [tb m]
  (with-mdb2 "esp" (insert! tb m)))
                            
(defn- update-
  "共用函数：更新保存记录到esp的tb表中。不需要用with-mdb2包围
  @tb 要更新的表，如 :hot
  @where 查询要更新记录的条件，如 {:_id (object-id '..')}  
  @f 带一个r记录参数的函数，如 (fn [r] {:date (date)}) 
  @replace 任意值（一般用 :replace 即可），代表要替代而不是合并 "
  [tb where f & replace]
  (with-mdb2 "esp"
    (let [rs (fetch tb :where where)]
      (doseq [r rs] 
        (update! tb r (if replace 
                        (f r) 
                        (into r (f r)) ))))))

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
        cfg0 ({:pn cfg-apply-pn :en cfg-apply-en :org cfg-apply-org} type)
        nam (dd-cert type)
        r (first (with-uid- type uid))
        cfg (if r 
              (for [[n id m] cfg0] [n id (merge m (if (id r) {:v (id r)} {}))])
              cfg0)]
    (input-form- cfg {:form (name type) :title (format "<font color=blue>%s</font> 申请表" nam)})))

(defn- search-field-
  "@tb :pn | :en | :org 等等
  @f 字段 :name 中文名称或 :cid 证书号 
  @s 字符串模式"
  [tb f s]
  (with-esp- (fetch tb :limit 1000 :where {f (re-pattern (or s ""))})))

(defn- search-auto-
  "根据输入值是否带中文来搜名称或者证书号，为空则搜出全部"
  [tb id]
  (cond (nullity? id) (data- tb)
        (wr3.util.Charsetx/hasChinese id) (search-field- tb :name id) 
        :else (search-field- tb :cid id) ))

(defn- list-
  "共用函数：列表显示pn、org、en
  @id name的pattern如'张' 
  @tb :pn :org :en "
  [id tb col-names col-ids]
  (let [nam (tb {:pn "考评员" :org "考评机构" :en "交通运输企业"})
        rt (search-auto- tb id)]
    (html
      [:h1 (format "%s 列表（%s 名）" nam (count rt))]
      (result-html- rt col-names col-ids {:form (str "docv/" (name tb))}))))

(defn- with-orgid-
  "orgid数组对应的机构名称
  @orgid 企业申请时自己选择的2个考评机构的id数组，如 ['4f8ae98475e0ae92833680cd' '4f8ae98475e0ae92833680d0'] "
  [orgid]
  (let [oids (map #(object-id %) orgid) ; 注意：直接 (map object-id orgid) 会报错，原因不明
        rs (with-esp- (fetch :org :only [:name] :where {:_id {:$in oids}}))]
    (map :name rs)))

(defn- doc-
  "显示指定表中指定object-id的记录内容。
  @tb 表名如 :pn :en :org 
  @id object-id字符串如 '4f8ad8ef75e0ae9283368075' 
  @m 定制化，参数 {:befor .. :after ..} :befor 在前面的html内容或者带rt参数的fn，
  :after 在后面的html内容或者带rt参数的 (fn [rt] ..) "
  ([tb id m]
    (let [rt (with-oid- tb id)]
      (html-body
        (when-let [before (:before m)] (if (fn? before) (before rt) before))
        [:table.wr3table {:border 1}
         [:caption (format "%s <u>%s</u>" (dd-form tb) (if-let [n (:name rt)] n ""))]
         [:tbody
          (for [[k v] (dissoc rt :_id)]
            [:tr 
             [:th {:style "text-align: left"} (or (dd-meta k) k) "："] 
             [:td (case k ; 显示转换，如：有些代码可以用字典得到名称
                    :type (or (dd-type (to-int v)) v) 
                    :type2 (or (dd-type2 (to-int v 11)) v) 
                    :grade (or (dd-en-grade (to-int v)) v)
                    :belong (str v (when-let [n ((get au/users v) :name)] (format " (%s)" n)))
                    :fulltime (if v "专职" "兼职")
                    :qual (or (dd-org-grade (to-int v)) v)
                    :admin (or (dd-pot (str v)) v)
                    :orgid (eui-combo {:id "orgid" :name "orgid"} (zipmap v (with-orgid- v)))
                    :orgid1 [:a {:href (str "/c/esp/docv/org/" v) :target "_blank"} "查看"]
                    :enid [:a {:href (str "/c/esp/docv/en/" v) :target "_blank"} "查看"]
                    (:info :content) (replace-all v "\r\n" "<br/>")
                    :reason (or (get (case tb :org-backup dd-org-backup :en-backup dd-en-backup) (to-int v)) v) ; 考评机构备案原因
                    :safe [:a {:href v} "查看"] ; 考评机构安全生产组织架构
                    (:resp :resp-review) [:b (resp-format- v)]
                    v)] ])]
         [:tfoot 
          [:tr {:align "center" :height "50px"} 
           [:td {:colspan 2 } (eui-button {:href "#" :onclick "window.close();"} "关闭")]]]] 
        (when-let [after (:after m)] (if (fn? after) (after rt) after)))))
  ([tb id] (doc- tb id {})))

(defn docv
  "service: 公用函数 docview，根据 form 和 object-id 来显示文档
  /c/esp/docv/form/id
  @ids[0] form 数据表名如 'pn' 'en' 'org' 'org-apply' 'org-backup' 等 
  @ids[1] id object-id字符串，如 '4fa12e1fa57efc69b5cd0890?' "
  [ids]
  (let [[form id _] ids]
    (doc- (keyword form) id)))

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
                                               (if is-type2-42? (dissoc dd-en-grade 3) dd-en-grade))] 
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
         (fileupload-field- "自评报告" "report" (:report m) {}) (space 10)
         (eui-button {:onclick (format "esp_stand_save(%s)" type2)} "提 交") ] (fileupload-dialog-) (repeat 10 [:br]) )))
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
    (cond 
      (wr3role? request "en") (update- :en-apply {:uid uid :type2 id} (fn [r] {:score0 sum}))
      (wr3role? request "org") (update- :en-apply {:uid enid :type2 id} (fn [r] {:score1 sum}))
      :else nil )
    "已经保存"))
  
(defn cert-renew
         "service：考评员、考评机构、企业的证书换证
  @id 'pn' 'org' 'en' 转换为:pn :org :en "
  [id request]
  (let [tb (keyword id)
        uid (wr3user request)
        rs (with-esp- (fetch tb :where {:uid uid :cid {:$exists true}}))
        y (case tb :pn 5 :org 5 :en 3 1) ; 有效期年份
        cname (dd-cert tb)]
    (html
      [:h1 "申请换证"]
      (if (empty? rs)
        (eui-tip (format "目前还没有%s证书，对于已有证书系统会自动提醒换证（%s年到期前3个月）。" cname y))
        (html
          [:h2 (format "目前已有的 《%s》：" cname)]
          (for [r rs] 
            (let [cid (:cid r)
                  cdate (if-let [d (:cdate r)] d (str (subs cid 0 4) "-1-1"))
                  cdate-end (date-add (:cdate r) y -3 0)]
            (html (eui-button {:href (format "/c/esp/docv/%s/%s" (name tb) (:_id r)) :target "_blank"}
                              (format "证书号：%s" cid)) (space 5)
                  (format "发证时间：%s，换证时间：<u><b>%s</b></u>" cdate cdate-end [:br] )))))))))

(defn cert-renew-resp
  "service: 主管机关对考评员、考评机构、企业的换证申请受理
  @id pn或org或en "
  [id]
  (let [cname (dd-form (keyword id))]
    (html
      [:h1 (format "%s换证申请受理" cname)]
      (eui-tip "暂无换证申请"))))

(defn cert-resp
  "service: 主管机关制发考评员、考评机构证书，考评机构制发企业证书。
  @id pn,en,org "
  [id]
  (let [title (format "%s制发" (dd-cert (keyword id)))]
    (html
      [:h1 title]
      (eui-tip "暂无需要制发的资格证（系统可直接套打A3纸，也可生成电子证书。）"))))

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
       (fileupload-field- "年度工作报告" sid (:freport r) {})
       [:br][:br]
       (eui-button {:onclick (format "esp_report_save('%s')" tb-report)} "保 存") (space 5) ]
      (fileupload-dialog-)
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
    (eui-button {:plain "true" :iconCls "icon-file"} "考评员培训情况汇总表（何时培训，多长时间，取得何种培训合格证书）") [:br]
    ))

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
  (html
    (eui-tip "暂无整改通知")))

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
    (do
      (update- :pn {:cid cid} (fn [r] {:contract1 (date)}))
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
  (doc- :en-apply id 
        {:after (fn [rt] (html
                           [:br] [:label "第一步："] 
                           (eui-button {:href (format "/c/esp/stand/%s?enid=%s" (:type2 rt) (:uid rt)) 
                                        :target "_blank"} "考评打分") [:br][:br]
                           [:label "第二步："] (yes-no-advice- {:onclick []}) ))}))

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
      [:h1 (format "%s主管的考评机构（%s 名）" (dd-pot admin) (count rs))]
      (eui-tip "请在如下的考评机构列表中自行选择两个。")
      (result-html- rs [] [:name :admin :_id :_select] {:form "docv/org"}) [:br]
      (eui-button {:onclick "esp_en_select_org()"} "提 交")
      [:script (format "esp_en_selected('%s')" (join (:orgid r) ","))]
      )))

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
      (linef rt3 {:title "考评员学历统计" :x "学历" :y "考评员人数"}) 
      )))

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
      (pief (apply array-map (flatten m1)) {})
      )))

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
        [:name :type2 :date :score0 :score1 :resp :resp-review :_id :_select]  ; todo: 考评机构评分score字段
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
        (result-html- rs ["举报时间" "举报人信息" "主管机关" "详情"] [:date :info :admin :_id] {:form "mot-hot-doc"})
        ))))

(defn mot-hot-doc
  "app: 主管机关查看并受理举报信息"
  [id]
  (let [r (with-oid- :hot id)]
    (doc- :hot id
          {:after (html
                    [:form#fm1 {:action "/c/esp/mot-hot-resp" :method "post"}
                     [:input {:name "oid" :type "hidden" :value (:_id r)}]
                     [:p [:label "处理意见："] (eui-textarea {:name "advice"})]
                     [:p [:label "转发至主管机关："] (eui-combo {:id "admin" :name "admin" :value (:admin r)} dd-pot)]
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

(defn mot-olap
  "service: 主管机关对下级机关的综合分析"
  []
  (let [rt1 (with-esp- (fetch :en :only [:admin :grade]))
        rt2 (sort (for [e (frequencies (for [{admin :admin grade :grade} rt1] [admin grade]))] 
                    [(ffirst e) (-> e first second str) (second e)]))]
    (html
      [:h1 "各级交通管理部门管辖企业分析"]
      (cross-table rt2 {:caption "主管机构各级企业数量统计表"
                        :dim-top-name "企业级别"
                        :dim-left-name "主管机构"
                        :f-dim-left (fn [v] (dd-pot v))
                        :f-dim-top (fn [v] (str (dd-en-grade (to-int v)) "企业"))}) )))

(defn mot-admin
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
       [:label [:b "选择主管机关："]] (eui-combo {:name "admin"} dd-pot) [:br][:br]
       (eui-button {:onclick "esp_hot_submit()"} "提 交")] )))
  
(defn hot-save
  "app: "
  [request]
  (let [vars (query-vars request)]
    (with-mdb2 "esp"
      (insert! :hot (into vars {:date (datetime)})))
    "举报信息已经保存"))
  
;;------------------------------------------------- test
(require 'wr3.clj.datagen)

(defn- t1
  "造org表的数据字段" 
  []
  (with-mdb2 "esp"
    (let [rs (fetch :org) name3 (wr3.clj.datagen/rand-name) ]
      (doseq [r rs] (let [m {:_id ""
                             :name ""
                             :legalp (name3)
                             :admin 13
                             :mobile (format "13%s" (apply str (random-n 9 9))) }]
                      (update! :org r (dissoc (into m r) :legelp)) ))) ))

(defn- gen-pn-cid-
  "考评员证书号格式为：YYYY—C—NA—XXXXX。YYYY表示年份，C表示类别，NA表示发证机关, XXXXX表示编号"
  [type]
  (format "2011-%s-%s-%05d" type (-> (rand-nth (vec dd-pot)) first) (rand-int 100000)))

(defn- gen-org-cid-
  "考评员证书号格式为：YYYY—C—NA—甲XXXXX。YYYY表示年份，C表示类别，NA表示发证机关, XXXXX表示编号"
  [r]
  (let [nam (:name r)
        c (if (has? nam "港") "G" "D")
        grade (rand-nth ["乙" "丙"])
        admin (:admin r)
        admin (to-int admin)
        na (subs (dd-pot admin) 0 2)]
    (format "2012-%s-%s-%s%04d" c na grade (rand-int 10000))))
  
;注：文件大小不能太大，1616行？或者？个字节

(use 'wr3.clj.app.esptmp)
(defn- t1 []
(with-mdb2 "esp"
  (doseq [[i n t] m1]
    (insert! :indic1 {:i i :name (name n) :type2 t}))))

(defn- t2 []
(with-mdb2 "esp"
  (doseq [[i j n t] m2]
    (insert! :indic2 {:i i :j j :name (name n) :type2 t}))))

(defn- t3 []
(with-mdb2 "esp"
  (doseq [[i j k n star s t] m3]
    (insert! :indic3 {:i i :j j :k k :name (name n) :star star :score s :type2 t}))))

;(with-mdb2 "esp" (destroy! :org-backup {:content {:$exists false}}))
;(t3)
;(update- :pn-train {:name "张文件1"} (fn [row] {:uid "pn1"}))
;(with-oid- :en-apply "4faa19f6b920d899978c1bb2")
;(with-mdb2 "esp" (destroy! :en-stand {:enid {:$exists true}}))
;(update- :en-apply {:oid {:$exists true}} (fn [row] (dissoc row :oid)) :replace)
;(update- :en-stand {:_id (object-id "4fceeaebb9207badd6a1a7a7")} (fn [r] {:uid  "en3"}))
;(with-esp- (fetch :en-stand :where {:enid {:$exists true}}))
;(update- :en-apply {:name "中国远洋物流公司"} (fn [r] {:enid "4f84e337b9201f14a1fe3717"}))
;(with-mdb2 "esp" (update! :en-apply {:uid "en1"} {:$set {:score0 989}}))
;(update- :en-apply {:uid "en1" :type2 "11"} (fn [r] {:score0 989}))
;(with-esp- (fetch :en-apply :where {:type2 {:$in [11 "11"]}}))