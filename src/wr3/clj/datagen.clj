;; todo
(ns wr3.clj.datagen)

(use 'wr3.clj.db 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.chart)
(use 'hiccup.core)

(def dbname "h2")

(defn index []
  (with-open [dbs (dbserver dbname)]
    (let [dg (tool.DataGen/create dbname)]
      (.close dg)
      (html-body 
        "" 
        (table-html (wr3-table (.loan dg))
                    {:id "t01" :style "color:blue"}) ))))

(defn img [] 
  (html [:img {:src (str "/chart?" (uri pie [1 3 5 7] [2 4 6 8] [0 5 10 15]))}]))