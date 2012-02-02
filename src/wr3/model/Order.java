package wr3.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义字段出现的顺序，以及是否出现
 * @author jamesqiu 2010-3-29
 */
@Retention(RetentionPolicy.RUNTIME) // 必须定义
@Target({ElementType.TYPE})
public @interface Order {

	public String[] value() default {};
}
