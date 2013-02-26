(ns ^{:doc "esp common utils for esp/espm/espreg/espfj"}
     wr3.clj.app.espc)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.nosql 'wr3.clj.u 'wr3.clj.n 'wr3.clj.tb)
(use 'wr3.clj.app.espconf)
(use 'somnium.congomongo 'hiccup.core)
(use 'clojure.contrib.json)
(require 'wr3.clj.db 'wr3.clj.app.espreg)

(defn where-admin
  [where admin]
  (if (= admin "01") where (into where {:admin admin})))

(defn apply-tb
  "根据字符串 'pn'/en/org/mot 得到表名 :pn-apply 等"
  [typ]
  (keyword (str typ "-apply")))
  
(defn user-info
  "获取用户在:user表中的所有信息"
  [request]
  (let [uid (wr3user request)]
    (with-mdb2 "esp" (fetch-one :user :where {:uid uid}))))
  
(defn user-admin
  "获取用户在:user表中的:admin代码，如'01' '14'；无用户或无:admin返回nil。"
  [request]
  (:admin (user-info request)))

(defn user-readonly?
  "如果用户在:user表中:readonly字段为“1”，返回true"
  [request]
  (= "1" (:readonly (user-info request))))
  
(defn- input-save-submit-
  "service: 共用函数，pn/org/en申请表提交保存或更新；todo：增加校验字段为空，@see espn/input-submit
  @id form名称如'pn' 'en' 'org' "
  [id request submit?]
  (let [vars (query-vars request)
        uid (wr3user request)
        tb1 (keyword id)
        tb2 (apply-tb id)
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

(defn with-pid-
  "得到 pn-apply/en-apply/org-apply/mot-apply 表中指定pid的多条记录"
  [tb pid]
  (with-esp- (fetch tb :where {:pid {:$in [(.toLowerCase pid) (.toUpperCase pid)]}} :sort {:date -1})))

(defn tb-count
  "得到tb的全记录数；或者tb中字段f符合匹配条件s的记录数。
  @tb 表名称如 :user 
  @m 定制化参数 {:f :age :s '张' :where ..}，其中： :s 匹配的字符串 '张' :f 字段如':name' 
  @usage: (tb-count :pn {:f :cid :s '2011' :where {:admin '14'}}) 
    (tb-count :pn) (tb-count :pn {:s '张'}
    (tb-count {:s '张' :where {:admin '14'}}) "
  ([tb] (with-mdb2 "esp" (fetch-count tb)))
  ([tb m] (let [where1 (if-let [s (:s m)] 
                         (let [f (or (:f m) (if (wr3.util.Charsetx/hasChinese s) :name :cid))] {f (re-pattern s)})
                         {})
                where2 (or (:where m) {})]
            (with-mdb2 "esp" (fetch-count tb :where (merge where1 where2))))))
  
(defn data-
  "取出数据表所有记录（100条以内）并持久化。
  @tb 如 :pn | :en | :org"
  ([tb] (with-esp- (fetch tb :limit 100)))
  ([tb m] 
    (let [limit (or (:limit m) 100)
          skip (or (:skip m) 0)
          where (or (:where m) {})]
      (with-esp- (fetch tb :skip skip :limit limit :where where)))))

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

(defn log-
  "在esp库的:log中日志记录消息m。
  @m 例如：{:msg '..' :url '..'} "
  [request m]
  (let [info (user-info request)
        {uid :uid role :role pid :pid} info
        no (session request "wr3no")]
    (insert- :log (into {:uid uid :role role :pid pid :no no :date (datetime) :date1 (date)} m))))

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
  "格式化'yes' 'no'的显示.  @yes-or-no 'yes' or 'no' "
  [yes-or-no]
  (format "<font color='%s'>%s</font>" (case yes-or-no "yes" "green" "no" "red" "gray") 
          (or (dd-resp yes-or-no) "尚未处理"))) 

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
            nn (map #(dd+ dd-type %) ss)]
        (join nn ", ")))))

(defn format-orgid1
  "通过uid字符串得到org名称， @m {:link true}"
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
          ids (if uid? ids (map #(object-id %) ids)) ; 注意：直接 (map object-id ids) 会报错，因为object-id是java调用的宏
          field (if uid? :uid :_id)
          rs (with-esp- (fetch tb :only [:name] :where {field {:$in ids}}))
          tb-name (name tb)]
      (if (:link m) 
        (map #(format "<a href='/c/esp/%s/%s' target='_blank'>%s</a>" 
                      (or (:form m) (str "docv/" tb-name)) (:_id %) (:name %)) rs)
        (map :name rs))) )
  ([tb ids] (format-ids- tb ids nil)))

(defn- format-usable-
  [s]
  (if (= s "0") "<font color=red>停用</font>" "可用"))
  
(defn- format-field-
  "在列表和文档中都涉及的字段值格式化（一般为查dd-xx ） "
  [k v]
  (case k
    :type (format-type v)
    :type2 (dd+ dd-type2 v) 
    :grade (dd+ dd-grade v)
    :fulltime (if v "专职" "<font color=gray>兼职</font>")
    :pnids (join (format-ids- :pn v {:link true :form "pn-doc" :uid true}) "、")
    :province (or (dd-admin-province v) v)
    :info (replace-all v "\r\n" "<br/>")
    (:resp :resp-eval :resp-review :resp-reg) [:b (format-resp- v)]
    :pass-direct (format-pass-direct v)
    :usable (format-usable- v)
    :ptype (dd+ dd-portal v)
    :renew (dd+ dd-renew v)
    v))

