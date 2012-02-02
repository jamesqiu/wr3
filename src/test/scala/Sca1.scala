package test.scala 

import wr3.scala.Tools._
import wr3.util._

object Sca1 {

  /**
   * 求600851475143 的最大质因子: 6857
   */
  def projectEuler3 = {
    val n = 600851475143L;
    val rt = (3 to Integer.MAX_VALUE by 2).find (i=>(n%i==0 && prime(n/i)))
    println("600851475143 的最大质因子: " + n/(rt.getOrElse(1)))
//    var i = 3;
//    var found = false;
//    while (!found) {
//    	i += 2;
//    	if (n%i==0 && prime(n/i)) {
//    		println("600851475143 的最大质因子: " + n/i)
//    		found = true
//    	}
//    }
  }

  def test1 = { 
    println("-- test1 --")
    5 to 9 map {i=>("%d**3=%d" format (i, i*i*i))} foreach println
  }
  
  def main(args: Array[String]): Unit = {
    println("-- Sca1.scala -- e")
    println("100.random=" + 100.random)
    println("100.randoms(7)=" + 100.randoms(7).toList) // 7个[0,100]的随机数
    println("100.randoms(1000).avg=" + 100.randoms(1000).toList.sum / 1000.0) // 7个[0,100]的随机数
//    projectEuler3
    test1
    println("")
    Console println Datetime.datetime
  }

}