(ns ^{:doc "数据生成，数值分析等
  可参考：https://github.com/weavejester/re-rand （用正则表达式生成随机字符串）
  https://github.com/paraseba/faker （生成美国的）"}
  wr3.clj.datagen)

(use 'wr3.clj.s 'wr3.clj.n 'wr3.clj.u 'wr3.clj.db 'wr3.clj.web 'wr3.clj.tb 'wr3.clj.chart)
(use 'hiccup.core)
(require 'wr3.clj.areacode)
(import wr3.util.Datetime)
(import [wr3.bank IDUtil OrgID])

(def dbname "h2")

(defn h2data []
  (with-open [dbs (dbserver dbname)]
    (let [dg (tool.DataGen/create dbname)]
      (.close dg)
      (html-body 
        "" 
        (table-html (wr3-table (.loan dg))
                    {:id "t01" :style "color:blue"}) ))))

; 百家姓
(def family-name100 (str "王李张刘陈杨黄赵吴周徐孙马朱胡郭何高林罗郑梁谢宋唐许韩冯邓曹彭曾肖田董袁潘于蒋蔡余杜叶程苏魏吕丁任沈"
              "姚卢姜崔鍾谭陆汪范金石廖贾夏韦傅方白邹孟熊秦邱江尹薛闫段雷侯龙史陶黎贺顾毛郝龚邵万钱严覃武戴莫孔向汤"))

(defn rand-name
   "生成由百家姓组成的随机姓名，2位或者3位"
   []
   (str (rand-nth family-name100) (rand-nth family-name100)
        (when (zero? (rand-int 2)) (rand-nth family-name100))))

(def peoples56 ["汉" "回" "畲" "塔塔尔" "阿昌" "哈萨克" "土家" "景颇" "哈尼" "土" "白" "维吾尔"
               "保安" "赫哲" "乌孜别克" "基诺" "布依" "拉祜" "锡伯" "黎" "东乡" "蒙古" "仫佬"
               "达斡尔" "藏" "毛南" "裕固" "俄罗斯" "德昂"  "僳僳"  "瑶" "朝鲜"  "布朗"  "满"
               "彝"  "门巴"  "侗"  "苗" "佤"  "羌" "独龙"  "怒"  "珞巴"  "普米"  "傣" "纳西"
               "高山" "壮" "额伦春"  "塔吉克"  "京"  "仡佬"  "鄂温克"  "撒拉"  "柯尔克孜" "水"])

(defn rand-peoples
  "得到56个民族中随机一种"
   []
   (rand-nth peoples56))

;移动134、135、136、137、138、139、150、151、152、157（TD）、158、159、188及暂未启用的187。
;联通130、131、132、155、156、186及暂未启用的185。
;电信133、153、189及暂未启用的180。
(def mobile-pre3 [134 135 136 137 138 139 150 151 152 157 158 159 188 187
                 130 131 132 155 156 186 185
                 133 153 189 180])            
(defn rand-mobile
  "生成11位随机中国手机号"
  []
  (format "%s%08d" (rand-nth mobile-pre3) (rand-int 100000000)))

(defn rand-date
  "生成往后112年（1900-2012）形如 '2000-2-13' 形式的日期字符串
  @from 初始日期字符串，不要则从当日开始"
  ([from] (date-add from (rand-int 112) (rand-int 13) (rand-int 31)))
  ([] (rand-date (date))))

(def province-list ["北京" "上海" "广东" "江苏" "陕西" "山东" "新疆" "湖南" "黑龙江" "湖北" "安徽" "浙江" "四川" "贵州" "甘肃" "福建" 
                    "辽宁" "重庆" "天津" "广西" "吉林" "海南" "河北" "河南" "内蒙古" "山西" "西藏" "青海" "江西" "云南" "宁夏"])

(defn rand-province
  "生成随机省简称或者全称，不含港澳台"
  ([] (rand-nth province-list))
  ([full?] (let [nam1 (rand-province) 
                 nam2 (case nam1 ("北京" "上海" "重庆" "天津") "市"
                        ("新疆" "广西" "内蒙古" "西藏" "宁夏") "自治区"
                        "省")] 
             (str nam1 nam2))))
  
(defn rand-pid
  "生成18位随机中国身份证"
  []
  (let [s17 (format "%s%s%03d" (rand-nth wr3.clj.areacode/areacode-list)
                    (date-format (rand-date "1900-1-1") "yyyyMMdd")
                    (inc (rand-int 1000)) )]
    (IDUtil/gen18 s17)))

(defn rand-orgid
  "生成8+1位随机中国组织机构代码"
  []
  (OrgID/toid (format "%08d" (rand-int 100000000))))

;(dotimes [i 10] (println (rand-orgid)))
;(count peoples)

 