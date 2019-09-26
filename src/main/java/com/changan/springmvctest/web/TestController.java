package com.changan.springmvctest.web;

import com.changan.springmvctest.annotation.Autowired;
import com.changan.springmvctest.annotation.Controller;
import com.changan.springmvctest.annotation.RequestMapping;
import com.changan.springmvctest.service.TestService;

/**
 * springmvc测试controller
 *
 * @author zab
 * @date 2019/9/26 8:13
 */
@Controller
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestService service;

    @RequestMapping("/test")
    public String test() {
        service.test();
        return "hello spring mvc, this is test method";
    }

    @RequestMapping("/test1")
    public String test1() {
        service.test();
        return "hello spring mvc, this is test1 method";
    }
}
