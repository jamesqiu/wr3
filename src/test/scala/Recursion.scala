package test.scala

import wr3.scala.Tools._

/**
 * 练习递归算法
 * @author jamesqiu 2010-8-11
 */
object Recursion {

  //--------------------------------- 闰年
  // 闰年计算辅助
  val cal = new java.util.GregorianCalendar()
  private def leapYear(y: Int) = cal.isLeapYear(y)
  private def nextLeap(y: Int) = (y + 1 to y + 100 find leapYear) get

  /**
   * 求第1000个闰年
   */
  def leap1000th(y: Int, i: Int): Int = if (i == 1000) y else leap1000th(nextLeap(y), i + 1)
  /**
   * 求2012是第几个闰年
   */
  def leap2012(y: Int, i: Int): Int = if (y == 2012) i else leap2012(nextLeap(y), i + 1)

  //--------------------------------- fib数列
  /**
   * 一般算法：
   * fib数列: 1 1 2 3 5 8 13 ...
   */
  def fib1(n: Int): Int = if (n <= 2) 1 else fib1(n - 1) + fib1(n - 2)

  /**
   * 尾递归算法：
   * fib(1,1,1,2,N)
   * fib(2,1,2,3,N)
   * fib(3,2,3,5,N)
   * fib(4,3,5,8,N)
   *  i: 当前fib数序号, 1 to N 顺序递增
   *  n_2,n_1：n的上两个fib数
   *  n: 当前fib数
   *  N：要求的是第几fib数
   */
  def fib2(i: Int, n_2: Int, n_1: Int, n: Int, N: Int): Int = if (i == N) n else fib2(i + 1, n_1, n, n_1 + n, N)
  def fib3(N: Int) = fib2(3, 1, 1, 2, N) // 2是第3个fib数

  //--------------------------------- N!
  def nn1(n: Int): BigInt = if (n <= 1) 1 else nn1(n - 1) * n
  /**
   * nn1(5, 1)
   * nn1(4, 1*5)
   * nn1(3, 1*5*4)
   * nn1(2, 1*5*4*3)
   * nn1(1, 1*5*4*3*2)
   */
  def nn2(n: Int, r: BigInt): BigInt = if (n <= 1) r else nn2(n - 1, r * n)
  def nn3(n: Int): BigInt = nn2(n, 1)

  //--------------------------------- n**m
  /**
   * p(2,10,1)
   * p(2,9, 1*2)
   * p(2,8, 1*2*2)
   */
  def pow1(n: BigInt, m: BigInt, r: BigInt): BigInt = if (n == 1 || m <= 0) r else pow1(n, m - 1, r * n)
  def pow(n: BigInt, m: BigInt) = pow1(n, m, 1)

  //--------------------------------- max
  // 自寻烦恼版的 List[T].max 递归实现
  def max0[T <% Ordered[T]](a: T, b: T) = if (a > b) a else b
  def max[T <% Ordered[T]](l: List[T]): T = if (l.size == 1) l(0) else max0(l(0), max(l.slice(1, l.size)))

  //--------------------------------- 八皇后问题（直线或者45度角视线内不能有皇后）
  /**
   * 思路，皇后在 (1 to 8) zip (12345678的全排列) 时，没有(r1-r0).abs==(c1-c0).abs的那些排列就是了
   * 一个8皇后排法的表示：(2,5)表示第2行第5列有皇后
   * Vector((1,1), (2,5), (3,8), (4,6), (5,3), (6,7), (7,2), (8,4))
   */
  // 所有每行每列一个皇后的排法
  def all = permutation(List(1, 2, 3, 4, 5, 6, 7, 8)).map(l => (1 to 8) zip l)
  // 45度视角内没有皇后的排法
  def isQueen8(m: Seq[(Int, Int)]) = {
    var found = false;
    for ((r1, c1) <- m; (r2, c2) <- m if (!found)) {
      if ((r2 - r1) != 0 && (r2 - r1).abs == (c2 - c1).abs) found = true
    }
    !found
  }
  // 筛出所有8皇后的排法
  def queen8 = all filter (v => isQueen8(v))
  // 第i个元素是皇后的行的输出，例如line(2)="0 0 1 0 0 0 0 0"
  def line(i: Int) = { var a = Array.fill(8)("0"); a(i) = "1"; a mkString (" ") }
  // 表示
  def queenString(m: Seq[(Int, Int)]) = m map (e => line(e._2 - 1)) mkString ("\n")

