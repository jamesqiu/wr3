(ns ^{:doc "
  wr3的系统级公共service，完成登录校验，勿删！仅控制应用服务器session范围内的东西。
  request传入的参数一般为 ?uid=foo&pwd=bar
  session中写入的值为：wr3user, wr3role（逗号分隔的角色字符串）, wr3action（登录页面提交验证的action）, wr3url（登录后前往的页面）
  "   
      :author "jamesqiu"}
  wr3.clj.app.auth)

(use 'wr3.clj.web)
(use 'hiccup.core)
(use 'clojure.contrib.json)

(def users 
  {"admin" {:name "管理员" :pwd "0fbce6c74ff376d18cb352e7fdc6273b" :roles "root,user"} ; "9***" 
   "user" {:name "用户" :pwd "1a1dc91c907325c69271ddf0c944bc72" :roles "user"} ; "pass"        
   "guest" {:name "访客" :pwd "098f6bcd4621d373cade4e832627b4f6" :roles "guest"} ; "test"
   ; esp 交通运输企业安全生产标准化管理系统需要的用户，密码空格
   "pn1" {:name "考评员1" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "pn"}
   "pn2" {:name "考评员2" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "pn"}
   "en1" {:name "企业1" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "en"}
   "en2" {:name "企业2" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "en"}
   "org1" {:name "考评机构1" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "org"}
   "org2" {:name "考评机构2" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "org"}
   "mot1" {:name "交通部用户1" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "mot"}
   "mot2" {:name "交通部用户2" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "mot"}
   "pot1" {:name "交通厅用户1" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "pot"}
   "pot2" {:name "交通委用户2" :pwd "7215ee9c7d9dc229d2921a40e899ec5f" :roles "pot"}
   })

(defn- ok?
  "用户名密码不对返回nil，否则返回用户名和角色"
  [uid pwd]
  (when-let [user (get users uid)]
    (when (= (:pwd user) (wr3.util.Stringx/md5 pwd)) 
      (dissoc user :pwd))))

(require 'wr3.clj.app.espreg)
(defn login
  "service: 提供用户名和密码验证服务.
  成功则在session中写入用户名，roles等，并返回如：{:name '管理员', :roles 'root,user', :url '前往的页面url'}
  失败则返回'null'。"
  [id ids request uid pwd]
  (let [uid (or uid id)
        pwd (or pwd (second ids))
        wr3url (session request "wr3url")
        f-check (if (.startsWith wr3url "/c/esp/") wr3.clj.app.espreg/check0 ok?)]
    (if-let [rt (f-check uid pwd)]
      (do 
        (session! request "wr3user" uid)
        (session! request "wr3role" (rt :roles))
        (println "-- wr3url:" wr3url)
        (json-str (into {:url wr3url :uid uid} rt)))
      "null")))

(defn logout
  "service: 注销当前用户，返回信息字符串"
  [request]
  (if-let [uid (wr3user request)]
    (do
      (session-close request)
      (str "已经将用户" uid "注销."))  
    "用户尚未登录.")) 

(defn who
  "service: 返回session中名为'wr3user'的当前已登录用户的json对象{:uid .. :name .. :roles ..}，未登录则返回'null' "
  [request]
  (if-let [uid (wr3user request)]
    (let [user (users uid)]
      (json-str (into {:uid uid} (select-keys user [:name :roles]))))
    "null"))

(defn url
  "service: 返回session的wr3url中记录的登录后前往的页面url，没有则返回'' "
  [request]
  (or (session request "wr3url") ""))
  
(defn index
  "app: 使用方法"
  []
  (let [fs [login who logout url]
        docs (map #(select-keys (meta %) [:name :doc]) fs)]
    (html-body
      (map #(html [:div (:name %) ": " [:br] "--- " (:doc %)]) docs))
    ))
;(ok? "user" "pass")
    
