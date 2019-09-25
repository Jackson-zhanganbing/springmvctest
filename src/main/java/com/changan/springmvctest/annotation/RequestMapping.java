package com.changan.springmvctest.annotation;

import java.lang.annotation.*;

/**
 * 标记映射地址
 *
 * @author zab
 * @date 2019-09-24 21:39
 */
@Target(value = {ElementType.METHOD, ElementType.TYPE})
@Documented
@Retention(value = RetentionPolicy.RUNTIME)
public @interface RequestMapping {
    String[] value() default {};
}
