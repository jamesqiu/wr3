package wr3.scala

/**
 * 对录入字符串进行校验
 * @author jamesqiu 2010-10-15
 */
object Check {

  /**
   * 组织机构代码，格式合乎规范，无逻辑错误
   * 组织机构代码由八位数字（或大写拉丁字母）本位码和一位数字（或大写拉丁字母）校验码组成，
   * 格式为XXXXXXXX-X，标准组织机构代码参照GB11714-1997。
   * C9 = 11 - MOD( ((1 to 8) map (Ci*Wi) sum), 11)
   */

  // 从前8位得到校验位
  def C9(s8: String) = {

    val Wi = Map((1, 3), (2, 7), (3, 9), (4, 10), (5, 5), (6, 8), (7, 4), (8, 2))
    val Ci = Map(('0', 0), ('1', 1), ('2', 2), ('3', 3), ('4', 4), ('5', 5), ('6', 6), ('7', 7), ('8', 8), ('9', 9),
      ('A', 10), ('B', 11), ('C', 12), ('D', 13), ('E', 14), ('F', 15),
      ('G', 16), ('H', 17), ('I', 18), ('J', 19), ('K', 20), ('L', 21),
      ('M', 22), ('N', 23), ('O', 24), ('P', 25), ('Q', 26), ('R', 27),
      ('S', 28), ('T', 29), ('U', 30), ('V', 31), ('W', 32), ('X', 33), ('Y', 34), ('Z', 35))
    val C9 = 11 - (s8.zipWithIndex.map(ci => (Ci.get(ci._1).get * Wi.get(ci._2 + 1).get)).sum) % 11
    C9 match { case 10 => 'X'; case 11 => '0'; case i => (i + '0').toChar }
  }

  // 判断是否一个合法的组织机构代码
  def isOrgNo(s: String): Boolean = {

    // 1、检测由 [8位数字或大写字母][-][1位数字或大写字母] 组成
    val check0 = "[0-9A-Z]{8}-[0-9X]".r.pattern.matcher(s).matches
    if (!check0) return false

    // 2、检测校验位正确性
    val check1 = C9(s.slice(0, 8)) == s(9)
    if (check1) true else false
    //		`match`(s, "12345678-0")
  }

  // 随机生成一个组织机构代码
  def orgNo() = {

    val cc = ('0' to '9') ++ ('A' to 'Z')
    import wr3.scala.Tools._
    def ci = cc.apply((cc.size - 1).random)
    val s8 = ((1 to 8) map (i => ci)).mkString
    s8 + "-" + C9(s8)
  }

  /**
   * 固定电话和传真号码, 参照“区号+电话号码”格式，有分机号的电话用“-”隔开，如：037165900893-1234
   */
  def isPhone(s: String) = "[0-9]{8,12}(-[0-9]+)?".r.pattern.matcher(s).matches

  /**
   * 手机号码参照标准11位号码，前不加“0”
   */
  def isMobile(s: String) = "[0-9]{11}".r.pattern.matcher(s).matches

  /**
   * 日期，符合标准格式yyyyMMdd
   */
  def isDate(s: String) = wr3.util.Datetime.isDate(s)
  /**
   * 电子邮件，符合标准格式
   */
  def isEmail(s: String): Boolean = {

    //    val pattern = "^[a-z0-9._%\\-+]+@(?:[a-z0-9\\-]+\\.)+[a-z]{2,4}$"
    // 微软提供的: \w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*
    val pattern = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*(\\.\\w+([-.]\\w+))*"
    val emailRegex = pattern.r
    emailRegex.pattern.matcher(s).matches
  }

  /**
   * 数值大小动态提示，防止出现误输入, min=null不控制下界，max=null不控制上界
   */
  def isRange(s: String, min: BigInt=Long.MinValue, max: BigInt=Long.MaxValue): Boolean = {
	  val c0 = "[-]?\\d+".r.pattern.matcher(s).matches
	  if (!c0) return false
	  (BigInt(s) >= min) && (BigInt(s) <= max)
  }

  /**
   * 关键字段不能为空
   */
  def isNull(s: String) = wr3.util.Stringx.nullity(s)

  /**
   * 是否合法18位身份证, 身份证号，符合标准格式，无逻辑错误
   */
  def isPID(s: String) = wr3.bank.IDUtil.is18(s)

  // ----------------- main() -----------------//
  def main(args: Array[String]) = {

	println("\nisOrgNo:")
    List("D2143569-X", "D2143569-x", "D2143569-Y", "D2143569-*", "d2143569-X", "2143569-X", "D2143569-Xyz")
      .foreach { s => println(isOrgNo(s) + ": " + s) }

    println("\nRandom orgNo:")
    println((1 to 5) map (c => "Random: " + orgNo) mkString ("\n"))

    println("\nisEmail:")
    List("_qiu._hui+3-4@mail-server.com.cn.com.cn.com.cn", "123@234", "qh@", "qh.mail.com")
      .foreach { s => println(isEmail(s) + ": " + s) }

    println("\nisPhone:")
    List("037165900893-1234", "62790202", "62790202-65", "10000", "0000123456789", "62790202-", "62790202--2", "62790202-1001-3")
      .foreach { s=> println(isPhone(s) + ": " + s) }

    println("\nisMobile:")
    List("13301350000", "13801235678", "62790202", "133013578910", "133 135 1234")
      .foreach { s=> println(isMobile(s) + ": " + s) }

    println("\ninRange:")
    List(("123", 100, 999), ("123", 123, 200), ("abc", 0, 100), ("123", 200, 100))
      .foreach { s=> println(isRange(s._1, min=s._2, max=s._3) + ": " + s) }
    println(isRange("123") + ": (123)")
    println(isRange("123", 100) + ": (123, 100)")
    println(isRange("123", max=200) + ": (123, max=200)")
    println(isRange("123", max=100) + ": (123, max=100)")

    println("--- The End ---")
  }
}