(ns ^{:doc "数据生成，数值分析等"}
  wr3.clj.datagen)

(use 'wr3.clj.db 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.chart)
(use 'hiccup.core)

(def dbname "h2")

(defn h2data []
  (with-open [dbs (dbserver dbname)]
    (let [dg (tool.DataGen/create dbname)]
      (.close dg)
      (html-body 
        "" 
        (table-html (wr3-table (.loan dg))
                    {:id "t01" :style "color:blue"}) ))))

(def fxs (str "王李张刘陈杨黄赵吴周徐孙马朱胡郭何高林罗郑梁谢宋唐许韩冯邓曹彭曾肖田董袁潘于蒋蔡余杜叶程苏魏吕丁任沈"
              "姚卢姜崔鍾谭陆汪范金石廖贾夏韦傅方白邹孟熊秦邱江尹薛闫段雷侯龙史陶黎贺顾毛郝龚邵万钱严覃武戴莫孔向汤"))

(defn rand-name
   "生成由形式组成的随机姓名"
   []
   (str (rand-nth fxs) 
        (rand-nth fxs) 
        (when (zero? (rand-int 2)) (rand-nth fxs))))
