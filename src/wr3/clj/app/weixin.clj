(ns ^{:doc "对微信公众号qiu_jj的响应"} wr3.clj.app.weixin)

(use 'wr3.clj.web)
(use 'clojure.contrib.json 'wr3.clj.s 'wr3.clj.u 'wr3.clj.n)
(use 'hiccup.core)

(require 'wr3.clj.datagen)
(require '[wr3.clj.nosql :as mdb])
(require '[somnium.congomongo :as mongo])
(require '[clojure.contrib.json :as json])
(require '[clojure.java.io :as io])

(import org.apache.commons.codec.digest.DigestUtils)

(def pub------ "对qiujj的处理")

(def webapp-dir "d:/data/dev3/webapp/")
(def ems-file-dir (str webapp-dir "wx/ems/"))
(def cygwin-dir "d:/soft/cygwin/bin/")
(def gm-dir "d:/soft/GraphicsMagick/gm.exe")
  
; 回应的text的模板
(def template-text (str "<xml>"
                        " <ToUserName><![CDATA[%s]]></ToUserName>"
                        " <FromUserName><![CDATA[%s]]></FromUserName>"
                        " <CreateTime>%s</CreateTime>"
                        " <MsgType><![CDATA[text]]></MsgType>"
                        " <Content><![CDATA[%s]]></Content>"
                        " <FuncFlag>0</FuncFlag>"
                        "</xml>"))
; 回应的news（图文消息）的模板；支持JPG、PNG格式，较好的效果为大图640*320，小图80*80
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
                        "</xml>"))

(def template-news-item (str "<item>"
                             " <Title><![CDATA[%s]]></Title>"
                             " <Description><![CDATA[%s]]></Description>"
                             " <PicUrl><![CDATA[%s]]></PicUrl>"
                             " <Url><![CDATA[%s]]></Url>"
                             "</item>"))

(def template-news-items (str "<xml>"
                              " <ToUserName><![CDATA[%s]]></ToUserName>"
                              " <FromUserName><![CDATA[%s]]></FromUserName>"
                              " <CreateTime>%s</CreateTime>"
                              " <MsgType><![CDATA[news]]></MsgType>"
                              " <Content><![CDATA[]]></Content>"
                              " <ArticleCount>%s</ArticleCount>"
                              " <Articles>"
                              "  %s"
                              " </Articles>"
                              " <FuncFlag>0</FuncFlag>"
                              "</xml>"))

(defn- xml-text
  "根据模板生成text类型的回应消息xml
  @m 请求消息的hash-map
  @text 回应的内容，注意控制字节数不要超过2048 "
  [m text]
  (format template-text 
          (:FromUserName m) (:ToUserName m) (int (/ (System/currentTimeMillis) 1000)) text))

(defn- xml-news
  "根据模板生成news类型的回应消息xml
  @m 请求消息的hash-map
  @title 回应标题; @text 回应内容; @pic-url 回应封面图url; @url 全文url "
  [m {title :title text :text pic-url :pic-url url :url}]
  (format template-news 
          (:FromUserName m) (:ToUserName m) (int (/ (System/currentTimeMillis) 1000)) title text pic-url url))

(defn- xml-news-item
  "根据模板生成news类型回应消息xml中的一条
  @title 回应标题; @text 回应内容; @pic-url 回应封面图url; @url 全文url 
  注：@text只在有一条"
  [{title :title text :text pic-url :pic-url url :url}]
  (format template-news-item 
          title text pic-url url))

(defn- xml-news-items
  "根据模板生成news类型的回应消息xml
  @m 请求消息的hash-map
  @items 最多10个的条目的coll，每个条目为xml字符串"
  [m items]
  (format template-news-items 
          (:FromUserName m) (:ToUserName m) (int (/ (System/currentTimeMillis) 1000)) (count items) (apply str items)))

(defn- log-
  "@m 如 {:from from :url url} "
  [m]
  (mdb/with-mdb2 "weixin" (mongo/insert! :log (into m {:date (datetime)}))))

(def qiujj------ "对qiujj的处理")
(def help (str "请敲入我认识的指令：\n"
               "say —— 看箴言学英文。\n"
               "pid —— 和随机机器人打招呼。\n"
               "oid —— 组织机构代码。\n"
               "诗 —— 吟唐宋古诗一首 \n"
               "图 —— 查看风景美图 \n"
               "女 —— 查看美女图片 \n"
               "去某某地 —— 看“去”字后面地方比如：去天安门 \n"
               "译中英文 —— 翻译中英文单词句子如：译举头望明月 1merry y没事偷着乐 \n"
               "直接发送位置会得到周边商家信息"
               ))

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
    (= s "say") (wr3.util.Exec/exec "fortune.exe")
    (in? s ["poem" "shi" "诗" "詩"]) (wr3.util.Exec/exec "fortune.exe 75% tang300b 25% song100b")
    (.endsWith s "=") (wr3.util.Exec/exec (format "%sperl -e \"print(%s)\" " 
                                                  cygwin-dir (subs+ s 0 -2))) ; 不公开
    :else (format "/::-| 不认识：%s \n%s" s help)))

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
  "每次查看3张美女图片"
  [m s]
  (let [dir (case s "b2" "b2" "b3" "b3" "b1")
        dir-n ({"b1" 647 "b2" 61 "b3" 115} dir) ; 各目录下的图片数量
        n (inc (rand-int dir-n))
        n3 [n (inc (safe-index n dir-n)) (inc (safe-index (inc n) dir-n))] 
        f (fn [dir n] {:pic-url (format "http://gotoreal.com/wx/%s/s/%s.jpg" dir n)
                       :url (format "http://gotoreal.com/wx/%s/%s.jpg" dir n)})
        item0 (xml-news-item (into {:title "美女图片系列（点击看大图）" :text "注：……"} (f dir n)))
        items (for [i (range (count n3))] 
                (xml-news-item (into {:title (format "美女图之%s：" (inc i)) :text "注：点击打开可查看大图"} 
                                     (f dir (nth n3 i))))) ]
    (xml-news-items m (cons item0 items))))

(def dianping-category-
  {"美食" [ "上海菜", "北京菜", "粤菜", "川菜", "湘菜", "江浙菜", "淮扬菜", "湖北菜", "江西菜", "云南菜", "山西菜", "蒙古菜",
         "徽菜", "闽菜", "鲁菜", "农家菜", "潮汕菜", "客家菜", "东北菜", "贵州菜", "台湾菜", "新疆清真", "西北菜", "日本",
         "韩国", "东南亚菜", "西餐", "家常菜", "烤鸭", "官府菜", "烤鱼", "素菜", "火锅", "自助餐", "小吃快餐", "面包甜点",
         "海鲜", "烧烤", "面馆", "麻辣烫", "酒吧", "咖啡", "茶馆" ],
   "休闲娱乐" [ "酒吧", "咖啡", "茶馆", "KTV", "电影院", "图书馆", "博物馆", "美术展览", "演出票务", "公园", "景点郊游",
           "足疗按摩", "洗浴", "游乐游艺", "桌面游戏", "DIY手工坊" ],
   "购物" [ "商场", "食品茶酒", "服饰", "鞋包", "珠宝饰品", "花店", "化妆品", "运动户外", "儿童服饰", "玩具", "亲子购物",
         "品牌折扣店", "家具", "家居建材", "书店", "眼镜店", "办公", "超市", "便利店", "药店" ],
   "丽人" [ "美发", "美容", "SPA", "化妆品", "瘦身纤体", "美甲", "瑜伽", "舞蹈", "写真", "整形", "齿科" ],
;   "结婚" [ "婚纱摄影", "婚宴", "婚戒首饰", "婚纱礼服", "婚庆公司", "彩妆造型", "司仪主持", "婚礼跟拍", "婚车租赁",
;         "婚礼小商品", "婚房装修" ],
   "亲子" [ "早教中心", "幼儿园", "小学", "亲子摄影", "亲子游乐", "亲子购物", "孕产护理" ],
   "运动健身" [ "游泳馆", "羽毛球馆", "健身中心", "瑜伽", "篮球场", "足球场", "高尔夫场", "保龄球馆", "乒乓球馆", "武术场馆",
           "壁球馆", "攀岩馆", "射箭馆", "骑马场", "溜冰场" ],
   "酒店" [ "五星级酒店", "四星级酒店", "三星级酒店", "经济型酒店", "公寓式酒店", "精品酒店", "青年旅舍", "度假村", "农家院" ],
   "爱车" [ "4S店", "汽车保险", "维修保养", "配件车饰", "驾校", "汽车租赁", "停车场", "加油站" ],
   "生活服务" [ "医院", "干洗店", "搬家", "家政", "银行", "学校", "小区", "商务楼", "旅行社", "培训", "宠物医院", "齿科", "快照冲印" ]
   })
  
(defn- dianping-
  "给出坐标和分类，从大众点评网得到返回的json字符串
  @latitude 纬度 40.123456 
  @longitude 经度 121.123456
  @category 类型，@see dianping-category-
  @return {:status 'OK'/'ERROR' :count int :}"
  [latitude longitude category]
  (let [appkey "623628530"
        secret "fd96db6ab6b84a32abf35582c1c4f799"
        dianping-template (str "http://api.dianping.com/v1/business/find_businesses?"
                               "latitude=%s&longitude=%s&category=%s&sort=7&appkey=%s&sign=%s")
        category1 (String. (.getBytes category "utf-8") "gbk")        
        category2 (url-encode category)
        params (into (sorted-map) {"latitude" latitude "longitude" longitude "category" category1 "sort" "7"})
        ps (apply str (for [[k v] params] (str k v)))
        sign (.toUpperCase (org.apache.commons.codec.digest.DigestUtils/shaHex (str appkey ps secret)))
        url (format dianping-template latitude longitude category2 appkey sign)
        rt (json/read-json (slurp url))]
    rt))
;(println (:businesses (dianping- "31.218173" "121.415695" "咖啡")))

(defn dianping
  "app: 
  @id category
  @lat @lng ?lat=40.123456&lng=121.123456"
  [id lat lng]
  (if-not (and id lat lng)
    "请从微信公众平台进行调用"
    (let [rt (dianping- lat lng id)
          ok? (= "OK" (:status rt))
          n (:count rt)]
      (html
        [:html [:head [:style {:type "text/css"} "body * { font-family:微软雅黑,Arial; } " ]]
         [:body
          [:h2 (format "【%s】类周边商户（%s家）" id n)]
          (cond
            (not ok?) [:font {:color "red"} "出错了！"]
            (zero? n) [:h3 (format "周边未登记【%s】相关商户" id)]
            :else (for [{n :name n2 :branch_name rat :rating_s_img_url addr :address tel :telephone 
                         d :distance img :photo_url url :business_url } (:businesses rt)]
                    [:div [:h3 n n2 [:img {:src rat}]]
                     [:p (format "地址：%s %s <br/>据当前位置<font color=red>%s米</font>" addr tel d)]
                     [:a {:href url :title "点击前往大众点评"} [:img {:src img}]] [:hr] ]) ) ]] ))))
  
(defn- reply-text
  "qiujj 回应text类型。返回文本或者图文消息"
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

(defn- gmap-url-
  "发来的location类型，得到googlemap的url"
  [x y]
  (format "http://ditu.google.cn/maps/api/staticmap?center=%s,%s&size=450x450&zoom=16&markers=%s,%s&sensor=false" 
          x y x y))
  
(defn- reply-location
  "qiujj 回应location类型，显示该地点的baidu地图url"
  [m]
  (let [[x y label] [(:Location_X m) (:Location_Y m) (:Label m)]
        url (gmap-url- x y)
        link (format (str "<a href=\"%s\">查看</a>") url)
        item0 (xml-news-item {:title label :text "周边商户" :pic-url url :url url})
        items (for [[k v] dianping-category- :let [s (format "【%s】类周边商户 >>" k)]]
                (xml-news-item {:title s :text s :pic-url ""
                                :url (format "http://gotoreal.com/c/weixin/dianping/%s?lat=%s&lng=%s" k x y)}))
        ]
    (log- {:type "location" :from (:FromUserName m) :in link})
    (xml-news-items m (cons item0 items))))
                 
(defn- reply-image
  "qiujj 回应image类型，显示该图片的url"
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
(def ems------ "对ems的处理")

(import java.util.Hashtable)
(def ems-login-map- (java.util.Hashtable.)) ; 保存{k: "微信用户id", v:"一次性6位登录code"}

(defn- rand6-
  []
  (apply str (for [i (range 6)] (rand-int 10))))

(defn- rand6-safe-
  []
  (let [n (rand6-)] 
    (if (.containsValue ems-login-map- n) (rand6-safe-) n)))

(defn- download-img-
  "把微信网站的图片download到gotoreal.com服务器上"
  [url fname]
  (with-open [in (io/input-stream url)
              out (io/output-stream (str ems-file-dir fname))] 
    (io/copy in out)))

;(import [wr3.util Exec])
(defn- handle-ems
  [m]
  (let [{from :FromUserName msg-type :MsgType content :Content} m
        m0 {:type msg-type :from from :to "ems"} 
        login-code (rand6-safe-) ]
    (case msg-type
      "text" (log- (into m0 {:in content}))
      "location" (let [{x :Location_X y :Location_Y label :Label} m]
                   (log- (into m0 {:x x :y y :label label})))
      "image" (let [{url :PicUrl} m
                    fname0 (format "%s-%s" from (System/currentTimeMillis))
                    fname1 (str fname0 ".jpg")
                    fname2 (str fname0 "-s.jpg")] 
                (download-img- url fname1)
                (wr3.util.Exec/exec (format "%s convert -resize 100x100 %s%s %s%s" 
                                            gm-dir ems-file-dir fname1 ems-file-dir fname2))
                (log- (into m0 {:url url :file fname1})))
      (log- (into m0 {:in (str "未知类型：" m)}) ))
    (.put ems-login-map- from login-code)    
    (xml-text m (format "已接收[%s]信息\n%s\n访问：\nhttp://gotoreal.com/c/weixin/ems/%s" msg-type (datetime) login-code))))

(defn- ems-list-
  "列出该微信账户下的最多100条记录"
  [from]
  (let [rs (mdb/with-mdb2 "weixin" 
             (vec (mongo/fetch :log :limit 100 :where {:from from :to "ems"} :sort {:date -1})))]
    (html
      (for [r rs :let [{t :type d :date} r ds (html [:font {:color "gray"} " " d] [:br])]]
        (case t
          "text" (let [s0 (escape-html (:in r))
                       s (if (> (count s0) 100) (str (subs s0 0 100) "...") s0)]
                   [:div ds (cond 
                              (.startsWith s0 "http://ci.baidu.com/") [:a {:href s0} "多媒体链接"] 
                              (.startsWith s0 "http://") [:a {:href s0} s]
                              :else s)])
          "image" (let [f (:file r)
                        f1 (if (.endsWith f ".jpg") (wr3.util.Stringx/leftback f ".jpg") f) ]
                     [:div  ds [:a {:href (format "/wx/ems/%s.jpg" f1)} [:img {:src (format "/wx/ems/%s-s.jpg" f1)}]]])
          "location" (let [{x :x y :y label :label} r] 
                       [:div ds (format "定位于：%s<br/>坐标：%s, %s" label x y) [:br] 
                        [:a {:href (gmap-url- x y)} "看地图"]]) 
          [:div ds (format "暂未处理类型为%s的消息" t)])) 
      [:div {:style "text-align:center; font-weight:bolder"} 
       [:br] [:br] [:a {:href "/c/weixin/ems-logout"} "注销退出"]] )))
  
(defn ems-logout
  [request]
  (session-close request)
  "已成功注销！")
  
(defn ems
  "显示ems的列表.
  @id "
  [id request]
  (let [code (or id "")
        from-in-session (session request "wr3WeixinEmsFrom")
        from-in-map (when-let [kv (find-first #(= code (val %)) ems-login-map-)] (key kv))]
    (html
      [:html
       [:head [:style {:type "text/css"} 
               "div { float:left; margin:10px; padding:5px; font-family:Arial; font-size:9px; border:1px dashed gray; "
               "  width:120px; height:120px; overflow:hidden; }; " ]]
       [:body
        (cond
          from-in-session (ems-list- from-in-session)
          from-in-map (do (session! request "wr3WeixinEmsFrom" from-in-map)
                        (.remove ems-login-map- from-in-map)
                        (ems-list- from-in-map))
          :else [:div [:font {:color "red"} "错误：非法连接，请从微信再次获取登录合法url。"]]) ]])))

;------------- 处理公众号 clj-lang 的请求. 函数例子及文档、src
(def clj------ "对clj的处理")

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
(def index------ "对所有公众账户的处理入口")

(defn- handle-unknown
  [m]
  (xml-text m (format "发给公众平台号%s的信息，尚未安排处理程序。" (:ToUserName m))))

(def dd-handle ; 微信公众平台内部号对应处理函数
  {"gh_bc4960f69978" handle-qiujj
   "gh_59b8f18417ae" handle-ems
   "gh_344792328c76" handle-clj})

(defn index
  "对get返回echostr；对post返回xml"
  [signature timestamp nonce echostr request]
  (if (= 19 (count echostr))
    (do ; get 方法响应 
      ;vars={:signature 0d76e6fed9cf5aa8dd9958c4b39f394d56fb24be, :nonce 1358692097, :timestamp 1358650225}
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
            f-handle (or (dd-handle (:ToUserName m)) handle-unknown)]
        (f-handle m) ))))

;--- 读今日录入的所有微信语音信息（原来可以，后来好像被屏蔽了，且mp_sid每次都不一样）
(import [java.net URL URLConnection])
(defn- media-of-today
  []
  (let [url (URL. "http://mp.weixin.qq.com/cgi-bin/getmessage?t=wxm-message&lang=zh_CN&count=50&timeline=1&day=0")
        cookie (str "hasWarningUser=1; mp_uin=1876066812; ts_uid=3204872866; mp_user=qiu_jj; mp_sid="
                    "SlJhSlZ5MnlVSVVmdjJPNV9QMG45WnBLQ3hoN3RSVWJHM1NBQVU1dEFfSTQxdDgxZjZtbzRMNUZobU1LYkVUUEhLb2dDdElicXBkc1JqcjVxTnNzdng2TVNSdTB1bVA4NGNuR0Q0UDNyMXRobFNsTFlyNFgycDk1Rk1qVmpsK1c"
                    )
        conn (doto (.openConnection url) (.setRequestProperty "Cookie" cookie) (.connect)) 
        buf (java.io.BufferedReader. (java.io.InputStreamReader. (.getInputStream conn)))
        lines (line-seq buf)
        s (apply str lines)
        s1 (right s "json-msgList")
        s2 (between s1 "[" "]")
        json (read-json (str "[" s2 "]"))
        fmt "http://mp.weixin.qq.com/cgi-bin/getvoicedata?uin=*******&skey=******&msgid=%s&fileid=0" ]
    (print-seq (for [{id :id typ :type} json :when (= typ "3")] (format fmt id)))))
;println (media-of-today)
  
  