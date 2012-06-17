(ns ^{:doc "wr3的系统级公共service，放置一些各个应用都会使用到的公共service，勿删！"
      :see "wr3.clj.app.auth 另一共系统公共service，用于登录校验"
      :author "jamesqiu"}
  wr3.clj.app.pub)

(use 'wr3.clj.web 'wr3.clj.s)
(use 'hiccup.core)
(import 'wr3.upload.FileUpload 'wr3.upload.File) ; filesave 函数用到
        
(defn fileupload
  "service: 文件上传弹出dialog的内容，提交按钮由js函数 fileupload_bt() 定义。
  @action 执行文件保存的service url，如'?action=/c/esp/filesave' "
  [action]
  (html 
    [:form {:name "fm_fileupload" :id "fm_fileupload" :method "POST" :action (or action "/c/pub/filesave")
            :enctype "multipart/form-data" :target "ifrm_fileupload"}
     (eui-text {:name "f_fileupload" :id "f_fileupload" :type "file"}) 
     (eui-tip "选择好文件后请按确定进行上传") ]
    [:iframe {:name "ifrm_fileupload" :style "display:none"}] ))

(defn filesave
  "service: 文件保存的缺省service。可以自己参考这个在应用中写自己的保存函数。
  文件上传form提交后的处理：1、保存文件；2、保存文件后调用js函数fileupload_ok()"
  [request]
  (let [fu (doto (FileUpload.) (.initialize request) .upload)
        myfile (.getFile (.getFiles fu) 0)
        fname0 (.getFileName myfile)
        fsize (.getSize myfile)
        wr3user (wr3user request)
        fname1 (format "/file/%s-%s.%s" wr3user (System/currentTimeMillis) (rightback fname0 "."))
        ]
    (when (not (.isMissing myfile))
      (do
        (.saveAs myfile (.getRealPath request fname1)) 
        (html-body
          [:h1 (format "文件名: %s (大小：%s)" fname0 fsize)]
          "文件上载完成"
          [:script (format "parent.fileupload_ok('%s')" fname1)])))))

