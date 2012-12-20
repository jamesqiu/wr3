(ns ^{:doc "从手机进行照片等文件上传的service、从PC浏览等服务"} 
     wr3.clj.app.fu)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.u 'wr3.clj.nosql)
(use 'hiccup.core 'somnium.congomongo)
(import 'wr3.upload.FileUpload 'wr3.upload.File) ; filesave 函数用到

(def head [:head 
           [:meta {:charset "utf-8"}]
           [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
           [:style {:type "text/css"} " body {font-family:微软雅黑,helvetica} "]])

(defn rand6-
  "六位随机验证码"
  []
  (let [f (fn [] (rand-int 10))]
    (apply str (for [i (range 6)] (f)))))

(defn rand6
  "不在coll中的6位随机验证码"
  [coll]
  (loop [n (rand6-)] (if-not (in? n coll) n (recur (rand6-)))))

(defn save
  "service: 文件上传form提交后的处理：1、保存文件；2、保存文件后调用js函数fileupload_ok()"
  [request]
  (let [fu (doto (FileUpload.) (.initialize request) .upload)
        myfile (.getFile (.getFiles fu) 0)
        mobile (.getParameter (.getRequest fu) "mobile")
        a-return [:a {:href "/fu"} "返 回"] ]
    (if-not (.isMissing myfile)
      (let [fname0 (.getFileName myfile) 
            fsize (.getSize myfile)
            mobile (if (nullity? mobile) "13300000000" mobile)
            r (with-mdb2 "fu" (fetch-one :user :where {:mobile mobile})) ; 该mobile号码是否已上传过？
            no6 (if r (:code r)
                  (let [codes (map :code (with-mdb2 "fu" (vec (fetch :user :only [:code]))))
                        code (rand6 codes)]
                    (with-mdb2 "fu" (insert! :user {:mobile mobile :code code :date (datetime)}))
                    code))
            fname (format "/fu/v/%s-%s.%s" mobile (System/currentTimeMillis) (rightback fname0 "."))]
        (.saveAs myfile (.getRealPath request fname)) 
        (with-mdb2 "fu" (insert! :log {:mobile mobile :fname0 fname0 :fname fname :fsize fsize :date (datetime)}))
        (html
          [:html head
           [:h2 (format "文件名: %s (大小：%s)" fname0 fsize)]
           [:h3 (format "文件上载完成，可使用手机号 %s 和标识号%s进行登录查看。" mobile (if r "" no6))]
           a-return]))
      (html [:html head 
             [:h2 "未选择上传文件"] a-return]))))
  
(defn view
  [request]
  (let [mobile "13300000000"
        rs (with-mdb2 "fu" (vec (fetch :log :where {:mobile mobile} :limit 100))) ]
    (html [:html head
           [:ul (for [{mobile :mobile fname0 :fname0 fname :fname fsize :fsize date :date} rs]
                  [:li [:a {:href fname} fname0] "(大小：" fsize " 日期：" date ")"] )
            ]])))
  
;(with-mdb2 "fu" (insert! :user {:mobile "13300000000" :code (rand6-) :date (datetime)}))  
