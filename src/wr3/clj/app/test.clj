(ns wr3.clj.app.test)
;;;;------------------------ 进行最简单功能的测试 /localhost/c/test

(use 'wr3.clj.web 'wr3.clj.n 'wr3.clj.u); :reload) ; 加不加 :reload 对速度有较大影响
(use 'hiccup.core)

(defn auth
  "该函数被 CljServlet 调用，也可手工调用：/c/test/auth?fname=m1 "
  [request fname & args]
  (cond
    (= fname "index") true
    (and (= fname "m1") (not (wr3user? request "admin"))) false
    :else true))
  
(defn index
  "service: 给出金额，得到大写读数及简读"
  [id]
  (let [id0 (or id "2468013570.146")
        id1 (Double/valueOf id0) ]
    (html [:div (str "金额 (圆)：" id0)][:br]
          [:div (str "精确读数：" (wr3.bank.Currency/asChinese id0))][:br]
          [:div (str "简略读数：" (wr3.bank.Currency/about id1))])))


(defn m1 [id]
  (html-body 
    ""
    (let [n (to-int (or id "7") 7)]
      (html (for [i (range 1 n)] [:div "序号：" i])))))
             
(defn coki
  "app: 测试jquery.cookie是否可用"
  [request]
  (html-body
    {:onload "alert($.cookie('wr3user'))"}
    [:h1 "测试cookie"]
    [:div (cookie request "wr3user")]))

(defn host
  [request]
  (let [sn (.getServerName request)]
  (html
    (case sn
      ("t430" "T430") "aaaaaaaaaaaaaaaaaaa"
      "localhost" "bbbbbbbbbbbbbbbbbb"
      "cccccccccccccccccc")
    [:br]
    (str sn))))

(import wr3.util.Word)
(import wr3.util.Filex)

(defn fpath
  [request]
  (let [fpath1 (.getRealPath request "/file/en1-1336557674246.doc")
        fpath2 (.getRealPath request "/file/en1-1336557674246.html")
        has-file? (Filex/has fpath2)]
    (html-body
      [:h1 "fpath"]
      [:h2 fpath1]
      [:h2 has-file?]
      (do (when-not has-file? (Word/toHtml fpath1 fpath2))
        "toHtml")
      [:a {:href "/file/en1-1336557674246.html"} "查看html文档"])))

(defn bs
  "测试 bootstrap"
  [request]
  (html-bs
    {:title "bootstrap test"}
    [:style {:type "text/css"} "body { margin:10px }"]
    ;--- row span
    [:div {:class "row-fluid"}
     (for [i (range 1 13)]
       [:div {:class "span1"} "span1"])] [:hr]
    ;--- head
    (for [i (range 1 7) :let [h (keyword (str "h" i))]]
      (vector h (str (name h) "——Nasoft 北京汇金科技股份有限公司 <small>清华大学东门液晶大厦2层（62790202）</small>" ))) [:hr]
    ;--- button
    (for [c '[normal primary info success warning danger inverse link] :let [n (str "btn-" c)]]
      (html (bs-button (str "按钮" n) {:cls (name c)} ) (space 5)))    
    ;--- form
    (bs-form "Form-horizontal" 
             ["姓名" [:input {} ""]] ["证件号" [:input {} ""]] 
             ["下拉框" [:select [:option 10] [:option 20]]]
             ["进行搜索" [:input {} ""]])
    ;--- table
    (let [t (html [:table {:class "%s"} [:caption [:h3 "报表测试 .%s"]]
                   [:thead [:tr [:th "姓名"] [:th "证件号"] [:th "电子邮件"]]]
                   [:tbody 
                    [:tr [:td "qh"] [:td "532525197803100012"] [:td "qh@nasoft.com"]]
                    [:tr [:td "james"] [:td "110114200003100012"] [:td "jamesqiu@sina.com"]] ]])]
      (for [c '[table table-bordered table-striped table-condensed]]
        (format t (str "table " c) c))) [:hr]
    ;--- bs-quote
    [:div {:class "row-fluid"}
     [:div {:class "span6"} 
      (bs-quote {:class "pull-right"} "北京清华紫光北美科技开发有限责任公司 北京汇金科技股份有限公司 清华大学东门液晶大厦2层北美集团（62790202）")]
     [:div {:class "span6"} 
      (bs-quote nil "北京清华紫光北美科技开发有限责任公司 北京汇金科技股份有限公司 清华大学东门液晶大厦2层北美集团（62790202）")]
     ] [:hr]
    ;--- list
    [:div {:class "row-fluid"}
     [:div {:class "span4"} [:ul [:li "姓名 jamesqiu"] [:li "证件号 532525197903180012"] ]]
     [:div {:class "span4"} [:ul {:class "unstyled"} [:li "姓名 jamesqiu"] [:li "证件号 532525197903180012"] ]]
     [:div {:class "span4"} [:ol [:li "姓名 jamesqiu"] [:li "证件号 532525197903180012"] ]]
     [:div {:class "span4"} [:dl [:dt "姓名"] [:dd "jamesqiu"] [:dt "证件号"] [:dd "532525197903180012"] ]]
     [:div {:class "span4"} [:dl {:class "dl-horizontal"} [:dt "姓名"] [:dd "jamesqiu"] [:dt "证件号"] [:dd "532525197903180012"] ] ]] [:hr]

    [:form {:action "/c/pub/filesave" :method "POST" :enctype "multipart/form-data"}
     [:input {:name "f_fileupload" :type "file"}]
     [:input {:type "submit"}]]
    [:hr]
    
    [:dl
     (for [[k v] (http-header request)]
       [:dl {:class "dl-horizontal"} [:dt k] [:dd v]])]
    
;<div class="row-fluid">
;    <div class="fileupload fileupload-new" data-provides="fileupload"><input type="hidden">
;        <div class="input-append">
;            <div class="uneditable-input span2" runat="server" id="statment1">
;                <i class="icon-file fileupload-exists"></i> 
;                <span class="fileupload-preview" style=""></span>
;            </div>
;            <span class="btn btn-file"><span class="fileupload-new">Select file</span
;            <span class="fileupload-exists">Change</span><input id="myFile" type="file" runat="server">
;            </span>
;            <a href="#" class="btn fileupload-exists" data-dismiss="fileupload" >Remove</a>
;        </div>
;    </div>
;</div>

    ))

(defn mt
  [request]
  (html
    [:html
     [:head 
      [:meta {:charset "utf-8"}]
      [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}] 
      [:style {:type "text/css"} "body * {font-family:微软雅黑,helvetica}"]]
     [:body {:width "100%"}
      [:form {:action "/c/pub/filesave" :method "POST" :enctype "multipart/form-data"}
       [:input {:name "f_fileupload" :type "file"}] [:br]
       [:input {:type "submit"}]]]]))

