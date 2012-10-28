(ns ^{:doc "为福建交通厅定制的考评员注册及审批"}
     wr3.clj.app.espfj)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.web 'wr3.clj.tb)
(use 'somnium.congomongo 'wr3.clj.nosql 'somnium.congomongo 'hiccup.core 'clojure.contrib.json)

(require '[wr3.clj.app.espc :as espc]) 
(require '[wr3.clj.app.espconf :as conf]) 

(def dd-province-fj
  (array-map
    "1"  "福州市"  
    "2"  "厦门市" 
    "3"  "宁德市" 
    "4"  "莆田市" 
    "5"  "泉州市" 
    "6"  "漳州市" 
    "7"  "龙岩市"
    "8"  "三明市" 
    "9"  "南平市" 
    "10" "平潭综合实验区" 
    ))

; 福建交通厅主管部门
(def dd-admin-fj
  (array-map
    "1401" "福建省交通运输厅"
    "1402" "福州市交通运输委"
    "1403" "厦门市交通运输局"
    "1404" "漳州市交通局"
    "1405" "泉州市交通运输委"
    "1406" "三明市交通运输局"
    "1407" "莆田市交通运输局"
    "1408" "南平市交通运输局"
    "1409" "龙岩市交通局"
    "1410" "宁德市交通运输局"
    "1411" "平潭综合实验区交建局"
    "1412" "漳州招商局经济技术开发区交通局"
    "1413" "福州港口局"
    "1414" "厦门港口局"
    "1415" "湄洲湾港口局"
    ))

;; 用espconf中的cfg-apply-pn配置，去掉换证部分，把主管机关换成福建省的。
(def cfg-pn
  (let [n (count conf/cfg-apply-pn)
        rt1 (take (- n 4) conf/cfg-apply-pn)
        rt2 (assoc (vec rt1) 2 ["常住地" :from {:t dd-province-fj :v "1" :require true}])
        rt3 (row-add rt2 6 ["职称证明文件" :titlefile {:t 'file}])
        rt4 (row-add rt3 17 ["相关专业从业年份证明文件" :beginfile {:t 'file}])
        rt5 (row-add rt4 17 ["证明人联系电话" :proofmobile {:require true}])
        rt6 (row-add rt5 17 ["证明人" :proofname {:require true}])
        rt7 (row-add rt6 17 ["证明单位" :prooforg {:require true}])
        ]
    (concat rt7 [["主管机关" :admin {:t dd-admin-fj :title "请自选主管机关" :require true}]])))

;--------------------------------------------------------------- 登录安全
(def auth-login-url "/c/espfj/login") ; 本ns的登录页面 /login.html

(defn auth
  "该 CljServlet 调用，用于本应用各函数的权限控制 "
  [request fname ids & args]
  (let [uid (wr3user request) 
        role (wr3role request)
        id (first ids)]
;    (println (format "uid=%s, role=%s, fname=%s" uid role fname))
    (cond
      (in? fname ["index" "index-close" "input" "input-submit" "input-login-check" "login" "login-submit" "logout"]) true
      (and (= fname "users") (not= role "1400") (not= role "1401")) false
      (= role "1400") true
      (in? role (keys dd-admin-fj)) true
      :else false)))

(defn index-close
  "2012-7-31 8:00PM，2012-8-23 8：00PM后关闭"
  []
  (html-body
    [:center {:style "border: 1px solid #369"}
     [:h1 {:style "padding:20px; background-color:#369; color: white; font-size: 22px; margin-top:0px"} 
      "福建省交通运输厅——考评员在线报名系统"]
     [:div {:style "min-height:200px"}
      [:h1 {:style "color:red;margin:50px"} 
       "请注意：2012年8月23日晚8点我们截至此次的报名。"[:br][:br]
       "如有疑问，请咨询福建省交通运输厅相关人员。"]]
     [:h2 {:style "padding:15px; background-color:lightgray; margin-bottom:0px; color:#333; text-shadow:0 1px 0 #EEE;"}
        "版权所有：福建省交通运输厅 2012 年"] ]))

(def body-head [:h1 {:style "padding:20px; background-color:#369; color: white; font-size: 22px; margin-top:0px"} 
           "福建省交通运输厅——考评员在线报名系统"] )

(def body-tail [:h2 {:style "padding:15px; background-color:lightgray; margin-bottom:0px; color:#333; text-shadow:0 1px 0 #EEE;"}
                "版权所有：福建省交通运输厅 2012 年"])
  
(defn index
  "app: 直接填写或者用身份证（密码：后4位）登录修改原来提交的内容"
  []
  (html-body
    [:center {:style "border: 1px solid #369"}
     body-head
     [:div {:style "min-height: 500px"}
      [:h2 "初次报名，请直接点击填写个人报名信息："][:br]
      (eui-button {:href "/c/espfj/input"} "填写个人报名信息") [:br][:br][:br][:hr][:br]
      [:h2 "已经填写提交过报名材料，希望进一步修改提交，请输入用户ID和密码登录："][:br]
      [:form#fm1
       [:label "用户代码："] (eui-text {:name "pid" :id "pid"}) [:br]
       [:label "用户密码："] (eui-text {:name "pass"}) [:br][:br]
       (eui-button {:onclick "espfj_input_login($('#fm1'))"} "登 录")]]
     body-tail ]))

(defn- with-pid-
  [pid]
  (with-mdb2 "espfj" (first (fetch :pn-apply :where {:pid {:$in [(.toLowerCase pid) (.toUpperCase pid)]}} :sort {:date -1}))))

(defn input-login-check
  "service: 校验用户名和密码是否符合"
  [pid pass]
  (json-str 
    (cond 
      (or (not= 18 (count pid)) (not= 4 (count pass))) false
      (not (.equalsIgnoreCase pass (subs pid 14 18))) false
      (empty? (with-pid- pid)) false
      :else true )))

(defn input
  "app: 填写考评员注册信息" 
  [id request]
  (let [wr3user (session! request "wr3user" "fj")
        wr3role (session! request "wr3role" "pn")
        rs (when id (with-pid- id))
        cfg-pn (if (not id) cfg-pn
                 (espc/cfg-set-values- cfg-pn rs))]
    (html-body
      {:js "app-espfj.js" 
       :onload (format "espfj_onload(%s)" (if rs (str "[" (join (split (:type rs) "&") ",") "]") "[]"))}
      [:center {:style "border: 1px solid #369"}
       [:h1 {:style "padding:20px; background-color:#369; color: white; font-size: 22px; margin-top:0px"} 
        "福建省交通运输厅——考评员在线报名系统"]
       [:div {:align "left"} 
        (eui-tip "请认真填写除如下可选字段外的所有信息：职称、职称证明文件、联系电话、传真号码、相关专业从业年份证明文件、
                  专业工作业绩附件（可选）、相关证明文件（其他证书）")]
       [:div {:align "center" :style "border:0px solid red"}
        [:input#typeText {:type "hidden" :value ""}] 
        (input-form 
          cfg-pn
          {:title "考评员资格申请表"
           :buttons (html
                      (eui-button {:onclick (format "espfj_input_submit('')" )} "提交申请") (space 5)
                      (eui-button {:onclick "$('form').get(0).reset()"} " 重 填 ")) })]
       [:h2 {:style "padding:15px; background-color:lightgray; margin-bottom:0px; color:#333; text-shadow:0 1px 0 #EEE;"}
        "版权所有：福建省交通运输厅 2012 年"] 
       ]
      (fileupload-dialog)) ))

(defn input-submit
  "service: 福建考评员申请表提交保存或更新
  @id form名称如'pn' 'en' 'org' "
  [request]
  (let [vars (query-vars2 request)
        m (into {} (for [[k v] vars :when (not (nullity? v))] [k v]))]
    (with-mdb "espfj" 
      (insert! :pn-apply (into m {:date (datetime)}))) 
    (format "已提交 %s 的申请表。" (vars :name) )))

(defn pn-apply-list
  "service：分页显示考评员申请列表
  @skip 跳过的记录数
  @del '0' '全部' '1' '正常' '-1' '垃圾箱（已删除）' 
  @resp 'yes' 'no' 'nil' "
  [skip del resp request]
  (let [role (wr3role request)
        where (if (in? role ["1400" "1401"]) {} {:admin role}) ; 管理员看全部，其他看自己管理范围的。
        options-del {"-" "全部" "0" "正 常（未删除）" "1" "垃圾箱（已删除）"}
        options-resp {"-" "全部" "yes" "已审批同意的" "no" "已审批不同意的" "nil" "尚未处理的"}
        where-del (case del 
                    "-" nil
                    "0" {:del {:$ne "1"}}
                    "1" {:del "1"})
        where-resp (case resp
                     "-" nil
                     "yes" {:resp "yes"}
                     "no" {:resp "no"}
                     "nil" {:resp nil} )
        where (merge where where-del where-resp)

        count1 (with-mdb2 "espfj" (fetch-count :pn-apply :where where))
        limit 20 ; 每页的限制记录值
        pages (int (Math/ceil (/ count1 limit)))
        pager-options (map #(let [k (* % limit)
                                  v (format "第%s页：%s-%s" 
                                            (inc %) (inc k) (if (= % (dec pages)) count1 (* limit (inc %))))]
                              (vector k v)) (range pages))
        rs2 (with-mdb2 "espfj" 
              (vec (fetch :pn-apply :skip (to-int skip) :limit limit :where where 
                          :sort {:del 1 :admin 1 :resp -1 :date -1})))
        ] 
    (html
      [:div {:style "margin:5px;"} 
       [:label {:for "pagers"} (format "共 %s 页 %s 条，选择：" (int (Math/ceil (/ count1 limit))) count1)]
       (eui-combo {:id "pagers" :value (to-int skip) :onchange "espfj_pn_list_onchange(true)"} pager-options)
       (space 5) [:label "范围选择："] (eui-combo {:id "del" :value del :onchange "espfj_pn_list_onchange(false)" } 
                                             options-del)
       (space 5) [:label "按处理结果筛选："] 
       (eui-combo {:id "resp" :value resp :onchange "espfj_pn_list_onchange(false)"} options-resp)
       ]
      (espc/result-html- 
        rs2
        ["主管机关" "报名日期" "姓名" "身份证" "单位" "职称" "现从事专业" "报名类型" "处理结果" "详情"]
        [:admin :date :name :pid :org :title :prof :type :resp :_id-fj]
        {:admin dd-admin-fj :form "admin-resp"}) )))

(defn admin
  "主管机关查看所有申请"
  [id request]
  (let [role (wr3role request)
        uid (wr3user request)
        rs (with-mdb2 "espfj" (fetch-one :user :where {:uid uid})) ; 登录用户的信息 
        ]
    (html-body 
      {:js "app-espfj.js"}
      [:center {:style "border: 1px solid #366"}
       [:h1 {:style "padding:20px; background-color:#366; color: white; font-size: 22px; margin-top:0px"} 
        "福建省交通运输厅——考评员在线报名系统"]
       [:div {:align "center" :style "border:0px solid red"}
        [:h2 {:align "right"}
         [:a {:href "/c/espfj/pn-search" :target "_blank"} "【查询考评员】"] (space 5)
         [:a {:href "#" :onclick "$.ajaxSetup({cache:false}); app_exit('/c/espfj/admin')"} "【注销】"] (space 5)
         [:a {:href "/c/espfj/passwd" :target "_blank"} "【修改密码】"] (space 5)
         (when (in? role ["1400" "1401"]) 
           (html
             [:a {:href "/c/espfj/users" :target "_blank"} "【管理用户】"] (space 5)
             [:a {:href "/c/espfj/olap-admin" :target "_blank"} "【各地报名情况】"]) ) ]
        [:h1 (format "考评员资格审批（%s）" (:name rs))] 
        [:div#list (pn-apply-list 0 "-" "-" request)]]
       [:br][:br][:br]
       [:h2 {:style "padding:15px; background-color:lightgray; margin-bottom:0px; color:#333; text-shadow:0 1px 0 #EEE;"}
        "版权所有：福建省交通运输厅 2012 年"] ] )))

(defn admin-resp
  "主管机关处理一个申请"
  [id request]
  (let [rs (with-mdb2 "espfj" (fetch-by-id :pn-apply (object-id id)))]
    (espc/doc- :pn-apply id 
              {:rs rs :admin dd-admin-fj :from dd-province-fj
               :before (html [:h1 {:align "center"} "考评员资格审批"]
                             (if (not= "1" (:del rs))
                               (eui-button {:onclick "espfj_admin_resp_del(1)" :iconCls "icon-cancel" :style "margin:10px"} 
                                           "删除此申请记录（放入垃圾箱）")
                               (eui-button {:onclick "espfj_admin_resp_del(0)" :iconCls "icon-undo" :style "margin:10px"}
                                           "恢复此记录")))
               :after (html 
                        [:h2 "审批意见："]
                        [:form#fm1 {:action "/c/espfj/admin-resp-submit" :method "post"}
                         [:input {:id "oid" :name "oid" :type "hidden" :value (:_id rs)}]
                         [:p [:label "处理意见："] (eui-textarea {:name "advice"} (:advice rs))]
                         [:p [:label "处理结果："] (eui-combo {:id "resp" :name "resp" :value (:resp rs)} 
                                                         {"yes" "同 意" "no" "不同意"})]
                         (eui-tip "符合免试申请条件者：")
                         (eui-text {:id "pass-direct" :name "pass-direct" :type "checkbox"}) (space 2) "直接颁发" 
                         [:p (eui-button {:onclick "$('#fm1').submit()" :iconCls "icon-ok"} "提 交")]] 
                        [:script "espfj_admin_resp()"]
                        )
               :orders (map second cfg-pn)
               }) ))

(defn admin-resp-submit
  [request]
  (let [{oid :oid resp :resp advice :advice pass-direct :pass-direct} (query-vars2 request) ]
    (with-mdb2 "espfj" (update! :pn-apply {:_id (object-id oid)}
                                {:$set {:resp resp :advice advice :pass-direct pass-direct}}))
    (html-body
      [:h2 "已保存"]
      (eui-button-close))))

(defn admin-resp-del
  [id flag request]
  (when-not (nullity? id)
    (with-mdb2 "espfj" (update! :pn-apply {:_id (object-id id)}
                                {:$set {:del flag}}))
    (if (= "1" flag) 
      "已删除申请记录，关闭后请刷新列表。"
      "已恢复申请记录，关闭后请刷新列表。")))

(defn login
  "登录界面"
  [request]
  (let []
    (html-body
      {:class "login_body"}
      [:img {:src "/img/idp-webfirst.png" :style "position: absolute; top: 50%; left: 50%; margin-top: -80px; margin-left: -110px;"}]
      [:form {:method "post" :ID "LoginForm" :name "LoginForm" :action "/c/espfj/login-submit" :class "login_form ui-corner-all" }
       [:center [:div {:style "color:yellow;margin-bottom:9px"} "提示：请输入管理用户名和密码进行登录。"]]
       "用户名称：" [:input {:id "uid" :name "uid" :style "" :class "ui-corner-all"} ""] [:br]
       "用户密码：" [:input {:id "pwd" :name "pwd" :type "password" :size 20 :maxlength 20 :class "ui-corner-all"}] [:br]
       [:p {:align "center" :style "padding:6px"}
        [:input {:type "submit" :value " 提 交 " :class "ui-state-default ui-corner-all" :style "font-weight:bold;color:green"}]]
       [:center [:img {:alt "nasoft" :src "/img/nasoft-rnd.png"}]]]
       [:script "document.title='考评员在线报名管理登录' "])))

(defn login-submit
  "从espfj库的user表中进行对比"
  [request]
  (let [vars (query-vars2 request)
        uid (:uid vars)
        pwd (:pwd vars)
        wr3url (session request "wr3url") 
        rs (with-mdb2 "espfj" (fetch-one :user :where {:uid uid :pwd (wr3.util.Stringx/md5 pwd)}))]
;    (println "rs=" rs ", wr3url=" + wr3url)
    (html
      (if (not rs) "用户名密码错误，详情请询问系统管理员。<a href='javascript:history.back()'>返回</a>"
        (do (session! request "wr3user" (:uid rs))
          (session! request "wr3role" (:role rs))
          (html (format "%s 认证成功！" (:name rs))
                [:script (format "window.location.href='%s' " wr3url)])) ))))

(defn passwd
  "用户管理：修改自己的密码（所有用户），增加用户（仅管理员）"
  [request]
  (let [uid (wr3user request)
        role (wr3role request)
        rs (with-mdb2 "espfj" (fetch-one :user :where {:uid uid})) ; 登录用户的信息
        ]
    (html-body
      [:h1 (fmt "更改密码（%s）" (:name rs))]
      [:form#fm1 {:action "/c/espfj/passwd-change" :method "POST"}
       [:label {:for "pwd0"} "请输入原密码："] [:input#pwd0 {:name "pwd0" :type "password"}][:br]
       [:label {:for "pwd1"} "请输入新密码："] [:input#pwd1 {:name "pwd1" :type "password"}][:br]
       [:label {:for "pwd2"} "再输入新密码："] [:input#pwd2 {:name "pwd2" :type "password"}][:br]
       [:br]
       (eui-button {:onclick "$('#fm1').submit()"} "确认修改") (space 5) 
       (eui-button-close) ] 
      )))

(defn passwd-change
  "service: 修改密码"
  [request]
  (let [uid (wr3user request)
        {pwd0 :pwd0 pwd1 :pwd1 pwd2 :pwd2} (query-vars2 request)
        [pwd0-md5 pwd1-md5 pwd2-md5] (map #(wr3.util.Stringx/md5 %) [pwd0 pwd1 pwd2])
        rs (with-mdb2 "espfj" (fetch-one :user :where {:uid uid}))]
    (cond
      (not= pwd0-md5 (:pwd rs)) "出错：原密码不正确！"
      (not= pwd1 pwd2) "出错：两次输入的新密码不一致！"
      :else (do (with-mdb2 "espfj" (update! :user rs {:$set {:pwd pwd1-md5}}))
              "已成功更改密码！") )))

(defn users
  "所有系统用户列表，仅管理员可见"
  [request]
  (let [rs (with-mdb2 "espfj" (vec (fetch :user :sort {:role 1} :where {:uid {:$ne "admin"}})))]
    (html-body
      [:center [:h1 "系统用户管理"]
       (eui-button {:href "/c/espfj/user" :target "_blank" :style "margin:10px"} "增加新用户" )
       (espc/result-html- rs
                         ["用户角色" "用户名称" "用户ID" "详情"]
                         [:role :name :uid :_id-fj]
                         {:admin dd-admin-fj :form "user"})])))

(defn user
  "@id id是用户oid字符串，没有id则表示新增用户操作"
  [id request]
  (let [rs (when id (with-mdb2 "espfj" (fetch-by-id :user (object-id id)))) ; 登录用户的信息
        ]
    (html-body
      [:h1 (fmt "用户管理（%s）" (or (:name rs) "增加新用户"))]
      [:form#fm1 {:action "/c/espfj/user-crud" :method "POST"}
       [:input {:name "oid" :type "hidden" :value id}]
       [:label {:for "name"} "用户名称："] [:input {:name "name" :value (or (:name rs) "")}][:br]
       [:label {:for "uid"} "用户标识："] [:input {:name "uid" :value (or (:uid rs) "请输入用户唯一标识")}][:br]
       [:label {:for "role"} "用户角色："] (eui-combo {:name "role" :value (or (:role rs) nil)} dd-admin-fj)[:br]
       [:label {:for "pwd"} "用户密码："] [:input {:name "pwd" :type "password"}][:br]
       [:br]
       (if (nil? id)
         (html 
           (eui-button {:onclick "$('#fm1').attr('action','/c/espfj/user-crud/add').submit()"} "新加用户") (space 5))
         (html
           (eui-button {:onclick "$('#fm1').attr('action','/c/espfj/user-crud/update').submit()"} "更新用户") (space 5) 
           (eui-button {:onclick "$('#fm1').attr('action','/c/espfj/user-crud/del').submit()"} "删除用户") (space 5) ))
       (eui-button-close) ] 
      )))

(defn user-crud
  "用户管理的crud.
  @crud 'add' 'del' 'update' "
  [id request]
  (with-mdb2 "espfj"
    (let [{oid :oid n :name role :role uid :uid pwd :pwd} (query-vars2 request)
          pn (fetch-by-id :user (when (not (nullity? oid)) (object-id oid)))
          pn-new {:name n :role role :uid uid :pwd (wr3.util.Stringx/md5 pwd)}]
      (case id
        "add" (if (fetch-one :user :where {:uid uid}) 
                (format "ID为 %s 的用户已经存在，请填写不同的ID" uid)
                (do (insert! :user pn-new) (str "已增加新用户" n)))
        "del" (do (destroy! :user pn) (str "已删除用户" n))
        "update" (do (update! :user pn pn-new) (str "已保存用户" n))
        "未知动作" ))))

(defn olap-admin
  []
  (let [rs (with-mdb2 "espfj" (mdb-group :pn-apply [:admin]))
        rt1 (sort-by :admin rs)
        rt2 (sort-by :count > rs)]
  (html-body
    [:h1 "按地市报名情况统计表："]
    [:table {:width "100%" :align "center"}
     [:tr
      [:td [:h2 "按主管机关排序："]
       (espc/result-html- rt1 ["主管机关" "报名小计"] [:admin :count] {:admin dd-admin-fj}) ]
      [:td [:h2 "按报名数量排序："]
       (espc/result-html- rt2 ["主管机关" "报名小计"] [:admin :count] {:admin dd-admin-fj}) ]]]
    )))


(defn pn-search
  "自动完成的en名称、证书号录入框的html片段
  @tb :en :org :pn "
  []
  (let []
    (html-body
      (eui-tip "请输入报名人姓名或身份证号进行模糊查询，并选定查看详情。")
      [:form {:id "fm1" :action "/c/espfj/pn-search-doc" :method "post" :target "_blank"}
       [:label "姓名或身份证号：" ]
       [:input#in {:name "in" :type "text" :style "width:300px;" :value ""}] (space 5)
       (eui-button {:onclick "$('#fm1').submit()"} "显示详情")]  [:br]
      (eui-button-close)
      [:script (format "input_autocomplete('%s',1)" "/c/espfj/name-pid-autocomplete")])))

(defn name-pid-autocomplete
  "service: en-search-input函数提交的自动完成查询
  @term 自动搜索控件自动附上的参数，带搜索字符串 "
  [term request]
  (let [role (wr3role request)
        where (if (in? role ["1400" "1401"]) {} {:admin role}) ; 管理员看全部，其他看自己管理范围的。
        where (into where {:$or [{:name (re-pattern term)} {:pid (re-pattern term)}]})
        _ (println "--" where)
        rs (with-mdb2 "espfj" (vec (fetch :pn-apply :limit 20 :where where)))]
    (json-str (map #(format "%s %s" (:pid %) (:name %)) rs))))

(defn pn-search-doc
  "显示搜索到"
  [in request]
  (let [pid (left in " ")
        row (with-mdb2 "espfj" (fetch-one :pn-apply :where {:pid pid}))]
    (if row
      (admin-resp (str (:_id row)) request) 
      (html-body [:h1 "未搜索到结果"] (eui-button-close)) )))

;(def doc1 (with-mdb2 "espfj" (fetch-by-id :pn-apply (object-id "5004f3fe2823f20ced2a6249")))) ; 1104  603
;  (doseq [[k v] (rest dd-admin-fj)]
;    (insert! :user {:name v :role k :uid k :pwd "7215ee9c7d9dc229d2921a40e899ec5f"})))

;(def orders (for [[k v] cfg-pn] v))
;(println orders)
;(def m1 (for [k orders :let [v (doc1 k)] :when v] [k v]))
;(def m2 (for [[k v] doc1 :when (not (in? k orders))] [k v]))
;(concat m1 m2)
;(apply array-map (flatten (concat (for [k orders :let [v (doc1 k)] :when v] [k v])
;                                  (for [[k v] doc1 :when (not (in? k orders))] [k v]))))
                            
(defn update-espfj
  "共用函数：更新保存记录到esp的tb表中。不需要用with-mdb2包围
  @tb 要更新的表，如 :hot
  @where 查询要更新记录的条件，如 {:_id (object-id '..')}  
  @fm 带一个r记录参数的函数f或者一个哈希m，如 (fn [r] {:date (date)}) 
  @replace 任意值（一般用 :replace 即可），代表要替代而不是合并 "
  [tb where fm & replace]
  (with-mdb2 "espfj"
    (let [rs (fetch tb :where where)]
      (doseq [r rs] 
        (let [fr (if (fn? fm) (fm r) fm)]
          (update! tb r (if replace fr (into r fr) )))))))

(defn update-date
  "运行一次，用来更新原来的:date字段格式 "
  []
  (do (update-espfj :pn-apply {} (fn [r] {:date (date-format (:date r) "yyyy-MM-dd HH:mm:ss")}))
    (html "转换完毕")))
  
;(with-mdb2 "espfj" (doseq [r (fetch :pn-apply :where {})] (println (:date r))));(date-format (:date r) "yyyy-MM-dd HH:mm:ss"))))