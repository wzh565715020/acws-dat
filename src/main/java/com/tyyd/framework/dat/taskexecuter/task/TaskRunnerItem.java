package com.tyyd.framework.dat.taskexecuter.task;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface TaskRunnerItem {

    String shardValue() default "";
}
