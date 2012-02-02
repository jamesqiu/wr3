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
@Retention(RetentionPolicy.RUNTIME) // 必须定义
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface AnnotationDefTest {

	// 一般来说，只有一个方法的Annotation，方法名一定定义为value。
	public int value() default -1;
	public String name() default "anonymous";
}
