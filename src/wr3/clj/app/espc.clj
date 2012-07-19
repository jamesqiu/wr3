(ns ^{:doc "esp common utils for esp/espm/espreg/espfj"}
     wr3.clj.app.espc)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.nosql 'wr3.clj.u 'wr3.clj.n 'wr3.clj.tb)
(use 'wr3.clj.app.espconf)
(use 'somnium.congomongo 'hiccup.core)

(defn- input-save-submit-
  "service: 共用函数，考评员、考评机构、企业申请表提交保存或更新
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
        (insert! tb2 (into m {:date (datetime)}))) )
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
  "查询出esp的数据并序列化之"
  [fetch-op]
  `(with-mdb2 "esp" (vec ~fetch-op)))

(defn with-oid-
  "通过object-id来查询文档
  @oid object-id的字符串"
  [tb oid]
  (with-mdb2 "esp" (fetch-by-id tb (object-id oid))))
  
(defn with-uid-
  "得到数据表中指定uid的多条记录，并序列化之
  @tb 数据表如 :pn :pn-apply "
  [tb uid]
  (with-esp- (fetch tb :where {:uid uid})))

(defn data-
  "取出数据表所有记录（1000条以内）并持久化。
  @tb :pn | :en | :org
  @todo 超过100条增加分页 "
  [tb]
  (with-esp- (fetch tb :limit 1000)))

(defn- wr3user-name
  "传入wr3user的uid，得到name"
  [uid]
  (if-let [rs (with-mdb2 "esp" (fetch-one :user :only [:name] :where {:uid uid}))]
    (:name rs) uid))

(defn- format-date-by
  "共用函数：格式化形如“2011-5-4”的日期"
  [s fmt]
  (let [[yyyy m d] (split s "-")] (format fmt yyyy (to-int m) (to-int d))))
  
(defn- format-date-
  "2011-5-4 -> 2011-05-04 便于显示和文本排序"
  [s] (format-date-by s "%s-%02d-%02d"))

(defn format-date-cert-
  "2011-5-4 -> '2011年 05月 04日' 在证书上显示"
  [s] (format-date-by s "%s年 %02d月 %02d日"))

(defn resp-format-
  "格式化'yes' 'no'的显示，用于函数(result-html- ..)， (doc- ..)
  @yes-or-no 'yes' or 'no' "
  [yes-or-no]
  (case yes-or-no 
    "yes" "<font color=green>同 意</font>" 
    "no" "<font color=red>不同意</font>" 
    "<font color=gray>尚未受理</font>"))

(defn pass-direct-format
  [on-off]
  (case on-off
    "on" "直接颁发"
    "否"))

(defn types-names
  "把形如 '1&2&5' 或者 '3'的多个或者1个类型转换成名称显示 "
  [types]
  (if (integer? types) 
    (dd-type types)
    (if (nullity? types) types
      (let [ss (split types "&")
            nn (map #(or (dd-type (to-int % 1)) %) ss)]
        (join nn ", ")))))

(defn result-html-
  "共用函数：对没有特殊要求的结果进行列表展示
  @rt Clojure.sql结果集 [{:c1 v :c2 v ..} ..]
  @head 表头名称 [活期余额 业务类型 币种 日期]，如果(empty? head)如[] nil，则使用dd-meta2自动从cols参数得到head
  @cols 列名称 [:ye :yw_type :bz :_created] 
  @m 客户化定制 {} 设置 :form 表示cols中含 :_id 时文档显示所使用的form "
  ([rt head cols m]
    (let [head (if (empty? head) (for [c cols] (or (dd-meta2 c) c)) head)]
      (result-html 
        rt 
        {:f-head (fn [thead] (for [th (cons "序号" head)] [:th th]))
         :f-row (fn [row-index row]
                  [:tr (bgcolor-css row-index)
                   [:td {:align "right" :style "color: lightgray"} row-index]
                   (for [col cols] 
                     (let [v0 (-> col row)
                           v (-> v0 str trim) ]
                       [:td (td-align v0) 
                        (case col
                          :_id [:a {:href (format "/c/esp/%s/%s" (:form m) v) :target "_blank"} "查看"]
                          :_id-fj [:a {:href (format "/c/espfj/%s/%s" (:form m) (:_id row)) :target "_blank"} "查看"] ; 福建
                          :_select [:input {:type "checkbox" :group "select" :sid (:_id row)}]
                          :_issue [:a {:href (case (:issue m)
                                               "pn-apply" (format "/c/esp/cert-issue-pn/%s" (:_id row))
                                               (format "/c/esp/cert-issue/%s/%s" (:issue m) (:_id row)))
                                       :target "_blank"} "发证"]
                          :_cdate-end (date-add (:cdate row) (dd-cert-year (:type m)) 0 0)
                          :type (types-names v)
                          :type2 (or (dd-type2 (to-int v0)) v)
                          :grade (or (dd-grade (to-int v0)) v)
                          :fulltime (if v0 "专职" "<font color=gray>兼职</font>")
                          :contract0 (format-date- v)
                          :contract1 (if v0 (format-date- v0) "<b>目前在职</b>")
                          :uid (wr3user-name v0)
                          :role (if-let [dd (:admin m)] (get dd v) v) ; espfj 角色
                          :freport [:a {:href v0 :target "_blank"} "查看"]
                          :info (replace-all v "\r\n" "<br/>")
                          :admin (or (get (or (:admin m) dd-admin) v) v)
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
                                               [:b (dd-grade (to-int grd))]) "；"
                                       (when-let [report (:report v0)] [:a {:href report} "自评报告"])) ; 企业达标自评
                          (:resp :resp-eval :resp-review) (resp-format- v0)
                          :pass-direct (pass-direct-format v)
                          v)])) ]) } )))
  ([rt head cols] (result-html- rt head cols {})))

(defn insert-
  "共用函数：保存新纪录到esp的tb表中，不需要用with-mdb2包围
  @tb 如:pn-train 
  @m {:name .. :scroe ..} "
  [tb m]
  (with-mdb2 "esp" (insert! tb m)))
                            
(defn update-
  "共用函数：更新保存记录到esp的tb表中。不需要用with-mdb2包围
  @tb 要更新的表，如 :hot
  @where 查询要更新记录的条件，如 {:_id (object-id '..')}  
  @fm 带一个r记录参数的函数f或者一个哈希m，如 (fn [r] {:date (date)}) 
  @replace 任意值（一般用 :replace 即可），代表要替代而不是合并 "
  [tb where fm & replace]
  (with-mdb2 "esp"
    (let [rs (fetch tb :where where)]
      (doseq [r rs] 
        (let [fr (if (fn? fm) (fm r) fm)]
          (update! tb r (if replace fr (into r fr) )))))))

(defn with-orgid-
  "orgid数组对应的机构名称
  @orgid 企业申请时自己选择的2个考评机构的id数组，如 ['4f8ae98475e0ae92833680cd' '4f8ae98475e0ae92833680d0'] "
  [orgid]
  (let [oids (map #(object-id %) orgid) ; 注意：直接 (map object-id orgid) 会报错，原因不明
        rs (with-esp- (fetch :org :only [:name] :where {:_id {:$in oids}}))]
    (map :name rs)))

(defn doc-
  "显示指定表中指定object-id的记录内容。
  @tb 表名如 :pn :en :org 
  @id object-id字符串如 '4f8ad8ef75e0ae9283368075' 
  @m 定制化，参数 {:befor .. :after ..} 
    :befor 在前面的html内容或者带rt参数的fn，
    :after 在后面的html内容或者带rt参数的 (fn [rt] ..) 
    :rs 不使用从esp库的tb表查数据，给定数据。
    :app 非esp的应用如'espfj'
  "
  ([tb id m]
    (let [rt (or (:rs m) (with-oid- tb id))]
      (html-body
        (when-let [before (:before m)] (if (fn? before) (before rt) before))
        [:table.wr3table {:border 1}
         [:caption (format "%s <u>%s</u>" (dd-form tb) (if-let [n (:name rt)] n ""))]
         [:tbody
          (for [[k v] (dissoc rt :_id)]
            [:tr 
             [:th {:style "text-align: left"} (or (dd-meta2 k) k) "："] 
             [:td (case k ; 显示转换，如：有些代码可以用字典得到名称
                    :type (types-names v) 
                    :type2 (or (dd-type2 (to-int v 11)) v) 
                    :grade (or (dd-grade (to-int v)) v)
                    :belong (str v (when-let [n (wr3user-name v)] (format " (%s)" n)))
                    :fulltime (if v "专职" "兼职")
                    :admin (or (get (or (:admin m) dd-admin) (str v)) v) ; espfj 可在m中指定 {:admin dd-admin-fj}
                    :from (or (get (or (:from m) dd-province) v) v) ; espfj 可在m中指定 {:admin dd-province-fj}
                    :orgid (eui-combo {:id "orgid" :name "orgid"} (zipmap v (with-orgid- v)))
                    :orgid1 [:a {:href (str "/c/esp/docv/org/" v) :target "_blank"} "查看"]
                    :enid [:a {:href (str "/c/esp/docv/en/" v) :target "_blank"} "查看"]
                    (:info :content) (replace-all v "\r\n" "<br/>")
                    :reason (or (get (case tb :org-backup dd-org-backup :en-backup dd-en-backup) (to-int v)) v) ; 考评机构备案原因
                    (:safe :photo :perf2 :proof :proof2 :proof3) [:a {:href v} "查看"] ; 考评机构安全生产组织架构等
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

