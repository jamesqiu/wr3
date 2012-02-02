(ns wr3.clj.app.dojo)

(use 'wr3.clj.web)
(use 'hiccup.core)

(defn index
  []
  (html-dojo
    "onload_test1()"
    [:span "官方网上帮助："]
    [:a {:href "http://dojotoolkit.org/widgets" :target "blank"} 
     "http://dojotoolkit.org/widgets"]
    
    [:div#container1 {:dojoType "dijit.layout.BorderContainer" :gutters "true" :style "width:100%; height:90%"}
     ;----------------------- top
     [:div {:dojoType "dijit.layout.ContentPane" :region "top"}
      (dojo-menu {:style "width: 200px"} [{"File" ["File111" "File222"]} 
                                          {"Edit" ["Edit222" "Edit333"]}
                                          {"Exit" nil}])
      
      ]
     ;----------------------- left
     [:div {:dojoType "dijit.layout.ContentPane" :splitter "true" :region "left" :style "width: 160px;"}
      [:h2 "Tree"]
      (dojo-tree {:url "/dojoroot/tree1.json"}
                 {:rootLabel "部分地区"}
                 {:html [:script {:type "dojo/method" :event "onClick" :args "item"}
                         "alert(store1.getLabel(item) + ', id=' + store1.getValue(item, 'id'));" ]})
      
      ]

     [:div {:dojoType "dijit.layout.ContentPane" :splitter "true" :region "center"}
      ;----------------------- main begin 
      [:h2 "日期, 时间"]
      (dojo-date {:id "birthday" :name "birthday" :value "2011-07-07"})
      (space 2)
      (dojo-time {:value "T23:03:30"})
      
      [:h2 "Text"]
      (dojo-text {:value "文本输入"})
      
      [:h2 "Textarea:"]
      (dojo-textarea {:value "文本：aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})

      [:h2 "Tabs:"]
      (dojo-tabs {:id "tabs3"} 
                 [{:title "食物" :html "好吃的东东……"}
                  {:title "Book" :html (html "book1" [:br] "book2") :selected "true" :closable "true"}
                  {:title "Foo" :html "3333333"} ])
      
      [:h2 "按钮"]
      (dojo-button {:iconClass "dijitEditorIcon dijitEditorIconCut" :html "Button测试2"})
      
      [:h2 "下拉选择"]
      (dojo-select nil {:ap "Apple" :ms-selected "Microsoft" :gg "Google"})
      
      
      [:h2 "菜单条"]
      ; 可定制化的菜单
      [:div#navMenu {:dojoType "dijit.MenuBar" :style "width: 300px"}
       [:div {:dojoType "dijit.PopupMenuBarItem"}
        [:span "File"]
        [:div {:dojoType "dijit.Menu" :id "fileMenu"}
         [:div {:dojoType "dijit.MenuItem" :onClick "alert('file 1')"}
          "File #1"]
         [:div {:dojoType "dijit.MenuItem" :onClick "alert('file 2')"}
          "File #2"]
         ]
        ]
       [:div {:dojoType "dijit.PopupMenuBarItem"}
        [:span "Edit"]
        [:div {:dojoType "dijit.Menu" :id "editMenu"}
         [:div {:dojoType "dijit.MenuItem" :onClick "alert('edit 1')"} 
          "Edit #1"]
         [:div {:dojoType "dijit.MenuItem" :onClick "alert('edit 2')"}
          "Edit #2"]
         ]
        ]
       [:div {:dojoType "dijit.MenuBarItem" :onClick "alert('Exit..')"}
        [:span "退出"]
        ]
       ]
      
      [:h2 "编辑器"]
      (dojo-editor {:id "editor1" :height "100px"
                    :html "This instance is created from a div directly with default toolbar and<br/>
                           plugins  <h3>测试</h3>"})

      [:h2 "单选框"]
      (dojo-checkbox {:value "yes" :html "同意以上条款" :checked ""}) 
      (space 2)
      (dojo-checkbox {:value "no" :html "缺省不选中"})
      
      [:h2 "inline编辑"]
      (dojo-inline {:html "时间"})
      
      [:h2 "Dialog对话框"]
      (dojo-dialog {:id "dialog1" :title "测试对话框：" 
                    :html "这是对话框<br/>222222222222222222222222222"})
      (dojo-button {:html (html "显示对话框"
                                [:script {:type "dojo/method" :event "onClick" :args "evt"}
                                 "dijit.byId('dialog1').show()"
                                 ]) })
      
      [:h2 "TooltipDialog对话框"]
      (dojo-tooltip {:html "Register"}
                      (html [:div 
                       [:label {:for "name2"} "Name:"]
                       (dojo-text {:id "name2" :name "name2" :value "hello"})
                       [:br]
                       [:label {:for "hobby2"} "Hobby:"]
                       (dojo-text {:id "hobby2" :name "hobby2"})
                       [:br]
                       (dojo-button {:type "submit" :html "Save"})
                       ]))
      
      [:h2 "TitlePane 可收缩"]
      (dojo-title {:title "Title Pane" :html "收缩内容……<br/><h2>标题2</h2>" :style "width:300px"})
      
      [:h2 "动画效果"]
      (dojo-button {:id "fadeOutButton" :html "Fade block out"})
      (dojo-button {:id "fadeInButton" :html "Fade block in"})
      [:div {:id "fadeTarget" :style "background-color:red;width:100px;height:100px"} "A red block"]
      
      ;----------------------- main end 
      ]
     ]    
    ))

