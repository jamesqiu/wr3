package test.scala

/**
 * 直接 extends Application 可直接按脚本书写
 * @author jamesqiu 2010-11-29
 */
object Sca2 extends Application {

	val (a,b) = (2.00-1.10, BigDecimal(2.00)-BigDecimal(1.10))
	println("2.00-1.10=" + a)
	println("BigDecimal(2.00)-BigDecimal(1.10)=" + b)

	println("\n1000 以内完美数 (等于所有因子之和的数):")
	def fac(n:Int) = 1 until n filter (i=>n%i==0)
	def perfect(n:Int) = fac(n).sum == n
	1 to 1000 filter perfect map(n => n + "=" + fac(n).mkString("+")) foreach println
}