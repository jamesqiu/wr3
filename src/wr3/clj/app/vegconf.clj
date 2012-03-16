(ns wr3.clj.app.vegconf)
;;;; ems.clj 的config信息

;----------------------------- layout 蔬菜市场应用
(def app-menu2
  (array-map
    :基础数据查看* (array-map
          :进场登记表* {:id "11-reg"} 
          :登记细项* {:id "12-reg2"}
          :交易明细流水表* {:id "13-trade"}
          :菜品代码表* {:id "14-variety"}
          )
    :统计报表管理* (array-map
          :结算中心报表 {:id "21-jszx"}     
          :蔬菜部报表* {:id "22-scb"}     
          :质管科报表 {:id "23-zgk"}     
          :其他报表 {:id "24-other"}     
          )
    :数据分析* (array-map
          :进场登记分析* {:id "31-enter"} 
          :交易流水分析 {:id "32-trade"} 
          :综合分析* {:id "33-other"} 
          )
    :其他 (array-map
          :系统管理 {:id "41-admin"} 
          :帮助 {:id "42-help"} 
          )
    ))

(def table-menu
  (array-map
    :数据统计 {}
    :数据字段 {}
    :数据查看 {}
    ))
    
(def app-menu2-sub
  (array-map
    :11-reg (array-map
              :数据统计* {:id "11-reg-count"}
              :数据字段* {:id "11-reg-cols"}
              :数据查看* {:id "11-reg-data"}
              )
    :12-reg2 (array-map
               :数据统计* {:id "12-reg2-count"}
               :数据字段* {:id "12-reg2-cols"}
               :数据查看* {:id "12-reg2-data"}
               )
    :13-trade (array-map
                :数据统计* {:id "13-trade-count"}
                :数据字段* {:id "13-trade-cols"}
                :数据查看* {:id "13-trade-data"}
                )
    :14-variety (array-map
                  :数据统计* {:id "14-variety-count"}
                  :数据字段* {:id "14-variety-cols"}
                  :数据查看* {:id "14-variety-data"}
                  )
    
    :21-jszx (array-map
               :结算中心现金流量表日报表 {}
               :结算中心业务数据月度汇总表 {}
               :结算中心月现金流量表 {}
               )
    :22-scb (array-map
              :蔬菜部经营量年度汇总表 {:id ""}
              :蔬菜部收入年度汇总表  {:id ""}
              :蔬菜品种汇总表* {:id "22-scb-vars"}
              :蔬菜成交价格表* {:id "22-scb-price"}
              :蔬菜行情表*       {:id "22-scb-quot"}
              :蔬菜品种销售表     {:id ""}
              :蔬菜品种销售汇总表   {:id ""}
              :蔬菜成交价格执行情况表 {:id ""}
              )
 
    :23-zgk (array-map 
              :质管科业务数据月度汇总表 {:id "23-heal"}
              :质管科价格变化周期检测表 {:id "24-envr"}
               )
    
    :24-other (array-map 
                :当日收入明细与经营量  {:id ""}
                :一次性商品单品种汇总表 {:id "26-envr"}
                :一次性商品混装菜汇总表 {:id ""}
                :短信平台数据发布审核表 {:id ""}
                )
    :31-enter (array-map
                :来车属地分析* {:id "31-enter-from"}
                :字典维度分析* {:id "31-enter-dict"}
                :时间维度分析* {:id "31-enter-time"}
                )
    :32-trade (array-map
                :字典维度分析 {:id "32-trade-dict"}
                :时间维度分析 {:id ""}
                :来车属地分析 {:id ""}
                )
    :33-other (array-map
                :十大菜品分析* {:id "33-other-top10"}
                :价格趋势分析* {:id "33-other-trend"}
                :地区分布* {:id "33-other-region"}
                )
    :41-admin (array-map
                :数据字典 {:id "41-dds"}
                )
   ))

