(ns wr3.clj.app.test)
;;;;------------------------ 进行最简单功能的测试 /localhost/c/test

(use 'wr3.clj.web 'wr3.clj.n) ; 加不加 :reload 对速度有较大影响
(use 'hiccup.core)

(defn index
  "service: 给出金额，得到大写读数及简读"
  [id]
  (let [id0 (or id "2468013570.146")
        id1 (Double/valueOf id0) ]
    (html [:div (str "金额 (圆)：" id0)][:br]
          [:div (str "精确读数：" (wr3.bank.Currency/asChinese id0))][:br]
          [:div (str "简略读数：" (wr3.bank.Currency/about id1))])))


(defn m1 [id]
  (html-body 
    ""
    (let [n (to-int (or id "7") 7)]
      (html (for [i (range 1 n)] [:div "序号：" i])))))
             