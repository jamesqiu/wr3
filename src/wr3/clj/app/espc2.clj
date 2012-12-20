(ns wr3.clj.app.espc2)

(use 'wr3.clj.web 'wr3.clj.s 'wr3.clj.nosql 'wr3.clj.u 'wr3.clj.n 'wr3.clj.tb)
(use 'wr3.clj.app.espconf)
(use 'somnium.congomongo 'hiccup.core)
(use 'clojure.contrib.json)
(use 'wr3.clj.app.espc)

(defn who
  "ajax: 非mot用户（pn/en/org）查询其他用户的信息。注：证书形如pn的YYYY—C—TA—XXXXXX，en/org的YYYY—TA—XXXXXX"
  [id request]
  (let [typ id
        type-name (dd-role typ)
        cert-name (dd-cert (keyword typ))
        name2 (case typ "pn" "姓名" "名称")
        cert-input (case typ "pn" (html (eui-text {:name "yyyy" :style "width:50px"} ) "—"
                                        (eui-text {:name "c" :style "width:20px"} ) "—"
                                        (eui-text {:name "ta" :style "width:30px"} ) "—"
                                        (eui-text {:name "xxxxxx" :style "width:70px"} ) )
                     (html (eui-text {:name "yyyy" :style "width:50px"} ) "—"
                           (eui-text {:name "ta" :style "width:30px"} ) "—"
                           (eui-text {:name "xxxxxx" :style "width:70px"} )))]
    (html 
      [:h1 type-name "信息查询"] 
      (eui-tip (format "请输入%s%s 或者 %s编号进行查询" type-name name2 cert-name))
      [:form#fm1 {} 
        [:p [:label type-name name2 "："]  (eui-text {:name "name"})]
        [:p [:label cert-name "编号："]   cert-input [:br]]
        [:p (eui-button {:onclick (format "ajax_load($('#result'), '/c/esp/who-search/%s?'+$('#fm1').serialize())" typ) 
                         :iconCls "icon-ok"} "查询") (space 5) (eui-button-reset "fm1")]
       ] 
      [:div#result])))

(defn who-search
  "ajax: 非mot用户（pn/en/org）查询其他用户的信息"
  [id request]
  (let [typ id
        {nam :name yyyy :yyyy c :c ta :ta xxxxxx :xxxxxx} (query-vars2 request)
        cid (case typ "pn" (format "%s-%s-%s-%s" yyyy c ta xxxxxx) (format "%s-%s-%s" yyyy ta xxxxxx))
        tb (apply-tb typ);(keyword typ)
        rt (with-esp- (fetch tb :where {:$or [{:name nam} {:cid cid}]}))
        n (count rt)
        role (wr3role request)
        fields (cond
                 (and (= role "org") (= typ "pn")) [:name :type :pid :date :admin :_id]
                 (and (= role "org") (= typ "en")) [:name :type2 :admin]
                 (and (= role "en") (= typ "pn")) [:name :photo :admin]
                 (and (= role "en") (= typ "org")) [:name :type :admin]
                 (and (= role "pn") (= typ "org")) [:name :type :admin]
                 (and (= role "pn") (= typ "en")) [:name :type2 :admin]
                 )]
    (html [:h2 "查询到" n "条记录"]
          (when (not= 0 n) (result-html- rt [] fields {:form "docv/pn"})))))