(defn- format-result-field-
  "把列表显示中某个字段的值进行格式化，如 :type字段的'1'->'道路运输'
  @row 一行数据如 {:type '1' ..}
  @col 字段名称如 :type :_id :pnids 
  @v0 字段原来的值如 '1' '4f8aeb2a75e0ae92833680e4' ['4f8aeb2a75e0ae92833680d9' '4f8aeb2a75e0ae92833680e2'] 
  @m 客户定制化的设置如 {:form 'docv/pn' :issue 'pn-apply' :type ..} "
  [row col v0 m]
  (let [v (.trim (str v0))]
    (case col
      :_id (if (:readonly? m) [:span {:title "权限受限，请联系主管机关管理员。"} "查看"] 
             [:a {:href (format "/c/esp/%s/%s" (:form m) v) :target "_blank"} "查看"])
      :_id-fj [:a {:href (format "/c/espfj/%s/%s" (:form m) (:_id row)) :target "_blank"} "查看"] ; 福建
      :_id-fj2 [:a {:onclick (format "espfj2_mark('%s')" (:_id row))} "标注"]
      :_select [:input {:type "checkbox" :group "select" :sid (:uid row)}]
      :_issue [:a {:href (case (:issue m)
                           "pn-apply" (format "/c/esp/cert-issue-pn/%s" (:_id row))
                           (format "/c/esp/cert-issue/%s/%s" (:issue m) (:_id row)))
                   :target "_blank"} "发证"]
      :_cdate-end (date-add (:cdate row) (dd-cert-year (:type m)) 0 0)
      :contract0 (format-date- v)
      :contract1 (if v0 (format-date- v0) "<b>目前在职</b>")
      :uid (if (:show-uid? m) v0 (wr3user-name v0))
      :admin-uid (wr3user-name v0)
      (:role :usertype) (or (get (:admin m) v) v) ; espfj 角色；注册用户角色；
      (:freport :refine-doc) (html [:a {:href v0 :target "_blank"} "下载"] (space 3)
                                   [:a {:href (when (not-nullity? v0) (str "/c/esp/doc-html?fname=" v0)) 
                                        :target "_blank"} "查看"])
      :admin (or (get (or (:admin m) dd-admin) v) v)
      :respdate (if (nil? v0) "<font color=gray>尚未处理</font>" v)
      :respdate-review (if (nil? v0) "<font color=gray>尚未处理</font>" v)
      :cstate (if (nil? v0) "正常" "撤销") ; 考评机构证书状态
      :reason (dd+ ({"org" dd-org-backup "en" dd-en-backup} (:id m)) v0)
      :content (if (> (count v) 20) (str (subs v 0 20) " ……") v)
      :stand (html "评分：" [:b (:sum v0)] "；" 
                   "等级：" (when-let [grd (:grade v0)] 
                           [:b (dd+ dd-grade grd)]) "；"
                   (when-let [report (:report v0)] [:a {:href report} "报告"])) ; 企业达标自评
      :orgid1 (format-orgid1 v {:link true})
      :belong (str v (when-let [n (wr3user-name v)] (format " (%s)" n)))
      (format-field- col v0))))

