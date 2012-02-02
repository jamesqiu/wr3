package test;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.win32.OS;
import org.eclipse.swt.internal.win32.TCHAR;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import wr3.gui.WinI;
import wr3.gui.Swt;
import wr3.util.tuple.Triple;

/**
 * ͨ��swt����windows��sdk����ʹ��OS�ࡣ
 * @author jamesqiu 2010-1-18
 */
public class TestSwt {

	
	/**
	 * ����win32 �Ի���
	 */
	public static void sdkMessage() {
		
		// MessageBoxW()
		OS.MessageBoxW(0, "����\0".toCharArray(), "����\0".toCharArray(), 0);
		
		// MessageBox()
		TCHAR text = TEXT("Hello cn����");
		TCHAR caption = TEXT("����1");
		
		int rt = OS.MessageBox(0, text, caption, 
				OS.MB_ICONINFORMATION | OS.MB_OKCANCEL);

		System.out.println("MessageBox return " + rt);
	}
	
	private static TCHAR TEXT(String s) {
		return new TCHAR(0, s, true);
	}
	
	/**
	 * ����һ����͸���Ĵ���
	 * @param args
	 */
	public static void windowRaw() {

		Display display = Display.getDefault();
		Shell shell = new Shell(display, SWT.SHELL_TRIM);
		createContent(shell);
		shell.open();
		shell.layout();
		
		Swt.alert(shell, "alert");
		Swt.confirm(shell, "confirm?");
		String file = Swt.fileDialog(shell);
		System.out.println("ѡ���ļ���" + file);
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	private static void createContent(Shell shell) {
		
        shell.setText("SWT ��͸������");
//        shell.setSize(500, 375); // ֻ����С
        shell.setBounds(100, 100, 300, 200); // ��λ�úʹ�С
        shell.setLayout(new GridLayout());

        // �µ�swt����������1�д�����������sdk����
//        shell.setAlpha(100);
        
        OS.SetWindowLong(shell.handle, OS.GWL_EXSTYLE, OS.GetWindowLong(
                        shell.handle, OS.GWL_EXSTYLE) ^ 0x80000);

        TCHAR lpLibFileName = new TCHAR(0, "User32.dll", true);
        int hInst = OS.LoadLibrary(lpLibFileName);
        if (hInst != 0) {
                String name = "SetLayeredWindowAttributes\0";
                byte[] lpProcName = new byte[name.length()];
                for (int i = 0; i < lpProcName.length; i++) {
                        lpProcName[i] = (byte) name.charAt(i);
                }
                final int fun = OS.GetProcAddress(hInst, lpProcName);
                if (fun != 0) {

                        OS.CallWindowProc(fun, shell.handle, 0, 150, 2);
                }
                OS.FreeLibrary(hInst);
        }
	}
	
	public static void fontSelect() {

		Swt.window(null, new WinI() {
			
			public void process(Shell shell) {
				Triple<String, String, Integer> rt = Swt.fontDialog(shell);
				System.out.println("font: " + rt);
			}
		});
	}
	
	public static void dirSelect() {
		
		Swt.window(null, new WinI() {
			public void process(Shell shell) {
				String rt = Swt.dirDialog(shell);
				System.out.println("dir: " + rt);
			}
		});
	}
	
	public static void colorSelect() {
		
		Swt.window(null, new WinI() {			
			public void process(Shell shell) {
				Triple<Integer, Integer, Integer> color = Swt.colorDialog(shell);
				System.out.println(color);
			}
		});
	}
	
	public static void filesSelect() {
		
		Swt.window(null, new WinI() {			
			public void process(Shell shell) {
				String[] files = Swt.filesDialog(shell);
				System.out.println(Arrays.asList(files));
			}
		});
	}

	public static void inputDialog() {
	
		Swt.window(null, new WinI() {
			public void process(Shell shell) {
				String s = Swt.input(shell, "������䣺");		
				shell.setAlpha(200);
				System.out.println("input dialog: " + s);
			}
		});
	}
	// ----------------- main() -----------------//
	public static void main(String[] args) {
		
//		sdkMessage();
//		window();
//		fontSelect();
//		dirSelect();
//		colorSelect();
//		filesSelect();
		inputDialog();
	}
	
}
