;;;; 从 wr3.util.Numberx 改写
(ns wr3.clj.n)

(defn to-int 
  "转换为整数, 可以给一个缺省值"
  ([s] (Integer/parseInt s))
  ([s n0] (try (Integer/parseInt s) (catch Exception e n0))))

(defn to-double
  "转换为整数, 可以给一个缺省值"
  ([s] (Double/parseDouble s))
  ([s n0] (try (Double/parseDouble s) (catch Exception e n0))))
  
(defn safe-index
  "把数组index限定在安全范围内[0, len-1] -1表示最后一个元素. len必须大于0"
  [i len]
  (if (< len 1) 0
    (let [i2 (rem i len)] (if (neg? i2) (+ i2 len) i2))))

(defn safe-range
  "把给定值n限定在指定区间[lower, upper]"
  [n lower upper]
  (cond (< n lower) lower (> n upper) upper :else n))

(defn random
  "得到一个[0, 100]范围的随机整数"
  ([] (rand-int 101))
  ; "得到一个[0, n-max]范围的随机整数"
  ([n-max] (rand-int (inc n-max))))

(defn random-n
  "得到个数为n的一系列[0, 100]范围的随机整数"
  ([n] (for [i (range n)] (random)))
  ; "得到个数为n的一系列[0, n-max]范围的随机整数"
  ([n n-max] (for [i (range n)] (random n-max))))

(defn pow
  "n的m次方n*n*n*..*n"
  [n m]
  ; 直接调用BigInteger的.pow速度最快
  (.pow (BigInteger/valueOf n) m)) 

; 关于Var，在1.3中应该还是可以进行重新值绑定的，但是对于名为“*var-name*”(头尾有两个星号)的变量不再默认为“dynamic”，
; 所以不能rebind（除非明确指定"^:dynamic"）。有兴趣的同学可以在1.3中分别验证一下。
; 下面的定义在1.3中不报错但好像无效
(def ^:dynamic ** pow)

(defn sum
  "Number序列相加"
  [sequ]
  (apply + sequ))

(defn product
  "Number序列相乘"
  [sequ]
  (apply * sequ))

(defn sign
  "得到正负，用字符表示"
  [n]
  (cond (neg? n) "-" (pos? n) "-" :else ""))

(defn digitals
  "得到一个正整数的各位数字"
  [n]
  ;(map #(- (int %) (int \0)) (str (Math/abs n)))) ; 或者用 (Character/getNumericValue c)
  (if (pos? n)
    (loop [i n r []] (if (zero? i) r (recur (quot i 10) (cons (mod i 10) r))))))

; 任意进制到10进制：2r1001, 16rff, 8r17

(defn bin
  "把一个任意进制整数（如：10，0xff，0103）表示为2进制，负整数带符号"
  [n]
  (Integer/toString n 2))

(defn hex
  "把一个任意进制整数表示为16进制小写"
  [n]
  (Integer/toString n 16))

(defn prime?
  "判断一个整数是否为质数（素数）"
  [n]
  (.isProbablePrime (BigInteger/valueOf n) 10))


;(use '[clojure.contrib.lazy-seqs :only (primes)])
; Lazy sequence of all the prime numbers. 因上面语句出错直接从 lazy_seqs.clj 源码中抽出使用.
(def primes
  (concat 
    [2 3 5 7]
    (lazy-seq
      (let [primes-from
            (fn primes-from [n [f & r]]
              (if (some #(zero? (rem n %))
                        (take-while #(<= (* % %) n) primes)) 
                (recur (+ n f) r)
                (lazy-seq (cons n (primes-from (+ n f) r)))))
            wheel (cycle [2 4 2 4 6 2 6 4 2 4 6 6 2 6  4  2
                          6 4 6 8 4 2 4 2 4 8 6 4 6 2  4  6
                          2 6 6 4 2 4 6 2 6 4 2 4 2 10 2 10])]
        (primes-from 11 wheel)))))

(defn prime-seq
  "质数列表"
  [n]
  (take n primes))

(defn prime-next
  "求>n的第一个质数"
  [n]
  (let [n2 (inc n)] (if (prime? n2) n2 (recur n2))))

(defn gcd
  "求A B的最大公约数"
  [A B]
  (if (zero? (* A B)) 1
    (loop [a A b B] (if (zero? (rem a b)) b (recur b (rem a b))))))

(defn factor1
  "整数n的第一个因子（也是最小因子、素数）"
  [n]
  (if (<= 0 n 1) n
    (loop [f 2] (if (zero? (rem n f)) f (recur (inc f))))))

(defn factors
  "整数n的所有因子"
  [n]
  (loop [i n r []] (let [f1 (factor1 i)] (if (= i f1) (conj r i) (recur (/ i f1) (conj r f1))))))

(defn fib
  "fib数列f(n)=f(n-1)+f(n-2)的第1～n项, 如(fib 8)=(1 2 3 5 8 13 21 34)"
  ([a b] (lazy-cat [a] (fib b (+ a b))))
  ([n] (take n (fib 1 1))))


