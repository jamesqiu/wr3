;;;; string 和 sequence 处理的utility
;;;; string 处理主要从 wr3.util.Stringx 改写
(ns wr3.clj.s)

(defn nullity?
  "判断字符串是否nil或者空"
  [s] (zero? (count s)))

(defn not-nullity?
  "判断字符串非空：不是nil，也不是空字符串
  @see nullity?"
  [s]
  (not (nullity? s)))

(defn include?
  "判断s是否含sub子字串，或者集合s是否含元素sub，参考 in? "
  [s sub]
  (if (or (nullity? s) (nullity? sub)) false
    (> (.indexOf s sub) -1)))

(def has? include?)

(defn capitalize
  "大写单词第一个字符，其他不管"
  [s]
  (if (nullity? s) s (apply str (Character/toUpperCase (first s)) (rest s))))

(defn capitalize+
  "大写单词第一个字符，其他变小写"
  [s]
  (if (nullity? s) s (capitalize (.toLowerCase s))))

; "capitalize 和 capitalize+ 的别名"
(def upper capitalize)
(def upper+ capitalize+)

(defn lower
  "小写单词第一个字符，其他不管。如 VipCust->vipCust"
  [s]
  (if (nullity? s) s (apply str (Character/toLowerCase (first s)) (rest s))))

(defn left
  "s中sep左边的部分"
  [s sep]
  (if (or (nullity? s) (nullity? sep)) ""
    (let [i (.indexOf s sep)] (if (< i 0) "" (subs s 0 i)))))

(defn right
  "s中sep右边的部分"
  [s sep]
  (if (or (nullity? s) (nullity? sep)) ""
    (let [i (.indexOf s sep)] (if (< i 0) "" (subs s (+ i (count sep)))))))

(defn str-reverse
  "字符串反转"
  [s]
  (if (nil? s) nil
    (apply str (reverse s))))

(defn leftback
  "s中最右边sep左边的部分"
  [s sep]
  (str-reverse (right (str-reverse s) sep)))

(defn rightback
  "s中最右边sep右边的部分"
  [s sep]
  (str-reverse (left (str-reverse s) sep)))

(defn between
  "s中sep1和sep2之间的部分"
  [s sep1 sep2]
  (left (right s sep1) sep2))

(defn replace-first
  "替换s中的第一个匹配的部分"
  [s from to]
  (if (not (include? s from)) s
    (str (left s from) to (right s from))))

(defn replace-all
  "替换s中的所有匹配的部分"
  [s from to]
  (if (not (include? s from)) s (str (left s from) to (replace-all (right s from) from to)))) ; 递归

(defn replace-between
  "把s中sep1和sep2之间的部分替换为to"
  [s sep1 sep2 to]
  (let [from (between s sep1 sep2)] (replace-first s from to)))

(defn replace-include
  "把s中sep1和sep2以及之间的部分替换为to"
  [s sep1 sep2 to]
  (let [from (between s sep1 sep2)] (replace-first s (str sep1 from sep2) to)))

