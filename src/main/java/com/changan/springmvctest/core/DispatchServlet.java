package com.changan.springmvctest.core;

import com.changan.springmvctest.annotation.Autowired;
import com.changan.springmvctest.annotation.Controller;
import com.changan.springmvctest.annotation.RequestMapping;
import com.changan.springmvctest.annotation.Service;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 分发请求
 *
 * @author zab
 * @date 2019-09-24 21:50
 */
public class DispatchServlet extends HttpServlet {

    List<String> fileNameList = new ArrayList<>();
    Map<String, Object> clazzMap = new HashMap<>();
    Map<String, Method> urlMapping = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURL().toString();
        String dispatchUrl = url.substring(url.indexOf("mvc/")).replaceAll("mvc","");
        //4、处理请求
        handleRequest(dispatchUrl);

    }

    @Override
    public void init() throws ServletException {
        super.init();
        System.out.println("init=========================================");
        //1、加载文件
        loadFiles("com/changan/springmvctest");

        System.out.println("fileNameList--------" + fileNameList.toString());

        //2、创建所有的类，放入map
        initClass();

        System.out.println("clazzMap----------" + clazzMap.toString());

        //3、找到请求的路径，对应到方法上
        mappingUrl();

        System.out.println("urlMapping------------" + urlMapping.toString());


    }

    private Object handleRequest(String url) {
        Method method = urlMapping.get(url);
        Class clazz = method.getDeclaringClass();
        String className = clazz.getCanonicalName();
        try {
           return method.invoke(clazzMap.get(className),null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void mappingUrl() {
        for (Map.Entry<String, Object> beanEntry : clazzMap.entrySet()) {
            String clazzName = beanEntry.getKey();
            Class clazz = null;
            Object object = null;
            String controllerUrl = null;
            try {
                clazz = Class.forName(clazzName);
                if (clazz.isAnnotationPresent(Controller.class)
                        && clazz.isAnnotationPresent(RequestMapping.class)) {
                    RequestMapping clazzRm = (RequestMapping) clazz.getAnnotation(RequestMapping.class);
                    controllerUrl = clazzRm.value()[0];
                    //取出该类中所有的方法，映射路径和方法
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (method.isAnnotationPresent(RequestMapping.class)) {
                            RequestMapping methodRm = method.getAnnotation(RequestMapping.class);
                            String methodUrl = methodRm.value()[0];

                            urlMapping.put(controllerUrl+methodUrl, method);

                        }
                    }

                    //注入service到controller
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if(field.isAnnotationPresent(Autowired.class)){
                            //获取字段本身类型
                            Class fieldType = field.getType();
                            String injectName = fieldType.getCanonicalName();
                            Object injectObj = clazzMap.get(injectName);
                            //获取字段所属类的类型
                            Class declaringClazz = field.getDeclaringClass();
                            String controllerName = declaringClazz.getCanonicalName();
                            Object controller = clazzMap.get(controllerName);
                            field.set(controller,injectObj);
                        }
                    }
                }

            } catch (Exception e) {
            }
        }
    }

    private void initClass() {
        for (String clazzName : fileNameList) {
            Class clazz = null;
            Object object = null;
            //如果被Controller或者Service标记的类，实例化放入map
            try {
                clazz = Class.forName(clazzName);
                if (clazz.isAnnotationPresent(Controller.class)
                        || clazz.isAnnotationPresent(Service.class)) {

                    object = clazz.getConstructor().newInstance();
                    clazzMap.put(clazzName, object);

                }
            } catch (Exception e) {
            }


        }
    }


    private void loadFiles(String basePath) {

        URL url = getClass().getClassLoader().getResource(basePath);
        String fileName = url.getFile();
        File[] baseFile = new File(fileName).listFiles();
        for (File file : baseFile) {
            if (!file.isDirectory()) {
                String filePath = basePath.replaceAll("\\/", ".");
                String tempPath = filePath + "." + file.getName();
                String trueNeedPath = tempPath.substring(0, tempPath.lastIndexOf("."));
                fileNameList.add(trueNeedPath);
            } else {
                loadFiles(basePath + "/" + file.getName());
            }
        }
    }

}
