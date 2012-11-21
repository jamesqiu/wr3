(ns wr3.clj.app.espreg)

(use 'wr3.clj.web 'wr3.clj.n 'wr3.clj.s 'wr3.clj.u)
(use 'wr3.clj.db 'wr3.clj.nosql 'wr3.clj.tb)
(use 'hiccup.core 'somnium.congomongo 'clojure.contrib.json)
(require '[wr3.clj.app.espconf :as conf])
(import cn.org.bjca.client.security.SecurityEngineDeal)
(import wr3.util.Stringx)

;;;;------------------------- esp 无证书登录验证
(defn- valid-user?
  [uid rune]
  (= (valid-rune uid) rune))

(defn check0
  "用户名密码不对返回nil，否则返回用户名和角色，被auth.clj的login函数调用. 没在mdb的esp中注册的用户如admin等会失败"
  [uid pwd]
  (let [r (with-mdb2 "esp" (fetch-one :user :where {:uid uid}))]
    (when (or (valid-user? (:uid r) pwd) (= (:pwd r) (wr3.util.Stringx/md5 pwd)))
      {:name (:name r) :roles (:role r)}) ))

;;;;------------------------- BJCA 证书登录页面及验证
;; bjca登录页面设置服务器Login函数所需的几个全局变量
(defn bjca-vars 
  "先和CA服务器通讯"
  [strServerSignedData strServerRan strServerCert]
  (format "<script LANGUAGE=javascript>
             var strServerSignedData = \"%s\"; 
			       var strServerRan = \"%s\";
			       var strServerCert = \"%s\";
           </script>" 
          strServerSignedData strServerRan strServerCert))

(defn container-name
  "service: 获取bjca登录时写入session的证书U盘唯一号 "
  [request]
  (str (session request "ContainerName")))

(defn- ca-login
  "bjca登录页面（新样式）
  @type :server :local
  @vars-js @type为:server时传入，@type为:local时无效 "
  [type vars-js]
  (let [action ({:local "ca-local-submit" :server "ca-submit"} type)
        onsubmit ({:local "esp_bjca_onsubmit_local()" :server "esp_bjca_onsubmit_server()"} type)]
    (html-body
      {:style "margin:0; padding:0" :js (into conf/bjca-js2 ["app-esp.js"]) :onload "esp_bjca_onload()"}
      (when (= type :server) vars-js) 
      [:div#moc_login
       [:div.moc_login_main
        ; 1 上：系统名称
        [:div.login_left_h2 [:img {:src "/esp/img/jtb_r2_c3.jpg"}]]
        ; 2 中
        [:div.login_left_con 
         ; 2.1 中左
         [:div.login_left_pic [:img {:src "/esp/img/jtb_r4_c2.jpg"}]]
         ; 2.2 中中
         [:div.login_left_log
          [:div.login_log_h3 [:img {:src "/esp/img/jtb_r4_c.jpg"}]]
          [:div.login_log_user
           [:div.user_info {:style "border:1px dashed #ccf"}
            [:form {:method "post" :ID "LoginForm" :name "LoginForm" 
                    :action (str "/c/espreg/" action) :onsubmit (str "return " onsubmit)}
             conf/bjca-prompt [:br][:br][:br]
             [:ul [:span "证书："] [:select {:id "UserList" :name "UserList" :style "width:215px" :class "ui-corner-all"} ""]]
             [:ul [:span "口令："] [:input {:id "UserPwd" :name "UserPwd" :type "password" :size 16 :maxlength 16}]] 
             [:input {:type "hidden" :ID "UserSignedData" :name "UserSignedData"}]
             [:input {:type "hidden" :ID "UserCert" :name "UserCert"}]
             [:input {:type "hidden" :ID "ContainerName" :name "ContainerName"}]
             [:input {:type "hidden" :ID "strRandom" :name "strRandom"}] 
             [:input {:type "hidden" :ID "strUserList" :name ""}] ; 保存 中文名称等
             [:center [:a {:href "/esp" :style "color:#069"} "返回主页"]] ]]
           [:div.user_sub
            [:ul [:img {:src "/esp/img/login_enter.png" :onclick "$('#LoginForm').submit()"}]]
            [:ul [:img {:src "/esp/img/login_reset.png" :onclick "$('#LoginForm').reset()"}]] ] ]]
         ; 2.3 中右
         [:div.login_right_pic [:img {:src "/esp/img/jtb_r4_c5.jpg"}]] ]
        ; 3 下：技术支持
        [:div {:class "login_bottom_txt"} "@CopyRight 2012 技术支持电话：13301357860、13301357875"] ]]
      conf/bjca-onchange
      [:script "document.title='标准化系统证书登录' "] )))

(defn ca-local
  "app: bjca不联服务器本地提交ukey，提交到ca-local-submit函数"
  [request]
  (ca-login :local nil))
  
(defn ca-server
  "app: BJCA服务器认证UKey，win下配置文件：%USERPROFILE%\\BJCAROOT\\SVSClient.properties "
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM")
        strServerCert (.getServerCertificate sed)
        strRandom (.genRandom sed 24)
        strSignedData (.signData sed (.getBytes strRandom))
        sr (session! request "Random" strRandom) 
        vars-js (bjca-vars strSignedData strRandom strServerCert) ]
    (ca-login :server vars-js)))

(defn ca
  "app: 可通过本地或者服务器认证bjca ukey"
  [request]
  ; 可调用 ca-local 或者 ca-server
  (case conf/login-mode
    :ca-local  (ca-local request)
    :ca-server (ca-server request) ))

(defn- bjca-verify
  "bjca服务器认证结果"
  [request]
  (let [vars (query-vars2 request)
        ;<bjca>
        _ (session! request "ContainerName" (:ContainerName vars)) ; session中保存登录U盘的唯一号
        sed (SecurityEngineDeal/getInstance "SM")
        ranStr (session request "Random") ; (:strRandom vars)
        clientCert (:UserCert vars)
        UserSignedData (:UserSignedData vars)
        certPub (.getCertInfo sed clientCert 8)
        retValue (.validateCert sed clientCert)
        uniqueIdStr (.getCertInfo sed clientCert 17)
        ; SF110010198709080314 或 1@5009JJ0686119689
        uniqueId (.getCertInfoByOid sed clientCert "1.2.156.112562.2.1.1.24") 
        signedByte (.base64Decode sed UserSignedData)        
        rt (.verifySignedData sed clientCert (.getBytes ranStr) signedByte) ; 认证结果 true/false
        ;</bjca>
        ]
    {:uniqueId uniqueId :rt rt :retValue retValue} ))
  
(defn- bjca-parse
  "从bjca取出的uniqueId中得到{:type .. :pid .. :no ..}
  @uniqueId 形如：'2@5009JJ0X0009970-2'  'SF210106198506020084' "
  [uniqueId]
  (let [PaperType (if (.startsWith uniqueId "SF") "SF" "JJ"); 'JJ' 'SF'
        PaperID (if (= PaperType "SF") (subs uniqueId 2) 
                  (str (left uniqueId "@") "@" (right uniqueId "JJ"))) ; pid
        pid (if (= PaperType "SF") PaperID
              (let [n (count PaperID)] (subs PaperID (- n 10))))]
    (if (= PaperType "SF") 
      {:type "SF" :pid pid}
      {:type "JJ" :pid pid :no (left uniqueId "@")})))    
  
(defn- bjca-decode
  "从bjca本地提交内容中直接解码读出证件号内容"
  [clientCert]
  (let [decs (Stringx/base64dec clientCert)
        idx (.indexOf decs "\u0002\u0001\u0001\u0018")
        idx1 (+ 2 (.indexOf decs "\u000c" (+ idx 4)))
        idx2 (.indexOf decs "\n" idx1)
        ; 2@5009JJ0X0009970-2  SF210106198506020084
        uniqueId (subs decs idx1 (dec idx2))]
    (bjca-parse uniqueId)))

(def db-reg "espdev") ; 用户注册的RDB数据源名称 esp 本机；espdev 运行

(defn- check1
  "验证方法1：ca服务器验证后外部sqlserver注册数据库验证"
  [request]
  (let [{uniqueId :uniqueId rt :rt} (bjca-verify request)
        {PaperType :type PaperID :pid} (bjca-parse uniqueId)
        ; 本地sqlserver认证已注册用户
        rs (select-row db-reg (format "SELECT PaperID,CommonName,usertype FROM userregister where PaperType='%s' and PaperID='%s' " 
                                      PaperType PaperID)) 
        wr3url (session request "wr3url") ]
    (cond
      (not rt) "证书认证失败"
      (not rs) "您所用的可能非【交通运输企业安全生产标准化系统】专用证书。"
      :else (do (session! request "wr3user" (str (:usertype rs) "-" PaperID))
              (session! request "wr3role" (:usertype rs))
              (html (format "%s（%s）认证成功！" (:commonname rs) PaperID)
                   [:script (format "window.location.href='%s' " wr3url)])) ) ))

(defn- check2
  "验证方法2：ca服务器验证后esp数据库验证"
  [request]
  (let [{uniqueId :uniqueId rt :rt retValue :retValue} (bjca-verify request)
        [pid no] ((juxt :pid :no) (bjca-parse uniqueId))
        ; 本地mdb认证已注册用户
        rs (with-mdb2 "esp" (fetch-one :user :where {:pid pid :usable {:$ne "0"}}))
        wr3url (session request "wr3url") ]
    (html
      [:head meta-utf8]
      [:body
       (cond
         (not rt) (format "证书认证失败（%s）：%s" uniqueId rt)
         (neg? retValue) (format "证书出现问题：%s" (conf/dd-retValue retValue))
         (not rs) "您所用的可能非【交通运输企业安全生产标准化系统】专用证书；或证书尚未由主管机关管理员完成审核。"
         :else (do 
                 (session! request "wr3user" (:uid rs))
                 (session! request "wr3role" (:role rs))
                 ; 写登录日志
                 (with-mdb2 "esp" (insert! :log {:uid (:uid rs) :role (:role rs) :pid pid :no no :url wr3url :date (datetime)}))
                 (html (format "%s（%s）认证成功！" (:name rs) pid)
                       [:script (format "window.location.href='%s' " wr3url)])) ) ])))

;(println (not (with-mdb2 "esp" (fetch-one :user :where {:pid "X0009980-0" :usable {:$ne "0"}}))))

(defn- check3
  "验证方法3：不使用ca服务器直接读取ukey中的pid信息，esp数据库验证"
  [request]
  (let [vars (query-vars2 request)
        clientCert (:UserCert vars)
        pid (:pid (bjca-decode clientCert))
        ; 本地mdb认证已注册用户
        rs (with-mdb2 "esp" (fetch-one :user :where {:pid pid})) 
        wr3url (session request "wr3url") ]
    (html
      [:head meta-utf8]
      [:body
       (if (not rs) "您所用的可能非【交通运输企业安全生产标准化系统】专用证书。"
         (do (session! request "wr3user" (:uid rs))
           (session! request "wr3role" (:role rs))
           (session! request "ContainerName" (:ContainerName vars)) ; ukey 的唯一号   
           (html (format "%s（%s）认证成功！" (:name rs) pid)
                 [:script (format "window.location.href='%s' " wr3url)])) )])))

(defn ca-submit
  "BJCA 插入证书密码提交后认证。
  如果ca调用ca-server，本函数可调用check1 check2 ；
  如果ca调用ca-local，则由ca-local-submit函数调用check3处理"
  [request]
;  (check1 request))
  (check2 request))

