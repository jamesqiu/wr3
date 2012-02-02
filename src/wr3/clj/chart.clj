(ns wr3.clj.chart)

(comment "
usage:
  (uri bar     [])
  (uri bar     [1 2 3 4 5])
  (uri line    [1 3 5 7] [2 4 6 8] [0 5 10 15])
  (uri pie     [1 3 5 7] [2 4 6 8] [0 5 10 15])
  (uri barline [1 3 5 7] [2 4 6 8] [0 5 10 15])

;(html [:img {:src (str \"/chart?\" (uri pie [1 3 5 7] [2 4 6 8] [0 5 10 15]))}])
")

(import '(wr3.web Chart))

(defn c2j
  "把Clojure的[[1 2 3] [4 5 6]]转为Java的{ArrayList(1,2,3), ArrayList(4,5,6)}"
  [data]
  (into-array (map #(java.util.ArrayList. %) data)))

(defn line
  "画1条～n条线，被uri函数替换"
  [& data]
  (.. (Chart/create) line (data (c2j data)) url))

(defn bar
  "画1条～n个柱图，被uri函数替换"
  [& data]
  (.. (Chart/create) bar (data (c2j data)) url))

(defn pie
  "画1条～n个饼图，被uri函数替换"
  [& data]
  (.. (Chart/create) pie (data (c2j data)) url))

(defmacro uri
  "根据chartype类型画1～n条图。"
  ([chartype]
    `(.. (Chart/create) ~chartype url))
  ([chartype & data]
    `(.. (Chart/create ) ~chartype (data (c2j (list ~@data))) url))
  )
;(macroexpand '(uri bar [1 2 3] [4 5 6]))


;; 测试
;(.. (Chart/create) (data (into-array (map #(java.util.ArrayList. %) [[1 2 3 4] [5 6 7 8]]))) url)
;(wr3.clj.chart/tojava '([1 2 3] [4 5 6]))
;(println (str "localhost/chart?"
;           (uri pie [1 3 5 7] [2 4 6 8] [0 5 10 15])))
