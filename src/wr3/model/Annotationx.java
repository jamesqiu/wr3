package wr3.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * 用于处理 JPA Domain (或称：Form / Model) 的 Annotation 信息读取。   
 * @author jamesqiu 2010-3-28
 */
public class Annotationx {

	/**
	 * 判断该field是否有给定的Annotation。
	 * @param f JPA domain中的字段
	 * @param anno
	 */
	public static boolean has(Field f, Class<? extends Annotation> clazz) {
		
		if (f==null || clazz==null) return false;
		return f.isAnnotationPresent(clazz);
	}
	
	/**
	 * 判断JPA domain是否有给定的Annotation
	 * @param domainClass
	 * @param annoClass
	 * @return
	 */
	public static boolean has(Class<?> domainClass, Class<? extends Annotation> annoClass) {
		
		if (domainClass==null || annoClass==null) return false;
		return domainClass.isAnnotationPresent(annoClass);
	}
	
	/**
	 * 判断1个 annotation 是否在 annotation[] 中
	 * @param annoClass
	 * @param anns
	 * @return
	 */
	public static boolean in(Class<? extends Annotation> annoClass, Annotation[] anns) {
		
		for (Annotation ann : anns) {
			if (ann.annotationType()==annoClass) return true;
		}
		return false;
	}
	
	/**
	 * 判断字段是否标识为@Id
	 * @param f
	 * @return
	 */
	public static boolean isId(Field f) {
		
		return has(f, Id.class);
	}
	
	/**
	 * 判断字段是否有@Meta
	 * @param f
	 * @return
	 */
	public static boolean hasMeta(Field f) {
		
		return has(f, Meta.class);
	}
	
	/**
	 * 判断JPA domain类是否有@Meta
	 * @param domainClass
	 * @return
	 */
	public static boolean hasMeta(Class<?> domainClass) {
		
		return has(domainClass, Meta.class);
	}
	
	/**
	 * 判断JPA domain字段是否非持久化字段（不写入数据库）
	 * @param f
	 * @return
	 */
	public static boolean isTransient(Field f) {
		
		return has(f, Transient.class);
	}
	
	/**
	 * 取字段的meta信息
	 * @param f JPA domain类中的字段
	 * @return meta信息
	 */
	public static String meta(Field f) {
		
		if (!hasMeta(f)) return null;
		Meta meta = f.getAnnotation(Meta.class);
		return meta.value();
	}
	
	/**
	 * 取JPA domain类的meta信息
	 * @param domainClass JPA domain类
	 * @return meta信息
	 */
	public static String meta(Class<?> domainClass) {
		
		if (!hasMeta(domainClass)) return null;
		Meta meta = (Meta) domainClass.getAnnotation(Meta.class);
		return meta.value();
	}
	
	/**
	 * 取 domain 的 meta 信息（如果有），或者class name
	 * @param domainClass
	 * @return
	 */
	public static String title(Class<?> domainClass) {
		
		return hasMeta(domainClass) ? meta(domainClass) : domainClass.getName();
	}
	
	/**
	 * 取 field 的 meta 信息（如果有），或者field name
	 * @param field
	 * @return
	 */
	public static String title(Field field) {
		
		return hasMeta(field) ? meta(field) : field.getName();
	}
	
	public static boolean hasOrder(Class<?> domainClass) {
		
		return has(domainClass, Order.class);
	}
	
	/**
	 * 得到domain类的@Order
	 * @param domainClass
	 * @return
	 */
	public static String[] order(Class<?> domainClass) {
		
		if (!hasOrder(domainClass)) return null;
		Order order = (Order) domainClass.getAnnotation(Order.class);
		return order.value();
	}
	
	/**
	 * 打印类中所有Field属性的annotations信息。
	 * @param clz
	 */
	public static void info(Class<?> clz) {
		
		System.out.printf("--------- %s \n", clz.getName());
		for (Field f : clz.getDeclaredFields()) {
			Annotation[] as = f.getAnnotations();
			if (as.length>0) {
				System.out.println(f.getName() + ": " + f.getType());
				for (Annotation a : as) {
					System.out.println("\t" + a);
				}
			}			
		}		
	}
	
}
