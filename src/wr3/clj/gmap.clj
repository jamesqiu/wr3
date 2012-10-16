;;;; google map, google earth API 包装

(ns wr3.clj.gmap)

(use 'wr3.clj.web)
(use 'hiccup.core)

(defn googlemap
  "调用google map api完成地图功能，显示功能在js函数中完成，如：(googlemap 'eui_gmap()')
  参数： 
  js: javascript函数名
  body: 多个hiccup片段"
  [js & body]
  (html-jquery
    {:onload js :js ["app-eui.js" "app-ems.js"]}
    [:script {:type "text/javascript" :src "http://maps.googleapis.com/maps/api/js?sensor=false&libraries=panoramio,geometry"}]
    (html (or body
              [:div {:id "map_canvas" :style "width:100%; height:100%"}] ))))

(defn googleearth
  "调用google earth api完成地图功能，显示功能在js函数中完成，如：(googleearth 'eui_gmap()')
  参数： 
  js: javascript函数名
  body: 多个hiccup片段"
  [js & body]
  (html-jquery
    {:onload js :js "app-eui.js"}
;    [:script {:type "text/javascript" :src "http://www.google.com/jsapi?key=ABQIAAAAs0-6DGuC36Q_Mdia37v4_hSshEQf46CYbggpaYkzXbm0SNgG0hSrUwpcoSHk2D87MXVow8mPgAnw4Q"}]
    [:script {:type "text/javascript" :src "http://www.google.com/jsapi"}]
    [:script "google.load('earth', '1');"]
    (html (or body
              [:div {:id "map3d" :style "border: 1px solid gray; width:100%; height:100%"}] ))))
    
