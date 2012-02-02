(ns wr3.clj.unitest 
  ;(:gen-class)
  )

;;;;------------- Clojure Unit Test
(use 'clojure.contrib.test-is)
(use '(wr3.clj s n u tb db dbconfig web CljServlet))
(use 'hiccup.core)

;;;--------------- wr3.clj.s
(deftest s-test []
  ;; nullity?
  (are (true? (nullity? _1))
    nil
    "")
  (are (false? (nullity? _1))
    "..."
    "\t"
    " ")
  ;; include?
  (are (true? (include? "hello world" _2))
    "h"
    "world"
    " "
    "hello world")
  (are (false? (include? _1 _2))
    "hello world" nil
    nil "..."
    nil nil)
  ;; capitalize capitalize+ upper upper+
  (are (= _1 (capitalize _2))
    "Hello world" "hello world"
    "HELLO" "HELLO"
    nil nil
    "" ""
    )
  (are (= _1 (upper+ _2))
    "Hello world" "HELLO WORLD"
    "Hello" "Hello"
    "Hello" "heLLo"
    )
  ;; lower
  (are (= _1 (lower _2))
    "vipCust" "VipCust"
    "helloWorld" "HelloWorld"
    "hello" "hello"
    )
  ;; left right leftback righback
  (are (= _1 _2)
    "hello" (left "hello world" " ")
    "a" (left "a.b.c" ".")
    "" (left ".a.b.c" ".")
    "" (left "" ".")
    "" (left nil nil)
    "" (left nil ".")
    "" (left "" nil)

    "world" (right "hello world" " ")
    "b.c" (right "a.b.c" ".")
    "" (right "abc." ".")
    "" (right "" ".")
    "" (right nil nil)
    "" (right "abc" nil)
    "" (right nil ".")
    (leftback "a-b-c" "-") "a-b"
    (leftback "hello world" " ") "hello"
    (leftback ".a.b.c" ".") ".a.b"
    (leftback "" ".") ""
    (leftback nil nil) ""
    (leftback "ab" nil) ""
    (leftback nil ".") ""
    (rightback "a-b-c" "-") "c"
    (rightback "hello world" " ") "world"
    (rightback ".a.b.c" ".") "c"
    (rightback ".a.b.c" "_") ""
    (rightback nil nil) ""
    (rightback "abc" nil) ""
    (rightback nil ".") ""
    )
  ;; str-reverse
  (are (= _1 _2)
    (str-reverse "abc") "cba"
    (str-reverse "cn中文") "文中nc"
    (str-reverse "") ""
    (str-reverse nil) nil
    )
  ;; between
  (are (= _1 _2)
    (between "a-b-c" "-" "-") "b"
    (between "foo(108)bar" "(" ")") "108"
    (between "" "(" ")") ""
    (between "foo(108)bar" "<" ">") ""
    (between "a-b-c" "-" nil) ""
    (between "a-b-c" nil "-") ""
    (between nil "<" ">") ""
    )
  ;; replace-first replace-all replace-between replace-include
  (are (= _1 _2)
    (replace-first "a-b-c" "-" "=") "a=b-c"
    (replace-all "a-b-c" "-" "=") "a=b=c"
    (replace-first "a-b-c" "." "=") "a-b-c"
    (replace-all "a-b-c" "." "=") "a-b-c"
    (replace-first "a-b-c" "." "=") "a-b-c"
    (replace-all "a-b-c" "-" "") "abc"
    (replace-first "a-b-c" "-" "") "ab-c"
    (replace-all "a-b-c" "-" nil) "abc"
    (replace-first "a-b-c" "-" nil) "ab-c"
    (replace-all "a-b-c" "" "-") "a-b-c"
    (replace-first "a-b-c" "" "-") "a-b-c"
    (replace-all "a-b-c" nil "-") "a-b-c"
    (replace-first "a-b-c" nil "-") "a-b-c"
    (replace-between "a-b-c-d" "-" "-" "ZZ") "a-ZZ-c-d"
    (replace-include "a-b-c-d" "-" "-" "ZZ") "aZZc-d"
    )
  ;; trim
  (are (= _1 _2)
    (trim " hello \t ") "hello"
    (trim "\the llo \t") "hello"
    (trim "") ""
    (trim nil) nil

    )
  ;; split join
  (are (= _1 _2)
    (split "a-b-c" "-") '("a" "b" "c")
    (split "-a-b-c-" "-") '("" "a" "b" "c")
    (split "a--b-c-" "-") '("a" "" "b" "c")
    (split "a-b-c" "=") '("a-b-c")
    (split "a-b-c" nil) '("a-b-c")
    (split nil nil) nil
    (join ["a" "b" "c"] "-") "a-b-c"
    (join '("a" "b" "c") "-") "a-b-c"
    (join '("a" "b" "c") "") "abc"
    (join ["a" nil "c"] "-") "a--c"
    (join ["a"] "-") "a"
    (join [] "-") ""
    (join nil "-") ""
    (join ["a" "b"] nil) "ab"
    )
  ;; unique
  (are (= _1 _2)
    (unique ["a" "b" "a" "c"]) ["a" "b" "c"]
    (unique ["a" "b" "c"]) ["a" "b" "c"]
    (unique [1 2 3 1 3 2]) [1 2 3]
    (unique []) []
    (unique nil) '()
    )
  ;; in
  (are (= _1 _2)
    (in? "ab" ["ac" "ab" "ac" "bc"]) true
    (in? 1 [2 1 3]) true
    (in? 1 [1]) true
    (in? 3 [1 2]) false
    (in? nil []) false
    (in? 3 []) false
    (in? 3 nil) false
    )
  ;; remove-e remove-n
  (are (= _1 _2)
    (remove-e [1 2 3 1 2] 1) [2 3 2]
    (remove-e [1 2 3 1 2] 1) [2 3 2]
    (remove-e [1] 1) []
    (remove-e [1] nil) [1]
    (remove-e nil 1) '()
    (remove-e nil nil) '()
    (remove-in [1 2 3 2 1] [1 2]) [3]
    (remove-in [1 2] [1 2]) []
    (remove-in [1 2] nil) [1 2]
    (remove-in nil [1]) '()
    (remove-in nil nil) '()
    )
  ;; index
  (are (= _1 _2)
    (position 3 [1 2 3]) 2
    (position 1 [1 2 3]) 0
    (position 4 [1 2 3]) -1
    (position 4 []) -1
    (position nil []) -1
    (position nil nil) -1
    )
  ;; insert insert-after
  (are (= _1 _2)
    (insert "hello world" " " ",") "hello, world"
    (insert "hello world" " " " cn中文") "hello cn中文 world"
    (insert "hello world" "-" ",") "hello world"
    (insert "hello world" "-" nil) "hello world"
    (insert "" "" ",") ""
    (insert nil " " ",") nil
    (insert-after "hello world" " " ": ") "hello : world"
    (insert-after "hello world" "world" ".") "hello world."
    (insert-after "hello world" "" ".") "hello world"
    (insert-after "hello world" "" nil) "hello world"
    (insert-after "hello world" nil ".") "hello world"
    (insert-after nil " " ".") nil
    )
  ;; fill pad-left pad-right
  (are (= _1 _2)
    (fill "ab" 4) "abab"
    (fill "ab" 3) "aba"
    (fill "ab" 1) "a"
    (fill "ab" 0) ""
    (fill "ab" -1) ""
    (fill "" 3) ""
    (fill nil 3) nil
    (pad-left "abc" 10 " ")  "       abc"
    (pad-left "abc" 10 "-")  "-------abc"
    (pad-left "abc" 10 "-*") "-*-*-*-abc"
    (pad-left "abc" 2 " ") "abc"
    (pad-left "abc" -1 ".") "abc"
    (pad-left "abc" 10 nil) "abc"
    (pad-left "abc" 10 "") "abc"
    (pad-left "" 10 "-") "----------"
    (pad-left nil 10 "-") "----------"
    (pad-right "abc" 10 " ")  "abc       "
    (pad-right "abc" 10 "-")  "abc-------"
    (pad-right "abc" 10 "-*") "abc*-*-*-*"
    (pad-right "abc" 2 " ") "abc"
    (pad-right "abc" -1 ".") "abc"
    (pad-right "abc" 10 nil) "abc"
    (pad-right "abc" 10 "") "abc"
    (pad-right "" 10 "-") "----------"
    (pad-right nil 10 "-") "----------"
    )
  ;; check
  (are (= _1 _2)
    (check "" "default") "default"
    (check nil "default") "default"
    (check " " "default") " "
    (check "foo" "default") "foo"
    )
  ;; sub+
  (are (= (subs+ _1 _2 _3) _4)
    "hello" 0 4 "hello"
    "hello" 0 0 "h"
    "hello" -1 -5 "hello"
    "hello" -1 -3 "llo"
    "hello" 5 4 "hello"
    "" 0 4 ""
    nil 0 4 ""
    )
  ;; slice
  (are (= (slice _1 _2 _3) _4)
    [1 3 5 7 9] 0 4 [1 3 5 7 9]
    [1 3 5 7 9] 0 0 [1]
    [1 3 5 7 9] -1 -5 [1 3 5 7 9]
    [1 3 5 7 9] -1 -3 [5 7 9]
    [1 3 5 7 9] 5 4 [1 3 5 7 9]
    [] 0 3 []
    nil 0 3 []
    )
  ;; gstring
  (are (= _1 _2)
    (gstring "${name1} at ${age1}, ${age1}.${name1}" {:name1 "qh" :age1 30}) "qh at 30, 30.qh"
    )
  )

;;;--------------- wr3.clj.n
(deftest n-test []
  (are (= _1 _2)
    (safe-index 0 3) 0
    (safe-index 3 3) 0
    (safe-index 10 3) 1
    (safe-index -1 3) 2
    (safe-index -3 3) 0
    (safe-index -4 3) 2
    (digitals 2011) '(2 0 1 1)
    )
  )

;;;--------------- wr3.clj.u
(deftest util-test []
  ;; permutations, combines
  (are (= _1 _2)
    (permutations nil) '(nil)
    (permutations []) '(nil)
    (permutations [1]) '((1))
    (permutations [1 2]) '((1 2) (2 1))
    (permutations ['a 'b 'c]) '((a b c) (a c b) (b a c) (b c a) (c a b) (c b a))
    (combines nil) '()
    (combines []) '()
    (combines [1]) '((1))
    (combines [1 2]) '((1) (2) (1 2))
    (combines ['a 'b 'c]) '((a) (b) (a b) (c) (a c) (b c) (a b c))
    )
  ;; fold-left fold-right
  (are (= _1 _2)
    (fold-left / 1 [2 3]) (/ (/ 1 2) 3)
    (fold-right / 1 [2 3]) (/ 1 (/ 2 3))
    )
  ;; all-true?
  (are (= _1 _2)
    (all-true? true true true false true) false
    (all-true? (= 1 1) (> 5 3)) true
    (all-true? (= 1 1) (> 5 6)) false
    )
  ;; ns-exist?
  (are (true? _1)
    (ns-exist? "wr3.clj.s")
    ;(not (ns-exist? "wr3.clj"))
    ;(not (ns-exist? "wr3.clj.s1"))
    )
  ;; fget
  (are (= _1 _2)
    (fget "wr3.clj.u" "fget") (var fget)
    (fget "wr3.clj.u" "fget2") nil
    ;(fget "wr3.clj.u2" "fget") nil
    )
  ;; fcall
  (are (= _1 _2)
    (<= 0 (fcall "wr3.clj.n" "random") 100) true
    (fcall "wr3.clj.s" "left" "hello-world" "-") "hello"
    ;(fcall "wr3.clj.n2" "random") "fcall error: (wr3.clj.n2/random) not found."
    (fcall "wr3.clj.n" "random2") "fcall error: (wr3.clj.n/random2) not found."
    )
  ;; fargc
  (are (= _1 _2)
    (fargc "wr3.clj.s" "left") 2
    (fargc "wr3.clj.n" "random") 0
    ;(fargc "wr3.clj.s2" "left") 0
    (fargc "wr3.clj.s" "left2") 0
    )
  )

;;--------------- wr3.clj.tb
(deftest table-test []
  (are (= _1 _2)
    ;; row?
    (row? []) true
    (row? [1 2]) true
    (row? ["qh" 18]) true
    (row? '()) false
    (row? nil) false
    ;; row
    (count (row 3)) 3
    (first (row 3 100)) 100 ; [100 100 100]
    (row 0) []
    ; row-get
    (row-get [1 3 5 7 9] 0) 1
    (row-get [1 3 5 7 9] -1) 9
    (row-get [1 3 5 7 9] -1) 9
    ; row-set
    (row-set [1 3 5 7 9] 0 100) [100 3 5 7 9]
    (row-set [1 3 5 7 9] -1 90) [1 3 5 7 90]
    )
  )

;; ------------ web
(deftest web-test []
  (are (= _1 _2)
    ;; hiccup
    (html [:h1 "hello"]) "<h1>hello</h1>"
    (html [:html [:body "hello"]]) "<html><body>hello</body></html>"
    (html [:body "foo" [:h1 "hello"] "bar"]) "<body>foo<h1>hello</h1>bar</body>"
    (html [:h1#id1 "hello"]) "<h1 id=\"id1\">hello</h1>"
    (html [:h1.c1 "hello"]) "<h1 class=\"c1\">hello</h1>"
    (html [:h1#id1.c1 "hello"]) "<h1 class=\"c1\" id=\"id1\">hello</h1>"
    (html [:div {:style "" :name "div1"} "hello"]) "<div name=\"div1\" style=\"\">hello</div>"
    ;; web
    (html-head "<style>...</style>" "hello") "<html><head><style>...</style></head><body>hello</body></html>"
    )
)

;; ------------ db dbconfig
(deftest db-test []
  (are (= _1 _2)
    ;; dbconfig
    (:driver (:h2 dbnames)) (:h2 drivers)
    (:username (:h2 dbnames)) ""
    (:password (:h2 dbnames)) ""
    ;; db
    ; TODO
    )
)

;; ------------ CljServlet
(deftest CljServlet-test []
  (are (= _1 _2)
    wr3.clj.CljServlet/nil-msg "fcall return nil"
    )
)

;;--------------- main
(defn -main [& args]
  (def t0 (System/currentTimeMillis))

  (s-test)
  (n-test)
  (util-test)
  (table-test)
  (web-test)
  (db-test)
  (CljServlet-test)

  (def t1 (System/currentTimeMillis))
  (println (/ (- t1 t0) 1000.0) "s")
)

(-main)
