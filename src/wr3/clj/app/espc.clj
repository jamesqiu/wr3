(ns ^{:doc "esp common utils for esp/espm/espreg/espfj"}
     wr3.clj.app.espc)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.nosql 'wr3.clj.u 'wr3.clj.n 'wr3.clj.tb)
(use 'wr3.clj.app.espconf)
(use 'somnium.congomongo 'hiccup.core)
(use 'clojure.contrib.json)

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
  "得到数据表中指定uid的*多条*记录，并序列化之
  @tb 数据表如 :pn :pn-apply "
  [tb uid]
  (with-esp- (fetch tb :where {:uid uid})))

(defn tb-count
  "得到tb的全记录数；或者tb中字段f符合匹配条件s的记录数。"
  ([tb] (with-mdb2 "esp" (fetch-count tb)))
  ([tb s] (let [f (if (wr3.util.Charsetx/hasChinese s) :name :cid)] (tb-count tb f s)))
  ([tb f s] (with-mdb2 "esp" (fetch-count tb :where {f (re-pattern (or s ""))}))))
  
(defn data-
  "取出数据表所有记录（1000条以内）并持久化。
  @tb 如 :pn | :en | :org"
  ([tb] (with-esp- (fetch tb :limit 100)))
  ([tb m] 
    (let [limit (or (:limit m) 100)
          skip (or (:skip m) 0)]
      (with-esp- (fetch tb :skip skip :limit limit)))))

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

(defn wr3user-name
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

(defn format-date-cert
  "2011-5-4 -> '2011年 05月 04日' 在证书上显示"
  [s] (format-date-by s "%s年 %02d月 %02d日"))

(defn- format-resp-
  "格式化'yes' 'no'的显示，用于函数(result-html- ..)， (doc- ..)
  @yes-or-no 'yes' or 'no' "
  [yes-or-no]
  (case yes-or-no 
    "yes" "<font color=green>同 意</font>" 
    "no" "<font color=red>不同意</font>" 
    "<font color=gray>尚未受理</font>"))

(defn- format-pass-direct
  [on-off]
  (case on-off "true" "直接颁发" "否"))

(defn- format-type
  "把形如 '1&2&5' 或者 '3'的多个或者1个类型转换成名称显示 "
  [types]
  (if (integer? types) 
    (dd-type types)
    (if (nullity? types) types
      (let [ss (split types "&")
            nn (map #(or (dd-type (to-int % 1)) %) ss)]
        (join nn ", ")))))

(defn format-orgid1
  "通过uid字符串得到org名称"
  ([uid m]
    (let [r (first (with-uid- :org uid))
          nam (or (:name r) uid)]
      (if (:link m) 
        [:a {:href (format "/c/esp/docv/org/%s" (:_id r)) :target "_blank"} nam]
        nam)))
  ([uid] (format-orgid1 uid nil)))

(defn format-ids-
  "得到oid列表、或者uid列表对应的名称列表
  @tb 如:org :pn 
  @ids 如：['4f8aeb2a75e0ae92833680de' '4f8aeb2a75e0ae92833680e2']
  @m 定制化参数：
   {:link true}表示带链接，nil表示不带链接 
   {:form ..} 表示用于显示doc的form
   {:uid true} 表示用uid，而不是oid "
  ([tb ids m]
    (let [uid? (:uid m)
          ids (if uid? ids (map #(object-id %) ids)) ; 注意：直接 (map object-id ids) 会报错，原因不明
          field (if uid? :uid :_id)
          rs (with-esp- (fetch tb :only [:name] :where {field {:$in ids}}))
          tb-name (name tb)]
      (if (:link m) 
        (map #(format "<a href='/c/esp/%s/%s' target='_blank'>%s</a>" 
                      (or (:form m) (str "docv/" tb-name)) (:_id %) (:name %)) rs)
        (map :name rs))) )
  ([tb ids] (format-ids- tb ids nil)))

(defn- format-result-field-
  "把列表显示中某个字段的值进行格式化，如 :type字段的'1'->'道路运输'
  @row 一行数据如 {:type '1' ..}
  @col 字段名称如 :type :_id :pnids 
  @v0 字段原来的值如 '1' '4f8aeb2a75e0ae92833680e4' ['4f8aeb2a75e0ae92833680d9' '4f8aeb2a75e0ae92833680e2'] 
  @m 客户定制化的设置如 {:form 'docv/pn' :issue 'pn-apply' :type ..} "
  [row col v0 m]
  (let [v (.trim (str v0))]
    (case col
      :_id [:a {:href (format "/c/esp/%s/%s" (:form m) v) :target "_blank"} "查看"]
      :_id-fj [:a {:href (format "/c/espfj/%s/%s" (:form m) (:_id row)) :target "_blank"} "查看"] ; 福建
      :_select [:input {:type "checkbox" :group "select" :sid (:uid row)}]
      :_issue [:a {:href (case (:issue m)
                           "pn-apply" (format "/c/esp/cert-issue-pn/%s" (:_id row))
                           (format "/c/esp/cert-issue/%s/%s" (:issue m) (:_id row)))
                   :target "_blank"} "发证"]
      :_cdate-end (date-add (:cdate row) (dd-cert-year (:type m)) 0 0)
      :type (format-type v)
      :type2 (or (dd-type2 (to-int v0)) v)
      :grade (or (dd-grade (to-int v0)) v)
      :fulltime (if v0 "专职" "<font color=gray>兼职</font>")
      :contract0 (format-date- v)
      :contract1 (if v0 (format-date- v0) "<b>目前在职</b>")
      :uid (if (:mot-user m) v0 (wr3user-name v0))
      :admin-uid (wr3user-name v0)
      :role (if-let [dd (:admin m)] (get dd v) v) ; espfj 角色
      :freport [:a {:href v0 :target "_blank"} "查看"]
      :info (replace-all v "\r\n" "<br/>")
      :admin (or (get (or (:admin m) dd-admin) v) v)
      :respdate (if (nil? v0) "<font color=gray>尚未处理</font>" 
                  (format "已于%s处理" v))
      :respdate-review (if (nil? v0) "<font color=gray>尚未处理</font>" 
                         (format "已于%s处理" v))
      :cstate (if (nil? v0) "正常" "撤销") ; 考评机构证书状态
      :reason (or (get (case (:id m) 
                         "org" dd-org-backup 
                         "en" dd-en-backup) 
                       (to-int v0)) v)
      :content (if (> (count v) 20) (str (subs v 0 20) " ……") v)
      :stand (html "评分：" [:b (:sum v0)] "；" 
                   "等级：" (when-let [grd (:grade v0)] 
                           [:b (dd-grade (to-int grd))]) "；"
                   (when-let [report (:report v0)] [:a {:href report} "报告"])) ; 企业达标自评
      (:resp :resp-eval :resp-review) (format-resp- v0)
      :pass-direct (format-pass-direct v)
      :orgid1 (format-orgid1 v {:link true})
      :pnids (join (format-ids- :pn v0 {:link true :form "pn-doc" :uid true}) "、")
      :belong (str v (when-let [n (wr3user-name v)] (format " (%s)" n)))
      v)))

(defn result-html-
  "共用函数：对没有特殊要求的结果进行列表展示
  @rt Clojure.sql结果集 [{:c1 v :c2 v ..} ..]
  @head 表头名称 [活期余额 业务类型 币种 日期]，如果(empty? head)如[] nil，则使用dd-meta自动从cols参数得到head
  @cols 列名称 [:ye :yw_type :bz :_created] 
  @m 客户化定制 {} 设置 :form 表示cols中含 :_id 时文档显示所使用的form "
  ([rt head cols m]
    (let [head (if (empty? head) (for [c cols] (or (dd-meta c) c)) head)]
      (result-html 
        rt 
        {:f-head (fn [thead] (for [th (cons "序号" head)] [:th th]))
         :f-row (fn [row-index row]
                  [:tr (bgcolor-css row-index)
                   [:td {:align "right" :style "color: lightgray"} row-index]
                   (for [col cols] 
                     (let [v0 (-> col row)]
                       [:td (td-align v0) (format-result-field- row col v0 m)])) ])
         })))
  ([rt head cols] (result-html- rt head cols {})))

(defn- format-doc-field-
  "把form文档显示中字段的值进行格式化，如 :type字段的'1'->'道路运输'
  @tb 表名如 :pn :org
  @k 字段名称如 :type :_id :pnids 
  @v 字段原来的值如 '1' '4f8aeb2a75e0ae92833680e4' ['4f8aeb2a75e0ae92833680d9' '4f8aeb2a75e0ae92833680e2'] 
  @m 客户定制化的设置 "
  [tb k v m]
  (case k ; 显示转换，如：有些代码可以用字典得到名称
    :type (format-type v) 
    :type2 (or (dd-type2 (to-int v 11)) v) 
    :grade (or (dd-grade (to-int v)) v)
    :belong (str v (when-let [n (wr3user-name v)] (format " (%s)" n)))
    :fulltime (if v "专职" "兼职")
    :admin (or (get (or (:admin m) dd-admin) (str v)) v) ; espfj 可在m中指定 {:admin dd-admin-fj}
    (:uid :admin-uid) (wr3user-name v)
    :from (or (get (or (:from m) dd-province) v) v) ; espfj 可在m中指定 {:admin dd-province-fj}
    :orgid (if (:orgid-as-select m) 
             (eui-combo {:id "orgid" :name "orgid"} (zipmap v (format-ids- :org v {:uid true})))
             (join (format-ids- :org v {:link true :uid true}) "，"))
    :orgid1 [:a {:href (str "/c/esp/docv/org/" v) :target "_blank"} (format-orgid1 v {:link true})]
    :pnids (join (format-ids- :pn v {:link true :form "pn-doc" :uid true}) "、")
    :enid [:a {:href (str "/c/esp/docv/en/" v) :target "_blank"} "查看"]
    (:info :content) (replace-all v "\r\n" "<br/>")
    :reason (or (get (case tb :org-backup dd-org-backup :en-backup dd-en-backup) (to-int v)) v) ; 考评机构备案原因
    (:safe :photo :perf2 :proof :proof2 :proof3 :titlefile :beginfile) [:a {:href v} "查看"] ; 考评机构安全生产组织架构等
    (:resp :resp-review) [:b (format-resp- v)]
    :pass-direct (format-pass-direct v)
    v))

(defn doc-
  "显示指定表中指定object-id的记录内容。
  @tb 表名如 :pn :en :org 
  @id object-id字符串如 '4f8ad8ef75e0ae9283368075' 
  @m 定制化，参数 {:befor .. :after ..} 
    :befor 在前面的html内容或者带rt参数的fn，
    :after 在后面的html内容或者带rt参数的 (fn [rt] ..) 
    :rs 不使用从esp库的tb表查数据，给定数据。
    :app 非esp的应用如'espfj'
    :orgid-as-select 为true则orgid显示为下拉框
    :orders [:name :pid ..] 字段显示的顺序 "
  ([tb id m]
    (let [rt (or (:rs m) (with-oid- tb id))
          rt (if-let [orders (:orders m)]
               (apply array-map (reduce into (concat (for [k orders :let [v (rt k)] :when v] [k v])
                                                     (for [[k v] rt :when (not (in? k orders))] [k v]))))
               rt)]
      (html-body
        (when-let [before (:before m)] (if (fn? before) (before rt) before))
        [:table.wr3table {:border 1}
         [:caption (format "%s <u>%s</u>" (dd-form tb) (if-let [n (:name rt)] n ""))]
         [:tbody
          (for [[k v] (dissoc rt :_id)]
            [:tr 
             [:th {:nowrap "true" :style "text-align: left"} (or (dd-meta k) k) "："] 
             [:td (format-doc-field- tb k v m)] ])]
         [:tfoot 
          [:tr {:align "center" :height "50px"} 
           [:td {:colspan 2 } (eui-button-close)]]]] 
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

(defn search-field-
  "@tb :pn | :en | :org 等等
  @f 字段 :name 中文名称或 :cid 证书号 
  @s 字符串模式
  @m 参数{:skip .. :limit .. :where } 注意：:where条件如果用到:name和:cid字段，可能会被s匹配覆盖。 "
  ([tb f s] (search-field- tb f s nil))
  ([tb f s m]
    (let [limit (or (:limit m) 100)
          skip (or (:skip m) 0)
          where (merge (:where m) {f (re-pattern (or s ""))} )]
      (with-esp- (fetch tb :skip skip :limit limit :where where)) )))

(defn search-auto-
  "根据输入值是否带中文来搜名称或者证书号，为空则搜出全部"
  [tb id m]
  (cond (nullity? id) (data- tb m)
        (wr3.util.Charsetx/hasChinese id) (search-field- tb :name id m) 
        :else (search-field- tb :cid id m) ))

(defn pager-options
  "每页100行记录的eui-combo的options，注意key是integer型。
  如: ([0 \"第1页：1-100\"] [100 \"第2页：101-200\"] [200 \"第3页：201-201\"]) "
  [n]
  (let [pages (int (Math/ceil (/ n 100)))]
    (map #(let [k (* % 100)
                v (format "第%s页：%s-%s" (inc %) (inc k) (if (= % (dec pages)) n (* 100 (inc %))))] 
            (vector k v))
         (range pages))))

(defn pager-html
  "显示分页的部分html片段。
  @count1 全部的记录条数
  @skip 跳过多少条
  @onchange js的onchange事件 "
  [count1 skip onchange]
  (html
      [:label {:for "pagers"} (format "共 %s 页 %s 条，选择：" (int (Math/ceil (/ count1 100))) count1)] 
      (eui-combo {:id "pagers" :name "pagers" :value skip :onchange onchange} (pager-options count1)) ))
  
(defn list-
  "共用函数：列表显示pn、org、en。相关函数： pn-list en-list org-list org-en-archive
  @id name的pattern如'张' 或者为nil
  @tb :pn :org :en 
  @col-names @col-ids 列显示名称，存储名称
  @m 含:skip等参数"
  [id tb col-names col-ids m]
  (let [nam (tb {:pn "考评员" :org "考评机构" :en "交通运输企业"})
        rt (search-auto- tb id m)
        count1 (if id (tb-count tb id) (tb-count tb))]
    (html
      [:h1 (format "%s 列表（%s 名）" nam (count rt))]
      (pager-html count1 (:skip m) (format "esp_pager('/c/esp/%s-list/%s')" (name tb) (if id id "")))
      (eui-button 
        {:href (format "/c/esp/list-export/exp-%s列表.csv?content-type=text/csv&charset=GBK&tb=%s&s=%s" 
                       (dd-form tb) (name tb) (or id "")) :style "margin:10px"} 
        (format "文件导出（全部%s行）" count1))
      (result-html- rt col-names col-ids {:form (str "docv/" (name tb))})
      [:br] )))

(defn result-csv
  [rt col-names col-ids]
  (let [fcol (fn [r c] (let [v (c r)] 
                         (wr3.util.Csv/toCsv 
                           (case c
                             :type (dd-type (to-int v))
                             :type2 (dd-type2 (to-int v))
                             :grade (dd-grade (to-int v))
                             :orgid1 (format-orgid1 v)
                             v))))
        frow (fn [r] (join (map #(fcol r %) col-ids) ","))]
    (str
      (join (map #(wr3.util.Csv/toCsv %) col-names) ",") "\r\n"
      (join (for [r rt] (frow r) ) "\r\n"))))

(defn list-export
  "导出列表。调用url形如：
  /c/esp/list-export/列表.csv?content-type=text/csv&charset=GBK&tb=en-apply&s=张
  @tb 'pn' 'en' 'org'
  @s 搜索字符串如'张' 
  todo：增加id搜索字符串，真正导出列表"
  [tb s]
  (let [tb (keyword tb)
        rt (if (= tb :en-apply) 
             (with-esp- (fetch :en-apply :where {:cid {:$exists true}}))
             (search-auto- tb s {:limit 0}))
        col-names ({:en-apply ["企业名称" "达标类别" "达标等级" "实施考评的考评机构"]
                    :pn ["姓名" "业务类型"]
                    :en ["企业名称" "业务类型" "达标等级"]
                    :org ["考评机构名称" "等级"]
                    } tb)
        col-ids ({:en-apply [:name :type2 :grade :orgid1]
                  :pn [:name :type]
                  :en [:name :type :grade]
                  :org [:name :grade]
                  } tb)
        ]
    (result-csv rt col-names col-ids) ))


(defn apply-nav-
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
      [:h1 (format "%s申请" (dd-cert type))]
      (eui-button {:href "#" :onclick (format "layout_load_center('/c/esp/%s-input')" (name type)) :style "margin:10px"}  
                  (if r1 "进行证书申请" "进行证书初次申请"))
      (when rs2
        (html [:h2 (format "已提交过%s次申请：" (count rs2))]
              (result-html- rs2 '[] [:date (case type :en :type2 :type) :resp :respdate :_id] 
                            {:form (format "docv/%s-apply" (name type))}) )) )))

(defn cfg-set-values-
  "cfg配置和已有记录合并：设置cfg的m中的:v缺省值"
  [cfg0 r]
  (for [[n id m] cfg0] [n id (merge m (if (id r) {:v (id r)} {}))]))

(defn apply-input-
  "共用函数：pn-input, org-input, en-input的录入表单。
  把缺省value设为用户最后录入的数据记录中的值，以便减少录入量。
  @type :pn :en :org "
  [request type]
  (let [uid (wr3user request)
        ntype (name type)
        cfg0 ({:pn cfg-apply-pn :en cfg-apply-en :org cfg-apply-org} type)
        nam (dd-cert type)
        r (first (with-uid- type uid))
        cfg (if r (cfg-set-values- cfg0 r) cfg0)]
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

(defn stand-
  "安全生产达标标准的企业自评、考评机构评分
  @type2 细类, 如11，51
  @enid 被评估的企业的uid
  @m 含各字段缺省值的hash-map，如：{:grade '2' :sum '998' :report '..' :f_1_1_1 '5'}  "
  ([type2 enid m]
    (let [;type2 (to-int (or id 11))
          is-type2-42? (= 42 type2)  ; 特殊：42——城市轨道交通运输企业（由于安全要求高，只能申请一二级达标）
          [rt1 rt2 rt3] (map #(get-stand- (str "indic" %) type2) [1 2 3]) ; "en-stand" -> "indic"
          fsetv (fn [k v0] (if-let [v (get m k)] (to-int v) v0)) ; k：字段名称如:grade,:sum； v0：字段缺省值
          role (:role m)
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
         [:h2 [:label (if (= role "mot") "申请达标级别：" "请选择申请达标级别：")] 
          (eui-combo {:id "grade" :name "grade"  :onchange "esp_stand_grade()"
                      :value (fsetv :grade 1) }
                     (if is-type2-42? (dissoc dd-grade 3) dd-grade))] 
         (eui-tip [:span#tip (dd-stand-tip (fsetv :grade 1))
                   (when is-type2-42? "<br/>注：城市轨道交通运输企业由于安全要求高，只能申请一二级达标，不能申请三级达标。")])
         [:table.wr3table {:border 1}
          [:caption (format "%s企业安全生产达标标准" (dd-type2 type2))]
          [:thead [:tr [:th "考核内容"] [:th {:colspan 2} "考核要点"] [:th {:nowrap "1"} "分数"] [:th "星级"] 
                   [:th (if enid "评估分" "自评分")] ]]
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
         (if (= role "mot")
           [:a {:href (:report m) :target "_blank"} "查看"]
           (fileupload-field "自评报告" "report" (:report m) {})) 
         (space 10)
         (if (= role "mot")
           (eui-button-close)
           (eui-button {:onclick (format "esp_stand_save(%s)" type2)} "提 交")) ] 
        (fileupload-dialog) (repeat 10 [:br]) )))
  ([type2] (stand- type2 nil {})))

(defn name-cid-search-input
  "自动完成的en名称、证书号录入框的html片段
  @tb :en :org :pn "
  [tb]
  (let [nam (dd-form tb)]
    (html
      (eui-tip (format "以下输入框提供自动完成功能，请输入%s名称或证书号关键字，并选定列表中的一个%s。" nam nam))
      [:label (format "%s名称或证书号：" nam)]
      [:input#in {:name "in" :type "text" :style "width:500px;" :value ""}]
      [:script (format "esp_name_cid_autocomplete('%s')" (name tb))])))

(defn name-cid-autocomplete
  "service: en-search-input函数提交的自动完成查询
  @id 'en' 'pn' 'org' 
  @term 自动完成控件自动附上的参数，带搜索字符串 "
  [id term request]
  (let [tb (keyword id)
        rs (search-auto- tb term {:limit 20})] ;  :where {:cid {:$exists true}}
    (json-str (map #(format "%s, 证书号:%s" (:name %) (:cid %)) rs))))

;;;-------------------------------------------------------------------------------------------------------- hot 热线举报投诉
(def hot---------- nil)

(defn hot
  "app: 举报热线"
  []
  (let [sid "fhot"]
    (html-body
      [:h1 "实名举报"]
      (eui-tip "任何单位和个人对考评机构的考评行为，有权向主管机关进行实名举报，主管机关会及时受理、组织调查处理，并为举报人保密。") [:br][:br]
      [:form {:id "fm1" :action "/c/espc/hot-save" :method "post"}
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
;(use 'wr3.clj.datagen)
;--- 注：文件大小不能大于64k（65536）字节，否则报错
;(with-esp- (fetch :en :where {:admin {:$exists false}}))
;(with-mdb2 "esp" (destroy! :user {:name #"^0815"}))
;(update- :refine {} (fn [r] (dissoc r :admin)) :replace)
;(insert- :user  {:name "考评员证书", :pid "110103198506020022", :role "pn", :uid "pn-110103198506020022", :admin "02"})
;(with-esp- (fetch :en-apply :where {:orgid ["4f8aebd175e0ae92833680f4" "4f8aebd175e0ae92833680ff"]}))
;(pager-options 201)