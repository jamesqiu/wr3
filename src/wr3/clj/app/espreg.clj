(ns wr3.clj.app.espreg)

(use 'wr3.clj.web 'wr3.clj.n 'wr3.clj.s 'wr3.clj.u)
(import cn.org.bjca.client.security.SecurityEngineDeal)

(defn index
  ""
  []
  (let [cfg [["考评员个人" "pn"] ["考评机构、企业单位" "en"]]]
  (html-body
    (repeat 5 [:br])
    (for [[k v] cfg]
      [:center [:h1 [:a {:href (format "/c/espreg/%s" v) :style "font-family:微软雅黑;font-size:21pt"} 
                     (format "【%s】注册申请" k)]]]))))

(def cfg-pn
  [["单位名称" "name" {:width}]
   ])

(defn pn
  "个人注册信息"
  []
  (let [cfg [["姓名" :name {:require true}]
             ["身份证" :pid]
             ["手机号" :mobile]
             ["地址" :address]
             ["证件上传" :pfile {:t 'file}]]]
    (html-body
      (input-form cfg {:title "考评员个人信息注册"
                       :buttons [:div (eui-button {} "保存") (space 5)
                                      (eui-button {} "取消")]})
      (fileupload-dialog))))

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
  "BJCA test"
  [request]
  (let [sed (SecurityEngineDeal/getInstance "SM")
        strServerCert (.getServerCertificate sed)
        strRandom (.genRandom sed 24)
        sr (session! request "Random" strRandom) ]
    (html-body
      [:h1 "strRandom(session:Random)=" strRandom]
      [:form {:method "post" :ID "LoginForm" :name "LoginForm" :action "/c/espreg/ca-submit"
              :onsubmit "return LoginForm_onsubmit()"}
       [:table
        [:tr [:td "选择证书"] [:td [:select {:id "UserList" :name "UserList" :style "width:200px"}]]]
        [:tr [:td "选择口令"] [:td [:input {:id "UserPwd" :name "pwd1" :type "password" :size 16 :maxlength 16}]]]
        [:tr [:td {:colspan 2} [:input {:type "submit" :value "提交"}]]] ]
       [:input {:type "hidden" :ID "UserSignedData" :name "UserSignedData"}]
       [:input {:type "hidden" :ID "UserCert" :name "UserCert"}]
       [:input {:type "hidden" :ID "ContainerName" :name "ContainerName"}]
       [:input {:type "hidden" :ID "strRandom" :name "strRandom"}] ]
      activex)))

(def dd-retValue
  {
   -1 "登录证书的根不被信任"
   -2 "登录证书超过有效期"
   -3 "登录证书为作废证书" 
   -4 "登录证书被临时冻结"
   })
 
(defn ca-submit
  "BJCA 插入证书密码提交后认证"
  [request]
  (let [vars (query-vars2 request)
        sed (SecurityEngineDeal/getInstance "SM")
        clientCert (:UserCert vars)
        UserSignedData (:UserSignedData vars)
        certPub (.getCertInfo sed clientCert 8)
        ranStr (:strRandom vars)
        retValue (.validateCert sed clientCert)
        uniqueIdStr (.getCertInfo sed clientCert 17)
        uniqueId (.getCertInfoByOid sed clientCert "2.16.840.1.113732.2")
        signedByte (.base64Decode sed UserSignedData)
        rt (.verifySignedData sed clientCert (.getBytes ranStr) signedByte) ; 认证结果
        ]
    (html-body
      (str vars)[:hr]
      (str (replace-all (debug-str sed clientCert UserSignedData certPub ranStr retValue uniqueIdStr uniqueId signedByte rt) 
                        "\n" "<br/>")) )))

;(defmacro and
;  ([] true)
;  ([x] x)
;  ([x & next]
;   `(let [and# ~x]
;      (if and# (and ~@next) and#))))

(def v1 10)
(def v2 20)
(def v3 30)
(print (let [v1 100 v2 200 v3 300] (debug-str v1 v2 v3)))
