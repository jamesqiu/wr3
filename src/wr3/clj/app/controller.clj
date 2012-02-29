;;;;------------------------------------
;;;; 缺省的 CljServlet 控制器，http://server/wr3/clj会调用 index 方法
;;;; 用法说明：
;;;;   - 公共函数可以被CljServlet直接调用，注意名字不要和其他namespace冲突；
;;;;   - 函数必须有返回值，否则CljServlet会提示返回nil；
;;;;   - index方法为缺省调用的方法；
;;;;   - 如果要读取request（或者session）和id，可使函数带1个参数，形如：
;;;;     [request id] 或 [request] 或 [id]
;;;;
;;;;------------------------------------
(ns wr3.clj.app.controller)

(use 'wr3.clj.chart 'wr3.clj.s 'wr3.clj.n 'wr3.clj.web)

(defn index
  "controller的缺省函数。 可由4种url访问到： ./c  ./c/-  ./c/-/index  ./c/controller/index "
  []
  (str
    "controller的缺省函数。 可由4种url访问到： <br/>"
    "<ul><li>./c  <li>./c/-  <li>./c/-/index  <li>./c/controller/index </ul><br/>"
    "webapp='" webapp "'"
    "<h2 align=center>Hello IDP-WebFirst</h2>"))

(defn- fac [n] (apply * (range 1 (inc n))))
(defn- facs [n]
  (apply str (map #(str % "!=" (fac %) "<br/>") (range 1 n)))
)

(defn date
  "日期测试。可用2种url访问到：./clj/-/date  ./clj/controller/date"
  []
  (html-head
    "<style>body {font-family: verdana}</style>"
    (.toLocaleString (java.util.Date.))
    "<br/>cn中文<br/>" (apply str (repeat 50 "."))
    "<br/>" (facs 31)))

(defn fibs
  "Fib数列和随机数图形"
  []
  (html-head (format "<h2 align=center style='font-family:微软雅黑'>%s</h2>" (:doc (meta fibs)))
    (format "<img src=\"%s/chart?%s\" /><br/>" webapp (uri bar (take 12 (fib 1 2))))
    (format "<img src=\"%s/chart?width=1000&%s\" /><br/>" webapp (uri line (random-n 100 10)))
    ))

(defn sets
  "传入含destruct参数request和id的1个Map, 进行session设置"
  [request  id]
;  (println "sets() request=" (.getClass request))
  (session! request "k1" id)
  (str "fss() k1=" (session request "k1")))

(defn gets
  "传入含destruct参数request（也可带id）的1个Map, 进行session读取"
  [request]
  (session request "k1"))

(defn- fdoc [fname] ; fname 参数是一个MapEntry, key是名称，value是函数指针
  (let [d1 (:doc (meta (val fname)))
        d2 (if (nil? d1) "无文档" d1)]
    (format "%s 函数:<br/>-- %s" (key fname) d2)))

(defn docs
  "得到所有action的doc说明"
  []
  (html-body 
    nil
    (join (map #(fdoc %) (ns-publics 'wr3.clj.controller)) "<br/><br/>")))

(defn vars
  "得到request的所有vars map"
  [request]
  (html-body 
    nil
    (query-vars request)))

(defn- fs [s] (let [n 25] (if (> (count s) n) (str (subs s 0 n) " ..") s)))

(defn- app1 [kv]
  (let [k (key kv)
        v0 (:doc (meta (val kv)))
        v (if (nil? v0) "无文档" v0)]
    (format "<h1><a href=\"%s\">%s</a></h1><p title=\"%s\">%s</p>" k k v (fs v))))

(defn- app2 []
  (map #(app1 %) (sort (ns-publics 'wr3.clj.controller))))

(defn apps
  "测试多了应用列表"
  []
  (html-apps (concat (app2) ["<h1>cn中文</h1><p>北京清华紫光北美科技开发有限责任公司</p>"])))

(defn f1 [] "f1() in source cn中文")
(defn f2 [] "f2()...保存即可，不用编译..")
;(fss {:request nil :id 108})





