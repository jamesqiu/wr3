package tool;

import wr3.util.Numberx;

/**
 * <pre>
 * 个人所得税计算。公式：(income - base) * b - c
 * <b>应纳个人所得税税额=(应纳税所得－扣除标准)*适用税率-速算扣除数</b> 
 * 扣除标准base: 2000元/月
 
(应纳税所得－扣除标准)   适用税率 速算扣除数
a                        b        c
--------------------------------------------
<500元的                 5%       0 
500～2000                10%      25 
2000～5000               15%      125 
5000～20000              20%      375 
20000～40000             25%      1375 
40000～60000             30%      3375 
60000～80000             35%      6375 
80000～100000            40%      10375 
100000～                 45%      15375 
--------------------------------------------
2011-9-1号后实行新标准：
<=1500元                 3%       0
<=4500元                 10%      105
<=9000元                 20%      555
<=35000元                25%      1005
<=55000元                30%      2755
<=80000元                35%      5505
80000～                  45%      13505
--------------------------------------------
 * 
 * </pre>
 * @author jamesqiu 2010-5-28
 */
public class Tax {

	/**
	 * 2011-9-1之前的标准
	 * @param income
	 * @return
	 */
	public static double tax(double income) {		
		final double BASE = 2000d;
		double a = income - BASE;
		if (a <= 0) 
			return 0;
		if (a <= 500) 
			return a * 0.03d - 0d;
		if (a <= 2000)
			return a * 0.10d - 25d;
		if (a <= 5000)
			return a * 0.15d - 125d;
		if (a <= 20000)
			return a * 0.20d - 375d;
		if (a <= 40000) 
			return a * 0.25d - 1375d;
		if (a <= 60000)
			return a * 0.30d - 3375d;
		if (a <= 80000)
			return a * 0.35d - 6375d;
		if (a <= 100000)
			return a * 0.40d - 10375d;
		else 
			return a * 0.45d - 15375d;
	}
	
	/**
	 * 2011-9-1 之后的新标准
	 * @param income
	 * @return
	 */
	public static double taxNew(double income) {		
		final double BASE = 3500d;
		double a = income - BASE;
		
		if (a <= 0) 
			return 0;
		if (a <= 1500) 
			return a * 0.03d - 0d;
		if (a <= 4500)
			return a * 0.10d - 105d;
		if (a <= 9000)
			return a * 0.20d - 555d;
		if (a <= 35000)
			return a * 0.25d - 1005d;
		if (a <= 55000) 
			return a * 0.30d - 2755d;
		if (a <= 80000)
			return a * 0.35d - 5505d;
		else 
			return a * 0.45d - 13505d;
	}
	
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
		if (args.length==0) {
			System.out.println("计算个人所得税：Tax income [-n]");
			return;
		}
		
		double income = Numberx.toDouble(args[0], 0);
		double tax;
		if (args.length==2 && args[1].equals("-n")) {
			tax = taxNew(income);
		} else {
			tax = tax(income);
		}
		System.out.printf("(%.2f - %.2f) = %.2f\n", income, tax, (income-tax));		
	}
}
