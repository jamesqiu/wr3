package wr3.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ���ڻ�ȡJPA Domain��meta��Ϣ���磺������������Annotation
 * @author jamesqiu 2010-3-22
 */
@Retention(RetentionPolicy.RUNTIME) // ���붨��
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Meta {

	public String value() default "";
}
