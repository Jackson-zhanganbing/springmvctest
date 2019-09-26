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
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分发请求
 *
 * @author zab
 * @date 2019-09-24 21:50
 */
public class DispatchServlet extends HttpServlet {


    /**
     * 扫描包的所有类（com.changan.springmvctest.core.DispatchServlet)集合
     */
    List<String> fileNameList = new ArrayList<>();

    /**
     * 类全名和实例化的类映射
    */
    Map<String, Object> clazzMap = new HashMap<>();

    /**
     * url和具体方法的映射
    */
    Map<String, Method> urlMap = new HashMap<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String url = req.getRequestURL().toString();
        String dispatchUrl = url.substring(url.indexOf("mvc/")).replaceAll("mvc", "");
        //4、处理请求
        Object response = handleRequest(dispatchUrl);
        PrintWriter printWriter = resp.getWriter();
        printWriter.print(response.toString());

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

        System.out.println("urlMap------------" + urlMap.toString());


    }

    private Object handleRequest(String url) {
        Method method = urlMap.get(url);
        Class clazz = method.getDeclaringClass();
        String className = clazz.getCanonicalName();
        try {
            return method.invoke(clazzMap.get(className), null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void mappingUrl() {
        for (Map.Entry<String, Object> beanEntry : clazzMap.entrySet()) {
            String clazzName = beanEntry.getKey();
            Class clazz = null;
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

                            urlMap.put(controllerUrl + methodUrl, method);

                        }
                    }

                    //注入service到controller
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        field.setAccessible(true);
                        if (field.isAnnotationPresent(Autowired.class)) {
                            //获取字段本身类型
                            Class fieldType = field.getType();
                            String injectName = fieldType.getCanonicalName();
                            Object injectObj = clazzMap.get(injectName);
                            //获取字段所属类的类型
                            Class declaringClazz = field.getDeclaringClass();
                            String controllerName = declaringClazz.getCanonicalName();
                            Object controller = clazzMap.get(controllerName);
                            field.set(controller, injectObj);
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
                if (clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)) {

                    object = clazz.getConstructor().newInstance();
                    clazzMap.put(clazzName, object);

                }
            } catch (Exception e) {
            }


        }
    }


    private void loadFiles(String basePath) {

        URL url = getClass().getClassLoader().getResource(basePath);
        if(url == null){
            return;
        }
        String fileName = url.getFile();
        File[] baseFile = new File(fileName).listFiles();
        if(baseFile == null){
            return;
        }
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
