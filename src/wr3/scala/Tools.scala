package wr3.scala
 
object Tools { 

  /**
   * 判断一个Int/BigInt是否质数
   */
  def prime(n: Int) = wr3.util.Numberx.isPrime(n)
  def prime(n: BigInt) = n.isProbablePrime(10)

  /**
   * 定义阶乘，尾递归算法
   * usage：5! = 120
   */
  def factorial(n: Int) = ((1:BigInt) to n) product
  implicit def foo(n: Int) = new { def ! = factorial(n) }

  /**
   * 定义padLeft，padRight
   */
  implicit def foo2(ss:String) = new { def padLeft(n:Int, s:String) = wr3.util.Stringx.padLeft(ss, n, s) }
  implicit def foo3(ss:String) = new { def padRight(n:Int, s:String) = wr3.util.Stringx.padRight(ss, n, s) }

  /**
   * 定义幂运算 n**m，尾递归算法
   * usage：2**10 = 1024
   */
  def power(n:Int, m:Int) = {
	  def f(n: Int, m: Int, r: BigInt): BigInt = m match {
	    case i if i <= 0 => r
	    case _ => f(n, m - 1, r * n)
	  }
	  f(n, m, 1)
  }
  implicit def foo2(n: Int) = new { def **(m: Int) = power(n, m) }

  /**
   * 对一个列表进行全排列。返回一个ListBuffer，包含所有排列顺序的列表。
   * 算法：排列abcd，就是把每一个元素(a,b,c,d)轮流拿出来排在最前面，后面跟余下元素(bcd,acd,abd,abc)的全排列
   */
  import scala.collection.mutable.ListBuffer
  // 把List中指定index的元素分离出来，如，split(List(a,b,c,d), 2) = (c, List(a,b,d))
  private def split[T](l:List[T], i:Int) = (l(i), l.slice(0,i) ::: l.slice(i+1,l.size))
  def permutation[T](l:List[T]):ListBuffer[List[T]] = if (l.size==1) ListBuffer(l) else {
	val rt = ListBuffer[List[T]]()
    for(i<-0 until l.size) {
    	val (e0, l1) = split(l, i)
    	for(e <- permutation(l1)) rt.append(e0 :: e)
    }
	rt
  }

  /**
   * scala application main()中使用，打印程序用法并退出
   */
  def usage = {
	  println("usage: following argument: 1|2|3|..." format this.getClass.getName)
	  exit
  }

  /**
   * 包装Stringx的常用函数, 直接使用：
   *   "abc-de".left("-");
   *   "abc(2010)def".between("(", ")");
   * 2010-10-12
   */
  import wr3.util._
  import Stringx._
  implicit def sleft(s0:String) = new { def left(s1:String) = Stringx.left(s0, s1) }
  implicit def sleftback(s0:String) = new { def leftback(s1:String) = Stringx.leftback(s0, s1) }
  implicit def sright(s0:String) = new { def right(s1:String) = Stringx.right(s0, s1) }
  implicit def srightback(s0:String) = new { def rightback(s1:String) = Stringx.rightback(s0, s1) }
  implicit def sbetween(s0:String) = new { def between(s1:String, s2:String) = Stringx.between(s0, s1, s2) }

  /**
   * 包装Numberx的常用函数，直接使用:
   *   "108".toInt(-1) // 108
   *   "10a".toInt(-1) // -1
   */
  import Numberx._
  implicit def s2int(s:String) = new { def int(i0:Int) = Numberx.toInt(s, i0) }
  implicit def srandom(n:Int) = new { def random = Numberx.random(n) }	// n.random [0~n]的随机整数
  implicit def srandoms(n:Int) = new { def randoms(m:Int) = Numberx.randoms(m, n) } // n.random(m) [0~n]的随机整数m个

}