(defn ca-local-submit
  "ca-local提交的结果响应"
  [request]
  (check3 request))

(defn pn-ca-read
  "app: 读取考评员U盘信息。用于org聘用pn "
  [request]
  (let []
    (html-body
      {:js (into conf/bjca-js2 ["app-esp.js"]) :onload "esp_pn_ca_onload()"}
      [:h1 "请选取考评员证书U盘并点击确定"]
      [:form {:method "post" :ID "LoginForm" :name "LoginForm" :onsubmit "return esp_bjca_onsubmit_local()"}
       "选择证书：" [:select {:id "UserList" :name "UserList" :style "width:220px" :class "ui-corner-all"} ""] [:br]
       ] 
      (eui-button {:onclick "esp_pn_ca_ok()"} "确定")
      conf/bjca-onchange )))

(defn ca-test
  "bjca本地测试获取U盘信息"
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM")]
    (html-body
       {:js (into conf/bjca-js2 ["app-esp.js"]) :onload "esp_bjca_onload()"}
       [:h1 "js 离线读取ukey属性"]
       [:form {:method "post" :ID "LoginForm" :name "LoginForm" 
               :action "/c/espreg/ca-test-submit" :target "ifrm1" 
               :onsubmit "return esp_bjca_onsubmit_local()"}
        "选择证书：" [:select {:id "UserList" :name "UserList" :style "width:220px" :class "ui-corner-all"} ""] [:br][:br]
        "选择口令：" [:input {:id "UserPwd" :name "pwd1" :type "password" :size 16 :maxlength 16 :class "ui-corner-all"}] [:br]
        (eui-button {:onclick "var uniqueid = 
          XTXAPP.SOF_GetCertInfoByOid(SOF_ExportUserCert($('#UserList').val(),KEY_SIGNOREXCHANGE),'1.2.156.112562.2.1.1.24');
          alert(uniqueid)"} "得到 uniqueId") (space 5)
        [:input {:type "submit" :value " 提 交 " :class "ui-state-default ui-corner-all" :style "font-weight:bold;color:green"}]
        [:input {:type "hidden" :ID "UserSignedData" :name "UserSignedData"}]
        [:input {:type "hidden" :ID "UserCert" :name "UserCert"}]
        [:input {:type "hidden" :ID "ContainerName" :name "ContainerName"}]
        [:input {:type "hidden" :ID "strRandom" :name "strRandom" :value "NDYxNjY1NTEwNzc0NTk1NTM4NTcxOTY0"}]
        ] 
       [:iframe#ifrm1 {:name "ifrm1" :style "width:90%; height:200px; border:1px solid red"}]
       conf/bjca-onchange )))
  
(defn ca-test-submit
  "ca-test提交的结果测试"
  [request]
  (let [vars (query-vars2 request)
        clientCert (:UserCert vars)
        rt (bjca-decode clientCert) ]
    (html-body
      [:h1 "rt: " rt]
      [:table (for [[k v] vars] [:tr [:td k] [:td v]])] )))

;(import cn.org.bjca.client.security.SecurityEngineDeal)
;(def sed (SecurityEngineDeal/getInstance "SM"))
;(def strServerCert (.getServerCertificate sed))
;(def strRandom (.genRandom sed 24))
;(def strSignedData (.signData sed (.getBytes strRandom)))
  
(defn ca-server-test
  "app: BJCA服务器连通性测试 "
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM")
        strServerCert (.getServerCertificate sed)
        strRandom (.genRandom sed 24)
        strSignedData (.signData sed (.getBytes strRandom))]
    (html-body
      [:h2 (str "strServerCert:<br/>" strServerCert)]
      [:h2 (str "strRandom:<br/>" strRandom)]
      [:h2 (str "strSignedData:<br/>" strSignedData)])))

