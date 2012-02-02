package test.scala

import _root_.wr3.util._
import _root_.wr3.scala.Tools._

/**
 * ������wr3��ʹ��scala�﷨��д����
 */
object ScalaTest {

  def test1 = {
    // ����Stringx�ķ���
    val s = "jamesqiu"
    Console println "Stringx.capitalize(" + s + ")=" + Stringx.capitalize(s)
    Console println s + ".capitalize=" + s.capitalize // �÷���scala��������

    // ��ʽ����Stringx�ķ���
    println("\nString right, rightback")
    Console println Stringx.right("1-2-3", "-")
    Console println Stringx.rightback("1-2-3", "-")
    Console println "1-2-3".rightback("-")

    // ��ʽ����padLeft,padRight
    println("\nString padLeft, padRight:")
    println("23".padLeft(5, "0"))
    println("23".padLeft(5, "Ab"))
    println("23.".padRight(5, "0"))
    println("23.".padRight(6, "01"))
  }

  def test2 = {
    // ���� Tools#prime ������
    def speed = {
      val t0 = System.currentTimeMillis
      val count = 1 to 1000000 count prime
      println("count primes from 1 to 1000000: " + count + ", time: " + (System.currentTimeMillis - t0))
    }

    speed
    println(1 to 2000000 filter prime sum)
  }

  def test3 = {
    // ����ȫ����
    permutation("abc".toList) foreach (l => println(l.mkString("")))
    println
    permutation(List(5, 6, 7)) foreach (l => println(l.mkString("")))

  }

  def test4 = {
    // ��Ŀ����ڶ�λ����7��6��8�����ڵ���������
    val l = List(3, 2, 2, 6, 7, 8)
    val l2 = permutation(l) filterNot (e => e(1) == 7) filterNot (e => (e.indexOf(6) - e.indexOf(8)).abs == 1)
    println(l + "�ڶ�λ����7��6��8�����ڵ���������:" + l2.size)
    //    l2 foreach println

  }

  def test5 = {
    // ���� Tools#power ������
    println("2 ** 1000 = " + 2 ** 1000)
    println("" + Integer.MAX_VALUE + " ** 2 = " + Integer.MAX_VALUE ** 2) // ���� Tools#power

  }

  // �������ڰ��µ�һ�⣬sum�������б�
  def test6 = {
    val l0 = List(31, -41, 59, 26, -53, 58, 97, -93, -23, 84)
    val size = l0.size
    var (ii, jj, max) = (0, 1, l0(0))
    for (i <- 0 until size) for (j <- i until size) {
      val l = l0 slice (i, j+1)
      val sum = l.sum
      if (sum > max) { ii = i; jj = j; max = sum }
    }
    println("%s\n%d-%d, %s.sum=%d" format (l0, ii, jj, l0.slice(ii, jj+1), max))
  }

  def test7 = {
    println("---test7---")
  }

  def main(args: Array[String]): Unit = {

    if (args.size == 0) usage
    args(0) match {
      case "1" => test1
      case "2" => test2
      case "3" => test3
      case "4" => test4
      case "5" => test5
      case "6" => test6
      case "7" => test7
      case _ => usage
    }
  }

}
