;;;;--------------------------------------------------
;;;; 可执行其他.clj文件函数的servlet，在web.xml中配置
;;;; TODO:
;;;;   支持xml、json、bin, 可以通过init-parameter在web.xml中进行分别配置：
;;;;   已临时解决xml: queryString只带 ?xml=utf-8 即可

;;;;--------------------------------------------------
(ns wr3.clj.CljServlet
  (:gen-class 
    :extends javax.servlet.http.HttpServlet))

(import java.io.PrintWriter)
(import wr3.BaseConfig)
(use '[wr3.clj.u :only (fcall fget fargs fargc ?)])

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
  "让app可以自行binding如下变量：id：第一个id值，ids：所有id值的map，request中各值：todo"
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

;; 处理形如 http://server:8080/wr3/clj/wr3.clj.controller1/action2?k1=v1 的url
(defn -doGet
  [this request response]
  ; jamesqiu (2011-7-16): 增加对输出xml的支持，即形如 http://server/c/chartf/chinamap2?$xml=gbk 的返回编码为gbk的xml
  (let [query-string (.getQueryString request) ; 可能为 nil "k1=v1&k2=v2" 等
        xml? (and query-string (.startsWith query-string "$xml="))
        xml-encoding (when xml? (.substring query-string 5))] ; 5 == (count "$xml=")
    (.setContentType response (if xml? (str "text/xml; charset=" xml-encoding)
                                "text/html; charset=utf-8"))
    
    (let [path-info (.getPathInfo request) ; 可能为 nil "/" "/wr3.clj.c1/a2/" 等
          ns-fn-id  (get-ns-fn-id path-info) 
          ns        (:ns ns-fn-id)
          fn        (:fn ns-fn-id)
          ids       (:id ns-fn-id)
          out       (.getWriter response)
          rt        (if (zero? (fargc ns fn)) 
                      (fcall ns fn)
                      (fcall-binding-params ns fn (make-binding-params request ids))
                      ; (fcall ns fn {:request request :id id})
                      )
          ]  ; todo：根据(meta (var foo))得到的参数名，绑定id，ids，request，request参数, 接下：
      ; 如：app写 (defn app1 [id k1 request] ...) 直接 (fcall ns fn id k1 request), 没有找到同名的直接赋值为nil
      (.println out (? rt "fcall return nil")) )))

;; 下面的方法写不写皆可
(defn -doPost 
  [_ request response]
  (-doGet nil request response))