(defn result-html-
  "共用函数：对没有特殊要求的结果进行列表展示
  @rt Clojure.sql结果集 [{:c1 v :c2 v ..} ..]
  @head 表头名称 [余额 币种 日期]，如果为空如[] '() nil，则使用dd-meta自动从cols参数得到head；
    如果为map如{:name '姓名'}，则为覆盖dd-meta的定制字段名称；
    如果为非空列表如['姓名' '年龄' ..]，则直接用作表头。
  @cols 列名称 [:ye :bz :_created] 
  @m 客户化定制 {} 设置 :form 表示cols中含 :_id 时文档显示所使用的form "
  ([rt head cols m]
    (let [head (if (and (seq head) (sequential? head)) head
                 (let [dd (if (map? head) (merge dd-meta head) dd-meta)]
                   (for [c cols] (or (dd c) c))))]
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
    :belong (str v (when-let [n (wr3user-name v)] (format " (%s)" n)))
    :admin (or (get (or (:admin m) dd-admin) (str v)) v) ; espfj 可在m中指定 {:admin dd-admin-fj}
    (:uid :admin-uid) (if (:show-uid? m) v (wr3user-name v))
    :from (or (get (or (:from m) dd-province) v) v) ; espfj 可在m中指定 {:admin dd-province-fj}
    :orgid (if (:orgid-as-select m) 
             (eui-combo {:id "orgid" :name "orgid"} (zipmap v (format-ids- :user v {:uid true})))
             (join (format-ids- :user v {:link true :uid true}) "，"))
    :orgid1 [:a {:href (str "/c/esp/docv/org/" v) :target "_blank"} (format-orgid1 v {:link true})]
    :enid [:a {:href (str "/c/esp/docv/en/" v) :target "_blank"} "查看"]
    :content (replace-all v "\r\n" "<br/>")
    (:safe :photo :perf1 :perf2 :proof :proofb :proof2 :proof3 :titlefile :beginfile :refine-doc :met :pns 
           :license :report :qual) [:a {:href v} "查看"]
    :reason (dd+ ({:org-backup dd-org-backup :en-backup dd-en-backup} tb) v) ; 考评机构备案原因
    :otype (or (dd-form (keyword v)) v)
    :role (or (dd-role v) v) ; 登录认证U盘user表的角色
    :pwd "*"
    :fnmenu (join (map dd-menu2 (split v "&")) ", ")
    :fntype (format-type v)
    :fngrade (join (map (comp dd-grade to-int) (split v "&")) ", ")
    :readonly (or ({"1" "仅查阅" "0" "正常操作"} v) v)
    (format-field- k v) ))

(defn doc2-
  "显示指定表中指定object-id的记录内容。
  @tb 表名如 :pn :en :org 
  @id object-id字符串如 '4f8ad8ef75e0ae9283368075' 
  @m 定制化，参数 {:befor .. :after ..} 
    :befor 在前面的html内容或者带rt参数的fn，
    :after 在后面的html内容或者带rt参数的 (fn [rt] ..) 
    :rs 不使用从esp库的tb表查数据，给定数据。
    :app 非esp的应用如'espfj'
    :orgid-as-select 为true则orgid显示为下拉框
    :orders [:name :pid ..] 字段显示的顺序 
    :onload onload的js内容 "
  [tb id m]
  (let [rt (or (:rs m) (with-oid- tb id))
        rt (if-let [orders (:orders m)]
             (apply array-map (reduce into (concat (for [k orders :let [v (rt k)] :when v] [k v])
                                                   (for [[k v] rt :when (not (in? k orders))] [k v]))))
             rt)]
    (html
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

(defn doc-
  "@see doc2- 用html-body包裹doc2-，使之可以作为独立页面。 "
  ([tb id m] (html-body 
               {:js "app-esp.js" :onload (:onload m)}
               (doc2- tb id m)))
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
  "显示分页的部分html片段（每页最多100行）。
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
  @m 含:skip、:limit、:where等参数"
  [id tb col-names col-ids m]
  (let [nam (tb {:pn "考评员" :org "考评机构" :en "交通运输企业"})
        rt (search-auto- tb id m)
        where (or (:where m) {})
        count1 (if id (tb-count tb {:s id :where where}) (tb-count tb {:where where}))]
    (html
      [:div#table1
       [:h1 (format "%s 列表（%s 名）" nam (count rt))]
       (pager-html count1 (:skip m) (format "esp_pager('/c/esp/%s-list/%s')" (name tb) (if id id "")))
       (eui-button 
         {:href (format "/c/esp/list-export/exp-%s列表.csv?content-type=text/csv&charset=GBK&tb=%s&s=%s" 
                        (dd-form tb) (name tb) (or id "")) :style "margin:10px"} 
         (format "文件导出（全部%s行）" count1))
       (result-html- rt col-names col-ids {:form (str "docv/" (name tb))})]
      [:br] )))

(defn result-page-
  "按分页形式展现列表
  @tb 表名如 :user :pn 
  @col-names 列中文名称如 ['名称' '机构']  {:org '机构'} 
  @col-ids 列id如 [:name :org] 
  @m 含:skip、:limit、:where :url 等参数. :skip 跳出条数；:limit 每页限制条件；:where 查询条件 :url 点击不同页码跳转的页面；
    其他条件，可以做result-html-的定制化参数 "
  [tb col-names col-ids m]
  (let [where (:where m)
        limit (or (:limit m) 100)
        skip (to-int (:skip m) 0)
        count1 (tb-count tb {:where where})
        rt (with-esp- (fetch tb :where where :limit limit :skip skip)) ]
    (html
      [:div#table1
       (pager-html count1 (:skip m) (format "esp_pager('%s')" (:url m)))
       (result-html- rt col-names col-ids (dissoc m :skip :limit :where :url)) ])))

(defn result-csv
  [rt col-names col-ids]
  (let [fcol (fn [r c] (let [v (c r)] 
                         (wr3.util.Csv/toCsv 
                           (case c
                             :type (dd+ dd-type v)
                             :type2 (dd+ dd-type2 v)
                             :grade (dd+ dd-grade v)
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

(def ukey-reapply-url-pn (str "http://219.141.223.141:8080/userregister/ShowReport.wx?PAGEID=registerfirst_pn"
                              "&report1_ACCESSMODE=update&from=login&txtpageid="))

(def ukey-reapply-url-en (str "http://219.141.223.141:8080/userregister/ShowReport.wx?PAGEID=registerfirst_en"
                              "&report1_ACCESSMODE=update&from=login&txtpageid="))

(defn apply-nav-
  "共用函数：各种证书申请导航页
  @typ :pn :en :org "
  [typ request]
  [request]
  (let [uid (wr3user request)
        pid (right uid "-")
        tb typ
        tb-apply (apply-tb (name tb))
        r0 (first (with-pid- tb-apply pid)) ; 初次申请记录
        r1 (first (with-uid- tb uid)) ; 保存的最后录入记录
        rs2 (with-esp- (fetch tb-apply :where {:uid uid :del {:$ne "1"}}))  ; 提交过的所有未标记删除记录
        cert-name (dd-cert typ)]
    (html
      [:h1 (format "%s申请" cert-name)]
      (eui-tip  (format "已通过报名申请%s并获取了登录认证U盘%s，可进行%s申请。" 
                        (if-let [oid (:_id r0)] (format "（<a href='/c/esp/docv/%s/%s' target='_blank'>查看</a>）"
                                  (name tb-apply) oid) "")
                        (if-let [oid (:_id r0)] 
                          (format "（<a href='%s%s' target='_blank'>再次申请</a>）"
                                  (case typ :pn ukey-reapply-url-pn ukey-reapply-url-en) pid) "")
                        cert-name))
      (eui-button {:href "#" :onclick (format "layout_load_center('/c/esp/%s-input')" (name typ)) :style "margin:10px"}  
                  (if r1 "进行证书申请" "进行初次申请"))      
      (when rs2
        (html [:h2 (format "已提交过%s次申请：" (count rs2))]
              (result-html- rs2 '[] [:date (case typ :en :type2 :type) :resp :respdate :_id] 
                            {:form (format "docv/%s-apply" (name typ))}) )) )))

(defn cfg-set-values-
  "cfg配置和已有记录合并：设置cfg的m中的:v缺省值
  @cfg0 缺省配置文件
  @r 结果hash-map {k1 v1 k2 v2 ..} "
  [cfg0 r]
  (for [[n id m] cfg0] ; n:字段名 id:字段id m:形如{:v "1" :t ..} 
    [n id (merge m (if (id r) {:v (id r)} {}))]))

(defn apply-input-
  "共用函数：pn-input, org-input, en-input的录入表单。
  把缺省value设为用户最后录入的数据记录中的值，以便减少录入量。
  @type :pn :en :org "
  [request typ]
  (let [uid (wr3user request)
        pid (right uid "-")
        ntype (name typ)
        cfg0 (dd-cfg-apply typ)
        nam (dd-cert typ)
        r (or (first (with-uid- typ uid)) (first (with-pid- (apply-tb ntype) pid))) ; 如：pn表或pn-apply表中已有记录
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
        {:js "app-esp.js"}
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

(def hot---------- nil)

(defn hot
  "app: 举报热线
  @todo: 举报对象为en、pn时可以搜索名称以便跟踪"
  []
  (let [sid "fhot"]
    (html-body
      {:js "app-esp.js"}
      [:h1 "实名举报"]
      (eui-tip "任何单位和个人对考评机构的考评行为，有权向主管机关进行实名举报，主管机关会及时受理、组织调查处理，并为举报人保密。") [:br][:br]
      [:form {:id "fm1" :action "/c/espc/hot-save" :method "post"}
       [:label [:b "举报人信息等："]] (eui-textarea {:name "info"} "姓 名：\n身份证号：\n联系方式：\n\n其 他：\n") [:br][:br]
       [:label [:b "要举报的对象："]] (eui-combo {:name "otype"} {:en "交通运输企业" :pn "考评员" :org "考评机构"}) [:br][:br]
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

(def pub---------- nil)
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
      (wr3role? request "en")  (update- :en-apply {:uid uid  :type2 id} (fn [r] {:score0 sum}))
      (wr3role? request "org") (update- :en-apply {:uid enid :type2 id} (fn [r] {:score1 sum}))
      :else nil )
    "已经保存"))
  
(defn cert-renew
  "service：考评员、考评机构、企业的证书换证
  @id 'pn' 'org' 'en' 转换为:pn :org :en "
  [id request]
  (let [tb (keyword id)
        tb-apply (apply-tb id)
        uid (wr3user request)
        rs (with-esp- (fetch tb-apply :where {:uid uid :cid {:$exists true}}))
        y (case tb :pn 5 :org 5 :en 3 1) ; 有效期年份
        cname (dd-cert tb)]
    (html
      [:h1 "换证申请"]
      (if (empty? rs)
        (eui-tip (format "目前还没有%s证书，对于已有证书系统会自动提醒换证（%s年到期前3个月）。" cname y))
        (html
          [:h2 (format "目前已有的 《%s》：" cname)]
          (eui-tip "请点击查看详情并进行换证申请")
          (result-html- rs [] [:cid :cdate :type :_cdate-end :_id] {:type tb :form (str "cert-renew-doc/" id)}) )))))

(defn cert-renew-doc
  "app: pn、en、org显示换证操作
  @ids ids[0]: 'pn','org','en'; ids[1]: object-id "
  [ids]
  (let [[id oid] ids
        tb (apply-tb id)]
  (doc- tb oid 
        {:after (html [:br](eui-tip "请注意填写标注与换证相关的内容！") [:br]
                      (eui-button {:href (format "/c/esp/cert-renew-input/%s" id)} "申请换证") )})))

(defn cert-renew-input
  "采用申请表单，但录入一些换证特定的字段"
  [id request]
  (html-body 
    {:js "app-esp.js"}
    (apply-input- request (keyword id))))

(defn- cert-renew-sql
  "@f fetch 或 fetch-count"
  [f id admin]
  (let [tb (apply-tb id)
        v (case id "pn" ["1" "2"] "en" ["3"] "org" ["4"]) ; @see dd-renew
        where (where-admin {:renew {:$in v}} admin) ]
    (with-mdb2 "esp" 
      (let [rs (f tb :where where)]
        (if (= f fetch) (vec rs) rs)))))

(defn cert-renew-data 
  [id admin]
  (cert-renew-sql fetch id admin))

(defn cert-renew-count 
  [id admin] 
  (cert-renew-sql fetch-count id admin))

(defn cert-renew-resp
  "service: 主管机关对考评员、考评机构、企业的换证申请受理
  @id pn或org或en "
  [id request]
  (let [admin (user-admin request)
        cname (dd-form (keyword id))
        rs (cert-renew-data id admin)
        ]
    (html
      [:h1 (format "%s换证申请受理" cname)]
      (result-html- rs [] [:name :date :type :renew :_id] 
                    {:form (format "mot-%s-apply" id)} ) )))

(defn cert-resp
  "service: 用户：org和mot；mot制发考评员、考评机构证书，org制发企业证书。
  @id 'pn','en','org' "
  [id request]
  (let [title (format "%s制发" (dd-cert (keyword id)))
        admin (user-admin request)]
    (html
      [:h1 title]
      (eui-tip (if (= id "pn") "系统可导出列表，由专业打印机进行打印制发。"
                 "系统可直接套打A3纸，也可生成电子证书。"))
      [:h2 "审核通过的申请：" ] 
      (eui-button {:href (format "/c/esp/cert-resp-export/%s/exp-证书列表.csv?content-type=text/csv&charset=GBK" id) 
                   :target "_blank" :style "margin:10px"} "导出电子表格")
      (case id
        "en" (let [rs (with-esp- (fetch :en-apply :where (where-admin {:resp-review "yes"} admin)))]
               (result-html- rs [] [:admin :name :resp :resp-eval :resp-review :respdate-review :_id :_issue] 
                             {:form "docv/en-apply" :issue "en-apply"}))
        "org" (let [rs (with-esp- (fetch :org-apply :where (where-admin {:resp "yes"} admin)))]
                (result-html- rs [] [:admin :name :resp :respdate :_id :_issue] 
                              {:form "docv/org-apply" :issue "org-apply"}))
        "pn" (let [rs (with-esp- (fetch :pn-apply :where (where-admin {:resp "yes"} admin)))]
               (result-html- rs {:name "姓名"} [:admin :name :resp :respdate :_id :_issue]
                             {:form "docv/pn-apply" :issue "pn-apply"}))
        nil) )))

(defn cert-resp-export
  [id request]
  (let [tb (apply-tb id)
        col-names (case id
                    "en" ["企业名称" "业务类型" "证书号" ]
                    ("org" "pn") ["名称" "业务类型" "证书号" ])
        col-ids (case id 
                  "en" [:name :type2 :cid]
                  ("org" "pn") [:name :type :cid])
        f (case id "en" :resp-review ("org" "pn") :resp)
        rs (with-esp- (fetch tb :where {f "yes"}))]
    (result-csv rs col-names col-ids) ))

(defn- has-digit4- [n] (some #(= \4 %) (str n)))
(defn- cert-sid-max 
  "得到保存在:cert表中某主管机关颁发过的最大证书号。
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
  "生成证书编号。en、org的达标证书和资质证书号，形如yyyy-ta-xxxxxx； pn的资格证，形如yyyy-c-ta-xxxxxx。
  其中： ta 表示2位主管机关代码('01' ~ '35')，c 表示5个业务类型代码('1' ~ '5')
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

(def comment-field ["备注" :comment {:t 'textarea :style "width:400px; height:60px"}])
  
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
             ["有效期开始日期" :date {:v (date)}] 
             comment-field
             ]] 
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
        cmt (:comment vars)
        [year type admin sid] (split cid "-")]
    (with-mdb2 "esp" (update! :cert {:sid "pn" :admin admin} {:$set {:max (to-int sid)}})) ; 更新max值
    (update- :pn-apply {:_id (object-id id)} (fn [r] {:cid cid :comment cmt :cdate date})) 
    (html-body
      [:h1 "已发证书：" [:font {:color "blue"} cid]][:br][:br]
      (eui-button-close) )))

(defn cert-issue
  "en, org制发证书，生成新证书号。
  @ids ids[0] typ 'en-apply' 'org-apply' ; ids[1] en-apply或者org-apply表中文档的object-id字符串 
  todo：已经打印过证书的，是否用原来的号，还是新生成一个号？ "
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
             ["名称" :name {:v (:name r) :style "width:400px"}]
             ["类型类别" :type {:v (dd+ type-dd type-value)}]
             ["达标/资质等级" "grade" {:v (dd+ dd-grade (:grade r))}]
             ["正本/副本选择" :copy {:t {"正本" "—— 打印正本 ——" "副本" "—— 打印副本 ——"}}]
             comment-field
             ] ]
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
        cmt (:comment vars)
        [year admin sid] (split cid "-")
        date-end (date-add date 3 0 0)
        copy (:copy vars)
        css1 "position:absolute;left:%d;top:%d;font-size:19pt"
        css2 "position:absolute;left:%d;top:%d;font-family:黑体;font-size:21pt;width:450px"
        css3 "position:absolute;left:%d;top:%d;font-size:20pt;width:230px"
        css4 "position:absolute;left:%d;top:%d;font-size:51pt;color:#9fa0a0;width:300px"
        f (fn [css x y] {:style (format css x y)}) ]
    (with-mdb2 "esp" (update! :cert {:sid en-org :admin admin} {:$set {:max (to-int sid)}})) ; 更新max值
    (update- (keyword apply-type) {:_id (object-id id)} (fn [r] {:cid cid :comment cmt :cdate date})) 
    (html
      [:body {:style "margin:0px; font-family: 宋体; font-size:21pt"}
       [:div {:style (format "background:url('/img/esp/cert-%s.jpg');border:1px solid red;width:1654px;height:1169px"
                             (left apply-type "-"))} ]
       [:div (f css1 250  785) (:cid vars)]
       [:div (f css1 250  832) (format-date-cert date) " 至 " (format-date-cert date-end)]
       [:div (f css2 1080 285) (:name vars)]
       [:div (f css2 1080 407) (:type vars)]
       [:div (f css2 1080 530) (:grade vars)]
       [:div (f css3 1262 926) (format-date-cert date)] 
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
 

(defn- apply-reg-count-
  "@typ 类型 en、pn、org、mot
  @uid? 是否有:uid字段, false没有:uid字段的是U盘报名申请，true有:uid字段的是资质/资格证书申请。
  @admin admin是那个主管机关的查询 
  @m 其他where条件
  @return [未处理的记录数 全部的记录数]"
  [typ uid? admin m]
  (let [tb (apply-tb typ)
        where-uid (if uid? {:$ne nil} nil)
        where (merge m {:uid where-uid :del {:$ne "1"}})
        where (where-admin where admin)
        where2 (into where {(if uid? :resp :resp-reg) {:$nin ["yes" "no"]}})] 
    [(with-mdb2 "esp" (fetch-count tb :where where2)) (with-mdb2 "esp" (fetch-count tb :where where))] ))

(defn count-apply
  "资质/资格证书申请条目数量统计。
  @t 类型 en、pn、org、mot"
  ([t admin m] (apply-reg-count- t true admin m))
  ([t admin] (apply-reg-count- t true admin {})))

(defn count-reg
  "报名申请条目数量统计。
  @t 类型 en、pn、org、mot"
  ([t admin m] (apply-reg-count- t false admin m))
  ([t admin] (apply-reg-count- t false admin {})))

(defn- apply-reg-th-
  "初次报名申请，或资格证书申请的列表显示列头
  @t pn/en/org/mot "
  [t]
  {:name (case t "pn" "姓名" "名称") })

(defn- apply-cert-th-
  "资格/资质证书申请的列表显示列头
  @t pn/en/org/mot "
  [t]
  {:name (case t "pn" "姓名" "名称") 
   :resp (case t "en" "受理结果" "审核结果") ; en的申请由org受理，mot审核；其他的申请又mot直接审核
   :respdate (case t "en" "受理日期" "审核日期")
   })

(defn- apply-reg-fs-
  "初次报名申请的列表显示字段
  @t pn/en/org/mot "
  [t]
  (case t
    "pn" [:admin :name :pid :org :title :type :date :resp-reg :_id]
    "mot" [:admin :name :date :resp-reg :_id]
    "en" [:admin :name :type2 :grade :date :resp-reg :_id]
    [:admin :name :type :grade :date :resp-reg :_id] ))
  
(defn- apply-cert-fs-
  "资格/资质证书申请的列表显示字段
  @t pn/en/org/mot "
  [t]
  (case t
    "pn" [:admin :name :pid :org :title :type :date :respdate :_id]
    "mot" [:admin :name :date :resp :respdate-review :_id]
    "en" [:admin :name :type2 :grade :date :resp :respdate-review :_id]
    [:admin :name :type :grade :date :resp :respdate :_id] ))
  
(defn reg-list
  "service: 报名初审列表
  @id pn/en/org/mot 
  @skip 分页skip值 "
  [id skip ftype fvalue request]
  (let [admin (user-admin request)
        skip (or skip 0)
        ftype (or ftype "")
        fvalue (or fvalue "")
        uri "skip='+$('#pagers').val()+'&ftype='+$('#ftype').val()+'&fvalue='+$('#fvalue').val()"
        list-js (format "ajax_load($('#list'), '/c/esp/reg-list/%s?%s)" id uri)
        tb (apply-tb id)
        where {:uid nil :del {:$ne "1"}}
        where (where-admin where admin)
        where (if (nullity? ftype) where 
                (into where {(keyword ftype) (case ftype
                                               ("name" "pid") (re-pattern fvalue)
                                               "resp-reg" (case fvalue ("yes" "no") fvalue {:$nin ["yes" "no"]})
                                               fvalue)}))
        count1 (with-mdb2 "esp" (fetch-count tb :where where))
        rs (with-esp- (fetch tb :skip (to-int skip) :limit 100 :where where :sort {:date -1})) ]
    (html
      [:h2 (format "%s报名申请列表" (dd-role id))]
      (pager-html count1 skip list-js)
      (result-html- rs (apply-reg-th- id) (apply-reg-fs- id) 
                    {:form (str "reg-resp-doc/" id)
                     :readonly? (user-readonly? request)})
      (when (> count1 100) (eui-tip "有多页，请点击下拉框翻页。")))))

(defn reg-resp
  "报名初审。对无登录认证U盘用户（无:uid）的报名信息初审 "
  [request]
  (let [admin (user-admin request)]
    (html
      [:h1 "进行报名申请初审"]
      (eui-tip "对考评员、考评机构、企业、主管机关用户未申请登录认证U盘前填写的初步报名申请信息进行审批。")
      (for [[k v] dd-role :let [[c0 c1] (count-reg k admin)]] 
        (eui-button {:onclick (format (str "ajax_load($('#filter'), '/c/esp/reg-filter-type/%s'); "
                                           "ajax_load($('#list'), '/c/esp/reg-list/%s'); ") k k)
                     :style "margin:5px" :title (format "有%s个尚未处理/共%s个报名申请" c0 c1)}
                    (format "%s <font color=red>%s</font>/%s" v c0 c1))) (space 5)
      (when (= "01" admin) (eui-button {:href "/c/esp/reg-olap" :target "_blank" :iconCls "icon-bar"} "各省厅报名数统计"))
      [:div#filter]
      [:div#list])))

(defn reg-resp-doc
  "处理一个报名申请
  @id object-id 字符串 "
  [ids request]
  (let [[typ id] ids ; typ: pn/en/org/mot id: object-id
        tb (apply-tb typ)
        rs (with-oid- tb id)
        deleted (or (:del rs) "0") ; '1': 此申请已经标记为删除, '0': 正常
        js (format "ajax_post('/c/esp/apply-resp-del/%s/%s/%s', function(){window.location.reload()} )" typ id deleted)
        js2 (format (str "ajax_post('/c/esp/apply-resp-change-admin/%s/%s?admin='+$('#admin').val(), " 
                         "function(){window.location.reload()} )") typ id)]
    (doc- tb id 
          {:rs rs :admin dd-admin :from dd-province
           :before (html [:h1 {:align "center"} (dd-role typ) "报名申请初审"]
                         (if (= deleted "1")
                           (eui-button {:onclick js :iconCls "icon-undo" :style "margin:10px"} "恢复此记录")
                           (eui-button {:onclick js :iconCls "icon-cancel" :style "margin:10px"} "删除此记录") )
                         (space 5)
                         (eui-combo {:id "admin" :value (:admin rs)} dd-admin) 
                         (eui-button {:onclick js2 :title "请关闭，交由调整后的主管机关处理该报名申请。"} "调整主管机关") )
           :after (html 
                    [:hr]
                    [:form#fm1
                     [:p [:label "初审意见："] (eui-textarea {:name "advice-reg"} (:advice-reg rs))]
                     [:p [:label "初审结果："] (eui-combo {:name "resp-reg" :value (:resp-reg rs)} dd-resp)]
                     [:p (eui-button {:onclick (format "ajax_form($('#fm1'), '/c/esp/reg-resp-submit/%s/%s')" typ id) 
                                      :iconCls "icon-ok"} "提 交")]] )
           :orders (map second (dd-cfg-apply (keyword typ)))
           }) ))

(defn reg-resp-submit
  "service: 保存报名申请初审结果，并传送给rdb"
  [ids request]
  (let [[typ id] ids ; typ：'pn'/'en'/'org'/'mot', id：object-id
        tb (apply-tb typ)
        {resp-reg :resp-reg advice-reg :advice-reg} (query-vars2 request)
        r (with-oid- tb id)
        fs {"TradeGuid" (str (System/currentTimeMillis))
            "PaperType" (case typ "pn" "SF" "JJ")
            "Usertype" typ
            "CommonName" (:name r)
            "PaperID" (.toUpperCase (:pid r)) ; rdb中需要证件号中的x都大写
            "Email" (:email r)
            "Province" nil
            "City" nil
            "PostalCode" (:pcode r)
            "TelephoneNumber" (case typ "pn" (:mobile r) (:tel r))
            "PostalAddress" (:address r)}
        fs (case typ "pn" fs
             (into fs {"LeagalPerson" (:legalp r)
                       "TransName" (:contact r)
                       "TransMobile" (:mobile r)
                       "TransPaperID" nil}))
        sql (format "insert into userregister (%s) values(%s) " 
                    (join (keys fs) ",") (join (vals fs) "," {:quo "'"}))]
    ; 写操作日志
    (log- request {:type "resp-reg" :oid id 
                   :msg (format "审批%s报名申请: %s, %s" (dd-role typ) (:name r) (format-resp- resp-reg) )})
    (update- tb {:_id (object-id id)} {:resp-reg resp-reg :advice-reg advice-reg})
    (when (= resp-reg "yes") (wr3.clj.db/update wr3.clj.app.espreg/db-reg sql))
    "已保存"))

(defn apply-resp-del
  "service: 把该报名申请删除到垃圾箱/或从垃圾箱中恢复; 
  通用：报名申请和证书申请都可以用
  @ids typ: 'pn'/'org'/'en'; id: object-id; deleted: '0'/'1' "
  [ids]
  (let [[typ id deleted] ids
        tb (apply-tb typ)]
    (when-not (nullity? id)
      (with-mdb2 "esp" (update! tb {:_id (object-id id)}
                                {:$set {:del (if (= deleted "1") "0" "1")}}))
      (if (= deleted "0") 
        "已删除申请记录，关闭后请刷新列表。"
        "已恢复申请记录，关闭后请刷新列表。"))))

;(println (with-esp- (fetch :en-apply :where {:orgid {:$in ["org1"]}})))

(defn apply-resp
  "service: 主管机关对考评员、考评机构、企业的申请受理. 
  @id 代表申请来源的字符串：pn,org,en 无U盘报名申请还包括mot "
  [id request]
  (let [tb (apply-tb id) ; :pn-apply :org-apply :en-apply
        uid (wr3user request) 
        cname (dd-cert (keyword id))
        where {:uid {:$ne nil} :del {:$ne "1"}}
        admin (user-admin request)
        where (where-admin where admin)
        mot-en? (and (wr3role? request "mot") (= id "en"))
        org-en? (and (wr3role? request "org") (= id "en"))
        where (if org-en? (into where {:orgid {:$in [uid]}}) where) ; 显示选中当前org来进行考评的企业
        rs (with-esp- (fetch tb :where where :sort {:date -1}))
        form (cond 
               (= id "pn") "mot-pn-apply"
               (= id "org") "mot-org-apply" 
               mot-en? "mot-en-apply" ; mot对en的受理
               org-en? "org-en-apply" )  ; org对en的受理
        tip (case id 
              "pn" (str "同意则颁发资格证书；<br/>注：直接从事交通运输安全生产行政管理工作10年以上，"
                        "熟悉掌握交通运输安全生产相关法规和企业安全生产标准化规定者，身体健康，经本人申请、所在单位推荐、"
                        "发证主管机关核准，可直接办理考评员资格证。")
              "org" "主管机构处理：（同意/不同意）+意见"
              "en" "企业申请处理：主管机关（同意+指派考评机构）/（不同意+意见）——> 考评机构（同意）/（不同意+意见）——> 主管机关审核")]
    (html
      [:h1 (format "受理%s申请" cname)]
      (eui-tip tip)
      (result-html- rs (apply-cert-th- id) (apply-cert-fs- id)
                    {:form form
                     :readonly? (user-readonly? request)}) [:br] )))

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
      (result-html- rs [(str (dd-form (keyword id)) "名称") "原因" "申请日期" "受理日期" "详情"]
                    [:uid :reason :date :respdate :_id] 
                    {:form (format "backup-resp-doc/%s" id) :id id}) [:br] )))

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
  
(defn cert-cancel
  "service: 主管机关对pn、org、en证书的撤销
  @id 'pn' 'org' 'en' "
  [id]
  (let [tb (keyword id)]
    (html
      [:h1 (format "%s撤销" (dd-cert tb))]
      [:form {:id "fm1" :action (format "/c/esp/cert-cancel-doc/%s" id) :method "get" :target "_blank"}
       (name-cid-search-input tb) [:br][:br]
       (eui-button {:onclick "$('#fm1').submit()"} " 查 询 ") ] )))

(defn cert-cancel-doc
  "service: 查询指定证书号，显示信息并撤销
  @id 'pn' 'org' 'en', 
  @in 名称及证书号
  @cid cid证书号
  @type :pn :org "
  [id in]
  (let [tb (keyword id)
        cid (right in "证书号:") 
        r (first (with-esp- (fetch tb :where {:cid cid})))
        id (str (:_id r))]
    (if r
      (doc- tb id 
            {:after (html
                      [:br] (eui-button {:onclick "alert('Todo')"} "撤销（并填写意见）") (space 3) (eui-textarea {} ) [:br][:br] )})
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

(defn yes-no-advice-
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

(defn refine
  "mot 下发整改意见给org或者en "
  [type]
  (let [tb type]
    (html
      [:h1 (format "选择%s下发整改通知" (dd-form type))]
      [:form {:id "fm1" :action (format "/c/esp/refine-doc/%s" (name type)) :method "get" :target "_blank"}
       (name-cid-search-input tb) [:br][:br]
       (eui-button {:onclick "$('#fm1').submit()"} " 查 询 ") ] )))
  
(defn refine-doc
  "service: 查询指定名称，显示信息并整改
  @id 'org' 'en', 
  @in 名称及证书号
  @cid cid证书号
  @type :pn :org "
  [id in]
  (let [tb (keyword id)
        [nam cid] (split in ", 证书号:")
        r (first (with-esp- (fetch tb :where {:name nam})))
        oid (str (:_id r))]
    (if r
      (doc- tb oid 
            {:after (html [:br] 
                          [:form#fm1 {:action "/c/esp/refine-save" :method "POST"}
                           [:input {:name "uid" :type "hidden" :value (:uid r)}]
                           [:label "整改意见："](eui-textarea {:name "advice-refine" :style "width:500px"} "整改……") [:br][:br] 
                           (eui-button {:onclick "$('#fm1').submit()"} "下发整改意见") [:br][:br]] )})
      (html-body (eui-tip (format "未找到该%s！" (dd-form tb))) [:br] (eui-button-close)) )))

(defn refine-save
  [request]
  (let [admin-uid (wr3user request)
        vars (query-vars2 request)
        m (merge vars {:admin-uid admin-uid :date (datetime)})]
    (do (insert- :refine m)
      (html-body "整改通知已经下发。" [:br][:br] (eui-button-close)))))
      
(defn refine-resp
  "org或en处理mot下发的整改意见和整改通知。"
  [request]
  (let [uid (wr3user request)
        rs (with-esp- (fetch :refine :where {:uid uid} :sort {:date -1}))]
    (html 
      [:h1 "整改意见及报告"]
      (result-html- rs [] [:date :uid :advice-refine :refine-doc :respdate-refine :_id] 
                    {:form "refine-resp-doc"}))))

(defn refine-resp-doc
  "app: org或en显示整改通知，并提交整改报告。"
  [id]
  (doc- :refine id 
        {:after (html [:br] 
                      [:form#fm1 {:action (str "/c/esp/refine-resp-save/" id) :method "POST"}
                       [:label "整改报告："] (fileupload-field "整改报告" "refine-doc" "" {}) [:br][:br] 
                       (eui-button {:onclick "$('#fm1').submit()"} "提交整改报告") [:br][:br]] 
                      (fileupload-dialog))}))

(defn refine-resp-save
  "保存整改报告附件"
  [id request]
  (let [vars (query-vars2 request)
        r (with-oid- :refine id)
        m (merge vars {:respdate-refine (datetime)})]
    (do
      (update- :refine {:_id (object-id id)} m)
      (html-body [:h2 "已上传整改报告。"] (eui-button-close)))))

(defn doc-save
  "app：通用ajax更新表字段（字段名及值从request中取）
  @ids 第一个参数为表名，第二个参数为object-id "
  [ids request]
  (let [[tb oid] (take 2 ids)
        vars (query-vars2 request)]
    (if (and tb oid)
      (do (update- (keyword tb) {:_id (object-id oid)} vars) "完成更新")
      "没指定表名或oid")))

(import wr3.util.Filex)
(import wr3.util.Word)
(defn doc-html
  "测试：把word文档保存成html"
  [fname request]
  (let [fpath1 (.getRealPath request fname)
        fname2 (str (leftback fname ".") ".html")
        fpath2 (.getRealPath request fname2)
        has-file? (Filex/has fpath2)]
  (html-body
    (if has-file? 
      [:h1 "文档已存在"]
      (do (Word/toHtml fpath1 fpath2)
        [:h1 "文档生成"] ))
    [:a {:href fname2} "点击查看"])))  
