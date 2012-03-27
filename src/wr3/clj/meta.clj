(ns ^{:doc "关系数据库、表、字段的meta信息。为方便管理，已经全部导入monggodb"
      :author "JamesQiu"}
     wr3.clj.meta)

;;; :name 中文名称
;;; :type 类型，可能为'db 'tb 'fd
;;; :etc 详细描述字符串

(use 'somnium.congomongo)
(use 'wr3.clj.u)

; 数据字段字典，表名-字段名-字段字典, 用于veg.clj
(def dds 
  {
   ;;---------- 用于veg.clj的表：
   :TB_TR_REGISTER_RECORD 
   {
    :SignMode '{"001" 地磅收费 "002" 卡机登记 "003" 郊菜收费 "004" 剩菜收费 "005" 卡机剩菜 "006" 二批登记}
    :Area '{"0" 客菜 "1" 郊菜 "2" 无卡} 
    :Method '{"0" 卡付费 "1" 现金付费 "2" 银行卡交费}
    :Model '{"0" 比例收费 "1" 定额收费 "2" 计件收费}
    :Status '{"0" 客菜进场   "1" 客菜回皮|郊菜未付款 "2" 郊菜已付款}
    :CleanSign '{"0" 未退 "1" 已退}
    :FieldSign '{"0" 未退 "1" 已退}
    :Symbol '{"0" 待检测 "1" 检测合格 "2" 不合格}
    }
   :TB_TR_REGISTER_DETAIL_INFO
   {
    :IsPackage '{"0" 无包装 "1" 带包装}
    :Symbol '{"0" 非 "1" 是}
    :IsBrand '{"0" 非品牌 "1" 品牌}
    :CheckFlag '{"0" 检测通过免检 "1" 待检  "2" 复检  "3" 终检  "4" 销毁}    
    :CheckSymbol '{"0" 总菜品 "1" 明细菜品}    
    }
   :TB_BASIC_VARIETY_DETAIL
   {
    :CheckFlag '{0 免检 1 待检}    
    }
   :TB_TR_TRADE_DETAIL_RECORD
   {
    :Methods '{0 卡机 1 红白单 2 单据}
    :MaundMode '{0 计重 1 计件 2 输重}
    :Status '{0 未结算 1 已结算 2 上家卡无效}
    }
   ;;---------- 其他表
   })

(defn- find-key-i
  "从哈希表m中取出第一个key为s（不区分大小写）的value"
  [m s]
  (get-key #(.equalsIgnoreCase (name %) s) m))
;  (some (fn [[k v]] (when (.equalsIgnoreCase (name k) s) v)) m))
  
(defn dd-map 
  "给出表名和字段名，从维度字典表（dds）中取出字典，不区分大小写。
  例如：(dd-map \"TB_TR_TRADE_DETAIL_RECORD\" \"Status\") ; 返回 {0 未结算, 1 已结算, 2 上家卡无效} "
  [tb fd]
  (reduce find-key-i dds [tb fd]) )

(defn meta-name
  "给出code，从mongodb的meta集合的dict表中取出第一个:name，不区分大小写。
  例如：(meta-name \"TB_TR_REGISTER_reCORd\") ; 返回 \"进场登记表\" "
  [code]
  (with-mongo (make-connection "meta")
    (let [rs (fetch :dict :where {:code (re-pattern (str "(?i)^" code "$"))})
          rt (map #(dissoc  % :_id) rs)
          nam (:name (first rt))]
      (or nam "/") )))

;; test
;(meta-name "TB_TR_REGISTER_reCORd")
;(dd-map "tb_TR_TRADE_DETAIL_RECORD" "Status")
;(keys (dds (keyword "TB_TR_REGISTER_RECORD")))