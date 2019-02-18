package com.tyyd.framework.dat.core.spi;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.ElementType;
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SPI {

    /**
     * config中的键值
     */
    String key() default "";

    /**
     * 默认扩展实现
     */
    String dftValue() default "";

}
