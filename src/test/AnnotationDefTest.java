/**
 * 
 */
package test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * @author jamesqiu 2010-3-14
 */
@Retention(RetentionPolicy.RUNTIME) // ���붨��
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface AnnotationDefTest {

	// һ����˵��ֻ��һ��������Annotation��������һ������Ϊvalue��
	public int value() default -1;
	public String name() default "anonymous";
}
