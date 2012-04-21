(ns ^{:doc "
  可执行其他.clj文件函数的servlet，在web.xml中配置:
   1.支持xml、json、bin, 可以参考web.xml中mimi-type的配置，queryString格式如下：
     ?mime-type=text/xml&charset=gbk （不指定则缺省为text/html, utf-8）
   2.app应用如esp.clj使用命名空间中的auth函数来进行权限控制，
     auth函数必须3个以上参数，第一个参数会传入request，第二个参数会传入fname（可选），第三个参数会传入ids；
     如果根本不想使用这些参数，可以直接带一个 & args 避免调用出错，或者用1～2个参数，最后一个用 & args
     返回true表示通过，继续输出页面；返回 false 等则表示验证没有通过，跳转到/login.html;
   3.类似Play!，自动绑定querystring中的值和app/*.clj函数中的同名参数，例如 clj1/foo/01/012?name=james&age=30 对应函数为
     (defn foo [name age] ...) 
       -- foo函数自动获取到name=james, age=30这两个值；也可以有3个自动绑定的名称：
     (defn foo [id ids request name age] ...) 
       -- foo函数自动获取到 id=01, ids='(01 012), request=request, name=james, age=30
      "}
  wr3.clj.CljServlet
  (:gen-class 
    :extends javax.servlet.http.HttpServlet))

(import java.io.PrintWriter)
(import wr3.BaseConfig)
(use '[wr3.clj.u :only (fcall require+ fget fargs fargc ? app-timestamp)])

;; 缺省的 namespace 及函数名, "wr3.clj.app.controller/index"
(def ns0 "wr3.clj.app.")
(def ns1 "controller")
(def fn0 "index") 

;; 如果 namespace 没有包名前缀，添加 ns0。如 controller1 -> wr3.clj.app.controller1
;; 如果 namespace 为-则用缺省ns。如 "-" -> wr3.clj.app.controller 
(defn- add-ns-prefix
  [ns]
  (if (= ns "-") (str ns0 ns1)
    (if (== (.indexOf ns ".") -1) (str ns0 ns) ns)))

;; 从path-info中得到namespace和函数名的Map形如:
;;   {:ns ns1 :fn fn1},
;; 或者还带id形如：
;;   {:ns "wr3.clj.app.controller" :fn "index :id '(1003 104)}, 
;;   其中id 是nil或者一个list
;; 例子:
;;   "/wr3.clj.app.c1/action2" -> {:ns "wr3.clj.app.c1" :fn "action2"}
;;   "/wr3.clj.app.c1"         -> {:ns "wr3.clj.app.c1" :fn "index"}
;;   ""                        -> {:ns "wr3.clj.app.controller" :fn "index"} 
;;   "/c2/m2/1003"             -> {:ns "wr3.clj.app.c2" :fn "m2" :id '(1003)}
(defn- get-ns-fn-id
  "path-info: url路径字符串;
  返回一个map含：ns: namespace字符串，fn: funcname字符串，id: 含所有id的map"
  [path-info]
  (let [ss (seq (.split (str path-info) "/"))
        n (count ss)]
    (if (<= n 1) {:ns (str ns0 ns1) :fn fn0} ; ("")
      (let [s1 (add-ns-prefix (second ss))]
        (if (== n 2) {:ns s1 :fn fn0}        ; ("" "controller1")
          {:ns s1 :fn (nth ss 2) :id (next (nnext ss))} )))))      ; ("" "-" "action2" "id1" "1003")

(defn- request-to-map
  "获得request的queryString中的key-value，以Map形式返回"
  [request]
  (if (nil? request) {}
    (let [m (.getParameterMap request)]
      (zipmap (keys m) (map #(apply str (interpose "&" %)) (vals m)))))) ; ?k1=a&k1=b 变为{"k1" "a&b"}

(defn- make-binding-params
  "让app可以自行binding如下变量：
  id：第一个id值；
  ids：所有id值的list；
  request：request对象；
  querystring中各值：在app/*.clj中函数定义的其他参数名字都可以自动绑定"
  [request ids]
  (merge {"id" (first ids) "ids" ids "request" request} (request-to-map request)))
  
(defn- fcall-binding-params 
  "把params自动绑定到同名的fname函数参数中， 例如：(binding-params 'fp3' {:p1 10 :pa 20 :pb 30 :g +})"
  [ns fname params]
  (let [arg-names (fargs ns fname)
        arg-values (map #(params (name %)) arg-names)]
;    (println "--- debug CljServlet.clj/fcall-binding-params: " params " | " arg-names " | " arg-values)
    (apply (fget ns fname) arg-values)))

;;;---------------- 以下为 HttpServlet 方法

(defn -init 
  "一次初始化"
  [this super]
  (let [context (.getServletContext super)
        wr3home (.getRealPath context "")
        webapp (.getContextPath context)]
    ; 不依赖AppFilter.init()，通过自行实现来设置contextPath，之后web app要用到webapp
    (when (nil? (System/getProperty BaseConfig/WR3HOME)) 
      (System/setProperty BaseConfig/WR3HOME wr3home))
    (when (nil? (BaseConfig/webapp)) 
      (BaseConfig/set BaseConfig/CONTEXT_PATH webapp)) 
    (println (format "--- CljServlet init: [%s/c/] ---" webapp))))

(import wr3.util.Stringx)

(def not-reload (= "0" (System/getProperty "wr3.clj.reload")))

(defn- need-reload?
  "jvm的设置-Dwr3.clj.reload=0优先，设为0则始终不reload；不设为0或者没有设置时：
  context中没有记录过时间         ， 不需要reload，返回false，并setAttribute；
  context中已经有时间并且和最新符合，不需要reload，返回false；
  context中已经有时间并且和最新不符，  需要reload，返回true， 并setAttribute"
  [context ns]
  (if not-reload false
    (let [t1 (.getAttribute context ns) 
          t2 (app-timestamp ns) ]
      (cond
        (nil? t1) (do (.setAttribute context ns t2) false)
        (= t1 t2) false
        :else (do (.setAttribute context ns t2) true)))))

;; 处理形如 http://server:8080/wr3/clj/wr3.clj.controller1/action2?k1=v1 的url
(defn -doGet
  [this request response]
  (let [context (.getServletContext request) ; setAttribute(str, str), getAttribute(str)
        ; 获取content-type和charset
        params (.getParameterMap request)
        content-type (Stringx/s (Stringx/join (get params "content-type") "&") "text/html")
        charset (Stringx/s (Stringx/join (get params "charset") "&") "utf-8")
        ; 执行函数
        path-info (.getPathInfo request) ; 可能为 nil "/" "/wr3.clj.c1/a2/" 等
        ns-fn-id  (get-ns-fn-id path-info) 
        ns (:ns ns-fn-id)
        fn (:fn ns-fn-id)
        ids (:id ns-fn-id)
        ; 判断能否有权限执行本函数fn，无auth函数视为有权限
        need-reload (require+ ns (need-reload? context ns))
        auth (if-let [f (and (not= fn "auth") (fget ns "auth"))] 
               (f request fn ids) 
               true) 
        rt (when (true? auth)
             (if (zero? (fargc ns fn)) 
               (fcall ns fn)
               ; (fcall ns fn {:request request :id id})
               (fcall-binding-params ns fn (make-binding-params request ids))))
        ]
;    (println "-- debug: auth =" auth " fn =" fn)
    ; jamesqiu (2012-2-25) 支持输出多客户：http://server/c/chartf/chinamap2?content-type=text/xml&charset=gbk
    (.setContentType response (format "%s; charset=%s" content-type charset))    
    (if (true? auth) 
      ; 如：app写 (defn app1 [id k1 request] ...) 直接 (fcall ns fn id k1 request), 没有找到同名的直接赋值为nil
      (-> response .getWriter
        (.println (? rt "fcall return nil")))
      (do
;        (println "-- debug: path-info =" (format "%s/c%s" (BaseConfig/webapp) path-info))
        (-> request .getSession
          (.setAttribute "wr3url" (format "%s/c%s" (BaseConfig/webapp) path-info)))
        (.sendRedirect response "/login.html"))
      )))

;; 下面的方法写不写皆可
(defn -doPost 
  [this request response]
  (-doGet this request response))
