package com.changan.springmvctest.web;

import com.changan.springmvctest.annotation.Autowired;
import com.changan.springmvctest.annotation.Controller;
import com.changan.springmvctest.annotation.RequestMapping;
import com.changan.springmvctest.service.TestService;

@Controller
@RequestMapping("/api")
public class TestController {

    @Autowired
    private TestService service;

    @RequestMapping("/test")
    public String test(){
        service.test();
        return "hello spring mvc";
    }
    @RequestMapping("/test1")
    public String test1(){
        service.test();
        System.out.println("hello spring mvc");
        return "hello spring mvc";
    }
}
