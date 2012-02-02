(ns wr3.clj.Draw)

;;;; 毛笔书写
(import '(javax.swing JFrame JComponent)
  '(java.awt RenderingHints Toolkit Color)
  '(java.awt.image BufferedImage)
  '(java.awt.event MouseAdapter MouseMotionAdapter KeyAdapter ))

(def screen-size (.getScreenSize (Toolkit/getDefaultToolkit)))
(def width (.. screen-size width))
(def height (.. screen-size height))

(def bimg (BufferedImage. width height BufferedImage/TYPE_INT_RGB))
(def bg (.getGraphics bimg))
(doto bg ; 设置anti-alias
  (.setColor Color/white)
  (.fillRect 0 0 width height)
  (.setColor Color/black)
  (.setRenderingHint RenderingHints/KEY_ANTIALIASING RenderingHints/VALUE_ANTIALIAS_ON))

(def r0 10) ; 原始落点半径
(def r1 (ref r0))
; 上一点的x y
(def x1 (ref r0))
(def y1 (ref r0))
; 当前点的x y
(def x2 (ref r0))
(def y2 (ref r0))

; 计算圆心距
(defn- d [x1 y1 x2 y2] (Math/sqrt  (+ (* (- x2 x1) (- x2 x1)) (* (- y2 y1) (- y2 y1)))))

; 根据圆心距及前半径算新半径
(defn- r2 [d]
  (let [r @r1
        rmax (* r0 3)
        rmin 1
        r2 (cond
             (zero? d) r0
             (< d r0)  (if (< r r0) (* 1.1 r) (* 1.005 r))
             :else     (if (> r r0) (/ r 1.8) (/ r 1.4)))]
    (cond (> r2 rmax) rmax (< r2 rmin) rmin :else r2)))

; 通过(x1 y1) (x2 y2)得到圆心距及新半径，并更新当前点半径@r1
(defn- set-r []
  (let [d (d @x1 @y1 @x2 @y2)
        r2 (r2 d)]
    (println "r2=" r2)
    (dosync (ref-set r1 r2))
    r2))

;;; 清理画布
(defn clear []
  (doto bg
    (.setColor Color/white)
    (.fillRect 0 0 width height)
    (.setColor Color/black)))

;;; 绘图细节
(defn draw [g]
  (let [r2 (set-r)]
    	(doto bg
      	(.fillOval (- @x2 r2) (- @y2 r2) (* 2 r2) (* 2 r2))))
  (dosync (ref-set x1 @x2) (ref-set y1 @y2))
  (.drawImage g bimg 0 0 nil)
  )

;;; 生成绘图组件
(def comp1
  (proxy [JComponent] []
    (update [g] (draw g))
    (paint [g] (draw g))
    ))

;;; 鼠标事件
(doto comp1
  (.addMouseListener
    (proxy [MouseAdapter] []
      (mousePressed [evt]
        (let [p (.getPoint evt)
              x (.x p)
              y (.y p)]
          (println "press: " [x y])
          (dosync
            (ref-set x1 x) (ref-set y1 y)
            (ref-set x2 x) (ref-set y2 y)
            (ref-set r1 r0)
            (.repaint comp1)
            )))
      (mouseClicked [evt]
        (when (= 2 (.getClickCount evt)) (clear))
        (.repaint comp1))))
  (.addMouseMotionListener
    (proxy [MouseMotionAdapter] []
      (mouseDragged [evt]
        (let [p (.getPoint evt)
              x (.x p)
              y (.y p)]
          (println "d: " [@x1 @y1 x y] "=" (d @x1 @y1 x y))
          (dosync
            (ref-set x2 x) (ref-set y2 y)
            (.repaint comp1))))))
  )

;;; 主Frame
(doto (JFrame. "毛笔书法")
  (.. getContentPane (add comp1))
  (.setSize (/ width 1.5) (/ height 1.5))
  (.setVisible true))

