(ns wr3.clj.app.espreg)

(use 'wr3.clj.web 'wr3.clj.n 'wr3.clj.s 'wr3.clj.u)
(use 'wr3.clj.db 'wr3.clj.nosql)
(use 'hiccup.core 'somnium.congomongo 'clojure.contrib.json)
(import cn.org.bjca.client.security.SecurityEngineDeal)
(import wr3.util.Stringx)

;;;;------------------------- esp 无证书登录验证
(defn check0
  [uid pwd]
  (println "-- check0 --")
  {:name "hello1" :roles "mot"})

;;;;------------------------- BJCA 证书登录页面及验证
; bjca证书验证返回值代表的含义
(def dd-retValue 
  {-1 "登录证书的根不被信任"
   -2 "登录证书超过有效期"
   -3 "登录证书为作废证书" 
   -4 "登录证书被临时冻结" })

(def activex
  "<OBJECT ID=\"XTXAPP\" CLASSID=\"CLSID:3F367B74-92D9-4C5E-AB93-234F8A91D5E6\" height=1 style=\"HEIGHT: 1px; LEFT: 10px; TOP: 28px; WIDTH: 1px\" width=1 VIEWASTEXT>
  <param name=\"CertListFormElement\" value=\"LoginForm.UserList\">
  <param name=\"ServerMode\" value=\"2\">
  <param name=\"AlertBeforeCertDate\" value=\"60\">
  <param name=\"ServerSignedData\" value= \"MEUCIFnTlKtaTG354EI4F5SKU1FBMpOTLToJs2t6FOZ8yK5kAiEA7f5vw1qLMUGlvAmfcPU5KFORUn1wVIPdVhpDjSTptsk=\" >
  <param name=\"ServerRan\" value= \"NDYxNjY1NTEwNzc0NTk1NTM4NTcxOTY0\" >
  <param name=\"ServerCert\" value= \"MIIEFjCCA72gAwIBAgIKGhAAAAAAAABGIzAKBggqgRzPVQGDdTBEMQswCQYDVQQGDAJDTjENMAsGA1UECgwEQkpDQTENMAsGA1UECwwEQkpDQTEXMBUGA1UEAwwOQmVpamluZyBTTTIgQ0EwHhcNMTExMTA4MTYwMDAwWhcNMTIxMTA5MTU1OTU5WjBeMRAwDgYDVQQpDAdURVNUMDAyMRAwDgYDVQQDDAdzbTJjZXJ0MQ0wCwYDVQQLDARiamNhMQ0wCwYDVQQKDARCSkNBMQ0wCwYDVQQKDARiamNhMQswCQYDVQQGDAJDTjBZMBMGByqGSM49AgEGCCqBHM9VAYItA0IABCmZyEamE/rn+h8avGyDHQKxHiVFoZIndsz+QVzYc7ma6tut7Widce80FTtHpyt6Pe1C8uyfZFJXaddwZWNYrzCjggJ7MIICdzAfBgNVHSMEGDAWgBQf5s/Uj8UiKpdKKYoV5xbJkjTEtjAdBgNVHQ4EFgQU2q8YnzPs3rS+SndwB4WIZ3Q9rAswCwYDVR0PBAQDAgbAMIGbBgNVHR8EgZMwgZAwX6BdoFukWTBXMQswCQYDVQQGDAJDTjENMAsGA1UECgwEQkpDQTENMAsGA1UECwwEQkpDQTEXMBUGA1UEAwwOQmVpamluZyBTTTIgQ0ExETAPBgNVBAMTCGNhMjFjcmwxMC2gK6AphidodHRwOi8vY3JsLmJqY2Eub3JnLmNuL2NybC9jYTIxY3JsMS5jcmwwGQYKKoEchu8yAgEBAQQLDAlKSlRFU1QwMDIwYAYIKwYBBQUHAQEEVDBSMCMGCCsGAQUFBzABhhdPQ1NQOi8vb2NzcC5iamNhLm9yZy5jbjArBggrBgEFBQcwAoYfaHR0cDovL2NybC5iamNhLm9yZy5jbi9jYWlzc3VlcjCBiQYDVR0gBIGBMH8wMAYDVR0gMCkwJwYIKwYBBQUHAgEWGyBodHRwOi8vd3d3LmJqY2Eub3JnLmNuL2NwczBLBgNVHSAwRDBCBggrBgEFBQcCARY2aHR0cDovL3d3dy5iamNhLm9yZy5jbi9zaXRlcy9kZWZhdWx0L2ZpbGVzL3NtMi1jcHMucGRmMBEGCWCGSAGG+EIBAQQEAwIA/zAXBgoqgRyG7zICAQEIBAkMB1RFU1QwMDIwGQYKKoEchu8yAgECAgQLDAlKSlRFU1QwMDIwHwYKKoEchu8yAgEBDgQRDA8xMDIwMDAwMDAwMDAwNzcwGQYKKoEchu8yAgEBBAQLDAlKSlRFU1QwMDIwCgYIKoEcz1UBg3UDRwAwRAIgCOM85UU7E2FjT5oddOxP/iEbbDd9skH4QdjHWG+Nm1gCIChXTmCbXcXbSlLNgdjxDl/JFNzXF8RMyr3Y+R2KkVk1\" >
   </OBJECT>")

(defn ca
  "BJCA test，win下需要配置文件：%USERPROFILE%\\BJCAROOT\\SVSClient.properties "
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM")
        strServerCert (.getServerCertificate sed)
        strRandom (.genRandom sed 24)
        sr (session! request "Random" strRandom) ]
    (html-body
      {:class "login_body"}
      [:img {:src "/img/idp-webfirst.png" :style "position: absolute; top: 50%; left: 50%; margin-top: -80px; margin-left: -110px;"}]
      [:form {:method "post" :ID "LoginForm" :name "LoginForm" :action "/c/espreg/ca-submit" :class "login_form ui-corner-all"
              :onsubmit "return LoginForm_onsubmit()"}
       [:center [:div {:style "color:yellow;margin-bottom:9px"} "提示：请先将系统专用UKey证书插入计算机USB口。"]]
       "选择证书：" [:select {:id "UserList" :name "UserList" :style "width:200px" :class "ui-corner-all"} ""] [:br]
       "选择口令：" [:input {:id "UserPwd" :name "pwd1" :type "password" :size 16 :maxlength 16 :class "ui-corner-all"}] [:br]
       [:p {:align "center" :style "padding:6px"}
        [:input {:type "submit" :value " 提 交 " :class "ui-state-default ui-corner-all" :style "font-weight:bold;color:green"}]]
       [:center [:img {:alt "nasoft" :src "/img/nasoft-rnd.png"}]]
       [:input {:type "hidden" :ID "UserSignedData" :name "UserSignedData"}]
       [:input {:type "hidden" :ID "UserCert" :name "UserCert"}]
       [:input {:type "hidden" :ID "ContainerName" :name "ContainerName"}]
       [:input {:type "hidden" :ID "strRandom" :name "strRandom"}] ]
      [:script "document.title='标准化系统证书登录' "]
      activex)))

