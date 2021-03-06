(ns wr3.clj.app.console)

(use 'hiccup.core)
(use 'wr3.clj.web 'wr3.clj.s)

(defn auth
  "该 CljServlet 调用，用于本应用各函数的权限控制 "
  [request fname ids & args]
  (let [root? (wr3role? request "root")]
    (case fname
      ("apps" "rsh" "resp" "exec") root?
      true)))

(defn reload 
  "app: 可动态reload wr3.clj下的所有clj，甚至自身 '/c/console/reload/app.console'
  在-Dwr3.clj.reload=1时可不用此app，在生产环境下-Dwr3.clj.reload=0时可以单独动态reload某个clj，
  既保证了生产环境的运行效率，又不失动态部署灵活性。"
  [id] 
  (let [id (or id "app.test")
        clj (str "wr3.clj." id)
        msg (str "wr3.clj.app.console/reload: " clj)] 
    (do
      (use (symbol clj) :reload) ; 进行动态reload
      (println msg)
      msg)))

(defn gc 
  "app: 对app server进行gc建议"
  []
  (let [msg "wr3.clj.app.console: System.gc()"]
    (System/gc)
    (println msg)
    msg))

(defn apps
  "所有常用应用"
  [request]
  (let [cfg (array-map :bank "银行经营分析系统演示（前提：db）"
                       :cdoc "在线查看函数文档及源码"
                       :chartf "各种图形（静态图及Flash图）效果演示"
                       :crud "表单-视图-文档 CRUD 演示（前提：mdb）"
                       :dbm "数据库表浏览（前提：db）"
                       :demo "简单测试eui应用布局"
                       :dojo "dojo前端的简单包装"
                       :ems "应急系统界面演示（前提：mdb）"
                       :esp "交通运输企业安全生产标准化管理系统（前提：mdb）"
                       :espfj "福建交通厅考评员管理（前提：mdb）"
                       :espm "交通运输企业安全生产标准化管理系统——移动版（前提：mdb）"
                       :eui "eui前端的包装测试；Google Map、Earth应用演示"
                       :grade "上市公司综合评价演示（前提：mdb）"
                       :task "项目及人员管理演示（前提：mdb）"
                       :test "临时测试app用"
                       :tool "小应用集：身份证、分词、拼音、税率、金额、译词等"
                       :veg "江桥市场蔬菜报表分析系统（前提：db）" )]
    (html-body
      [:table.wr3table {:align "center"}
       [:caption (format "app应用一览表 (用户：%s)" (wr3user request))]
       [:tbody
        [:tr [:th "应 用"] [:th "说 明"]]
        (for [[k v] cfg] 
          [:tr [:td [:a {:href (str "/c/" (name k)) :target "_blank"} k]] [:td v]]) ]])))

(import [wr3.util Exec])

(defn exec
  "app: 执行rsh提交的结果"
  [request cmd args]
  (let [rt (if args (Exec/exec cmd args) (Exec/exec cmd))]
    (html
      [:textarea {:style "width:99%; height:99%"} rt])))
  
(defn rsh
  "app: 输入命令；上传文件至file目录 "
  [request]
  (let []
    (html-body
      [:form {:action "/c/console/exec" :method "POST" :target "_blank"}
       [:label "cmd: "][:input {:name "cmd" :value "cmd /c dir" :size 100}] [:br]
       [:label "args: "][:input {:name "args" :value "" :size 100}] [:br]
       [:input {:type "submit"}] ]
      [:hr]
      [:form#fm1 {:name "fm1"}
       [:label "文件："]
       (fileupload-field "文件" "tmpfile" "" {})]
      (fileupload-dialog))))

