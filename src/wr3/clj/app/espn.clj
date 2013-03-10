(ns ^{:doc "pn、en/org/mot无登录认证U盘注册报名信息" }
     wr3.clj.app.espn)

(use 'wr3.clj.s 'wr3.clj.u 'wr3.clj.web 'wr3.clj.nosql)
(use 'hiccup.core 'somnium.congomongo 'clojure.contrib.json)

(require '[wr3.clj.app.espc :as espc]) 
(require '[wr3.clj.app.espconf :as conf]) 

(defn body-head
  [apply-type]
  [:h1 {:style "padding:20px; background-color:#369; color: white; font-size: 22px; margin-top:0px"} 
   (format "%s报名申请" (conf/dd-role apply-type))] )
  
(def body-tail [:h2 {:style "padding:15px; background-color:lightgray; margin-bottom:0px; color:#333; text-shadow:0 1px 0 #EEE;"}
                "版权所有：交通运输部 2012 年"])
  
(defn index
  "app: 直接填写或者用身份证（密码：后4位）登录修改原来提交的内容"
  [request]
  (let [wr3url (session request "wr3url")
        apply-type (check (right wr3url "/index/") "pn") ; 根据之前session中的wr3url判断是何种申请
        [pname pid] (case apply-type "pn" ["考评员姓名" "身份证号码"] ["联系人员姓名" "组织机构代码"])]
    (html-body
      {:js "app-espfj.js"}
      [:center {:style "border: 1px solid #369"}
       (body-head apply-type)
       [:div {:style "min-height: 500px"}
        [:h2 "初次报名，请直接点击填写报名信息："][:br]
        (eui-button {:href (str "/c/espn/input/" apply-type)} "填写报名信息") [:br][:br][:br][:hr][:br]
        [:h2 "已经填写提交过报名信息，希望进一步修改提交，请输入如下信息进行登录："][:br]
        [:form#fm1
         [:input {:name "ptype" :type "hidden" :value apply-type}]
         [:label pname "："] (eui-text {:name "pname" :style "width:200px"}) [:br]
         [:label pid "："] (eui-text {:id "pid" :name "pid" :style "width:200px"}) [:br][:br]
         (eui-button {:onclick (format "espfj_input_login($('#fm1'), 'espn', '%s')" apply-type)} "登 录")]]
       body-tail ])))
  
(defn- with-pid-
  "得到 pn-apply/en-apply/org-apply/mot-apply 表中指定pid的一条记录"
  [ptype pid]
  (let [tb (keyword (str (or ptype "pn") "-apply"))]
    (with-mdb2 "esp" 
      (first (fetch tb :where {:pid {:$in [(.toLowerCase pid) (.toUpperCase pid)]}} :sort {:date -1})))))

(defn input-login-check
  "service: 校验考评员/联系人姓名和证件号是否符合"
  [ptype pname pid]
  (let [f (case ptype "pn" :name :contact)]
    (json-str 
      (cond (or (nullity? pname) (nullity? pid)) false
            :else (let [r (with-pid- ptype pid)] (= pname (f r))) ))))

; 初审通过后pn的ukey申请url连接，后跟pid
(def ukey-apply-url-pn (str "http://219.141.223.141:8080/userregister/ShowReport.wx?PAGEID=registerfirst_pn"
                            "&report1_ACCESSMODE=update&from=register&txtpageid="))
; 初审通过后en/org/mot的ukey申请url连接，后跟pid
(def ukey-apply-url-en (str "http://219.141.223.141:8080/userregister/ShowReport.wx?PAGEID=registerfirst_en"
                            "&report1_ACCESSMODE=update&from=register&txtpageid="))
(println 
  (let [cfg2 conf/cfg-apply-en
        n (count cfg2)
        n2 (- n 5)]
    (concat (subvec cfg2 0 n2) (for [[v1 v2 m] (subvec cfg2 n2)] [v1 v2 (into m {:require false})]) )) 
  )
  
(defn input
  "app: 填写考评员注册信息
  @ids 第一个参数为申请类型pn/en/org/mot，第二个参数为身份证号或组织机构代码pid（可选）" 
  [ids request]
  (let [[ptype pid] ids
        r (when pid (with-pid- ptype pid))
        cfg ({"pn" conf/cfg-apply-pn 
              "en" (let [c2 conf/cfg-apply-en n (count c2) n2 (- n 5)] 
                     (concat (subvec c2 0 n2) (for [[v1 v2 m] (subvec c2 n2 (- n 2))] [v1 v2 (into m {:require false})]) )) 
              "org" conf/cfg-apply-org 
              "mot" conf/cfg-apply-mot} ptype)
        cfg (if (not pid) cfg (espc/cfg-set-values- cfg r))
        title (str (conf/dd-role ptype) "报名申请")
        ukey-apply-url (case ptype "pn" ukey-apply-url-pn ukey-apply-url-en)
        tip (case (:resp-reg r) 
              "yes" (format "已经通过初审 %s，请进行 <b><a href='%s%s'>登录认证U盘申请</a></b>。" 
                            (apply str (repeat 5 "<img src='/css/easyui/icons/ok.png'/> ")) ukey-apply-url pid)
              "no" (format "没有通过初审 %s，请核实所填信息或咨询主管机关。<font color=red>附审核意见：【%s】</font> " 
                        (apply str (repeat 5 "<img src='/css/easyui/icons/cancel.png'/> ")) (:advice-reg r))
              "请认真填写如下所有信息，并等待主管机关进行报名申请初审。")]
    (html-body
      {:js "app-espfj.js" 
       :onload (format "espfj_onload(%s)" (if r (str "[" (join (split (:type r) "&") ",") "]") "[]"))}
      [:center {:style "border: 1px solid #369"}
       [:h1 {:style "padding:20px; background-color:#369; color: white; font-size: 22px; margin-top:0px"} 
        "交通运输部——" title]
       [:div {:align "left"} 
        (eui-tip tip)]
       [:div {:align "center" :style "border:0px solid red"}
        [:input#typeText {:type "hidden" :value ""}] ; 用于保存业务类型选择最多两种的值
        (input-form 
          cfg
          {:title title
           :buttons (html
                      (eui-button {:onclick (format "espfj_input_submit('%s', 'espn')" ptype)} "提交申请") (space 5)
                      (eui-button {:onclick "$('form').get(0).reset()"} " 重 填 ")) })]
       [:h2 {:style "padding:15px; background-color:lightgray; margin-bottom:0px; color:#333; text-shadow:0 1px 0 #EEE;"}
        "版权所有：交通运输部 2012 年"] ]
      (fileupload-dialog)) ))

(defn- input-submit-check-
  "进行姓名是否为空，证件号是否合法等校验
  @ptype 'en'/'pn'/'org'/'mot' 
  @m 字段 "
  [ptype m]
  (let [c1 (or (not-nullity? (:name m)) "姓名/名称：不能为空")
        c2 (or (let [pid (:pid m)] 
                 (case ptype "pn" (wr3.bank.IDUtil/is18 pid)
                   (and pid (wr3.bank.OrgID/isid (.toUpperCase (trim pid))))))
               "证件号码：格式不符合规则")
        c3 (or (not= "00" (:admin m)) "主管机关：不能为空")
        c4 (or (not= "00" (:province m)) "所在省市：不能为空")
        [c5 c6 c7 c8 c9 c10] (for [k [:photo :proof :proofb :proof2 :mobile :type]] 
                               (or (not= ptype "pn") (not-nullity? (k m)) 
                                   (str ((into conf/dd-meta {:type "申请专业类型"}) k) "：不能为空")))
        cs [c1 c2 c3 c4 c5 c6 c7 c8 c9 c10] ]
    {:rt (apply all-true? cs) :msg (join (remove true? cs) "\n")}))
  
(defn input-submit
  "service: 初次报名申请表提交保存或更新. @todo 已经审批通过的update时带上 :resp-reg :advice-reg
  @id form名称如'pn' 'en' 'org' "
  [id request]
  (let [tb (keyword (str (or id "pn") "-apply"))
        vars (query-vars2 request)
        m (into {} (for [[k v] vars :when (not (nullity? v))] [k v])) ; 只保存非空的字段
        {pid :pid nam :name contact :contact} vars
        check-rt (input-submit-check- id m)]
    (if (:rt check-rt)
      (do (with-mdb2 "esp" (update! tb {:pid pid} (into m {:date (datetime)})))
        (json-str {:rt true 
                   :msg (format (str "已提交 %s 的申请表。\n\n"
                                     "请等待主管机关进行初审，可使用 %s 和 %s 登录查阅进度或修改。\n\n"
                                     "初审通过后可进行登录认证U盘申请。")
                                (vars :name)
                                (case id "pn" (str "姓名：" nam) (str "联系人姓名：" contact))
                                (case id "pn" (str "身份证号码：" pid) (str "组织机构代码：" pid)) )}))
      (json-str check-rt))))


