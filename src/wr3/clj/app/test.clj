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