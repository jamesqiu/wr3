(ns ^{:doc "对微信公众号qiu_jj的响应"} wr3.clj.app.weixin)

(use 'wr3.clj.web)
(require 'wr3.clj.datagen 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u)
(require '[wr3.clj.nosql :as mdb])
(require '[somnium.congomongo :as mongo])

(def help (str "请敲入我认识的指令：\n"
               " say  看箴言学英文。\n"
               " pid  和随机机器人打招呼。\n"
               " oid  组织机构代码。\n"
               " 诗   吟唐宋古诗一首 \n"
               " 图   查看风景美图 \n"
               " 美女 查看美女图片 \n"
               " 去某某地 看“去”字后面地方比如：去天安门 \n"
               ))

(def webapp-dir "d:/data/dev3/webapp/")
(def cygwin-dir "d:/soft/cygwin/bin/")
(def fortune (str cygwin-dir "fortune.exe"))

(defn- pid-string-
  [pid]
  (let [[area birth sex] (wr3.bank.IDUtil/infos pid)
        age (- 2013 (wr3.clj.n/to-int (wr3.clj.s/left birth "-")))
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
    (wr3.clj.s/in? s ["hi" "hello" "你好" "您好" "早上好" "嗨" "喂"]) "您好！ /::) "
    (= s "pid") (pid-string- (wr3.clj.datagen/rand-pid))
    (= s "oid") (wr3.clj.datagen/rand-orgid)
    (= s "say") (wr3.util.Exec/exec fortune)
    (wr3.clj.s/in? s ["poem" "shi" "诗" "詩"]) (wr3.util.Exec/exec (str fortune " 75% tang300b 25% song100b"))
    (.endsWith s "=") (wr3.util.Exec/exec (format "%sperl -e \"print(%s)\" " 
                                                  cygwin-dir (wr3.clj.s/subs+ s 0 -2))) ; 不公开
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

(defn xml-text
  "根据模板生成text类型的回应消息xml
  @m 请求消息的hash-map
  @text 回应的内容"
  [m text]
  (format template-text (:FromUserName m) (:ToUserName m) (int (/ (System/currentTimeMillis) 1000)) text))

(defn xml-news
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
    (xml-news m {:title (wr3.clj.s/left fname ".jpg") :text (format "请欣赏图片[%s]\n点击可打开查看大图。" fname)
                 :pic-url pic-url :url url}) ))

(defn- reply-map-
  [m s]
  (let [loc (subs s 1)
        loc2 (java.net.URLEncoder/encode loc "UTF-8") ; 中文编码
        pic-url (format "http://api.map.baidu.com/staticimage?center=%s&width=300&height=300&zoom=16&markers=%s" loc2 loc2)
        url (format "http://api.map.baidu.com/staticimage?center=%s&width=600&height=600&zoom=18&markers=%s" loc loc)]
    (xml-news m {:title (str "前往：" loc) :text "请点击打开地图。" 
                 :pic-url pic-url :url url} )) )

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
  (mdb/with-mdb2 "weixin" (mongo/insert! :log (into m {:date (wr3.clj.u/datetime)}))))

(defn- reply-text
  "回应text类型。返回文本或者图文消息"
  [m]
  (let [s (.toLowerCase (.trim (:Content m)))]
    (log- {:type "text" :from (:FromUserName m) :in s})
    (cond
      (wr3.clj.s/in? s ["图" "圖" "pic"]) (reply-pic- m)
      (.startsWith s "去") (reply-map- m s)
      (wr3.clj.s/in? s ["美女" "女" "b1" "b2" "b3"]) (reply-girl- m s)
      :else (xml-text m (reply- s)))))

(defn- reply-location
  "回应location类型，显示该地点的baidu地图url"
  [m]
  (let [[x y label] [(:Location_X m) (:Location_Y m) (:Label m)]
        text0 (format "地点：%s\n 坐标：%s,%s\n\n" label x y)
        url (format "http://api.map.baidu.com/staticimage?center=%s,%s&width=450&height=450&zoom=17&markers=%s,%s"
                    y x y x)]
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
            msg-type (:MsgType m) ]
        (case msg-type
          "text" (reply-text m)
          "location" (reply-location m)
          "image" (reply-image m)
          (reply-text (assoc m :Content (str "未知类型" msg-type))) 
          )))))

;(wr3.clj.web/url-encode "center%3D昆明湖%26width%3D400%26height%3D300%26zoom%3D16%26markers%3D昆明湖")
;  (wr3.clj.web/url-decode "%E6%B8%85%E5%8D%8E")
