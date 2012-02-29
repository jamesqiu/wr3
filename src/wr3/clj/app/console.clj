(ns wr3.clj.app.console)

(use 'wr3.clj.web)

(defn reload 
  "app: 可动态reload wr3.clj下的所有clj，甚至自身 '/c/console/reload/app.console'
  在-Dwr3.clj.reload=1时可不用此app，在生产环境下-Dwr3.clj.reload=0时可以单独动态reload某个clj，
  既保证了生产环境的运行效率，又不失动态部署灵活性。"
  [id] 
  (let [id (or id "app.test")
        clj (str "wr3.clj." id)
        msg (str "wr3.clj.app.console/reload: " clj)] 
    (do
      (use (symbol clj) :reload) ; 进行动态reload
      (println msg)
      msg)))

(defn gc 
  "app: 对app server进行gc建议"
  []
  (let [msg "wr3.clj.app.console: System.gc()"]
    (System/gc)
    (println msg)
    msg))

