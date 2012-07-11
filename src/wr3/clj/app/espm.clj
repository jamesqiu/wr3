(ns ^{:doc "esp的移动平台版本。实现主管机关mot的部分查询、操作功能"} 
     wr3.clj.app.espm)

(use 'hiccup.core 'somnium.congomongo)
(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.nosql)
(use 'wr3.clj.app.espconf)

(require '[wr3.clj.app.esp :as esp ] :reload)

(def jm-head
  (html
    [:head [:title "Mobile App"]
     [:meta {:http-equiv "Content-Type" :content "text/html; charset=utf-8"}]
     [:meta {:name "viewport" :content "width=device-width, initial-scale=1"}] 
     [:link {:rel "stylesheet" :href "/css/jquery.mobile-1.1.0.min.css"}]
     [:script {:src "/js/jquery-1.7.1.min.js"}]
     [:script {:src "/js/jquery.mobile-1.1.0.min.js"}] ]))

(defmacro html-jm
  "常用的html body模板，使用jquery ui和easyui，参数：
  m：属性，body：内容
  usage：(html-body {:onload 'my_function()'} '...') "
  [m & body]
  `(html "<!DOCTYPE html>" [:html jm-head [:body ~m ~@body]]))

(defn jm-header
  [m & items] (tags :div {:data-role "header" :data-theme "b"} m items))

(defn jm-content
  [m & items] (tags :div {:data-role "content" :data-theme "c"} m items))

(defn jm-footer
  [m & items] (tags :div {:data-role "footer" :data-theme "d"} m items))
    
(defn jm-list
  "列表，可输入筛选.
  @m {:type :ol ..} :ol 或者 :ul
  @items 每一项形如 [:li ..] "
  [m & items]
  (tags (or (:type m) :ul) 
        {:data-role "listview" :data-inset "true" :data-divider-theme "c" :data-filter "true"} m items))

(defn jm-list-head
  "列表分类标题"
  [m & items]
  (tags :li {:data-role "list-divider" :role "heading"} m items))

(defn jm-list1
  "列表中一项的内容"
  [& items]
  (tags :li {:data-theme "c"} nil items))

(defn jm-list-aside 
  "列表中的右侧内容，放在jm-list1函数中"
  [s] [:p {:class "ui-li-aside"} s])

(defn jm-list-count 
  "列表中的右侧计数，放在jm-list1函数中"
  [n] [:span {:class "ui-li-count"} n])

(defn jm-list-icon 
  "列表中的左侧图标，放在jm-list1函数中"
  [url] [:img {:class "ui-li-icon" :src url}])

; data-icon: plus/minus/delete, arrow-r/l/u/d, check/gear, refresh/forward/back, grid/star/alert/info/home/search
(defn jm-button
  "由<a href=..>转换来的button "
  [m s]
  (tags :a {:data-role "button" :data-icon "star" :data-transition "fade" :data-inline "true" :href "#"} m s))

(defn jm-tabs
  "@items 如：[:ul [:li [:a "
  [m & items]
  (tags :div {:data-role "navbar" :data-iconpos "top"} m [:ul items]))

(defn jm-tab1
  [m s]
  [:li [:a (merge {:href "#" :data-theme "c" :data-icon "star"} m) s]])

(defn jm-accords
  [m & items]
  (tags :div {:data-role "collapsible-set" :data-theme "b" :data-content-theme "c"} m items))
  
(defn jm-accord1
  [m & items]
  (tags :div {:data-role "collapsible" :data-collapsed "true"} m items))
        
(defn jm-grids
  "items 形如：<div class='ui-block-a'>..</div> <div class='ui-block-b'>..</div> <div class='ui-block-a'>..</div>"
  [m & items]
  (tags :div {:class "ui-grid-c"} m items))

(defn jm-grid1
  "m: {:col 'a' ..} 'a' 'b' 'c' 'd'等表示列号 "
  [m & items]
  (tags :div {:class (str "ui-block-" (or (:col m) "a"))} m items))

(defn jm-text
  "@m {:label .. :type ..} 
  其中:type可为：text password email range search number tel url date time month week datetime datetime-l color "
  [id m]
  (html [:div {:data-role "fieldcontain"}
         [:fieldset {:data-role "controlgroup"}
          [:label {:for id} (:label m)]
          [:input (merge {:id id :placeholder "" :value "" :type "text"} (dissoc m :label))]]]))

(defn jm-textarea
  "@m {:label .. :value ..} "
  [id m]
  (html [:div {:data-role "fieldcontain"}
         [:fieldset {:data-role "controlgroup"}
          [:label {:for id} (:label m)]
          [:textarea (merge {:id id :placeholder ""} (dissoc m :label :value)) (:value m)]]]))

(defn jm-switch
  "@m {:label .. :on .. :off } :on :off 表示开关的显示 "
  [id m]
  (html [:div {:data-role "fieldcontain"}
         [:fieldset {:data-role "controlgroup"}
          [:label {:for id} (:label m)]
          [:select (merge {:id id :name id :data-role "slider"} (dissoc m :label))
           [:option {:value "off"} (or (:off m) "Off")]
           [:option {:value "on"}  (or (:on m)  "On")] ]]]))

(defn jm-combo
  "jm-combo是一眼看到第一个选项（适用于选项很多时），而jm-radio一眼看到所有选项（适用于选项很少时）
  @m {:label .. } options如 {v1 label1 v2 label2} "
  [id m options]
  (html [:div {:data-role "fieldcontain"}
         [:fieldset {:data-role "controlgroup"}
          [:label {:for id} (:label m)]
          [:select (merge {:id id :name id :data-native-menu "true"} (dissoc m :label))
           (for [[k v] options] [:option {:value k} v])]]]))

(defn jm-radio
  "横向或者竖向单选radios，和jm-combo相比，jm-radio是一眼看到所有选项
  @m {:label .. :type} :type为 'horizontal' （一行） 或者 'vertical' （多行）
  @options如 {v1 label1 v2 label2} "
  [id m options]
  (html [:div {:data-role "fieldcontain"}
         [:fieldset {:data-role "controlgroup" :data-type (or (:type m) "horizontal")}
          [:legend {:for id} (:label m)]
          (for [[k v] options] 
            (html [:input {:name id :id (str id "_" k) :value k :type "radio"}]
                  [:label {:for (str id "_" k)} v]) )]]))

(defn jm-check
  "多选
  @m {:label .. :type} :type为 'horizontal'（一行） 或者 'vertical' （多行）
  @options如 {v1 label1 v2 label2} "
  [id m options]
  (html [:div {:data-role "fieldcontain"}
         [:fieldset {:data-role "controlgroup" :data-type (or (:type m) "horizontal")}
          [:legend {:for id} (:label m)]
          (for [[k v] options] 
            (html [:input {:name id :id (str id "_" k) :value k :type "checkbox"}]
                  [:label {:for (str id "_" k)} v]) )]]))

;;;-----------------------------------
(def cfg-mot
  {:theme "c"
   :nav [["待办事宜"
          ["考评员申请"    "applys/pn"]
          ["考评机构申请"  "applys/org"]
          ["企业申请"      "applys/en"]
          ["投诉举报" nil] ]
         ["考评员一览"
          ["考评员查询" nil]
          ["考评员统计分析" nil]
          ["考评员培训、考试" nil] ]
         ["考评机构一览"
          ["考评机构查询" nil]
          ["考评机构统计分析" nil]]
         ["交通运输企业一览"
          ["企业查询" nil]
          ["企业统计分析" nil]]
         ["下级机构一览"
          ["主管机关统计分析" nil]]
         ] })

(defn jm-frame
    "可视化框架"
    [title & items]
    (html-jm {}
             (jm-header {} [:h3 (or title "安全生产标准化系统")]
                        [:a {:href "/c/espm" :data-icon "home" :data-iconpos "notext" :data-direction "reverse"} ])
             (jm-content {} items)
             (jm-footer {}  [:div {:align "center" :style "padding:10px"} "版权所有：gotoreal.com"]) ))

;;;;----------------------------------------------------------- app, service
(defn index
  []
  (html-jm {}
           (jm-header {}
                      [:h3 "安全生产标准化系统"]
                      [:a {:href "/c/espm" :data-icon "home" :data-iconpos "notext" :data-direction "reverse"} ])
           (jm-content {}
                       (jm-list {:data-divider-theme (:theme cfg-mot)}
                                (for [[h & items] (:nav cfg-mot)]
                                  (html (jm-list-head {} [:h4 {:style "color:#369"} (format ">>&nbsp; %s：" h)])
                                        (for [[t url] items]
                                          (jm-list1 [:a {:href (if url (str "/c/espm/" url) "#")} t]))))
                                ))
           (jm-footer {}  [:div {:align "center" :style "padding:10px"} "版权所有：gotoreal.com"])
           ))

(defn applys
  [id]
  (let [tb (keyword (str id "-apply"))
        rs (esp/with-esp- (fetch tb))]
    (jm-frame
      nil
      (jm-list
        {:type :ol}
        (jm-list-head {} [:center (tb dd-form)] (jm-list-count (count rs)))
        (for [{n :name t :type t2 :type2 d :date r :resp} rs]
          (jm-list1  [:a {:href "#" :title (case r "yes" "同意" "no" "不同意" "尚未处理")} 
                      (format "%s （%s）" n 
                              (if (= id "en") (dd-type2 (to-int  t2)) (dd-type (to-int t)) ) )
                      (jm-list-icon (fmt "/css/easyui/icons/%s.png" (case r "yes" "ok" "no" "no" "help"))) ]
                     [:span {:style "font-size:12px; padding-left:20px; color:#639"} "申请时间：" d])) ))))


(defn mot-index 
  [request]
  (let [[n-pn n-org n-en] (map #(with-mdb2 "esp" (fetch-count % :where {:resp nil})) 
                               [:pn-apply :org-apply :en-apply])
        f (fn [n s] [:a {} (format "待处理的<font color=red>%s</font>申请：%s" s (if (zero? n) "无" n))])]
    (jm-frame 
      "待办事宜"
      (jm-list 
        {}
        (jm-list-head {} "待办事宜：")
        (jm-list1 (f n-pn "考评员"))
        (jm-list1 (f n-org "考评机构"))
        (jm-list1 (f n-en "企业")))
      )))

(defn jm 
  [request]
  (html-jm 
    {:onload ""}
    (jm-header 
      {}
      [:h1 "交通运输标准化系统"] 
      [:a {:href "/c/espm" :data-icon "home" :data-iconpos "left" :data-direction "reverse"} "主页"]
      [:a {:href "#" :data-icon "search" :data-iconpos "notext" :data-rel "dialog" :data-transition "fade"} "Search"]
      )
    (jm-content 
      nil 
      (jm-list {} 
               (jm-list-head {} "Head1")
               (jm-list1 [:a {:href "/c/esp" :target "_blank"} "111"]) 
               (jm-list1 "222") 
               (jm-list-head {} "Head2")
               (jm-list1 "333") 
               (jm-list1 "444") 
               )
      (jm-tabs {}
               (jm-tab1 {} "标签1")
               (jm-tab1 {} "标签2")
               (jm-tab1 {} "标签3"))
      (jm-button {:href "/esp" :target "_blank"} "链接按钮") [:br]   
      (jm-accords {}
                  (jm-accord1 {} [:a {:href "#"} "连接1"] [:h1 "hello"] [:h2 "1111111"])
                  (jm-accord1 {} [:h1 "world"] [:h2 "22222222"] [:h3 "2aaaaaaaaaa"])
                  )
      (jm-grids {:style "border:1px solid red; width:500px" :align "center"}
                (jm-grid1 {:col "a"} (jm-button {} "aa"))
                (jm-grid1 {:col "b"} (jm-button {} "bb"))
                (jm-grid1 {:col "c"} "333")
                (jm-grid1 {:col "a"} "aaa")
                (jm-grid1 {:col "b"} "bbb")
                (jm-grid1 {:col "c"} (jm-button {} "cc"))
                )
      [:form {:style "border:1px dashed gray; padding:5px"}
       (jm-text "id1" {:label "姓名"})
       (jm-text "id2" {:label "日期" :type "date"})
       (jm-text "id3" {:label "搜索" :type "search"})
       (jm-text "id4" {:type "color"})
       (jm-textarea "id5" {:label "说明" :value "aaa\nbbb"})
       (jm-switch "id6" {:label "营业" :on "开门" :off "关门"})
       (jm-combo "id7" {:label "下拉"} (into {} (map #(vector % (fill (str %) 10)) (range 10))))
       (jm-radio "id8" {:label "选择" :type "vertical"} {1 "aaa"  2 "bbb" 3 "ccc"})
       (jm-check "id9" {:label "选择" :type "horizontal"} {1 "aaa"  2 "bbb" 3 "ccc"})
       [:input {:type "submit" :value "提交按钮"}]
       ]
      
      [:img {:src "http://maps.google.com/maps/api/staticmap?center=tsinghua&zoom=13&size=256x256&sensor=false"}]
      [:pre "Header：" (http-header request)]
      )
    (jm-footer {}
               [:div {:align "center" :style "padding:10px"} "版权所有：gotoreal.com"]
               )
    (set-title "Mobile测试")
    ))


