package com.changan.springmvctest.annotation;

import java.lang.annotation.*;

/**
 * 标记这是一个服务
 *
 * @author zab
 * @date 2019-09-24 21:34
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Service {
    String value() default "";
}
