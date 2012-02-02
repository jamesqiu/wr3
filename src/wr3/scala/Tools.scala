package wr3.scala
 
object Tools { 

  /**
   * �ж�һ��Int/BigInt�Ƿ�����
   */
  def prime(n: Int) = wr3.util.Numberx.isPrime(n)
  def prime(n: BigInt) = n.isProbablePrime(10)

  /**
   * ����׳ˣ�β�ݹ��㷨
   * usage��5! = 120
   */
  def factorial(n: Int) = ((1:BigInt) to n) product
  implicit def foo(n: Int) = new { def ! = factorial(n) }

  /**
   * ����padLeft��padRight
   */
  implicit def foo2(ss:String) = new { def padLeft(n:Int, s:String) = wr3.util.Stringx.padLeft(ss, n, s) }
  implicit def foo3(ss:String) = new { def padRight(n:Int, s:String) = wr3.util.Stringx.padRight(ss, n, s) }

  /**
   * ���������� n**m��β�ݹ��㷨
   * usage��2**10 = 1024
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
   * ��һ���б����ȫ���С�����һ��ListBuffer��������������˳����б�
   * �㷨������abcd�����ǰ�ÿһ��Ԫ��(a,b,c,d)�����ó���������ǰ�棬���������Ԫ��(bcd,acd,abd,abc)��ȫ����
   */
  import scala.collection.mutable.ListBuffer
  // ��List��ָ��index��Ԫ�ط���������磬split(List(a,b,c,d), 2) = (c, List(a,b,d))
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
   * scala application main()��ʹ�ã���ӡ�����÷����˳�
   */
  def usage = {
	  println("usage: following argument: 1|2|3|..." format this.getClass.getName)
	  exit
  }

  /**
   * ��װStringx�ĳ��ú���, ֱ��ʹ�ã�
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
   * ��װNumberx�ĳ��ú�����ֱ��ʹ��:
   *   "108".toInt(-1) // 108
   *   "10a".toInt(-1) // -1
   */
  import Numberx._
  implicit def s2int(s:String) = new { def int(i0:Int) = Numberx.toInt(s, i0) }
  implicit def srandom(n:Int) = new { def random = Numberx.random(n) }	// n.random [0~n]���������
  implicit def srandoms(n:Int) = new { def randoms(m:Int) = Numberx.randoms(m, n) } // n.random(m) [0~n]���������m��

}