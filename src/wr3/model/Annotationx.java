package wr3.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import javax.persistence.Id;
import javax.persistence.Transient;

/**
 * ���ڴ��� JPA Domain (��ƣ�Form / Model) �� Annotation ��Ϣ��ȡ��   
 * @author jamesqiu 2010-3-28
 */
public class Annotationx {

	/**
	 * �жϸ�field�Ƿ��и�����Annotation��
	 * @param f JPA domain�е��ֶ�
	 * @param anno
	 */
	public static boolean has(Field f, Class<? extends Annotation> clazz) {
		
		if (f==null || clazz==null) return false;
		return f.isAnnotationPresent(clazz);
	}
	
	/**
	 * �ж�JPA domain�Ƿ��и�����Annotation
	 * @param domainClass
	 * @param annoClass
	 * @return
	 */
	public static boolean has(Class<?> domainClass, Class<? extends Annotation> annoClass) {
		
		if (domainClass==null || annoClass==null) return false;
		return domainClass.isAnnotationPresent(annoClass);
	}
	
	/**
	 * �ж�1�� annotation �Ƿ��� annotation[] ��
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
	 * �ж��ֶ��Ƿ��ʶΪ@Id
	 * @param f
	 * @return
	 */
	public static boolean isId(Field f) {
		
		return has(f, Id.class);
	}
	
	/**
	 * �ж��ֶ��Ƿ���@Meta
	 * @param f
	 * @return
	 */
	public static boolean hasMeta(Field f) {
		
		return has(f, Meta.class);
	}
	
	/**
	 * �ж�JPA domain���Ƿ���@Meta
	 * @param domainClass
	 * @return
	 */
	public static boolean hasMeta(Class<?> domainClass) {
		
		return has(domainClass, Meta.class);
	}
	
	/**
	 * �ж�JPA domain�ֶ��Ƿ�ǳ־û��ֶΣ���д�����ݿ⣩
	 * @param f
	 * @return
	 */
	public static boolean isTransient(Field f) {
		
		return has(f, Transient.class);
	}
	
	/**
	 * ȡ�ֶε�meta��Ϣ
	 * @param f JPA domain���е��ֶ�
	 * @return meta��Ϣ
	 */
	public static String meta(Field f) {
		
		if (!hasMeta(f)) return null;
		Meta meta = f.getAnnotation(Meta.class);
		return meta.value();
	}
	
	/**
	 * ȡJPA domain���meta��Ϣ
	 * @param domainClass JPA domain��
	 * @return meta��Ϣ
	 */
	public static String meta(Class<?> domainClass) {
		
		if (!hasMeta(domainClass)) return null;
		Meta meta = (Meta) domainClass.getAnnotation(Meta.class);
		return meta.value();
	}
	
	/**
	 * ȡ domain �� meta ��Ϣ������У�������class name
	 * @param domainClass
	 * @return
	 */
	public static String title(Class<?> domainClass) {
		
		return hasMeta(domainClass) ? meta(domainClass) : domainClass.getName();
	}
	
	/**
	 * ȡ field �� meta ��Ϣ������У�������field name
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
	 * �õ�domain���@Order
	 * @param domainClass
	 * @return
	 */
	public static String[] order(Class<?> domainClass) {
		
		if (!hasOrder(domainClass)) return null;
		Order order = (Order) domainClass.getAnnotation(Order.class);
		return order.value();
	}
	
	/**
	 * ��ӡ��������Field���Ե�annotations��Ϣ��
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
