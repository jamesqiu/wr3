(ns ^{:doc "常用eui layout布局的demo应用模板"} 
     wr3.clj.app.demo)

;;;------ 必须的
(use 'hiccup.core)
(use 'wr3.clj.web)
;;;------ 可选的

(require '[wr3.clj.app.auth :as au])
(defn auth
  "该函数被 CljServlet 调用 "
  [request fname & args]
  (cond
    (session request "wr3user") true
    :else false))

(defn index
  ""
  []
  (html-body
    [:h1 "使用说明及链接"]
    [:h2
     [:a {:href "/c/demo/layout1"} "蓝色模板"] (space 7)
     [:a {:href "/c/demo/layout2"} "金色模板"]
     ]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; layout

;; app-conf 说明：
;; :id              应用的标识，用于?
;; :title           应用名称，位于上方
;; :search-title    搜索框的label，位于右上方
;; :nav             导航条，位于左边
;; :menu            菜单条，位于上方
(def app-conf
  {:id "app1"
   :title "IDP行业应用——演示系统"
   :search-title "代码模糊查询"
   
   :nav [["业务工作"            "icon-pen" ; title id
          [["综合业务"          "icon-sum"    "indic0_bt"] ; title icon id 
           ["列表查看"          "icon-list"   "hs300_bt" ]
           ["统计报表"          "icon-pie"    "report3_bt"]]]
         
         ["系统管理及帮助"      "icon-search"
          [["网站样式"          "icon-search" "site_bt"]
           ["使用帮助"          "icon-help"   "help_bt"]]]]
   
   :menu [["核心业务数据查询"   "icon-pen"    "m1"
           ["客户信息"          "icon-sum"    "m11"
            ["名称查询"         ""            "m111"]
            ["客户统计"         ""            "m112"]]
           ["客户经理信息"      "icon-sum"    "m12"]
           ]
          ]
   })

(defn- menubar
  "生成menubar，数据来源于(:nav app-conf) "
  []
  (html
    (eui-menubar 
      {}
      (eui-menu "mn1" {:id "m1" :iconCls "icon-edit"} "[eui-menubar/eui-menu]")
      (eui-menu "mn2" {:iconCls "icon-search"} "搜索") 
      (eui-menu "mn3" {:iconCls "icon-help" :plain "false"} "帮助")
      ) 
    [:div {:id "mn1" :style "width: 200px"}
     [:div "和eui-menubar并列的div"]
     [:div {:class "menu1-sep"}]
     [:div {:iconCls "icon-cut"} "Cut"]
     [:div
      [:span "Toolbar"]
      [:div {:style "width:150px;"}
       [:div "New Toolbar..."] ]]]
    [:br]))

(defn- app-top
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id "layout_north" :style "height: 80px; padding: 10px;" }
    [:span {:class "layout_title"} (:title app-conf)]
    [:div {:style "position: absolute; right: 10px; top: 8px; color: gray"} "当前用户: " 
     [:span#wr3user {:style "color:red; font-weight:bold"} ".."] (space 3)
     [:script "app_user()"]
     [:a {:href "#" :onclick "app_exit('/c/demo/layout1')"} "退出"]]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher "demo_search" :style "width: 250px;" :value "000001" :title (:search-title app-conf)} 
       [:div {:name "范围A" :iconCls "icon-ok" :style "margin: 0px"} "AA范围"]
       [:div {:name "范围B" :iconCls "icon-tip" :style "margin: 0px"} "BB范围"])]          
    ))

(defn- app-left
  "layout.west"
  []
  (eui-region 
    "west" 
    {:title "快捷导航" :style "width: 210px"}
    [:div {:style "margin: 10px"} "全局信息提示内容……" ]
     
    (eui-accord 
      {:id "accord1" :style "" }
      (for [[title icon nav2] (:nav app-conf)]
        (eui-accord- 
          {:iconCls icon} title
          (for [[title icon id] nav2]
            (html
              (eui-button {:id id :plain "true" :iconCls icon} title) [:br] ) ))) )))

(defn- app-main
  "layout.center"
  []
  (eui-region 
    "center" 
    {:border "false" :style "padding: 10px"} 
    [:h2 "主显示页面"] ))

(defn layout1
  "app: 蓝色背景的app模板"
  []
  (eui-layout
    {:id "layout1" :onload "demo_onload()"}
    ;----------------------- north
    (app-top)   
    ;----------------------- west
    (app-left)  
    ;----------------------- center
    (app-main)  
    ;----------------------- east
;    (app-right)
    ;----------------------- south
    (eui-foot-region) ))

(defn layout2
  "app: 黄色背景的app模板"
  []
  nil)