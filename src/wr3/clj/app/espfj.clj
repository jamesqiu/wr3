(ns ^{:doc "为福建交通厅定制的考评员注册及审批"}
     wr3.clj.app.espfj)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.web)
(use 'somnium.congomongo 'wr3.clj.nosql 'somnium.congomongo 'hiccup.core)
(use 'wr3.clj.app.espconf)
(require '[wr3.clj.app.espconf :as conf])

; 福建交通厅主管部门
(def dd-admin-fj 
  (array-map
    "1401" "福建省交通运输厅"
    "1402" "福州市交通运输委"
    "1403" "厦门市交通运输局"
    "1404" "漳州市交通局"
    "1405" "泉州市交通运输委"
    "1406" "三明市交通运输局"
    "1407" "莆田市交通运输局"
    "1408" "南平市交通运输局"
    "1409" "龙岩市交通局"
    "1410" "宁德市交通运输局"
    "1411" "平潭综合实验区交建局"
    "1412" "漳州招商局经济技术开发区交通局"
    "1413" "福州港口局"
    "1414" "厦门港口局"
    "1415" "湄洲湾港口局"
    ))

;; 用espconf中的cfg-apply-pn配置，去掉换证部分，把主管机关换成福建省的。
(def cfg-pn
  (let [n (count conf/cfg-apply-pn)
        rt1 (take (- n 4) conf/cfg-apply-pn)]
    (concat rt1 [["主管机关" :admin {:t dd-admin-fj :title "请自选主管机关"}]])))

(defn index
  "填写考评员注册信息"
  []
  (html-body
    [:center {:style "border: 1px solid lightgray"}
     [:h1 {:style "padding:20px; background-color:#369; color: white; font-size: 22px; margin-top:0px"} 
      "福建省交通厅——考评员在线报名系统"]
     [:div {:align "center" :style "border:0px solid red"}
      (input-form 
        cfg-pn
        {:title "考评员资格证 申请表"
         :buttons (html
                    (eui-button {:onclick (format "espfj_input_submit('')" )} "提交申请") (space 5)
                    (eui-button {:onclick "$('form').get(0).reset()"} " 重 填 ")) })]
     [:h2 {:style "padding:10px; background-color:lightgray; margin-bottom:0px; color: #333; text-shadow: 0 1px 0 #EEE;"}
      "版权所有：福建省交通运输厅 2012 年"] 
     ]
     (fileupload-dialog)) )

(defn input-submit
  "service: 福建考评员申请表提交保存或更新
  @id form名称如'pn' 'en' 'org' "
  [request]
  (let [vars (query-vars2 request)
        m (into {} (for [[k v] vars :when (not (nullity? v))] [k v]))]
    (with-mdb "espfj" 
      (insert! :pn-apply (into m {:date (datetime)}))) 
    (format "已提交 %s 的申请表。" (vars "name") )))
  