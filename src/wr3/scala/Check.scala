package wr3.scala

/**
 * ��¼���ַ�������У��
 * @author jamesqiu 2010-10-15
 */
object Check {

  /**
   * ��֯�������룬��ʽ�Ϻ��淶�����߼�����
   * ��֯���������ɰ�λ���֣����д������ĸ����λ���һλ���֣����д������ĸ��У������ɣ�
   * ��ʽΪXXXXXXXX-X����׼��֯�����������GB11714-1997��
   * C9 = 11 - MOD( ((1 to 8) map (Ci*Wi) sum), 11)
   */

  // ��ǰ8λ�õ�У��λ
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

  // �ж��Ƿ�һ���Ϸ�����֯��������
  def isOrgNo(s: String): Boolean = {

    // 1������� [8λ���ֻ��д��ĸ][-][1λ���ֻ��д��ĸ] ���
    val check0 = "[0-9A-Z]{8}-[0-9X]".r.pattern.matcher(s).matches
    if (!check0) return false

    // 2�����У��λ��ȷ��
    val check1 = C9(s.slice(0, 8)) == s(9)
    if (check1) true else false
    //		`match`(s, "12345678-0")
  }

  // �������һ����֯��������
  def orgNo() = {

    val cc = ('0' to '9') ++ ('A' to 'Z')
    import wr3.scala.Tools._
    def ci = cc.apply((cc.size - 1).random)
    val s8 = ((1 to 8) map (i => ci)).mkString
    s8 + "-" + C9(s8)
  }

  /**
   * �̶��绰�ʹ������, ���ա�����+�绰���롱��ʽ���зֻ��ŵĵ绰�á�-���������磺037165900893-1234
   */
  def isPhone(s: String) = "[0-9]{8,12}(-[0-9]+)?".r.pattern.matcher(s).matches

  /**
   * �ֻ�������ձ�׼11λ���룬ǰ���ӡ�0��
   */
  def isMobile(s: String) = "[0-9]{11}".r.pattern.matcher(s).matches

  /**
   * ���ڣ����ϱ�׼��ʽyyyyMMdd
   */
  def isDate(s: String) = wr3.util.Datetime.isDate(s)
  /**
   * �����ʼ������ϱ�׼��ʽ
   */
  def isEmail(s: String): Boolean = {

    //    val pattern = "^[a-z0-9._%\\-+]+@(?:[a-z0-9\\-]+\\.)+[a-z]{2,4}$"
    // ΢���ṩ��: \w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*
    val pattern = "\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*(\\.\\w+([-.]\\w+))*"
    val emailRegex = pattern.r
    emailRegex.pattern.matcher(s).matches
  }

  /**
   * ��ֵ��С��̬��ʾ����ֹ����������, min=null�������½磬max=null�������Ͻ�
   */
  def isRange(s: String, min: BigInt=Long.MinValue, max: BigInt=Long.MaxValue): Boolean = {
	  val c0 = "[-]?\\d+".r.pattern.matcher(s).matches
	  if (!c0) return false
	  (BigInt(s) >= min) && (BigInt(s) <= max)
  }

  /**
   * �ؼ��ֶβ���Ϊ��
   */
  def isNull(s: String) = wr3.util.Stringx.nullity(s)

  /**
   * �Ƿ�Ϸ�18λ���֤, ���֤�ţ����ϱ�׼��ʽ�����߼�����
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