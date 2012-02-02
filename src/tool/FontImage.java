package tool;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import wr3.util.Numberx;
import wr3.util.Stringx;
import static wr3.util.Stringx.*;

/**
 * 使用本机字体生成透明背景的文本图像。
 * 来源：Grails RichUI中的Font
 * 使用：@see utils.bat
 * @author jamesqiu 2010-1-28
 */
public class FontImage {

    //The following code is based on a blog post by Rene Gosh http://rghosh.free.fr/groovyimages/index.html
    public RenderedImage createImage(String text, 
    		String fontName, String style, int size, String color) {
    	
		// Font
		int fontStyle = getFontStyle(style);
		Font font = new Font(fontName, fontStyle, size);

		// Color
		color = Stringx.replace(color, "#", "0x"); // "0x${color.replace('#', '')}"
		Color fontColor = Color.decode(color);

		// Determine bounds
		Map<String, Object> bounds = determineBounds(font, text);

		// Create image
		double width = ((Rectangle2D) (bounds.get("rectangle"))).getWidth(); // bounds.rectangle.width
		int ascent = ((FontMetrics) (bounds.get("fontMetrics"))).getAscent(); // bounds.fontMetrics.ascent;
		BufferedImage image = new BufferedImage((int) Math.ceil(width) + 5,
				(int) Math.ceil(ascent) + 10, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = (Graphics2D) image.getGraphics();// image.graphics;

		// Anti aliasing
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);

		// Transparent background
		graphics.setColor(new Color(0, 0, 0, 0));
		graphics.fillRect(0, 0, image.getWidth(), image.getHeight());

		// Font
		graphics.setColor(fontColor);
		graphics.setFont(font);
		graphics.drawString(text, 0, graphics.getFontMetrics().getAscent());

		// Read AWT image
		ParameterBlock pb = new ParameterBlock();
		pb.add(image);
		PlanarImage renderedImage = (PlanarImage) JAI.create("awtImage", pb);

		return renderedImage;
    }
    
    /**
     * @param style "plain"/"bold"/"italic"
     * @return
     */
    private int getFontStyle(String style) {

    	if(Stringx.nullity(style)) return Font.PLAIN;
    	
    	try {
    		// return Font."${style.toUpperCase()}"
			return Font.class.getDeclaredField(style.toUpperCase()).getInt(Font.class);
		} catch (Exception e) {
			e.printStackTrace();
			return Font.PLAIN;
		}
    }
    
    
    //Determine bounds for given font and text
    private Map<String, Object> determineBounds(Font font, String text) {
    	
  		int width = 1000;
  		int height = 1000;
  		
    	BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	Graphics2D graphics = (Graphics2D) image.getGraphics();    	
    	graphics.setFont(font);
    	
    	Map<String, Object> bounds = new LinkedHashMap<String, Object>();
    	
//  	bounds["rectangle"] = graphics.fontMetrics.getStringBounds(text, graphics)
//  	bounds["fontMetrics"] = graphics.fontMetrics
    	bounds.put("rectangle", graphics.getFontMetrics().getStringBounds(text, graphics));
    	bounds.put("fontMetrics", graphics.getFontMetrics());
  		return bounds;
    }	
    
    // ----------------- main() -----------------//
    public static void main(String[] args) throws FileNotFoundException {

    	if (args.length==0) {
    		String usage = "java tool.FontImage  " +
    			"文本  字体名称  plain/bold/italic  字号  颜色  输出文件名\n" +
    			"ru fontimage \"\" \"\" \"\" \"\" \"\" \"\"";
    		System.out.println(usage);
    		return;
    	}
    	
    	String text = s(args[0], "Nasoft公司xxx系统");
    	String name = s(args[1], "微软雅黑");
    	String style = s(args[2], "plain");
    	int size = Numberx.toInt(args[3], 32);
    	String color = s(args[4], "#000055");
    	String filename = s(args[5], "title.png");
    	
		FontImage o = new FontImage();		
		// 若要写入request用：
		// response.setContentType("image/png");
		// fos = response.getOutputStream();
		FileOutputStream fos = new FileOutputStream(new File(filename)); 
		RenderedImage image = o.createImage(text, name, style, size, color);
		JAI.create("encode", image, fos, "PNG", null);
	}
}
