package com.generate;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

//todo: 双马，循环define servlet
//todo: 最小体积回显马
//todo: 适配各种SpringBoot
//todo: 各种SpringBoot的内存马类型
public class Utils {
    public static String stringSet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static Stack<String> readablePackageNames = new Stack();
    public static Stack<String> readableMethodNames = new Stack();
    public static Stack<String> readableClassNames = new Stack();
    public static Stack<String> readableFiedNames = new Stack();

    static {
        String[] readableFiedNamesList = new String[]{
                "name",
                "msg",
                "code",
                "flag",
                "cookie",
                "encode",
                "decode",
                "value",
                "error",
                "date",
                "state",
                "id",
                "no"
        };
        String[] readablePackageNamesList = new String[]{
                "com.common.api.",
                "com.controller.",
                "com.viewer.",
                "com.model.",
                "org.common.api.",
                "org.controller.",
                "org.viewer.",
                "org.model.",
                "org.web.asm.",
                "com.web.asm.",
                "org.dto.",
                "com.dto.",
                "org.apache.ext.",
                "org.junit.ext.",
                "org.junit.internal.",
                "org.junit.builders.",
                "org.apache.commons.",
                "org.apache.commons.cl.",
                "org.hamcrest.bean.",
                "org.hamcrest.collections."

        };
        String[] readableMethodNamesList = new String[]{
                "setName",
                "getName",
                "index",
                "getAge",
                "setAge",
                "init",
                "encode",
                "decode"
        };
        String[] readableClassNamesList = new String[]{
                "ComonApiGetss",
                "LgAuthentiDto",
                "MesgeBoxxs",
                "BaseRunner",
                "CaseVersion",
                "Aserrt",
                "ComparisonCompact",
                "ThrowingRun",
                "Compute",
                "Descriptions",
                "CoreControlller",
                "RequestMappings",
                "ResponseBodies",
                "RunnerWiths",
                "ClassesBefore",
                "Ignorer",
                "FiMethodOrder",
                "Ruller",
                "Clouder"
        };

        randomStringArray(readableFiedNamesList);
        randomStringArray(readablePackageNamesList);
        randomStringArray(readableMethodNamesList);
        randomStringArray(readableClassNamesList);

        for (String s : readableFiedNamesList)
            readableFiedNames.push(s);
        for (String s : readablePackageNamesList)
            readablePackageNames.push(s);
        for (String s : readableMethodNamesList)
            readableMethodNames.push(s);
        for (String s : readableClassNamesList)
            readableClassNames.push(s);
    }
    public static void a(){

    }

    public static void randomStringArray(String[] source){
        Random random = new Random();
        String temp = "";
        for (int i = 0; i < source.length; i++) {
            int i1 = random.nextInt(source.length);
            int i2 = random.nextInt(source.length);
            temp = source[i1];
            source[i1] = source[i2];
            source[i2] = temp;
        }
    }

    public static String randomString(int length){
        char[] chars = stringSet.toCharArray();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i=0; i<length; i++){
            Random random = new Random();
            int index = random.nextInt(52);
            stringBuilder.append(chars[index]);
        }
        return stringBuilder.toString();
    }

    public static String getReadableMethodName(String methodName){
        if (!"".equals(methodName))
            return methodName;
        return readableMethodNames.pop();
    }

    public static String getReadableClassName(String className){
        if (!"".equals(className))
            return className;
        return readableClassNames.pop();
    }

    public static String getReadablePackageName(String packageName){
        if (!"".equals(packageName))
            return packageName;
        return readablePackageNames.pop();
    }

    public static ArrayList<String> getReadableFiledName(int length){
        ArrayList<String> usedFiledNames = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            usedFiledNames.add(readableFiedNames.pop());
        }
        return usedFiledNames;
    }

    public static String loadTempFile(String filePath){
        String methodBody = "";
        try {
            InputStream baseServletIps = GenerateAttack.class.getResourceAsStream(filePath);
            int baseServletSize = 0;
            baseServletSize = baseServletIps.available();
            byte[] baseServletBytes = new byte[baseServletSize];
            baseServletIps.read(baseServletBytes);
            methodBody = new String(baseServletBytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return methodBody;
    }
}