(defn- check1
  "验证方法1：ca服务器验证后外部注册数据库验证"
  [request]
  (let [vars (query-vars2 request)
        ;<bjca>
        sed (SecurityEngineDeal/getInstance "SM")
        clientCert (:UserCert vars)
        UserSignedData (:UserSignedData vars)
        certPub (.getCertInfo sed clientCert 8)
        ranStr (:strRandom vars)
        retValue (.validateCert sed clientCert)
        uniqueIdStr (.getCertInfo sed clientCert 17)
        uniqueId (.getCertInfoByOid sed clientCert "1.2.156.112562.2.1.1.1") ; JJ638610457 
        signedByte (.base64Decode sed UserSignedData)
        rt (.verifySignedData sed clientCert (.getBytes ranStr) signedByte) ; 认证结果
        ;</bjca>
        PaperType (subs uniqueId 0 2)
        PaperID (let [s (subs uniqueId 2)] (if (= PaperType "JJ") (str (subs s 0 8) "-" (subs s 8)) s ))
        wr3url (session request "wr3url")
        rs (select-row "esp" (format "SELECT PaperID,CommonName,usertype FROM userregister where PaperType='%s' and PaperID='%s' " 
                     PaperType PaperID)) ]
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
  (let [vars (query-vars2 request)
        ;<bjca>
        sed (SecurityEngineDeal/getInstance "SM")
        clientCert (:UserCert vars)
        UserSignedData (:UserSignedData vars)
        certPub (.getCertInfo sed clientCert 8)
        ranStr (:strRandom vars)
        retValue (.validateCert sed clientCert)
        uniqueIdStr (.getCertInfo sed clientCert 17)
        uniqueId (.getCertInfoByOid sed clientCert "1.2.156.112562.2.1.1.1") ; JJ638610457 
        signedByte (.base64Decode sed UserSignedData)
        rt (.verifySignedData sed clientCert (.getBytes ranStr) signedByte) ; 认证结果
        ;</bjca>
        PaperType (subs uniqueId 0 2) ; 'JJ' 'SF'
        PaperID (let [s (subs uniqueId 2)] (if (= PaperType "JJ") (str (subs s 0 8) "-" (subs s 8)) s )) ; pid
        wr3url (session request "wr3url")
        rs (with-mdb2 "esp" (fetch-one :user :where {:pid PaperID}))]
    (html
      (cond
        (not rt) "证书认证失败"
        (not rs) "您所用的可能非【交通运输企业安全生产标准化系统】专用证书。"
        :else (do (session! request "wr3user" (str (:role rs) "-" PaperID))
                (session! request "wr3role" (:role rs))
                (html (format "%s（%s）认证成功！" (:name rs) PaperID)
                      [:script (format "window.location.href='%s' " wr3url)])) ) )))

(defn- check3
  "验证方法3：不使用ca服务器直接读取ukey中的pid信息，esp数据库验证"
  [request]
  (let [vars (query-vars2 request)
        clientCert (:UserCert vars)
        UserSignedData (:UserSignedData vars)
        ranStr (:strRandom vars)
        dec (Stringx/base64dec clientCert)
        idx (.indexOf dec "\2\1\1\1")
        PaperType (subs dec (+ idx 8) (+ idx 10))
        PaperID (if (= PaperType "JJ") 
                  (str (subs dec (+ idx 10) (+ idx 18)) "-" (subs dec (+ idx 18) (+ idx 19) )) 
                  (subs dec (+ idx 10) (+ idx 28)))
        wr3url (session request "wr3url")
        rs (with-mdb2 "esp" (fetch-one :user :where {:pid PaperID})) ]
    (html
      (if (not rs) "您所用的可能非【交通运输企业安全生产标准化系统】专用证书。"
        (do (session! request "wr3user" (str (:role rs) "-" PaperID))
          (session! request "wr3role" (:role rs))
          (html (format "%s（%s）认证成功！" (:name rs) PaperID)
                [:script (format "window.location.href='%s' " wr3url)])) ))))

(defn ca-submit
  "BJCA 插入证书密码提交后认证"
  [request]
  (check1 request))

(defn who
  "service: 返回session中名为'wr3user'的当前已登录用户的json对象{:uid .. :name .. :roles ..}，未登录则返回'null' "
  [request]
  (if-let [uid (wr3user request)]
    (let [user (with-mdb2 "esp" (fetch-one :user :where {:uid uid}))]
      (json-str {:uid uid :name (:name user) :roles (:role user)}))
    "null"))

; 00359131-X 00002106-3
;(import wr3.bank.OrgID)
;(dotimes [i 50]
;  (let [s8 (format "%08d" (random 99999999))]
;  (println (OrgID/toid s8))))
;(OrgID/toid "00359131") ; 34448380-;
;(OrgID/isid "61032343-X")
