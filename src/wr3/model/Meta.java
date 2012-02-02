package wr3.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用于获取JPA Domain类meta信息（如：中文描述）的Annotation
 * @author jamesqiu 2010-3-22
 */
@Retention(RetentionPolicy.RUNTIME) // 必须定义
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface Meta {

	public String value() default "";
}
