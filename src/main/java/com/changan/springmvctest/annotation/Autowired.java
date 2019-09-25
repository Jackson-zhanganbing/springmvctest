package com.changan.springmvctest.annotation;

import java.lang.annotation.*;

/**
 * 标记装配
 *
 * @author zab
 * @date 2019-09-24 21:34
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Autowired {
}
