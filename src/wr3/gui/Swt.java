package wr3.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import wr3.util.Stringx;
import wr3.util.tuple.Triple;
import wr3.util.tuple.Tuple;

/**
 * @author jamesqiu 2010-1-18
 * SWT工具类
 * TODO 可以参考Groovy来定制api
 */
public class Swt {

	/**
	 * 缺省可见windows
	 * @param title 窗口标题
	 * @param closure 为window添加处理功能的闭包
	 */
	public static void window(String title, WinI closure) {

		Display display = Display.getDefault();
		Shell shell = new Shell(display, SWT.SHELL_TRIM);

		if (Stringx.nullity(title)) {
			title = "SWT窗口";
		}
		shell.setText(title);
		setIcon(shell);

		shell.setSize(550, 350); // 缺省
		// 在shell中增加其他控件，或其他操作（setSize(), pack()）
		if (closure!=null) {
			closure.process(shell);
		}

		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
	}


	/**
	 * @param shell
	 *            shell to show message box
	 * @param message
	 *            message of alert message box
	 */
	public static void alert(Shell shell, String message) {

		MessageBox dialog = new MessageBox(shell);
		dialog.setMessage(message);
		dialog.open();
	}

	/**
	 * @param shell
	 *            shell to show message box
	 * @param message
	 *            message of confirm message box
	 * @return true if SWT.YES choice, false if SWT.NO choice
	 */
	public static boolean confirm(Shell shell, String message) {

		MessageBox dialog = new MessageBox(shell, SWT.YES | SWT.NO);
		dialog.setMessage(message);
		if (dialog.open() == SWT.YES) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 用户输入的对话框。
	 * @param shell
	 * @param message
	 * @return 用户输入的内容
	 */
	public static String input(Shell shell, String message) {

		InputDialog dialog = new InputDialog(shell);
		dialog.setMessage(message);
		return dialog.open();
	}

	/**
	 * 文件选择对话框
	 * @param shell
	 * @return 选中的文件绝对路径，或者null（cancel）
	 */
	public static String fileDialog(Shell shell) {

		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		return dialog.open();
	}

	/**
	 * 文件多选对话框
	 * @param shell
	 * @return
	 */
	public static String[] filesDialog(Shell shell) {

		FileDialog dialog = new FileDialog(shell, SWT.MULTI);
		String files = dialog.open();
		if (files==null) return new String[0];
		return dialog.getFileNames();
	}

	/**
	 * 目录选择对话框
	 * @param shell
	 * @return 选中的目录绝对路径，或者null（cancel）
	 */
	public static String dirDialog(Shell shell) {

		DirectoryDialog dialog = new DirectoryDialog(shell);
		return dialog.open();
	}

	/**
	 * 颜色选择对话框
	 * @param shell
	 * @return <R, G, B> 每个是0-255的色度
	 */
	public static Triple<Integer, Integer, Integer> colorDialog(Shell shell) {

		ColorDialog dialog = new ColorDialog(shell);
		RGB rgb = dialog.open();
		if (rgb==null) return null;
		return Tuple.from(rgb.red, rgb.green, rgb.blue);
	}

	/**
	 * 字体选择对话框
	 * @param shell
	 * @return <font-name, font-style, font-size>
	 */
	public static Triple<String, String, Integer> fontDialog(Shell shell) {

		FontDialog dialog = new FontDialog(shell);
		FontData font = dialog.open();
		if (font==null) return null;
		String name = font.getName();
		int stylei = font.getStyle();
		String styles = null;
		switch (stylei) {
		case 0: styles = "normal"; break;
		case 1: styles = "bold"; break;
		case 2: styles = "italic"; break;
		case 3: styles = "bold italic"; break;
		default: break;
		}
		int size = font.getHeight();

		return Tuple.from(name, styles, size);
	}

	/**
	 * set icon file in same path with class as application icon.
	 * @param shell shell to set app icon
	 * @param cls class to get icon resource. 可用getClass()获取;
	 * @param icon icon file name
	 */
	public static void setIcon(Shell shell, Class<?> cls, String icon) {

		ImageData imagedata = new ImageData(cls.getResourceAsStream(icon));
		Image image = new Image(null, imagedata);
		shell.setImage(image);
	}

	/**
	 * 为shell设置缺省图标。
	 * @param shell
	 */
	public static void setIcon(Shell shell) {

		setIcon(shell, Swt.class, "default.png");
	}

	// ----------------- main() -----------------//
	/**
	 * 如果是32位的swt.jar，需要32位的jdk来运行。
	 */
	public static void main(String[] args) {
		WinI closure = null;
		Swt.window("SWT窗口", closure);
	}
}
