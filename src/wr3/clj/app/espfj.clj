(ns ^{:doc "为福建交通厅定制的考评员注册及审批"}
     wr3.clj.app.espfj)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.web 'wr3.clj.tb)
(use 'somnium.congomongo 'wr3.clj.nosql 'somnium.congomongo 'hiccup.core)
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
        rt2 (assoc (vec rt1) 2 ["常住地" :from {:t dd-province-fj :v "1"}])
        rt3 (row-add rt2 6 ["职称证明文件" :titlefile {:t 'file}])
        rt4 (row-add rt3 17 ["相关专业从业年份证明文件" :beginfile {:t 'file}])
        rt5 (row-add rt4 17 ["证明人联系电话" :proofmobile {}])
        rt6 (row-add rt5 17 ["证明人" :proofname {}])
        rt7 (row-add rt6 17 ["证明单位" :prooforg {}])
        ]
    (concat rt7 [["主管机关" :admin {:t dd-admin-fj :title "请自选主管机关"}]])))

(defn auth
  "该 CljServlet 调用，用于本应用各函数的权限控制 "
  [request fname ids & args]
  (let [uid (wr3user request) 
        role (wr3role request)
        id (first ids)]
;    (println (format "uid=%s, role=%s, fname=%s" uid role fname))
    (cond
      (in? fname ["index" "input-submit" "login" "login-submit" "logout"]) true
      (and (= fname "users") (not= role "1400") (not= role "1401")) false
      (= role "1400") true
      (in? role (keys dd-admin-fj)) true
      :else false)))

(defn index
  "填写考评员注册信息" 
  [request]
  (let [wr3user (session! request "wr3user" "fj")
        wr3role (session! request "wr3role" "pn")]
    (html-body
      {:onload "espfj_onload()"}
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

(defn admin
  "主管机关查看所有申请"
  [id request]
  (let [uid (wr3user request)
        role (wr3role request)
        rs (with-mdb2 "espfj" (fetch-one :user :where {:uid uid})) ; 登录用户的信息
        where (if (in? role ["1400" "1401"]) {} {:admin role}) 
        rs2 (with-mdb2 "espfj" (vec (fetch :pn-apply :where where)))] ; 管理员看全部，其他看自己管理范围的。
    (html-body 
      {:onload ""}
      [:center {:style "border: 1px solid #366"}
       [:h1 {:style "padding:20px; background-color:#366; color: white; font-size: 22px; margin-top:0px"} 
        "福建省交通运输厅——考评员在线报名系统"]
       [:div {:align "center" :style "border:0px solid red"}
        [:h2 {:align "right"}
         [:a {:href "#" :onclick "$.ajaxSetup({cache:false}); app_exit('/c/espfj/admin')"} "【注销】"] (space 5)
         [:a {:href "/c/espfj/passwd" :target "_blank"} "【修改密码】"] (space 5)
         (when (in? role ["1400" "1401"]) 
           [:a {:href "/c/espfj/users" :target "_blank"} "【管理用户】"]) ]
        [:h1 (format "考评员资格审批（%s）" (:name rs))]
        (espc/result-html- rs2
                         ["主管机关" "报名日期" "姓名" "身份证" "报名类型" "处理结果" "直接颁发" "详情"]
                         [:admin :date :name :pid :type :resp :pass-direct :_id-fj]
                         {:admin dd-admin-fj :form "admin-resp"})
        ]
       [:br][:br][:br]
       [:h2 {:style "padding:15px; background-color:lightgray; margin-bottom:0px; color:#333; text-shadow:0 1px 0 #EEE;"}
        "版权所有：福建省交通运输厅 2012 年"] 
       ] )))

(defn admin-resp
  "主管机关处理一个申请"
  [id request]
  (let [rs (with-mdb2 "espfj" (fetch-by-id :pn-apply (object-id id)))]
    (espc/doc- :pn-apply id 
              {:rs rs :admin dd-admin-fj :from dd-province-fj
               :before [:h1 {:align "center"} "考评员资格审批"]
               :after (html 
                        [:h2 "审批意见："]
                        [:form#fm1 {:action "/c/espfj/admin-resp-submit" :method "post"}
                         [:input {:name "oid" :type "hidden" :value (:_id rs)}]
                         [:p [:label "处理意见："] (eui-textarea {:name "advice"} (:advice rs))]
                         [:p [:label "处理结果："] (eui-combo {:id "resp" :name "resp" :value (:resp rs)} 
                                                         {"yes" "同 意" "no" "不同意"})]
                         (eui-tip "符合免试申请条件者：")
                         (eui-text {:id "pass-direct" :name "pass-direct" :type "checkbox"}) (space 2) "直接颁发" 
                         [:p (eui-button {:onclick "$('#fm1').submit()"} "提 交")]] )})))

(defn admin-resp-submit
  [request]
  (let [{oid :oid resp :resp advice :advice pass-direct :pass-direct} (query-vars2 request) ]
    (with-mdb2 "espfj" (update! :pn-apply {:_id (object-id oid)}
                                {:$set {:resp resp :advice advice :pass-direct pass-direct}}))
    (html-body
      [:h2 "已保存"]
      (eui-button {:href "#" :onclick "window.close();"} "关闭"))))

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
  ""
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
       (eui-button {:href "#" :onclick "window.close();"} "关闭") ] 
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
       (eui-button {:href "#" :onclick "window.close();"} "关闭") ] 
      )))

(defn user-crud
  "用户管理的crud.
  @crud 'add' 'del' 'update' "
  [id request]
  (with-mdb2 "espfj"
    (let [{oid :oid n :name role :role uid :uid pwd :pwd} (query-vars2 request)
          pn (fetch-by-id :user (object-id oid))
          pn-new {:name n :role role :uid uid :pwd (wr3.util.Stringx/md5 pwd)}]
      (case id
        "add" (if pn 
                (format "ID为 %s 的用户已经存在，请填写不同的ID" uid)
                (do (insert! :user pn-new) (str "已增加新用户" n)))
        "del" (do (destroy! :user pn) (str "已删除用户" n))
        "update" (do (update! :user pn pn-new) (str "已保存用户" n))
        "未知动作" ))))

;(with-mdb2 "espfj"
;  (doseq [[k v] (rest dd-admin-fj)]
;    (insert! :user {:name v :role k :uid k :pwd "7215ee9c7d9dc229d2921a40e899ec5f"})))