;;--------------------------------------------
(defn who
  "service: 返回session中名为'wr3user'的当前已登录用户的json对象{:uid .. :name .. :roles ..}，未登录则返回'null' "
  [request]
  (if-let [uid (wr3user request)]
    (let [user (with-mdb2 "esp" (fetch-one :user :where {:uid uid}))]
      (json-str {:uid uid :name (:name user) :roles (:role user)}))
    "null"))

(defn user-reg
  "app: 列表查看申请过UKey的用户"
  []
  (let [sql "SELECT PaperID,CommonName,usertype, TradeGuid FROM userregister order by TradeGuid desc"
        rs (select-all db-reg sql)]
    (html-body 
      (result-html rs {:f-td (fn [row-index col-index [k v] row]
                               [:td (td-align v) 
                                (case k
                                  :tradeguid [:a {:href (str "/c/espreg/user-import/" v) :target "_blank"} "审批该用户"]
                                  v) ]) }))))

;(println (select-all "esp" "select top 3 * from userregister order by TradeGuid desc"))
  
(defn user-import
  "app: 查看某一个申请过UKey的用户
  @id 注册rdb表中的TradeGuid字段内容 "
  [id]
  (let [sql (format "SELECT PaperID,CommonName,usertype FROM userregister where TradeGuid='%s' " id)
        r (select-row db-reg sql)
        uid (str (:usertype r) "-" (:paperid r))
        r1 (with-mdb2 "esp" (fetch-one :user :where {:uid uid}))]
    (html-body 
      [:h2 "注册用户："] r
      [:br]
      (if r1
        (html [:h2 "已存在或已导入相同证件号的用户："] r1 [:br][:br]
              (eui-button-close))
        (html [:h2 "系统尚未发现相同证件号的用户："] [:br]
          (eui-button {:onclick (format "ajax_post('/c/espreg/user-import-submit/%s')" id)} "请导入")))
      )))

(defn user-import-submit
  "service: 导入sqlserver注册用户到esp系统mdb中。
  @id 注册rdb表中的TradeGuid字段内容 "
  [id]
  (let [sql (format "SELECT PaperID,CommonName,usertype FROM userregister where TradeGuid='%s'" id)
        r (select-row db-reg sql)
        {nam :commonname pid :paperid role :usertype} r
        m {:name nam :pid pid :role role :uid (str role "-" pid) :date-import (datetime)}]
    (do (with-mdb2 "esp" (insert! :user m))
      (format "已经将 %s 导入并生效" nam))))

; 00359131-X 00002106-3
;(import wr3.bank.OrgID)
;(dotimes [i 50]
;  (let [s8 (format "%08d" (random 99999999))]
;  (println (OrgID/toid s8))))
;(OrgID/toid "00359131") ; 34448380-;
;(OrgID/isid "61032343-X")
