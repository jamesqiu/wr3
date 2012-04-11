(ns ^{:doc "常用eui layout布局的demo应用模板"
      :todo "待成熟后把通用函数都命名为frame-xx封装到wr3.clj.web中"} 
     wr3.clj.app.demo)

(use 'hiccup.core)
(use 'wr3.clj.web)

(require '[wr3.clj.app.auth :as au])
(defn auth
  "该函数被 CljServlet 调用 "
  [request fname & args]
  (cond
    (session request "wr3user") true
    :else false))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;; layout

;; app-conf 说明：
;; :name            应用名称，用于javascript函数的前缀，如 function demo_xxx()
;; :style           上方色调，["layout_north" "layout_title"]（蓝色）或 ["layout_north1" "layout_title1"]（金色）
;; :title           应用名称，位于上方
;; :searcher        搜索框的label，位于右上方
;; :nav             导航条，位于左边
;; :menu            2层菜单条，位于上方
(def app-conf
  {:name "demo"
   :style (map #(str % "1") ["layout_north" "layout_title"])  ; "" 蓝色 或者 "1" 金色
   :title "IDP行业应用——演示系统"
   :searcher ["000001" ; default-value
              ["AA范围" "range1" "icon-ok"] ; label name icon
              ["BB范围" "range2" "icon-tip"] ]
   :nav [["业务工作"            "icon-pen" ; title id
          ["综合业务"          "icon-sum"    "indic0_bt"] ; title icon id 
          ["列表查看"          "icon-list"   "hs300_bt" ]
          ["统计报表"          "icon-pie"    "report3_bt"]]
         
         ["系统管理及帮助"      "icon-search"
          ["网站样式"          "icon-search" "site_bt"]
          ["使用帮助"          "icon-help"   "help_bt"]]
         ]   
   :menu [["基础数据查看" 
           ["进场登记表"     "11-reg"] 
           ["登记细项"       "12-reg2"]
           ["交易明细流水表" "13-trade"]
           ["菜品代码表"     "14-variety"]]
          
          ["统计报表管理"
           ["结算中心报表"   "21-jszx"]     
           ["蔬菜部报表*"    "22-scb"]     
           ["质管科报表"     "23-zgk"]     
           ["其他报表"       "24-other"]]     
          
          ["数据分析*"
           ["进场登记分析*" "31-enter"] 
           ["交易流水分析" "32-trade"] 
           ["综合分析*" "33-other"]]
          
          ["其他"
           ["系统管理" "41-admin"] 
           ["帮助" "42-help"]]
          ] 
   })

(def top-height (if (:menu app-conf) 135 85))

(defn- top-menu
  []
  (eui-tabs
    {:style "height: 65px; position: absolute; left:0px; bottom: 0px;"}
    (for [[title & m2] (:menu app-conf)]
      (eui-tab1
        (html [:span {:style "font-size: 14px; padding-left: 10px; padding-right: 10px"} title]) 
        {:closable "false" :style "padding: 4px"} 
        (for [[title id] m2] 
          (eui-button {:id id :plain "true" :iconCls "icon-arrow" :group2 "menu2"} title)) ))))
  
(defn- app-top
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id (first (:style app-conf)) :style (format "height: %spx; padding: 10px;" top-height) }
    [:span {:class (second (:style app-conf))} (:title app-conf)]
    [:div {:style "position: absolute; right: 10px; top: 8px; color: gray"} "当前用户: " 
     [:span#wr3user {:style "color:red; font-weight:bold"} ".."] (space 3)
     [:script "app_user()"]
     [:a {:href "#" :onclick "app_exit('/c/demo/layout1')"} "退出"]]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher (str (:name app-conf) "_search") :style "width: 250px;" :value (first (:searcher app-conf))} 
       (for [[label nam icon] (rest (:searcher app-conf))]
         [:div {:name nam :iconCls icon :style "margin: 0px"} label] ))]
    ; 1、2级导航条
    (when (:menu app-conf) (top-menu)) ))

(defn- app-left
  "layout.west"
  []
  (eui-region 
    "west" 
    {:title "快捷导航" :style "width: 210px"}
    [:div {:style "margin: 10px"} "全局信息提示内容……" ]
     
    (eui-accord 
      {:id "accord1" :style "" }
      (for [[title icon & nav2] (:nav app-conf)]
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

(defn index
  []
  (eui-layout
    {:id "layout1" :onload (str (:name app-conf) "_onload()")}
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