(defn trim
  "把字符串前后、中间的所有' ', '\t','　'都替换掉"
  [s]
  (reduce #(apply replace-all %1 %2) [s [" " ""] ["\t" ""] ["　" ""]]))

(defn split
  "把s按照sep分隔成sequence, 注意某些正则表达式符号要转移，如$要用\\$"
  [s sep]
  (if (nil? s) nil
    (if (nil? sep) (list s)
      (apply list (.split s sep)))))

(defn join
  "把sequence列表用sep连接成一个字符串.
  @m 客户化参数的hash-map，含 :pre :post :quo 例如：
    (join [10 20 30] ',' {:quo '\"' :pre '<' :post '>'}) "
  ([sequ sep] (apply str (interpose sep sequ)))
  ([sequ sep m] 
    (let [quo (or (:quo m) "")
          pre (or (:pre m) "")
          post (or (:post m) "")]
      (str pre (join (map #(str quo % quo) sequ) sep) post))))

;(println (join [10 20 30] "," {:pre "[" :post "]" :quo "\""})) ; join使用例子

(defn unique
  "sequence 集合去重复元素"
  [sequ]
  (apply list (set sequ)))

(defn unique?
  "sequence 是否无重复；有重复元素则返回false，无重复元素则返回true"
  [sequ]
  (apply distinct? sequ))

(defn in?
  "判断某元素e是否在sequence中"
  [e sequ]
  (let [rt (some #(= e %) sequ)]
    (if (nil? rt) false true)))

(defn remove-e
  "删除sequence中所有等于e的元素"
  [sequ e]
  (remove #(= e %) sequ))

(defn remove-in
  "删除sequence中的n个元素ee"
  [sequ ee]
  (remove #(in? % ee) sequ))

(defn remove-nth
  "删除sequence中的第n个元素，0开始"
  [sequ n]
  (concat (take n sequ) (drop (inc n) sequ)))

(defn position
  "元素e在sequence中的位置"
  [e sequ]
  (let [n (count sequ) m (count (take-while #(not (= e %)) sequ))]
    (if (= m n) -1 m)))

(def index-of position)

(defn insert
  "在字符串s的sep前插入addon"
  [s sep addon]
  (if (not (include? s sep)) s
    (str (left s sep) addon sep (right s sep))))

(defn insert-after
  "在字符串s的seq后插入addon"
  [s sep addon]
  (if (not (include? s sep)) s
    (str (left s sep) sep addon (right s sep))))

(defn fill
  "用指定字符串with填充为指定长度为len的字符串"
  [with len]
  (if (nullity? with) with
    (let [len2 (if (< len 0) 0 len)]
      (.substring (apply str (repeat len2 with)) 0 len2))))

(defn pad-left
  "左填充（右对齐）如果填充的只是一个字符，可以用(format \"%010d\" 42)"
  [s len with]
  (if (or (> (count s) len) (nullity? with)) s
    (str (subs (fill with len) 0 (- len (count s))) s)))

(defn pad-right
  "右填充（左对齐）"
  [s len with]
  (if (or (> (count s) len) (nullity? with)) s
    (str s (subs (fill with len) (count s)))))

(defn check
  "s为nil或空则返回缺省值"
  [s default]
  (if (nullity? s) default s))

(use '[wr3.clj.n :only (safe-index)])
(defn subs+
  "subs的安全增强版，能处理下标溢出，包含上下界[start end]"
  [s start end]
  (if (nullity? s) "" ; 函数尽量勿返回nil，以便后续处理
    (let [i0 (safe-index start (count s)) i1 (safe-index end (count s))]
      (subs s (min i0 i1) (inc (max i0 i1))))))

(defn slice
  "sequence中[start end]之间（含上下界）的元素，上下界会被处理到安全范围内"
  [sequ start end]
  (if (or (= [] sequ) (nil? sequ)) [] ; 函数尽量勿返回nil，以便后续处理
    (let [i0 (safe-index start (count sequ)) i1 (safe-index end (count sequ))]
      (subvec (vec sequ) (min i0 i1) (inc (max i0 i1))))))

(defn gstring
  "形如Groovy类型可含变量的GString。
   [s]: 变量必须在*ns*中用def定义过，可写表达式, 如：\"${name1}: After 5 years ${name1} is ${(+ age 5)}\"
   [s var-value-map]: 变量自己定义如{:name \"qh\" :age 30}，*不可写*表达式, 如：\"${name}'s age ${age}\" "
  ([s]
    (let [vars (re-seq #"\$\{[^}]+\}" s)
          values (map #(eval (read-string (between % "${" "}"))) vars)]
      (reduce #(apply replace-all %1 %2) s (zipmap vars values))))
  ([s var-value-map]
    (let [vars (re-seq #"\$\{\w+\}" s)
          values (map #((keyword (between % "${" "}")) var-value-map) vars)]
      (reduce #(apply replace-all %1 %2) s (zipmap vars values)))))

;; 计算espreg码值
(defn valid-rune [s] (fill (str (apply + (map int s))) 6))

;; 大小写字母列表
(def a-z (map char (range 97 123)))
(def A-Z (map char (range 65 91)))

(defn email?
  "如果是email地址，返回true，否则返回false。不能带中文，否则返回false @2012-12-14"
  [s]
  (let [p #"(?i)[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (not= nil (re-matches p s))))

; @date 2013-1-3
(defn base64enc [s] (wr3.util.Stringx/base64enc s))
(defn base64dec [s] (wr3.util.Stringx/base64dec s))
(defn md5 [s] (wr3.util.Stringx/md5 s))
