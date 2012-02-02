package test.scala

import wr3.scala.Tools._

/**
 * ��ϰ�ݹ��㷨
 * @author jamesqiu 2010-8-11
 */
object Recursion {

  //--------------------------------- ����
  // ������㸨��
  val cal = new java.util.GregorianCalendar()
  private def leapYear(y: Int) = cal.isLeapYear(y)
  private def nextLeap(y: Int) = (y + 1 to y + 100 find leapYear) get

  /**
   * ���1000������
   */
  def leap1000th(y: Int, i: Int): Int = if (i == 1000) y else leap1000th(nextLeap(y), i + 1)
  /**
   * ��2012�ǵڼ�������
   */
  def leap2012(y: Int, i: Int): Int = if (y == 2012) i else leap2012(nextLeap(y), i + 1)

  //--------------------------------- fib����
  /**
   * һ���㷨��
   * fib����: 1 1 2 3 5 8 13 ...
   */
  def fib1(n: Int): Int = if (n <= 2) 1 else fib1(n - 1) + fib1(n - 2)

  /**
   * β�ݹ��㷨��
   * fib(1,1,1,2,N)
   * fib(2,1,2,3,N)
   * fib(3,2,3,5,N)
   * fib(4,3,5,8,N)
   *  i: ��ǰfib�����, 1 to N ˳�����
   *  n_2,n_1��n��������fib��
   *  n: ��ǰfib��
   *  N��Ҫ����ǵڼ�fib��
   */
  def fib2(i: Int, n_2: Int, n_1: Int, n: Int, N: Int): Int = if (i == N) n else fib2(i + 1, n_1, n, n_1 + n, N)
  def fib3(N: Int) = fib2(3, 1, 1, 2, N) // 2�ǵ�3��fib��

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
  // ��Ѱ���հ�� List[T].max �ݹ�ʵ��
  def max0[T <% Ordered[T]](a: T, b: T) = if (a > b) a else b
  def max[T <% Ordered[T]](l: List[T]): T = if (l.size == 1) l(0) else max0(l(0), max(l.slice(1, l.size)))

  //--------------------------------- �˻ʺ����⣨ֱ�߻���45�Ƚ������ڲ����лʺ�
  /**
   * ˼·���ʺ��� (1 to 8) zip (12345678��ȫ����) ʱ��û��(r1-r0).abs==(c1-c0).abs����Щ���о�����
   * һ��8�ʺ��ŷ��ı�ʾ��(2,5)��ʾ��2�е�5���лʺ�
   * Vector((1,1), (2,5), (3,8), (4,6), (5,3), (6,7), (7,2), (8,4))
   */
  // ����ÿ��ÿ��һ���ʺ���ŷ�
  def all = permutation(List(1, 2, 3, 4, 5, 6, 7, 8)).map(l => (1 to 8) zip l)
  // 45���ӽ���û�лʺ���ŷ�
  def isQueen8(m: Seq[(Int, Int)]) = {
    var found = false;
    for ((r1, c1) <- m; (r2, c2) <- m if (!found)) {
      if ((r2 - r1) != 0 && (r2 - r1).abs == (c2 - c1).abs) found = true
    }
    !found
  }
  // ɸ������8�ʺ���ŷ�
  def queen8 = all filter (v => isQueen8(v))
  // ��i��Ԫ���ǻʺ���е����������line(2)="0 0 1 0 0 0 0 0"
  def line(i: Int) = { var a = Array.fill(8)("0"); a(i) = "1"; a mkString (" ") }
  // ��ʾ
  def queenString(m: Seq[(Int, Int)]) = m map (e => line(e._2 - 1)) mkString ("\n")

  //--------------------------------- Hanoi��ŵ��
  import scala.collection.mutable.Stack
  def hanoi(n: Int) = {
    // n��һ��ȫ��Stack(1,2,3,...,n)
    def tow(n: Int) = { var s = Stack[Int](); (n to 1 by -1) foreach (e => s push e); s }
    // ���ɳ�ʼ3��
    val ss = List(tow(n), Stack[Int](), Stack[Int]())
    // ���ַ��������յ��̶�����
    def fill(n: Int, s: String) = ("%" + n + "s") format s
    // 3��������߸߶�
    def max(ss: List[Stack[Int]]) = ss map (_.size) max
    // ���һ����
    def out(ss: List[Stack[Int]]) = {
      val n = max(ss)
      val s1 = ss map (s => s.mkString) map (s => fill(n, s)) // List("123", "  4", " 56")
      val s = (for (i <- 0 until n) yield ("" + s1(0)(i) + " " + s1(1)(i) + " " + s1(2)(i))) mkString ("\n")
      println(s + "\n- - -")
    }

    out(ss)
    // �ݹ��ƶ��㷨
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

  //--------------------------------- ��̨��
  /**
   * ��n��̨�ף�ÿ��������1�׻���2�ף��г����е��Ϸ�
   */
  def step(n: Int) = {

    // �����Ϸ�
	def count(n:Int):Int = n match {
		case 1 | 2 => n
		case _ => count(n-1)+count(n-2)
	}
	println("count = " + count(n))

    /**
     * �г������Ϸ�
     * (1 2) * (1 2)
     * (11 12 21 22) * (1 2)
     * (111 112 121 122_ 211 212_ 221_ 222x) * (1 2)
     * (1111 1112_ 1121_ 1211_ 2111_) * (1 2)
     * (11111_)
     *
     */
    def next(a: List[List[Int]]) =
      (for (i <- a; j <- List(1, 2)) yield {if (i.sum==n) i else i:::j::Nil}) .filter(_.sum<=n).distinct
    val l1 = List(List(1),List(2)) // ��һ��������1̨����2̨
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
    		println("--- ���꣺")
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
    		println("--- �˻ʺ����� (n=%d)" format n2)
    		val q8 = queen8
    		q8 foreach (e => println(queenString(e) + "\n---------------"))
    		println("count=" + q8.size)
    	}
    	case "6"|"hanoi" => {
    		var n3 = 7
    		println("--- ��ŵ�� (n=%d)" format n3)
    		hanoi(n3)
    	}
    	case "7"|"step" => {
    		val n4 = 5
    		println("--- ��̨�� (n=%d)" format n4)
    		step(n4)
    	}
    	case _ => {
    		println(usage)
    	}
    }
  }
}
