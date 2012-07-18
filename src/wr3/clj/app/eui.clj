(ns wr3.clj.app.eui)
;;;; JQuery EasyUI 测试
(use 'wr3.clj.s 'wr3.clj.u 'wr3.clj.web); :reload)
(use 'hiccup.core)

;------------------------- menubar, accord, other 函数用于index应用
(defn- menubar
  "用于index app"
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
    
(defn- accord
  "用于index app"
  []
  (eui-accord 
    {:style ""}
    (eui-accord- 
      nil "(eui-accord/eui-accord-)"
      (html
        [:h3  "(eui-button)"]
        (eui-button {:id "dlg1_bt"} "开关Dialog") 
        (eui-button {:id "win1_bt"} "开关Window") 
        (eui-button {:id "panel_bt"} "显示Panel") 
        (eui-button {:id "progress_bt"} "进度条演示") 
        [:br]
        (eui-button {:plain "true" :iconCls "icon-search"} "属性plan true") 
        (eui-button {:plain "true" :iconCls "icon-help"} "帮助") 
        [:br]
        (eui-button {:plain "true" :iconCls "icon-ok"} "") 
        (eui-button {:plain "true" :iconCls "icon-cancel"} "") 
        [:br]
        (eui-button {:href "http://g.cn" :target "_blank"} "URL 按钮") [:br] ))
    (eui-accord- 
      {:selected "true"} "输入/选择"
      (html
        [:label "(eui-calendar)"] (eui-calendar)
        [:br][:label "下拉框(eui-combo，需js)："] 
        (eui-combo {:id "s1"} '{01 java 02 ruby 03 python 04 clojure 05 scala})
        [:br][:label "下拉框多选(eui-combo，需js)："] 
        (eui-combo {:id "s2"} '{01 java 02 ruby 03 python 04 clojure 05 scala})
        [:br][:label "选日期(eui-datebox)："]
        (eui-datebox nil)
          [:br][:label "选时间(eui-datetimebox)："]
          (eui-datetimebox {:style "width: 200px" :value "2011-9-1 12:00:00"})
          [:br][:label "录必填项(eui-text)："]
          (eui-text nil)
          [:br][:label "录email(eui-email)："]
          (eui-email {:required "true"})
          [:br][:label "录url(eui-url)："]
          (eui-url {:required "true"})
          [:br][:label "录多行文本(eui-textarea)："]
          (eui-textarea {:style "height: 30px"})
          [:br][:label "输入1-100的数字(eui-numberbox)："] 
          (eui-numberbox {:min 1})
          [:br][:label "选择0-100的数字(eui-numberspin)："] 
          (eui-numberspin nil)
          (eui-searchbox 
            {:searcher "eui_search1" :style "width: 300px"} 
            [:div {:name "范围A" :iconCls "icon-ok" :style "margin: 0px"} "搜索(eui-searchbox)"]
            [:div {:name "范围B" :style "margin: 0px"} "局部搜索"])          
          ))
    (eui-accord- 
      {:iconCls "icon-reload"} "菜单, 弹出信息" 
      (html 
        [:h2 "代码查看main.js"]
        (eui-button {:id "cmenu_button"} "右键菜单(eui-context-menu)") [:br]
        (eui-button {:plain "true" :id "message1"} "弹出信息($.messager.show)")
        (eui-button {:plain "true" :id "progress1"} "弹出进度条($.messager.progress") [:br]
        (eui-button {:plain "true" :id "alert1"} "Alert($.messager.alert)")
        (eui-button {:plain "true" :id "error1"} "Error")
        (eui-button {:plain "true" :id "info1"} "Info")
        (eui-button {:plain "true" :id "ask1"} "Question")
        (eui-button {:plain "true" :id "warn1"} "Warning") [:br]
        (eui-button {:plain "true" :id "confirm1"} "Confirm($.messager.confirm)") 
        (eui-button {:plain "true" :id "prompt1"} "Prompt($.messager.prompt)")
        (eui-tree 
          {}
          [:li [:span "Folder(eui-tree)"]
           [:ul 
            [:li {:state "closed"} [:span "子目录1"]
             [:ul 
              [:li [:span [:a {:href "#"} "File 11"]] ]
              [:li [:span "File 12"] ]
              [:li [:span "File 13"] ] ] ]
            [:li [:span "File 2"] ]
            [:li [:span "File 3"] ]
            [:li "File 4"]
            [:li "File 5"] ] ]
          [:li [:span "File21"] ] )
          ))))

(defn- other
  "用于index app"
  []
  (html
    (eui-tip "提示信息(eui-tip)")
    
    (eui-dialog 
      "dlg1" {:title "测试(eui-dialog)" :closed "true"} 
      (html [:h1 "测试"] [:h2 "对话框内容……"]))
    
    (eui-window {:id "win1" :title "测试(eui-window)" :style "width: 500px; height: 300px; padding: 10px"} [:h1 "Windows"])
    
    [:h3 "(eui-pager) $('#pager1').pagination"]
    (eui-pager "pager1")  ; 分页条，请查看js  
    
    (eui-context-menu 
      "cm1" {} 
      (html [:div "菜单1"] 
            [:div 
             [:span "菜单2"] 
             [:div {:style "width: 120px"}
              [:div {:iconCls "icon-ok"} "菜单21"] 
              [:div [:b "菜单22"]]
              ]]
            [:div {:class "menu1-sep"}]
            [:div {:iconCls "icon-save"} "菜单3"])) [:br]
    
    [:h3 "eui-progressbar"]
    (eui-progressbar
      {:id "pb1" :value 50}) [:br]
    
    (eui-panel 
      {:id "panel1" :closed "true" :title "测试(eui-panel)"
       :style "color: blue; width: 500px; padding: 5px" }
      (for [i (range 7)] [:p (str "Panel " i)]) )
    
    (eui-tabs 
      nil
      (eui-tab1 "(eui-tabs/eui-tab1)" {:closable "false"} "hello")
      (eui-tab1 "Tab2" {:closable "true" :style "padding:20px" :cache "false" :href "/c/test"}
                "This is Tab2 with close button.")
      (eui-tab1 "Tab3" {:iconCls "icon-reload" :closable "true" :style "padding:20px;"}
                "Table 333")
      (eui-tab1 "Tab4" {:closable "true"}
                "Table 444")
      (eui-tab1 "Tab5 with sub tabs" {:closable "true" :iconCls "icon-cut" :style "padding:10px"}
                (eui-tabs {:fit "true" :plain "true" :style "height:100px;width:300px"}
                          [:div {:title "title1" :style "padding:10px;" } "Content 1"]
                          [:div {:title "title2" :style "padding:10px;" } "Content 2"]
                          [:div {:title "title3" :style "padding:10px;" } "Content 3"])))
    ))  

(defn index
  "app: easyui的例子" 
  []
  (html-jquery
    {:onload "eui_onload()"}
    [:h1 "打开EasyUI自带Demo: " (space 3)
     [:a {:href "F:/lib/jQuery/EasyUI/jquery-easyui-1.2.4/demo/" :target "_blank"} "[Demo]"] (space 3) 
     [:a {:href "http://easyui.btboys.com/api/" :target "_blank"} "[API doc]"] (space 6)
     "Layout的例子: "
     [:a {:href "/c/eui/layout" :target "_blank"} "Google地图/地球"]
     ]
    (menubar)
    (tags 
      :div {:class "easyui-layout" :style "width: 100%; height: 700px"} nil
      (eui-region "west" {:title "eui-region" :style "width: 360px" :split "true"} (accord))
      (eui-region "center" {:border "true" :style "padding: 10px"} (other))
      (eui-region "south" {:style "height: 40px; background: #cceeff; padding: 10px; text-align: center"} "版本信息等")
      )))

(def app-menu
  {:应急值守 {
          :即时通讯 {} 
          :通讯录 {}
          :文电公告 {}
          }
   :指挥调度 {
          :事件接报 {}
          :指挥调度 {}
          }
   :查询统计 {
          :历史情况 {} 
          :最新统计 {} 
          :智能分析 {}
          }
   :通知公告 {
          :政策 {}
          :法规 {} 
          :文电公告 {}
          }
   :功能演示 {
          :Google地图 {:id "gmap_bt"}
          :Google地球 {:id "gearth_bt"}
          :Arcgis地图 {:id "arcgis_bt"}
          :清空 {:id "foo_bt"}
          }
   })
    
(defn- app-top
  "layout.north"
  []
  (eui-region 
    "north" 
    {:id "layout_north" :style "height: 135px; padding: 10px;" }
    [:span {:class "layout_title"} "IDP应急指挥管理支撑平台"]
    [:div {:style "float: right"} "当前用户: 系统管理员"]
    ; 搜索条
    [:div {:style "position: absolute; right: 10px; top: 35px"}
     (eui-searchbox 
       {:searcher "eui_search1" :style "width: 250px;"} 
       [:div {:name "范围A" :iconCls "icon-ok" :style "margin: 0px"} "功能搜索"]
       [:div {:name "范围B" :iconCls "icon-tip" :style "margin: 0px"} "局部搜索"])]          
    ; 1、2级导航条
    (eui-tabs 
      {:style "height: 65px; position: absolute; left:0px; bottom: 0px;"}
      (map-indexed 
        (fn [i [m1 m2]] 
          (eui-tab1 
            (html [:span {:style "font-size: 14px; padding-left: 10px; padding-right: 10px"} m1]) 
            {:closable "false" :style "padding: 4px" (if (= (++ i) (count app-menu)) "selected" "foo") "true"} 
            (map 
              #(eui-button 
                 (merge {:plain "true" :iconCls "icon-arrow"} (val %)) (key %)) 
              m2))) 
        app-menu)
      )))

(defn- app-left
  "layout.west"
  []
  (eui-region 
    "west" 
    {:title "快捷导航" :style "width: 200px"} 
    ) )

(def gmap-links
  (array-map
    :gtask "公司项目组分布"
    :gip "你在哪里上网？"
    :gcoord "显示坐标等信息"
    :gmark "用气泡标记位置"
    :gclick "点击切换焦距"
    :grandom "随机标记多个位置"
    :gclick2mark "点击标记位置"
    :gnoui "不显示UI控件"
    :gcui "自定义UI控件"
    :gpie "数量可视化"
    :grect "区域覆盖"
    :gpic "图片覆盖"
    :gkm "kml/kmz 图层"
    :gfusion "Fusion 图层"
    :gtraffic "实时交通路况"
    :gphoto "全世界的照片库"
    :gfly "飞行航拍角度"
    :gmine "黄金集团矿企"
    :groad "行程标示"
    :ggeo "地球几何路径"
   ))

(def gearth-links
  (array-map
    :g3hello "Hello Google地球"
    :g3basic "定点查看目标"
    ))

(defn app-left-gmap
  "app: 在左边west区域显示google地图例子"
  []
  (eui-accord 
    {:id "accord1" :style "" }
    (eui-accord- 
      {:iconCls "icon-search"} "Google Map 示例"
      (map-indexed 
        (fn [i [k v]] 
          (eui-button 
            {:id (str (name k) "_bt") :plain "true" :iconCls "icon-search" :title (name k) :group "gmap"} 
            (format "%d. %s" (++ i) v))) 
        gmap-links) 
      ;--- <定位>
      (eui-menubar 
        {:style ""} 
        (eui-menu "gmenu1" {:iconCls "icon-search"} "定位到特定地点"))
      [:div {:id "gmenu1" :style "width: 200px"}
       [:div {:id "glocate1_bt" :iconCls "icon-arrow"} "艾菲尔铁塔"]
       [:div {:id "glocate2_bt" :iconCls "icon-arrow"} "八达岭长城"]
       [:div {:id "glocate3_bt" :iconCls "icon-arrow"} "珠穆朗玛峰"]
       [:div {:id "glocate4_bt" :iconCls "icon-arrow"} "尼亚加拉瀑布"]
       [:div {:id "glocate5_bt" :iconCls "icon-arrow"} "金字塔"]]
      ;--- </定位>
      (eui-button {:id "gmap_help_bt" :plain "true" :iconCls "icon-help" 
                   :href "http://code.google.com/apis/maps/documentation/javascript/examples/index.html" :target "_blank"} 
                  "Google Map 帮助") [:br]
      )))

(defn app-left-gearth
  "app: 在左边west区域显示google地球3D例子"
  []
  (eui-accord 
    {:id "accord1" :style "" }
    (eui-accord- 
      {:iconCls "icon-search"} "Google Earth 示例"
      (map-indexed 
        (fn [i [k v]] 
          (eui-button 
            {:id (str (name k) "_bt") :plain "true" :iconCls "icon-search" :title (name k) :group "gearth"} 
            (format "%d. %s" (++ i) v))) 
        gearth-links) 
      (eui-button {:id "gearth_help_bt" :plain "true" :iconCls "icon-help" 
                   :href "http://code.google.com/apis/earth/documentation/examples.html" :target "_blank"} 
                  "Google Earth 帮助") [:br]
      )))

  
(defn app-left-foo
  "app: 在左边west区域显示空"
  []
  (html 
    [:ul
     [:li [:h3 "刷新"]]]))
  
(defn- app-main
  "layout.center"
  []
  (eui-region 
    "center" 
    {:border "false" :style "padding: 2px"} 
    [:iframe {:id "ifrm1" :src "" 
              :width "99%" :height "99%" :style "border: 1px dotted gray"}] ))

(defn- app-foot
  "layout.south"
  []
  (eui-region 
    "south" 
    {:style "height: 30px; background: #cde; padding: 5px; text-align: center; color: #444"} 
    "版本信息: IDP集成数据平台系列——行业支撑平台 @" (year)))

(defn layout
  "app: 常用应用框架"
  []
  (eui-layout
    {:id "layout1" :onload "eui_layout()"}
    ;----------------------- north
    (app-top)
    ;----------------------- west
    (app-left)
    ;----------------------- center
    (app-main)
    ;----------------------- south
    (app-foot) ))

;; google map ,earth
(use 'wr3.clj.gmap)

(defn glocate
  "app: 调用google map api显示一个位置, 如：/c/eui/gmap/48.858288/2.294427
  具体绘图在js函数gmap()中完成"
  [ids]
  (googlemap (format "eui_glocate(%s,%s);" (first ids) (second ids))))

(defn gmap
  "app: 调用google map页面，执行main.js中如eui_gtask(), eui_gclick2mark()的js函数"
  [id]
  (googlemap (format "eui_%s()" id)))

(defn gearth
  "app: 调用google earth页面，执行main.js中如eui_g3basic()的js函数"
  [id]
  (googleearth (format "eui_%s()" id)))

;;----------------------------- 测试
(defn t
  "app: 测试"
  []
  (html-jquery
    {:onload "eui_onload()"}
    (eui-tabs 
      {:style "height: 65px; width: 1000px"}
      (eui-tab1 "应急值守" {:closable "false" :style "padding: 3px" :iconCls "icon-reload"}
                (eui-button {:plain "true" :iconCls "icon-tip"} "即时通讯")
                (eui-button {:plain "true"} "通讯录")
                (eui-button {:plain "true" :iconCls "icon-back"} "文电公告")
                )
      (eui-tab1 "指挥调度" {:closable "false" :style "padding: 3px"}
                (eui-button {:plain "true"} "事件接报")
                (eui-button {:plain "true"} "指挥调度")
                )
      (eui-tab1 "查询统计" {:closable "false" :style "padding: 3px"}
                (eui-button {:plain "true"} "历史情况")
                (eui-button {:plain "true"} "最新统计")
                (eui-button {:plain "true"} "智能分析")
                )
      (eui-tab1 "通知公告" {:closable "false" :style "padding: 3px"}
                (eui-button {:plain "true"} "政策")
                (eui-button {:plain "true"} "法规")
                (eui-button {:plain "true"} "文电公告")
                ) ) ))

(defn t2
  "app: 测试div内的定位"
  []
  (html-jquery
    [:span "把上一级div的 position 设为 absolute 或者 relative, 下一级的div就可以用 position: absolute来定位在上一级内"][:br][:br]
    [:div {:style "width: 300px; height: 300px; border:1px solid red; position: relative"}
      [:div {:style "width: 100px; height: 100px; border: 1px solid blue; position: absolute; left: 0px; top: 0px"} "top-left"]
      [:div {:style "width: 100px; height: 100px; border: 1px solid blue; position: absolute; right: 0px; top: 0px"} "top-right"]
      [:div {:style "width: 100px; height: 100px; border: 1px solid blue; position: absolute; left: 0px; bottom: 0px"} "bottom-left"]
      [:div {:style "width: 100px; height: 100px; border: 1px solid blue; position: absolute; right: 0px; bottom: 0px"} "bottom-right"]
      ]
    [:hr]
    [:div {:style "width: 350px; height: 350px; border: 1px solid blue"}
     [:div {:style "float: left; width: 100px; height: 100px; border: 1px solid red; margin:5px"} "111111"]
     [:div {:style "float: left; width: 100px; height: 100px; border: 1px solid red; margin:5px"} "222222"]
     [:div {:style "float: left; width: 100px; height: 100px; border: 1px solid red; margin:5px"} "333333"]
     [:div {:style "float: left; width: 100px; height: 100px; border: 1px solid red; margin:5px"} "444444"]
     [:div {:style "float: left; width: 100px; height: 100px; border: 1px solid red; margin:5px"} "555555"]
     [:div {:style "float: left; width: 100px; height: 100px; border: 1px solid red; margin:5px"} "666666"]
     [:div {:style "float: left; width: 100px; height: 100px; border: 1px solid red; margin:5px"} "777777"]]
    ))




