package wr3.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * �����ֶγ��ֵ�˳���Լ��Ƿ����
 * @author jamesqiu 2010-3-29
 */
@Retention(RetentionPolicy.RUNTIME) // ���붨��
@Target({ElementType.TYPE})
public @interface Order {

	public String[] value() default {};
}
