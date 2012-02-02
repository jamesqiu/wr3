package wr3.bank;

import wr3.util.Numberx;

/**
 * <pre>
 * 银行存款利息算法.
 * 输入: 本金, 年利率, 存款时间(月或者天), 利息税率
 * 输出: 税后利息, 税前利息, 利息税
 * 
 * usage:
 *  interest = new Interest();
 *  interest.amount(100000);
 *  interest.annualRate(1.8); // 1.8%
 *  interest.month(3); // interest.day(10);
 *  interest.tax(20); // 20%
 *  rt1 = interest.after();
 *  rt2 = interest.before();
 *  rt3 = interest.tax();
 * 
 * 参考: 
 *   1) 浦发的人民币年利率, 活期存款为0.36厘, 1年期整存整取为2.25, 定活两便为2.25*0.6=1.35
 *   2) 定期存款按照存款日的年利率r0计算, 无论存期内利率如何变化; 
 *      提前支取(全部或者部分)按照支取当日的利率r1计算, 剩余按r0计算; 
 *      但银行经储户同意可自动转存后分段计息.
 * </pre> 
 * @author jamesqiu 2009-2-5
 *
 */
public class Interest {

	/**
	 * 
	 * @param amount 本金
	 * @param rate 年利率, rate=1.8厘按照1.8%即0.018计算
	 * @param month 存款时间
	 * @param taxRate 利息税率, taxRate=20厘按照20%即0.2计算
	 * @return 税后本金
	 */
	public static double after(double amount, double rate, int month, double taxRate) {
		
		double interest = amount * rate / 100 / 12 * month;
		double tax = interest * taxRate / 100;
		return amount + interest - tax;
	}
	
	// ---------------------- main() ----------------------
	public static void main(String[] args) {
		
		if (args.length < 4) {
			System.out.println("usage:" +
					"Interest 1.本金  2.年利率  3.存款月份数  4.利息税率");
			return;
		}
		
		double amount = Numberx.toDouble(args[0], 100000.0);
		double rate = Numberx.toDouble(args[1], 1.8);
		int month = Numberx.toInt(args[2], 3);
		double taxRate = Numberx.toDouble(args[3], 20);
		
		System.out.println(Interest.after(amount, rate, month, taxRate));
	}
}
