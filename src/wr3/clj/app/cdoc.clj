;;;; Clojure docs 联机参考
(ns wr3.clj.app.cdoc)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.web 'wr3.clj.chart 'wr3.clj.db)
(use 'hiccup.core)
(use 'somnium.congomongo)

(require 'clojure.contrib.repl-utils)

;;
(def funs (mapcat ns-publics (all-ns)))

(defn index
  []
  (html
    [:html (head-set-join
             (head-js "SyntaxHighlighterCore.js")
             (head-js "SyntaxHighlighterClojure.js")
             (head-css "SyntaxHighlighterCoreDefault.css"))
     [:body {:onload "cdoc_onload();"}
      [:h2 "请输入Clojure函数名（支持模糊查询、自动完成）："]
      [:form.app
       [:input#in {:type "text" :style "width:500px;" :value "filter"}] " "
       [:input#ok {:type "button" :value " 提交处理 "}] " "][:br]
      [:div "结果："][:br]
      [:div#rt  ""]
      ]]))


(use 'clojure.contrib.json)

(defn auto
  "service: 提供自动完成功能的列表，使用: ./cdoc/auto?term=map"
  [request]
  (let [term0 ((query-vars request) "term")
        term (if (nil? term0) "-" term0)]
    (json-str
      (vec
        (map #(.substring (str (val %)) 2)
             (filter #(has? (str (key %)) term) funs))))))

(defn show
  "service：提供函数doc及源码显示"
  [ids]
  (let [id0 (if ids (join ids "/") "filter")]
    (html
        [:style "pre {font-family: consolas; font-size: 14px;}"]
        [:h2 "------ 文档 ------"]
        [:pre (let [m (meta (resolve (symbol id0)))] (format "%s\n%s\n  %s\n" id0 (:arglists m) (:doc m))) ][:br]
        [:h2 "------ 源码 ------"]
        [:pre {:class "brush: clojure"} (source-fn2 id0)]
        [:script {:type "text/javascript"} "SyntaxHighlighter.highlight();"]
        )))