package wr3.bank;

import wr3.util.Numberx;

/**
 * <pre>
 * ���д����Ϣ�㷨.
 * ����: ����, ������, ���ʱ��(�»�����), ��Ϣ˰��
 * ���: ˰����Ϣ, ˰ǰ��Ϣ, ��Ϣ˰
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
 * �ο�: 
 *   1) �ַ��������������, ���ڴ��Ϊ0.36��, 1����������ȡΪ2.25, ��������Ϊ2.25*0.6=1.35
 *   2) ���ڴ��մ���յ�������r0����, ���۴�����������α仯; 
 *      ��ǰ֧ȡ(ȫ�����߲���)����֧ȡ���յ�����r1����, ʣ�ఴr0����; 
 *      �����о�����ͬ����Զ�ת���ֶμ�Ϣ.
 * </pre> 
 * @author jamesqiu 2009-2-5
 *
 */
public class Interest {

	/**
	 * 
	 * @param amount ����
	 * @param rate ������, rate=1.8�尴��1.8%��0.018����
	 * @param month ���ʱ��
	 * @param taxRate ��Ϣ˰��, taxRate=20�尴��20%��0.2����
	 * @return ˰�󱾽�
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
					"Interest 1.����  2.������  3.����·���  4.��Ϣ˰��");
			return;
		}
		
		double amount = Numberx.toDouble(args[0], 100000.0);
		double rate = Numberx.toDouble(args[1], 1.8);
		int month = Numberx.toInt(args[2], 3);
		double taxRate = Numberx.toDouble(args[3], 20);
		
		System.out.println(Interest.after(amount, rate, month, taxRate));
	}
}
