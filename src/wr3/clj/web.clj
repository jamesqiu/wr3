;;;; web Utility collection: uri,query-string,path-info; request, session
;;;; 还包装了 dojo 和 jquery easyui
(ns wr3.clj.web)

(use 'wr3.clj.s 'wr3.clj.u)
(use 'hiccup.core)

(import '(wr3 BaseConfig))
(import '(javax.servlet.http HttpSession))

;;; ------------ session 处理
(defn session
  "从request中得到session指定key的value, 没有则为nil"
  [^HttpSession request ^String k]
  (if (or (nil? request) (nullity? k)) nil
    (let [s (.getSession request)]
      (if (nil? s) nil
        (.getAttribute s k)))))

(defn session!
  "从request中得到session，设置其key的value"
  [^HttpSession request ^String k v]
  (when (and (not (nil? request)) (not (nullity? k)))
    (let [s (.getSession request)]
      (when (not (nil? s))
        (.setAttribute s k v)))))

(defn session-close
  "从request中得到session，令其失效"
  [^HttpSession request]
  (when (not (nil? request))
    (let [s (.getSession request)]
      (when (not (nil? s))
        (.invalidate s)))))

(defn wr3user
  "得到session中wr3user变量的值，即用户uid"
  [request]
  (session request "wr3user"))
  
(defn wr3user? 
  "判断session中wr3user变量是否为指定用户"
  [request uid]
  (= uid (wr3user request)))

(defn wr3role
  "得到session中wr3role变量的值，即用户roles以逗号分隔的值"
  [request]
  (session request "wr3role"))
  
(defn wr3role? 
  "判断session中wr3role变量是否含指定角色role"
  [request role]
  (has? (split (wr3role request) ",") role))

(defn cookie
  "从request中取出指定名称的cookie值字符串，找不到返回nil"
  [request cname]
  (wr3.util.Servletx/cookie request cname))

(defn http-header
  "得到浏览器的信息如： {'Host' 'localhost' 'Connection' 'keep-alive' ..} "
  [request]
  (into {} (for [k (enumeration-seq (.getHeaderNames request))] [k (.getHeader request k)])))

;;; ------------ query var map 处理
(defn query-vars
  "获得request的queryString中的key-value，以hash-map形式返回.
  注意：key是字符串而不是keyword"
  [^HttpSession request]
  (if (nil? request) {}
    (let [m (.getParameterMap request)]
      (zipmap (keys m) (map #(join % "&") (vals m)))))) ; ?k1=a&k1=b 变为{"k1" "a&b"}

(defn query-vars2
  "获得request的queryString中的key-value，以hash-map形式返回.
  注意：key是keyword而不是字符串"
  [^HttpSession request]
  (if (nil? request) {}
    (let [m (.getParameterMap request)]
      (zipmap (map keyword (keys m)) (map #(join % "&") (vals m)))))) ; ?k1=a&k1=b 变为{"k1" "a&b"}

;;; ------------ html wrapper

(defn html-head
  "第一个参数为自定义head内容；之后多个字符串参数放在html body内"
  [head & ss]
  (str "<html><head>" head "</head><body>" (apply str ss) "</body></html>"))

(def webapp (BaseConfig/webapp))
(def css-main
  (format "<link href=\"%s/css/main.css\" rel=\"stylesheet\" type=\"text/css\"></link>" webapp))
(def js-main
  (format "<script type=\"text/javascript\" src=\"%s/js/main.js\"></script>" webapp))
(def meta-utf8
  (html [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]))
;; html的head设置字符串"<head><meta ...><script ...></head>"
(def head-set (html [:head meta-utf8 js-main]))

(defn head-set-join
  "除了head-set，在<head>内添加其他内容"
  [& s]
  (html [:head meta-utf8 js-main s]))

(defn head-js
  "生成<head>中包含<webapp>/js/下指定.js文件的html片段"
  [js] (html [:script {:type "text/javascript" :src (format "%s/js/%s" webapp js)} ""]))

(defn head-set-join-js
  "除了head-set, 在<head>内添加1个或多个<webapp>/js/下的.js文件，如['a.js' 'b.js'] 
  @js 1个js文件 'a.js'，多个js文件 ['a.js' 'b,js' ..] "
  [js]
  (html [:head meta-utf8 js-main (map head-js (if (string? js) (list js) js))]))
  
(defn html-js
  "生成含js代码的html片段"
  [& s] (html [:script {:type "text/javascript"} (apply str s)]))

(defn head-css
  "生成<head>中包含<webapp>/css/下指定.css文件的html片段"
  [css]
  (html [:link {:type "text/css" :rel "stylesheet" :href (format "%s/css/%s" webapp css)}]))

(defn html-css
  "生成含css代码的html片段"
  [& s]
  (html [:style (apply str s)]))

(defn get-id
  "得到id第一个值或者缺省值"
  [id default]
  (if-let [id0 (first id)] id0 default))

(defmacro html-body
  "常用的html body模板，使用jquery ui和easyui，参数：
  @m：属性，例如 {:onload 'a()'}, :js属性表示1个或多个<head>中的.js文件如：{:js 'a.js'} {:js ['a.js' 'b.js']}
  @body：内容
  usage：(html-body {:onload 'my_function()'} '...') "
  [m & body]
  `(html [:html 
          (if (:js ~m) (head-set-join-js (:js ~m)) head-set) 
          [:body (if (:js ~m) (dissoc ~m :js) ~m) ~@body]]))

(def #^{:macro true} html-jquery #'html-body) ; 别名
;(defmacro html-jquery [m & body] `(html-body ~m ~@body))

(defn html-apps
  "应用主导航界面，列出所有app（类手机界面方式）"
  [apps]
  (html-body {:onload "app_onload();"} (for [app apps] [:div.app app])))

(defn html-main
  "把多个字符串参数放在定义了main.css, main.js的html body内，@see html-body"
  [& ss]
  (html-body nil (apply str ss)))

;;----------------------------------- dojo wrapper

(defmacro html-dojo
  [onload-js & body]
  `(html [:html 
          [:head meta-utf8 
           [:link {:type "text/css" :rel "stylesheet" 
                   :href (format "%s/dojoroot/dojo/resources/dojo.css" webapp)}]
           [:link {:type "text/css" :rel "stylesheet" 
                   :href (format "%s/dojoroot/dijit/themes/claro/claro.css" webapp)}]
           [:script {:type "text/javascript" :djConfig "parseOnLoad: true" 
                     :src (format "%s/dojoroot/dojo/dojo.js" webapp)} ""]           
           [:script {:type "text/javascript" 
                     :src (format "%s/dojoroot/dijit/dijit.js" webapp)} ""]           
           [:script {:type "text/javascript" 
                     :src (format "%s/dojoroot/dojo-main.js" webapp)} ""]           
           ]
          [:body {:onload ~onload-js :class "claro" :style "padding: 10px"}  ; 在此设置对应的:class "claro"很重要，否则dijit都没有样式
           ~@body]]))

(defn dojo-date
  "得到一个dojo DateTextBox，m参数：如{:id \"d1\" :value \"2011-07-07\"}"
  [m]
   [:input (merge {:value "2010-02-13"
                   :dojoType "dijit.form.DateTextBox"
                   :constraints "{formatLength:'long'}"
                   :required "true"
                   :trim "true"} 
                  m)])

(defn dojo-time
  "得到一个dojo TimeTextBox，m参数：如{:id \"d1\" :value \"T12:03:00\"}"
  [m]
  [:input (merge {:value "T12:30:00"
                  :dojoType "dijit.form.TimeTextBox"}
                 m)])

(defn dojo-text
  "得到一个dojo TextBox，m参数：如{:id \"d1\" :value \"...\"}"
  [m]
  [:input (merge {:dojoType "dijit.form.TextBox" :value "" :intermediateChanges "true"} m)])

(defn dojo-textarea
  "得到一个dojo SimpleTextarea，m参数：如{:id \"d1\" :value \"111<br/>222\"}"
  [m]
  [:textarea (merge {:dojoType "dijit.form.SimpleTextarea" :value "" 
                     :style "width:200px; height:100px"} m)])

(defn dojo-editor
  "得到一个dojo Editor (RichText Editor)，
m参数：如{:id 'd1' :html '111<br/>222'}"
  [m]
  [:div (merge {:dojoType "dijit.Editor"} (dissoc m :html))
   [:p (:html m)]])

(defn dojo-select
  "得到一个dojo FilteringSelect.
   m: {:id 'id1' :name 'id1'} 
   m2: {:ap 'Apple' :or-selected 'Orrage' :pe 'Pears'}"
  [m m2]
  [:select (merge {:dojoType "dijit.form.FilteringSelect"} m)
   (apply str 
          (for [e m2 :let [k (-> e key name) v (-> e val name) sel (.endsWith (str k) "-selected")]] 
            (html [:option 
                   {:value (if sel (left k "-") k) (if sel "selected" "foo") ""} 
                   v]) ))])

(defn dojo-checkbox
  "得到一个dojo CheckBox，
m参数：如{:id 'd1' :html 'agree with this'}"
  [m]
  [:input (merge {:dojoType "dijit.form.CheckBox" :value "y"} (dissoc m :html))
   [:label (:html m)]])

(defn dojo-inline
  "得到一个dojo InlineEditBox，
m参数：如{:id 'd1' :html 'click to edit this text'}"
  [m]
  [:span (merge {:dojoType "dijit.InlineEditBox" :editor "dijit.form.TextBox"
                 :editorParams "{constraints: {places:0} }" :width "100px"} 
                (dissoc m :html))
   (:html m)])

(defn dojo-tabs
  "得到一个dojo TabContainer.
m参数：如{:id \"d1\" :style \"...\"}
m2参数：如[{:title 'Tab1' :html '111'} {:title 'Tab2' :html '222'} {:title 'Tab3' :href '/service2'}] "
  [m m2]
  [:div 
   (merge {:dojoType "dijit.layout.TabContainer" :style "width:350px; height:100px"} m)
   (apply str 
     (map #(html [:div
                  (merge {:dojoType "dijit.layout.ContentPane"} (dissoc % :html)) 
                  (:html %)]) 
          m2))])

(defn dojo-button
  "得到一个dojo Button，m参数：如{:id \"d1\" :html \"Button1\"}"
  [m]
  [:button (merge {:type "button" :dojoType "dijit.form.Button"} (dissoc m :html))
   (:html m)])

(defn dojo-menu
  "得到一个dojo MenuBar，
m: 如{:id 'd1' :name 'd1'}
m2: [{'File' ['Edit' 'Exit']} {'Help' ['About' 'Index']}]"
  [m m2]
;  [{"File" ["File11" "File22"]} {"Edit" ["Edit11" "Edit22"]}]
  (let [fn-items (fn [item] (html 
                              [:div {:dojoType "dijit.PopupMenuBarItem"}
                               [:span (key (first item))]
                               [:div {:dojoType "dijit.Menu"}
                               (map #(apply str (html [:div {:dojoType "dijit.MenuItem"} %]))
                                    (val (first item)))]]
                              ))]
    [:div {:dojoType "dijit.MenuBar"}
     (map fn-items m2)]
      
    ))

(defn dojo-dialog
  "得到一个dojo Dialog,
  m: {:title 'Title1' :html 'this is content'}"
  [m]
  [:div (merge {:dojoType "dijit.Dialog" :title "对话框"} (dissoc m :html))
   (:html m)])

(defn dojo-tooltip
  "得到一个dojo DropDownButton，
  @m: 如{:id 'd1' :html 'click here'}
  @m2: [:div [:label 'Name:'] (dojo-text ..) (dojo-button {:type 'submit'} 'Save')]"
  [m m2]
  [:div (merge {:dojoType "dijit.form.DropDownButton"} (dissoc m :html))
    [:span (:html m)]
    [:div {:dojoType "dijit.TooltipDialog"} m2]])

(defn dojo-tree
  "得到一个dojo DropDownButton，
m-store: 如{:url '/dojoroot/tree1.json'}
m-model: 如{:rootLabel 'Root here'}
m-tree:  如{:html (html [:script..])}"
  [m-store m-model m-tree]
  (html
    [:div (merge {:dojoType "dojo.data.ItemFileReadStore" :jsId "store1" :url "/dojoroot/tree1.json"} 
                 m-store)]
    [:div (merge {:dojoType "dijit.tree.ForestStoreModel" :jsId "model1" :store "store1"
                  :query "{n: 1}" :rootId "root1" :rootLabel "根节点" :childrenAttrs "children"}
                 m-model)]
    [:div (merge {:dojoType "dijit.Tree" :id "tree1" :model "model1" :openOnClick "true"  :showRoot "true"}
                 (dissoc m-tree :html))
     (:html m-tree)]))
   
(defn dojo-title
  "得到一个dojo TitlePane（可收缩的白板）
m: 如{:title 'Title 2' :html 'aaaaaaaa..bbbbbbb'}"
  [m]
  [:div (merge {:dojoType "dijit.TitlePane" :title "Title"} (dissoc m :html)) 
   (:html m) ])

;;----------------------------------- dojo wrapper (-End-)

;------------------------- eui wrapper (-begin-)
(defmacro tags
  "通用的一个[:tag 下合并m0和m，带body的html片段，参数：
  tag: tag如 :div :html
  m0: 原来的属性map
  m: 客户的属性map
  body: body"
  [tag m0 m & body]
  `(html [~tag (merge ~m0 ~m) ~@body]))
  
(defn eui-tip
  "带图标的提示信息，s: 提示内容"
  [s]
  [:div.eui-info
   [:div {:class "eui-tip icon-tip"}] [:div s]])

(defn eui-button
  "把一个[:a}连接显示为一个圆角按钮
  m: [:a}的属性，s: 显示标签"
  [m s]
  (tags :a {:class "easyui-linkbutton" :href "#" :onclick ""} m s))

(defn eui-button-close
  "关闭窗口的按钮"
  []
  (eui-button {:onclick "window.close();"} "关闭"))

(defn eui-button-submit
  "submit form的按钮. @fm fm的id字符串，如'fm1' "
  [form-id]
  (eui-button {:onclick (format "$('#%s').submit()" form-id) :iconCls "icon-ok"} "提交"))

(defn eui-button-reset
  "reset form的按钮. @fm fm的id字符串，如'fm1' "
  [form-id]
  (eui-button {:onclick (format "$('#%s')[0].reset()" form-id) :iconCls "icon-undo"} "重置"))

(defn eui-accord
  "Accordion界面框架，内容用eui-accord-"
  [m & items]
  (tags :div {:class "easyui-accordion1" :style "width:350px; height:550px"} m items))

(defn eui-accord-
  "Accordion界面里的一项，title: 标题, s: 内容" 
  [m title & body]
  (tags :div {:title title :iconCls "icon-ok" :style "overflow:auto;padding:10px;"} m body))

(defn eui-calendar
  "显示一个当月日期表"
  ([m]
    (tags :div {:class "easyui-calendar" :style "width:180px;height:180px;"} m nil))
  ([] (eui-calendar nil)))

(defn eui-datebox
  "日期选择器"
  [m] 
  (tags :input {:class "easyui-datebox" :required "true"} m))
  
(defn eui-datetimebox
  "日期+时间选择器"
  [m]
  (tags :input {:class "easyui-datetimebox" :required "true"} m))
  ;(eui-datebox (merge {:class "easyui-datetimebox"} m)))
  
(defn eui-combo
  "得到一个下拉选择列表，需要在js中初始化: $('#id1').combobox()，多选可以 $('#id1').combox({multiple:true}) ，参数：
  m: {:id 'id1' :value '02'} id是必须的, :value 缺省选中的value
  options: 如'{01 java 02 ruby 03 python}"
  [m options]
  (tags :select m nil (for [[k v] options] [:option (merge {:value k} (when (= k (:value m)) {:selected "true"})) v])))
;  (tags :select {:id id} nil (map #(vector :option {:value (key %)} (val %)) options)))

(defn eui-text
  "输入必填文本框, 
  有:required属性，无论值为true，false，都代表必须填写"
  [m]
  (tags :input {:class "easyui-validatebox" :type "text"} m))

(defn eui-textarea
  "输入必填多行文本区, 
  有:required属性，无论值为true，false，都代表必须填写"
  [m & text]
  (tags :textarea {:class "easyui-validatebox" :style "height: 100px"} m text))

(defn eui-email
  "输入email框，发生变化时校验"
  [m]
  (eui-text (merge {:validType "email"} m)))

(defn eui-url
  "输入url框"
  [m]
  (eui-text (merge {:validType "url"} m)))

(defn eui-numberbox
  "输入限定范围的数字，失去焦点后格式化，非数字格式化为空"
  [m]
  (tags :input {:class "easyui-numberbox" :min "0" :max "100" :precision "2"} m))

(defn eui-numberspin
  "上下箭头选择限定范围的数字"
  [m]
  (tags :input {:class "easyui-numberspinner" :min "0" :max "100"  :increment "10"} m))
  
(defn eui-dialog
  "显示一个对话框，参数：
  @id: 对话框id, 如myid1，对应按钮id为 '#myid1_ok', '#myid1_cancel'，需要自己写js响应click事件（否则报错）
  @m: 对话框属性
  @body: 对话框内容"
  [id m & body]
  (html
    (tags :div {:id id :class "easyui-dialog1" :title "对话框1" :resizable1 "true"
                :style "width:400px;height:200px; left:400px;top:150px;padding:10px"
                :buttons (format "#%s-buttons" id)} m body)
    (tags :div {:id (str id "-buttons")} nil
          (eui-button {:id (str id "_ok") :iconCls "icon-ok"} "确定")
          (eui-button {:id (str id "_cancel")} "取消")) ))

(defn eui-window
  "显示一个窗口，和dialog相似，但右上角多几个按钮"
  [m & body]
  (tags 
    :div 
    {:class "easyui-window" :closed "true" :modal "true" :title "Test Window" :style "width:500px;height:300px;"} 
    m body))

(defn eui-layout
  "layout 布局，内含多个eui-region
  可用js操作：$('#layout1').layout('collapse','west');"
  [m & body]
  (html-body (merge {:class "easyui-layout"} m) body))
  
(defn eui-region
  "eui-layout的各个块，参数：
  position: north, west, east, south, center
  m: 属性
  body: 内容"
  [position m & body]
  (tags :div {:region position :border "true" :split "false" :style "min-height: 30px"} m body ))

(defn eui-context-menu
  "右键菜单"
  [id m s]
  (tags :div {:id id :class "easyui-menu1" :style "width:120px;"} m s))

(defn eui-menubar
  "下拉菜单条, 和splitbutton不同，主菜单项仅显示"
  [m & menus]
  (tags :div {:style "background:#C9EDCC; padding:5px; width:600px;"} m menus))
  
(defn eui-menu
  "下拉菜单条下的菜单项"
  [id m s]
  (tags :a {:class "easyui-menu1button" :href "javascript:void(0)" :menu1 (str "#" id)} m s))
  
(defn eui-pager
  "分页条，需要在js中写代码"
  [id]
  (tags :div {:style "background:#efefef; border:1px solid #ccc;"} {:id id}))

(defn eui-panel
  "显示panel面板，closed true时不显示不占位"
  [m & body]
  (tags
    :div
    {:class "easyui-panel" :style "width:500px; height:200px; padding:5px;"
     :title "Panel标题" :iconCls "icon-save"
     :collapsible "true" :minimizable "true"
     :maximizable "true" :closable "true"}
    m body))

(defn eui-progressbar
  "显示一个进度条，参数:
  m: 如 {:value 85} 表示80%"
  [m]
  (tags :div {:class "easyui-progressbar1" :style "width:300px;" :value 80} m))
  
(defn eui-searchbox
  "搜索输入框，可以下拉选择不同搜索范围。参数：
  m: 属性，例如 {:id 'id1' :searcher 'f1' }
  menu: 下拉搜索范围选择框，如: [:div {:name '..' :iconCls 'icon-ok'} '..'] [:div ...]"
  [m & menu]
  (html
    (tags :input {:class "easyui-searchbox" :searcher "eui_search1" :prompt "请输入搜索关键字" 
                  :menu1 "#searchbox_menu" :style "width:300px"} m)
    (tags :div {:id "searchbox_menu" :style "width:120px"} nil 
          (or menu [:div {:name "查询" :iconCls "icon-ok"} "查询"] ))))

(defn eui-tabs
  "显示Tabs标签页框架"
  [m & body]
  (tags :div {:class "easyui-tabs1" :style "width:700px;height:250px;"} m body))

(defn eui-tab1
  "Tabs标签页的一页"
  [title m & body]
  (tags :div {:title title :closable "true" :style "padding:10px;"} m body))

(defn eui-tree
  "显示一个Tree目录树，所有的子项目为 [:li [:ul"
  [m & body]
  (tags :ul {:class "easyui-tree" :animate "true" :dnd "true"} m body))

;; eui layout 常用模式
(defn eui-foot-region
  "layout.south"
  ([m info] (eui-region "south"
                  (merge {:style "height: 30px; background: #cde; padding: 5px; text-align: center; color: #444"} m) 
                  (or info (str "版本信息: IDP集成数据平台系列——行业支撑平台 @" (year)))))
  ([] (eui-foot-region nil nil)))

;---------------------------------- eui wrapper (-end-)

(defn space 
  "1个或者n个html占位空格"
  ([] "&nbsp;")
  ([n] (apply str (repeat n "&nbsp;"))))

;---------------------------------- eui frame layout (-begin-)
(defn- frame-top-menu
  [cfg]
  (eui-tabs
    {:style "height: 65px; position: absolute; left:0px; bottom: 0px;"}
    (for [[title & m2] (:menu cfg)]
      (eui-tab1
        (html [:span {:style "font-size: 14px; padding-left: 10px; padding-right: 10px"} title]) 
        {:closable "false" :style "padding: 4px"} 
        (for [[title id] m2] 
          (eui-button {:id id :plain "true" :iconCls "icon-arrow" :group2 "menu2"} title)) ))))
  
(defn frame-top
  "layout.north"
  [cfg]
  (eui-region 
    "north" 
    {:id (first (:style cfg)) :style (format "height: %spx; padding: 10px;" (if (:menu cfg) 135 70)) }
    [:span {:class (second (:style cfg))} (:title cfg)]
    [:div {:style "position: absolute; right: 10px; top: 8px; color: lightgray"} "当前用户: " 
     [:span#wr3user {:style "color:lightgray; font-weight:bold"} ".."] (space 3)
     [:script (format "app_user('%s')" (:name cfg))]
     [:a {:href "#" :onclick (format "app_exit('/%s')" (:name cfg))} "退出"] ]
    ; 搜索条
    (when (:searcher cfg)
      [:div {:style "position: absolute; right: 10px; top: 35px"}
       (eui-searchbox 
         {:searcher (str (:name cfg) "_search") :style "width: 250px;" :value (first (:searcher cfg))} 
         (for [[label nam icon] (rest (:searcher cfg))]
           [:div {:name nam :iconCls icon :style "margin: 0px"} label] ))])
    ; 1、2级导航条
    (when (:menu cfg) (frame-top-menu cfg)) ))

(defn frame-left-right
  "layout.west | layout.east"
  [cfg]
  (eui-region 
    (if (= "right" (:left-or-right cfg)) "east" "west") 
    {:title "快捷导航" :style "width: 210px"}
    [:div {:style "margin: 10px"} 
     (eui-button {:href (or (:main cfg) (str "/" (:name cfg))) :plain "true" :iconCls "icon-back" } "返回首页") ]
    (eui-accord 
      {:id "accord1" :style "" }
      (for [[title icon & nav2] (:nav cfg)]
        (eui-accord- 
          {:iconCls icon} (str (space 2) title) ; 在icon和文字之间加上2个空格的间隙
          (for [[title icon id url] nav2]
            (let [url2 (if (and url (.startsWith url "/")) url
                         (format "/c/%s/%s" (:name cfg) (or url id)))]
              (html (eui-button {:id id :plain "true" :iconCls icon :title url2
                                 :onclick (format "layout_load_center('%s')" url2) }
                                title) [:br] ) ))) ))))

(defn frame-main
  "layout.center"
  [cfg]
  (eui-region 
    "center" 
    {:border "false" :style "padding: 10px"} 
    (if-let [fmain (:frame-main cfg)]
      fmain
      (html
        [:h2 "主显示页面"] ))))

(defn frame-foot
  "layout.foot"
  [cfg]
  (eui-foot-region))

(defn frame-index
  "@cfg 含如下参数的hashmap：
  :name      应用名称，用于javascript函数的前缀，如 function demo_xxx()
  :style     上方色调，[\"layout_north\" \"layout_title\"]（蓝色）或 [\"layout_north1\" \"layout_title1\"]（金色）
  :title     应用名称，位于上方
  :searcher  搜索框的label，位于右上方
  :nav       导航条，位于左边
  :menu      2层菜单条，位于上方
  @m 客户化定制hashmap，有如下参数：
  :left-or-right 'left' or 'right'
  :js 包含在<head>中的.js文件，如： 'a.js' 或 ['a.js' 'b.js']
  :after 包含在<body>最后的html片段
  :top/left/right/main/foot 或者 frame-top | frame-left | frame-right | frame-main | frame-foot 的html片段
  "
  [cfg]
  (eui-layout
    {:id "layout1" :onload (str (:name cfg) "_onload()") :js (:js cfg) }
    ;----------------------- north
    (frame-top cfg)   
    ;----------------------- west or east
    (frame-left-right cfg)
    ;----------------------- center
    (frame-main cfg)  
    ;----------------------- south
    (frame-foot cfg) 
    (:after cfg) ; 补充在top部分后面的html片段 
    ))
  
;---------------------------------- eui frame layout (-end-)

(defn fileupload-field
  "文件上传相关的2个字段：（1）一个hidden字段，上传成功文件后显示文件链接；（2）一个上传按钮，弹出真正的文件选取上传； 
  @nam 字段名称如'简历' 
  @sid 字段id、name如'resume'
  @v 初始赋值 
  @m 字段的其他属性如 {:id .. :name .. :title ..}  "
  [nam sid v m] 
  (html 
    (eui-text (merge m {:id sid :name sid :type "hidden" :value (or v "")}))
    [:span (when (not (nullity? v))
             (html [:a {:href v :target "_blank"} "查看"] (space 3)))]
    (eui-button {:onclick (format "fileupload_dlg_open('%s', '%s')" (str nam) sid)} "上传文件..")))

(defn fileupload-dialog
  "生成初始不可见的文件保存对话框的html，由上传按钮的 js 函数唤出。"
  []
  (html (eui-dialog "fileupload" {:closed "true" :href "/c/pub/fileupload"})
        [:script "fileupload_bt()"]))

(defn input-form
  "统一录入表单，根据如cfg文件来确定字段。 
  @cfg 表单录入项的配置，['字段显示字符串' :字段名称 {字段属性}]：
    [ [\"字段Label\" :field-name {:require true :t 'file :title \"字段说明\"}]
      [\"性别\" :sex {:t [\"男\" \"女\"]}] ...]
    字段属性中：:require true/false 表示是否必填
                :t 表示类型，不写表示text，vector和map表示下拉列表，其他特殊：'textarea 'file 'email
  @m 含 {:action :title :buttons :others ..} 的其他定制化参数, 
    :action为执行保存的url；
    :title为标题，
    :buttons 为提交保存取消等自定义按钮 
    :others 其他自定义属性
  注意：如果有{:t 'file}的字段，记着在后面放置 (fileupload-dialog)  "
  [cfg m]
  (let [css-label "font-family:微软雅黑;font-size:14px;vertical-align:center;height:35px;border-bottom:1px dotted gray"]
    (html
      [:form {:id "fm1" :method "POST" :action (:action m)}
       [:table {:style "margin-left: 30px"}
        [:caption {:style "padding: 5px"} [:h1 (m :title)]]
        (for [[nam id {t :t v :v require :require title :title}] cfg]
          (let [sid (name id)
                attr {:id sid :name sid :value v :title title 
                      :required (if (true? require) "true" nil)}] ; 通用attrib
            [:tr 
             [:td {:style css-label} [:label (str nam "：")]]
             [:td {:style "border-bottom:1px dotted gray"}
              (cond 
                (= t 'textarea)  (eui-textarea attr v)
                (= t 'file)      (fileupload-field nam sid v attr)
                (= t 'date)      (eui-datebox  attr)
                (= t 'email)     (eui-email    attr)
                (map? t)         (eui-combo    attr t)
                (vector? t)      (eui-combo    attr (apply array-map (flatten (for [e t] [e e]))))
                :else            (eui-text     (into attr {:style "width: 250px"}))) ]
             [:td [:font {:color (if require "red" "lightgray")} "*"]] ]))
        [:tfoot [:tr [:td {:colspan 3 :align "center" :style "padding: 15px"} 
                      [:p "注：带红色<font color='red'>*</font>的为必填项。"] (:buttons m) ]]]]] )))

(defn input-check-require
  "检查根据input配置@cfg录入的结果@vars中，是否所有:require true的项都确实录入了。
  @vars 输入内容如{:link 'url1', :file '', :type '1', :title 'title111'} 
  @cfg input表单配置如： (def cfg-portal [ ['类型' :type {:t dd-portal :require true}]
	                                         ['标题' :title {:require true}] ['连接' :link {}] ])
  @return 形如 {:rt true/false :error nil/'('类型' '标题') }。其中:rt为true表示必填项全填了，:error 是未填的必填项的名称列表 "
  [vars cfg]
  (let [rt (for [[fname fid {require :require t :t}] cfg :when require]
             (if (nullity? (vars fid)) fname true))]
    (if (apply all-true? rt) 
      {:rt true :error nil} 
      {:rt false :error (remove true? rt)}) ))

(defn set-title 
  "用js更改浏览器的title"
  [s] [:script (format "document.title='%s'" s)])

;;;---------------------------- jquery mobile
