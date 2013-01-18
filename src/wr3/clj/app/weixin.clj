(ns ^{:doc "对微信公众号qiu_jj的响应"} wr3.clj.app.weixin)

(use 'wr3.clj.web)
(use 'clojure.contrib.json 'wr3.clj.s 'wr3.clj.u 'wr3.clj.n)

(require 'wr3.clj.datagen)
(require '[wr3.clj.nosql :as mdb])
(require '[somnium.congomongo :as mongo])

(def help (str "请敲入我认识的指令：\n"
               "say —— 看箴言学英文。\n"
               "pid —— 和随机机器人打招呼。\n"
               "oid —— 组织机构代码。\n"
               "诗 —— 吟唐宋古诗一首 \n"
               "图 —— 查看风景美图 \n"
               "美女 —— 查看美女图片 \n"
               "去某某地 —— 看“去”字后面地方比如：去天安门 \n"
               "译中英文 —— 翻译中英文单词句子如：译举头望明月 1merry y没事偷着乐"
               ))

(def webapp-dir "d:/data/dev3/webapp/")
(def cygwin-dir "d:/soft/cygwin/bin/")
(def fortune (str cygwin-dir "fortune.exe"))

(defn- pid-string-
  [pid]
  (let [[area birth sex] (wr3.bank.IDUtil/infos pid)
        age (- 2013 (to-int (left birth "-")))
        call (case sex 
               "男" (cond (< age 10) "小帅哥" (< age 30) "小弟" (< age 40) "大哥" (< age 60) "老大哥" (< age 80) "大叔" :else "老伯")
               "女" (cond (< age 10) "小美女" (< age 30) "小妹" (< age 40) "大姐" (< age 60) "老大姐" (< age 80) "阿姨" :else "阿婆"))
        people (wr3.clj.datagen/rand-peoples)
        nam (wr3.clj.datagen/rand-name)]
    (format "机器人 /:8-) \n%s\n%s %s族%s\n%s 向您问好！" pid area people call nam)))
  
(defn- reply-
  "针对发给qiu_jj微博的text类型文字进行回应"
  [s]
  (cond
    (in? s ["hi" "hello" "你好" "您好" "早上好" "嗨" "喂"]) "您好！ /::) "
    (= s "pid") (pid-string- (wr3.clj.datagen/rand-pid))
    (= s "oid") (wr3.clj.datagen/rand-orgid)
    (= s "say") (wr3.util.Exec/exec fortune)
    (in? s ["poem" "shi" "诗" "詩"]) (wr3.util.Exec/exec (str fortune " 75% tang300b 25% song100b"))
    (.endsWith s "=") (wr3.util.Exec/exec (format "%sperl -e \"print(%s)\" " 
                                                  cygwin-dir (subs+ s 0 -2))) ; 不公开
    :else (format "/::-| 不认识：%s \n%s" s help)))

; 回应的text的模板
(def template-text (str "<xml>"
                        " <ToUserName><![CDATA[%s]]></ToUserName>"
                        " <FromUserName><![CDATA[%s]]></FromUserName>"
                        " <CreateTime>%s</CreateTime>"
                        " <MsgType><![CDATA[text]]></MsgType>"
                        " <Content><![CDATA[%s]]></Content>"
                        " <FuncFlag>0</FuncFlag>"
                        " </xml>"))
; 回应的news（图文消息）的模板
(def template-news (str "<xml>"
                        " <ToUserName><![CDATA[%s]]></ToUserName>"
                        " <FromUserName><![CDATA[%s]]></FromUserName>"
                        " <CreateTime>%s</CreateTime>"
                        " <MsgType><![CDATA[news]]></MsgType>"
                        " <Content><![CDATA[]]></Content>"
                        " <ArticleCount>1</ArticleCount>"
                        " <Articles>"
                        " <item>"
                        " <Title><![CDATA[%s]]></Title>"
                        " <Description><![CDATA[%s]]></Description>"
                        " <PicUrl><![CDATA[%s]]></PicUrl>"
                        " <Url><![CDATA[%s]]></Url>"
                        " </item>"
                        " </Articles>"
                        " <FuncFlag>0</FuncFlag>"
                        " </xml>"))

(defn- xml-text
  "根据模板生成text类型的回应消息xml
  @m 请求消息的hash-map
  @text 回应的内容，注意控制字节数不要超过2048 "
  [m text]
  (format template-text (:FromUserName m) (:ToUserName m) (int (/ (System/currentTimeMillis) 1000)) text))

(defn- xml-news
  "根据模板生成text类型的回应消息xml
  @m 请求消息的hash-map
  @title 回应标题; @text 回应内容; @pic-url 回应封面图url; @url 全文url "
  [m {title :title text :text pic-url :pic-url url :url}]
  (format template-news (:FromUserName m) (:ToUserName m) (int (/ (System/currentTimeMillis) 1000))
          title text pic-url url))

(def pics (filter #(.endsWith % ".jpg") 
                  (map #(.getName %) (file-seq (java.io.File. (str webapp-dir "/wx/s/"))))))

(def fname-map {"s20.jpg" "王珣伯远帖(屏高).jpg", "s9.jpg" "在树上.jpg", "s8.jpg" "光与雾.jpg", 
                "s7.jpg" "元阳梯田-雾.jpg", "s6.jpg" "元阳梯田-绿.jpg", "s5.jpg" "元阳梯田-晨.jpg", 
                "s4.jpg" "元阳梯田-云.jpg", "s3.jpg" "云南-香格里拉-普达措国家公园.jpg", 
                "s2.jpg" "云南-迪庆-白马雪山.jpg", "s1.jpg" "云南-德钦-卡瓦格博.jpg", "s0.jpg" "s", 
                "s19.jpg" "王献之东山帖(屏高).jpg", "s18.jpg" "森林雨雾.jpg", "s17.jpg" "棕林5.jpg", 
                "s27.jpg" "青海-玛多-扎陵湖.jpg", "s16.jpg" "树与藤.jpg", "s26.jpg" "雾中.jpg", 
                "s15.jpg" "林荫道.jpg", "s25.jpg" "雨后阳光.jpg", "s14.jpg" "新疆-阿勒泰-喀纳斯湖.jpg", 
                "s24.jpg" "贵州-安顺-黄果树瀑布.jpg", "s13.jpg" "新疆-伊犁-赛里木湖畔.jpg", 
                "s23.jpg" "苏东坡寒食2(屏宽).jpg", "s12.jpg" "广西-桂林-遇龙河.jpg", "s22.jpg" "苏东坡寒食1(屏宽).jpg", 
                "s11.jpg" "宁夏-中卫-腾格里沙漠.jpg", "s21.jpg" "王羲之兰亭序(屏高).jpg", "s10.jpg" "大围山森林.jpg"})
  
;(let [ss (map-indexed (fn [i e] [(str "\"s" i ".jpg\"") (str "\"" (.getName e) "\"")]) (file-seq (java.io.File. "f:/wx/s/")))]
;  (println (into {} ss))
;  (doseq [[f2 f1] ss] (println "mv" f1 f2)))

(defn- reply-pic-
  [m]
  (let [pic (rand-nth pics)       
        fname (fname-map pic)
        pic-url (format "http://gotoreal.com/wx/s/%s" pic)
        url (format "http://gotoreal.com/wx/%s" fname)]
    (xml-news m {:title (left fname ".jpg") :text (format "请欣赏图片[%s]\n点击可打开查看大图。" fname)
                 :pic-url pic-url :url url}) ))

(defn- reply-map-
  [m s]
  (let [loc (subs s 1)
        loc2 (java.net.URLEncoder/encode loc "UTF-8") ; 中文编码
        pic-url (format "http://ditu.google.cn/maps/api/staticmap?center=%s&size=300x300&zoom=13&markers=%s&sensor=false" loc2 loc2)
        url (format "http://ditu.google.cn/maps/api/staticmap?center=%s&size=600x600&zoom=15&markers=%s&sensor=false" loc loc)]
    (xml-news m {:title (str "前往：" loc) :text "请点击打开地图。" 
                 :pic-url pic-url :url url} )) )

; 使用youdao的中文翻译api
(def trans-service (str "http://fanyi.youdao.com/openapi.do?keyfrom=gotoreal&key=2018479743"
                        "&type=data&doctype=json&version=1.1&q="))

(require '[clojure.contrib.json :as json])

(defn- translate-
  "翻译中英文单词、句子"
  [s]
  (let [q (url-encode (subs s 1))
        rt (json/read-json (slurp (str trans-service q)))
        {query :query trans :translation basic :basic web :web} rt ]
    (str
      query "\n" (join trans "；") "\n\n"
      (when basic (str "基本释义：\n" 
                       (or (:phonetic basic) "【无音标】") "\n"
                       (join (:explains basic) "；") "\n\n"))
      
      (when web (str "网络释义：\n"
                     (join (for [{k :key v :value} web] (str k "：" (join v "；"))) "\n"))) )))

;(println (translate- "译玛雅人坑爹"))

(defn- reply-girl-
  [m s]
  (let [dir (case s "b2" "b2" "b3" "b3" "b1")
        n (case s "b2" (inc (rand-int 61)) "b3" (inc (rand-int 115)) (inc (rand-int 647)))
        pic-url (format "http://gotoreal.com/wx/%s/s/%s.jpg" dir n)
        url (format "http://gotoreal.com/wx/%s/%s.jpg" dir n)]
    (xml-news m {:title "美女图" :text "点击打开可查看大图"
                 :pic-url pic-url :url url})))

(defn- log-
  "@m 如 {:from from :url url} "
  [m]
  (mdb/with-mdb2 "weixin" (mongo/insert! :log (into m {:date (datetime)}))))

(defn- reply-text
  "回应text类型。返回文本或者图文消息"
  [m]
  (let [s (.toLowerCase (.trim (:Content m)))]
    (log- {:type "text" :from (:FromUserName m) :in s})
    (cond
      (= s "hello2bizuser") (xml-text m help)
      (in? s ["图" "圖" "pic"]) (reply-pic- m)
      (.startsWith s "去") (reply-map- m s)
      (in? (first s) [\1 \y \Y \译]) (xml-text m (translate- s))
      (in? s ["美女" "女" "b1" "b2" "b3"]) (reply-girl- m s)
      :else (xml-text m (reply- s)))))

(defn- reply-location
  "回应location类型，显示该地点的baidu地图url"
  [m]
  (let [[x y label] [(:Location_X m) (:Location_Y m) (:Label m)]
        text0 (format "地点：%s\n 坐标：%s,%s\n" label x y)
        url (format (str "<a href=\"http://ditu.google.cn/maps/api/staticmap?"
                         "center=%s,%s&size=450x450&zoom=16&markers=%s,%s&sensor=false\">查看</a>") x y x y)]
    (println (format "location from:%s  to:%s" (:FromUserName m) (:ToUserName m)))
    (log- {:type "location" :from (:FromUserName m) :in url})
    (xml-text m  (str text0 url))))

(defn- reply-image
  "回应image类型，显示该图片的url"
  [m]
  (let [{from :FromUserName to :ToUserName url :PicUrl} m
        text (format "图片链接：<a href=\"%s\">查看</a>" url)]
    (log- {:type "image" :from from :in url})
    (format template-text from to (int (/ (System/currentTimeMillis) 1000))
            text)))
  
;------------- 处理公众号 qiu_jj 的请求
(defn- handle-qiujj
  [m]
  (let [msg-type (:MsgType m)]
    (case msg-type
      "text" (reply-text m)
      "location" (reply-location m)
      "image" (reply-image m)
      (reply-text (assoc m :Content (str "未知类型" msg-type))) )))

;------------- 处理公众号 ems_pub 的请求. todo: 文件上传可用fu.clj的功能函数
(defn- handle-ems
  [m]
  (xml-text m (format "发给公众平台号ems_pub(%s)的信息" (:ToUserName m))))

;------------- 处理公众号 clj-lang 的请求. 函数例子及文档、src
(def clj-ns-fns (mapcat ns-publics ['clojure.core]))
(def clj-fns (sort (keys clj-ns-fns)))
;(println clj-fns)

(defn- reply-clj-
  "给出clojure.core函数名模糊查询，得到列表或者文档+源码"
  [s]
  (let [s (or s "")
        fns (filter #(has? (str %) s) clj-fns)
        rt (cond
             (in? (symbol s) clj-fns) ["--- 文档 ---" 
                                       (let [m (meta (resolve (symbol s)))] (format "%s\n%s\n  %s\n" s (:arglists m) (:doc m)))
                                       "--- 源码 ---" 
                                       (source-fn2 s)]
             (empty? fns) ["请输入clojure.core函数名全称如“reduce”，或部分如："
                           (join (take 30 (shuffle clj-fns)) " ")   
                           "参阅Clojure手册： http://qiujj.com/static/clojure-handbook.html" ]
             :else (take 100 fns))
        rt (join rt "\n") ]
    (format "clojure.core函数[%s]:\n%s\n--- end ---" s rt)))
  
(defn- handle-clj
  [m]
  (let [rt (reply-clj- (:Content m))
        n (count (.getBytes rt))
        rt (if (< n 2048) rt
             (str (subs+ rt 0 (- 2048 100)) (format "\n…… 消息超长： %s>2048字节！" n)))]
    (xml-text m rt)))
  
;------------- 处理没有撰写服务程序的公众号的请求
(defn- handle-unknown
  [m]
  (xml-text m (format "发给公众平台号%s的信息，尚未安排处理程序。" (:ToUserName m))))

(def id-map ; 微信公众平台内部号对应处理函数
  {"gh_bc4960f69978" handle-qiujj
   "gh_59b8f18417ae" handle-ems
   "gh_344792328c76" handle-clj})

(defn index
  "对get返回echostr；对post返回xml"
  [signature timestamp nonce echostr request]
  (if (= 19 (count echostr))
    (do ; get 方法响应
      (comment (println (format "响应微信公众号GET请求：signature=[%s] timestamp=[%s] nonce=[%s] echostr=[%s]" 
                                signature timestamp nonce echostr)))
      echostr)
    (do ; post 方法响应
      (let [len (.getContentLength request)
            is (.getInputStream request)
            buffer (make-array Byte/TYPE len)
            bs (.read is buffer 0 len)
            xml (String. buffer "utf-8")
            dom (clojure.xml/parse (java.io.ByteArrayInputStream. (.getBytes xml "utf-8")))       
            m (into {} (for [{tag :tag content :content} (:content dom)] [tag (first content)]))
            msg-type (:MsgType m) 
            f-handle (or (id-map (:ToUserName m)) handle-unknown)]
        (f-handle m) ))))

;(url-encode "center%3D昆明湖%26width%3D400%26height%3D300%26zoom%3D16%26markers%3D昆明湖")
;  (url-decode "%E6%B8%85%E5%8D%8E")

;--- 读今日录入的所有微信语音信息（原来可以，后来好像被屏蔽了，且mp_sid每次都不一样）
(import [java.net URL URLConnection])
(defn media-of-today
  []
  (let [url (URL. "http://mp.weixin.qq.com/cgi-bin/getmessage?t=wxm-message&lang=zh_CN&count=50&timeline=1&day=0")
        cookie (str "hasWarningUser=1; mp_uin=1876066812; ts_uid=3204872866; mp_user=qiu_jj; mp_sid="
                    "SlJhSlZ5MnlVSVVmdjJPNV9QMG45WnBLQ3hoN3RSVWJHM1NBQVU1dEFfSTQxdDgxZjZtbzRMNUZobU1LYkVUUEhLb2dDdElicXBkc1JqcjVxTnNzdng2TVNSdTB1bVA4NGNuR0Q0UDNyMXRobFNsTFlyNFgycDk1Rk1qVmpsK1c"
                    )
        conn (doto (.openConnection url) (.setRequestProperty "Cookie" cookie) (.connect)) 
        buf (java.io.BufferedReader. (java.io.InputStreamReader. (.getInputStream conn)))
        lines (line-seq buf)
        s (apply str lines)
;        _ (println (wr3.util.Charsetx/gbk2utf s))
        s1 (right s "json-msgList")
        s2 (between s1 "[" "]")
        json (read-json (str "[" s2 "]"))
;        _ (print-seq json)
        fmt "http://mp.weixin.qq.com/cgi-bin/getvoicedata?uin=*******&skey=******&msgid=%s&fileid=0"
        ]
    (print-seq (for [{id :id typ :type} json :when (= typ "3")] (format fmt id)))))

;println (media-of-today)
  
  