  //--------------------------------- Hanoi汉诺塔
  import scala.collection.mutable.Stack
  def hanoi(n: Int) = {
    // n的一个全塔Stack(1,2,3,...,n)
    def tow(n: Int) = { var s = Stack[Int](); (n to 1 by -1) foreach (e => s push e); s }
    // 生成初始3塔
    val ss = List(tow(n), Stack[Int](), Stack[Int]())
    // 把字符串左边填空到固定长度
    def fill(n: Int, s: String) = ("%" + n + "s") format s
    // 3个塔的最高高度
    def max(ss: List[Stack[Int]]) = ss map (_.size) max
    // 输出一个塔
    def out(ss: List[Stack[Int]]) = {
      val n = max(ss)
      val s1 = ss map (s => s.mkString) map (s => fill(n, s)) // List("123", "  4", " 56")
      val s = (for (i <- 0 until n) yield ("" + s1(0)(i) + " " + s1(1)(i) + " " + s1(2)(i))) mkString ("\n")
      println(s + "\n- - -")
    }

    out(ss)
    // 递归移动算法
    def move(n: Int, from: Int, to: Int, via: Int): Unit = {
      if (n == 1) {
        ss(to) push (ss(from) pop)
        out(ss)
      } else {
        move(n - 1, from, via, to)
        move(1, from, to, via)
        move(n - 1, via, to, from)
      }
    }
    move(n, 0, 2, 1)
  }

  //--------------------------------- 上台阶
  /**
   * 上n级台阶，每步可以上1阶或者2阶，列出所有的上法
   */
  def step(n: Int) = {

    // 仅就上法
	def count(n:Int):Int = n match {
		case 1 | 2 => n
		case _ => count(n-1)+count(n-2)
	}
	println("count = " + count(n))

    /**
     * 列出所有上法
     * (1 2) * (1 2)
     * (11 12 21 22) * (1 2)
     * (111 112 121 122_ 211 212_ 221_ 222x) * (1 2)
     * (1111 1112_ 1121_ 1211_ 2111_) * (1 2)
     * (11111_)
     *
     */
    def next(a: List[List[Int]]) =
      (for (i <- a; j <- List(1, 2)) yield {if (i.sum==n) i else i:::j::Nil}) .filter(_.sum<=n).distinct
    val l1 = List(List(1),List(2)) // 第一步可以走1台或者2台
    def f(a:List[List[Int]]):List[List[Int]] = if (a(0).size==n) a else f(next(a))
    f(l1) foreach (l=>println(l mkString))
  }

  //--------------------------------- main()
  def main(args: Array[String]): Unit = {

    var t0 = 0L
    var usage = "usage: ru Recursion 1|2|3|4|5|6|7"
    if (args.size==0) {
    	println(usage)
    	return
    }
    args(0) match {
    	case "1"|"leap"=> {
    		println("--- 闰年：")
    		println(leap1000th(-1, 0)) // 4068
    		println(leap2012(-1, 0)) // 501
    	}
    	case "2"|"fib"=> {
    		val n = 42
    		println("--- fib(%d)" format n)
    		var t0 = System.currentTimeMillis
    		println(fib1(n)) //
    		println(System.currentTimeMillis - t0)
    		t0 = System.currentTimeMillis
    		println(fib3(n))
    		println(System.currentTimeMillis - t0)
    	}
    	case "3"|"nn" => {
    		val N = 50
    		println("--- %d!" format N)
    		t0 = System.currentTimeMillis
    		println(nn1(N))
    		println(System.currentTimeMillis - t0)
    		t0 = System.currentTimeMillis
    		println(nn3(N))
    		println(System.currentTimeMillis - t0)
    	}
    	case "4"|"max" => {
    		val l = List.fill(10)(scala.util.Random.nextInt(101))
    		println("--- max(%s)" format l)
    		println(max(l))
    		println(l max)
    	}
    	case "5"|"queen" => {
    		val n2 = 8
    		println("--- 八皇后问题 (n=%d)" format n2)
    		val q8 = queen8
    		q8 foreach (e => println(queenString(e) + "\n---------------"))
    		println("count=" + q8.size)
    	}
    	case "6"|"hanoi" => {
    		var n3 = 7
    		println("--- 汉诺塔 (n=%d)" format n3)
    		hanoi(n3)
    	}
    	case "7"|"step" => {
    		val n4 = 5
    		println("--- 上台阶 (n=%d)" format n4)
    		step(n4)
    	}
    	case _ => {
    		println(usage)
    	}
    }
  }
}
