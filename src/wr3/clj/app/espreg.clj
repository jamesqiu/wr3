(ns wr3.clj.app.espreg)

(use 'wr3.clj.web 'wr3.clj.n 'wr3.clj.s 'wr3.clj.u)
(use 'wr3.clj.db 'wr3.clj.nosql 'wr3.clj.tb)
(use 'hiccup.core 'somnium.congomongo 'clojure.contrib.json)
(import cn.org.bjca.client.security.SecurityEngineDeal)
(import wr3.util.Stringx)

;;;;------------------------- esp 无证书登录验证
(defn check0
  "用户名密码不对返回nil，否则返回用户名和角色"
  [uid pwd]
  (let [r (with-mdb2 "esp" (fetch-one :user :where {:uid uid}))]
;    (println "-- check0 --" r)
    (when (= (:pwd r) (wr3.util.Stringx/md5 pwd))
      {:name (:name r) :roles (:role r)}) ))

;;;;------------------------- BJCA 证书登录页面及验证
; bjca证书验证返回值代表的含义
(def dd-retValue 
  {-1 "登录证书的根不被信任"
   -2 "登录证书超过有效期"
   -3 "登录证书为作废证书" 
   -4 "登录证书被临时冻结" })

(def bjca-activex
  "<OBJECT ID=\"XTXAPP\" CLASSID=\"CLSID:3F367B74-92D9-4C5E-AB93-234F8A91D5E6\" height=1 style=\"HEIGHT: 1px; LEFT: 10px; TOP: 28px; WIDTH: 1px\" width=1 VIEWASTEXT>
  <param name=\"CertListFormElement\" value=\"LoginForm.UserList\">
  <param name=\"ServerMode\" value=\"2\">
  <param name=\"AlertBeforeCertDate\" value=\"60\">
  <param name=\"ServerSignedData\" value= \"MEUCIFnTlKtaTG354EI4F5SKU1FBMpOTLToJs2t6FOZ8yK5kAiEA7f5vw1qLMUGlvAmfcPU5KFORUn1wVIPdVhpDjSTptsk=\" >
  <param name=\"ServerRan\" value= \"NDYxNjY1NTEwNzc0NTk1NTM4NTcxOTY0\" >
  <param name=\"ServerCert\" value= \"MIIEFjCCA72gAwIBAgIKGhAAAAAAAABGIzAKBggqgRzPVQGDdTBEMQswCQYDVQQGDAJDTjENMAsGA1UECgwEQkpDQTENMAsGA1UECwwEQkpDQTEXMBUGA1UEAwwOQmVpamluZyBTTTIgQ0EwHhcNMTExMTA4MTYwMDAwWhcNMTIxMTA5MTU1OTU5WjBeMRAwDgYDVQQpDAdURVNUMDAyMRAwDgYDVQQDDAdzbTJjZXJ0MQ0wCwYDVQQLDARiamNhMQ0wCwYDVQQKDARCSkNBMQ0wCwYDVQQKDARiamNhMQswCQYDVQQGDAJDTjBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABCmZyEamE/rn+h8avGyDHQKxHiVFoZIndsz+QVzYc7ma6tut7Widce80FTtHpyt6Pe1C8uyfZFJXaddwZWNYrzCjggJ7MIICdzAfBgNVHSMEGDAWgBQf5s/Uj8UiKpdKKYoV5xbJkjTEtjAdBgNVHQ4EFgQU2q8YnzPs3rS+SndwB4WIZ3Q9rAswCwYDVR0PBAQDAgbAMIGbBgNVHR8EgZMwgZAwX6BdoFukWTBXMQswCQYDVQQGDAJDTjENMAsGA1UECgwEQkpDQTENMAsGA1UECwwEQkpDQTEXMBUGA1UEAwwOQmVpamluZyBTTTIgQ0ExETAPBgNVBAMTCGNhMjFjcmwxMC2gK6AphidodHRwOi8vY3JsLmJqY2Eub3JnLmNuL2NybC9jYTIxY3JsMS5jcmwwGQYKKoEchu8yAgEBAQQLDAlKSlRFU1QwMDIwYAYIKwYBBQUHAQEEVDBSMCMGCCsGAQUFBzABhhdPQ1NQOi8vb2NzcC5iamNhLm9yZy5jbjArBggrBgEFBQcwAoYfaHR0cDovL2NybC5iamNhLm9yZy5jbi9jYWlzc3VlcjCBiQYDVR0gBIGBMH8wMAYDVR0gMCkwJwYIKwYBBQUHAgEWGyBodHRwOi8vd3d3LmJqY2Eub3JnLmNuL2NwczBLBgNVHSAwRDBCBggrBgEFBQcCARY2aHR0cDovL3d3dy5iamNhLm9yZy5jbi9zaXRlcy9kZWZhdWx0L2ZpbGVzL3NtMi1jcHMucGRmMBEGCWCGSAGG+EIBAQQEAwIA/zAXBgoqgRyG7zICAQEIBAkMB1RFU1QwMDIwGQYKKoEchu8yAgECAgQLDAlKSlRFU1QwMDIwHwYKKoEchu8yAgEBDgQRDA8xMDIwMDAwMDAwMDAwNzcwGQYKKoEchu8yAgEBBAQLDAlKSlRFU1QwMDIwCgYIKoEcz1UBg3UDRwAwRAIgCOM85UU7E2FjT5oddOxP/iEbbDd9skH4QdjHWG+Nm1gCIChXTmCbXcXbSlLNgdjxDl/JFNzXF8RMyr3Y+R2KkVk1\" >
  </OBJECT>")

;; bjca登录页面的js
(defn bjca-js 
  [strServerSignedData strServerRan strServerCert]
  (str "<script type='text/javascript' src='/esp/js/date.js'></script>"
       "<script type='text/javascript' src='/esp/js/XTXApp.js'></script>"
       (format "<script LANGUAGE=javascript>
                  var strServerSignedData = \"%s\"; 
			            var strServerRan = \"%s\";
			            var strServerCert = \"%s\";
                </script>" strServerSignedData strServerRan strServerCert)
       "<SCRIPT LANGUAGE=JAVASCRIPT event=OnLoad for=window>
		       GetUserList(\"LoginForm.UserList\");
		       LoginForm.UserPwd.focus();
	      </SCRIPT>
	      <SCRIPT LANGUAGE=javascript event=OnUsbKeyChange for=XTXAPP>
		       ChangeUserList(\"LoginForm.UserList\");
		       LoginForm.UserPwd.focus();
	      </SCRIPT>"))

; bjca usbkey 插拔响应代码
(def bjca-on-change
  (html bjca-activex
        [:script "if ($.browser.msie) XTXAPP.attachEvent('OnUsbkeyChange', esp_bjca_onchange)"] ))

(defn ca-local
  "app: bjca不联服务器本地提交ukey，提交到ca-local-submit函数"
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM")]
    (html-body
      {:class "login_body"}
      [:img {:src "/img/idp-webfirst.png" :style "position: absolute; top: 50%; left: 50%; margin-top: -80px; margin-left: -110px;"}]
        [:form {:method "post" :ID "LoginForm" :name "LoginForm" :action "/c/espreg/ca-local-submit" :class "login_form ui-corner-all"
                :onsubmit "return XTXAPP.AddSignInfo(LoginForm.UserPwd.value)"}
         [:center [:div {:style "color:yellow;margin-bottom:9px"} 
                   "提示：请先将系统专用UKey证书（<a href='#' style='color:#fcc'>申请</a>）插入计算机。"]]
         "选择证书：" [:select {:id "UserList" :name "UserList" :style "width:220px" :class "ui-corner-all"} ""] [:br]
         "选择口令：" [:input {:id "UserPwd" :name "pwd1" :type "password" :size 16 :maxlength 16 :class "ui-corner-all"}] [:br]
         [:p {:align "center" :style "padding:6px"}
          [:input {:type "submit" :value " 提 交 " :class "ui-state-default ui-corner-all" :style "font-weight:bold;color:green"}] ]
         [:center 
          [:a {:href "/esp" :style "color:white"} "返回主页"] [:br] 
          [:img {:alt "nasoft" :src "/img/nasoft-rnd.png"}]]
         [:input {:type "hidden" :ID "UserSignedData" :name "UserSignedData"}]
         [:input {:type "hidden" :ID "UserCert" :name "UserCert"}]
         [:input {:type "hidden" :ID "ContainerName" :name "ContainerName"}]
         [:input {:type "hidden" :ID "strRandom" :name "strRandom"}] ]
        bjca-activex
        [:script "document.title='标准化系统证书登录' "] )))

(defn ca-server
  "app: BJCA服务器认证UKey，win下配置文件：%USERPROFILE%\\BJCAROOT\\SVSClient.properties "
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM")
        strServerCert (.getServerCertificate sed)
        strRandom (.genRandom sed 24)
        strSignedData (.signData sed (.getBytes strRandom))
        sr (session! request "Random" strRandom) ]
    (html
      [:html
       (head-set-join (bjca-js strSignedData strRandom strServerCert))
       [:body {:class "login_body" :onload "esp_bjca_onload()" }
        [:img {:src "/img/idp-webfirst.png" :style "position: absolute; top: 50%; left: 50%; margin-top: -80px; margin-left: -110px;"}]
        [:form {:method "post" :ID "LoginForm" :name "LoginForm" :action "/c/espreg/ca-submit" :class "login_form ui-corner-all"
                :onsubmit (format "return esp_bjca_onsubmit('%s')" strRandom)}
         [:center [:div {:style "color:yellow;margin-bottom:9px"} 
                   "提示：请先将系统专用UKey证书（<a href='#' style='color:#fcc'>申请</a>）插入计算机。"]]
         "选择证书：" [:select {:id "UserList" :name "UserList" :style "width:220px" :class "ui-corner-all"} ""] [:br]
         "选择口令：" [:input {:id "UserPwd" :name "pwd1" :type "password" :size 16 :maxlength 16 :class "ui-corner-all"}] [:br]
         [:p {:align "center" :style "padding:6px"}
          [:input {:type "submit" :value " 提 交 " :class "ui-state-default ui-corner-all" :style "font-weight:bold;color:green"}] ]
         [:center 
          [:a {:href "/esp" :style "color:white"} "返回主页"] [:br] 
          [:img {:alt "nasoft" :src "/img/nasoft-rnd.png"}]]
         [:input {:type "hidden" :ID "UserSignedData" :name "UserSignedData"}]
         [:input {:type "hidden" :ID "UserCert" :name "UserCert"}]
         [:input {:type "hidden" :ID "ContainerName" :name "ContainerName"}]
         [:input {:type "hidden" :ID "strRandom" :name "strRandom"}] ]
        [:script "document.title='标准化系统证书登录' "]]] )))

(defn ca
  "app: 可通过本地或者服务器认证bjca ukey"
  [request]
  ; 可调用 ca-local 或者 ca-remote
  (ca-server request)) 

(defn- bjca-verify
  "bjca服务器认证结果"
  [request]
  (let [vars (query-vars2 request)
        ;<bjca>
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
    {:uniqueId uniqueId :rt rt} ))
  
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

(def db-reg "espdev") ; 用户注册的RDB数据源名称

(defn- check1
  "验证方法1：ca服务器验证后外部注册数据库验证"
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
  (let [{uniqueId :uniqueId rt :rt} (bjca-verify request)
        pid (:pid (bjca-parse uniqueId))
        ; 本地mdb认证已注册用户
        rs (with-mdb2 "esp" (fetch-one :user :where {:pid pid}))
        wr3url (session request "wr3url") ]
    (html
      [:head meta-utf8]
      [:body
       (cond
         (not rt) (format "证书认证失败（%s）：%s" uniqueId rt)
         (not rs) "您所用的可能非【交通运输企业安全生产标准化系统】专用证书。"
         :else (do (session! request "wr3user" (:uid rs))
                 (session! request "wr3role" (:role rs))
                (html (format "%s（%s）认证成功！" (:name rs) pid)
                      [:script (format "window.location.href='%s' " wr3url)])) ) ])))
       
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

(defn ca-test
  "app: 不与服务器通讯直接读bjca UKey内容。仅用于ie"
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM") ]
    (html-body
      {:class "login_body"}
      [:img {:src "/img/idp-webfirst.png" :style "position: absolute; top: 50%; left: 50%; margin-top: -80px; margin-left: -110px;"}]
      [:form {:method "post" :ID "LoginForm" :name "LoginForm" :action "/c/espreg/ca-test-submit" :class "login_form ui-corner-all"
              :target "ifrm1" :onsubmit "return XTXAPP.AddSignInfo(LoginForm.UserPwd.value)"}
       "选择证书：" [:select {:id "UserList" :name "UserList" :style "width:220px" :class "ui-corner-all"} ""] [:br]
       "选择口令：" [:input {:id "UserPwd" :name "pwd1" :type "password" :size 16 :maxlength 16 :class "ui-corner-all"}] [:br]
       [:p {:align "center" :style "padding:6px"}
        [:input {:type "submit" :value " 提 交 " :class "ui-state-default ui-corner-all" :style "font-weight:bold;color:green"}] ]
       [:input {:type "hidden" :ID "UserSignedData" :name "UserSignedData"}]
       [:input {:type "hidden" :ID "UserCert" :name "UserCert"}]
       [:input {:type "hidden" :ID "ContainerName" :name "ContainerName"}]
       [:input {:type "hidden" :ID "strRandom" :name "strRandom" :value "NDYxNjY1NTEwNzc0NTk1NTM4NTcxOTY0"}] ]
      bjca-activex
      [:iframe#ifrm1 {:name "ifrm1" :style "width:90%; height:200px; border:1px solid red"}] )))

(defn ca-test-submit
  "ca-test提交的结果测试"
  [request]
  (let [vars (query-vars2 request)
        clientCert (:UserCert vars)
        rt (bjca-decode clientCert)]
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
  "service: 导入注册用户到esp系统
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
