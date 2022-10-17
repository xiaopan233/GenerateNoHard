package com;

import org.apache.commons.cli.*;

import java.util.ArrayList;
import java.util.HashMap;

public class Usage {
    /*
        url
        commandArg
        header
        headerValue
        serverMode
        attackMode
    * */
    private static ArrayList<String> modes = new ArrayList<>();
    private static ArrayList<String> encodes = new ArrayList<>();
    private static HashMap<String, String> args = new HashMap<>();
    private static Options options = new Options();
    private static StringBuilder modeList = new StringBuilder();
    private static StringBuilder encodeList = new StringBuilder();

    static {
        modes.add("SpringBoot.registerHandler");
        modes.add("SpringBoot.registerMapping");
        modes.add("Tomcat.6789.Filter");
//        modes.add("Tomcat.67.Filter");
        modes.add("Tomcat.89.Filter");
//        modes.add("Tomcat.10.Filter");
        modes.add("Jndi.Ldap.URLClassLoader");


        encodes.add("classFile");
        encodes.add("base64");
        encodes.add("hex");
        encodes.add("byte");

        for (String mode : modes) {
            modeList.append(mode + ", ");
        }
        for (String encode : encodes) {
            encodeList.append(encode + ", ");
        }

        options.addOption("url", true, "memory shell url");
        options.addOption("commandArg", true, "command variable name");
        options.addOption("mode", true, "memory shell mode: [" + modeList.toString() + "]");
        options.addOption("encode", true, "byte code encode: [" + encodeList.toString() + "]");
        options.addOption("headerPassword", true, "shell connect password via http header");
        options.addOption("filePath", true, "additional argument. specify class file path");
//        options.addOption("libPath", true, "jar lib path");
        options.addOption("jndiServerIp", true, "jndiServerIp");
        options.addOption("jndiServerPort", true, "jndiServerPort");
        options.addOption("httpServerIp", true, "httpServerIp");
        options.addOption("httpServerPort", true, "httpServerPort");
        options.addOption("webPath", true, "webPath");
        options.addOption("help", false, "help infomation");
    }

    public static boolean hasModes(String mode) {
        for (String m : modes) {
            if (m.equals(mode)){
                return true;
            }
        }
        return false;
    }

    public static boolean parseUserArgs(String[] args){
        CommandLineParser commandLinePaser = new DefaultParser();

        try {
            CommandLine parse = commandLinePaser.parse(options, args);
            //if help or args loss
            if (parse.hasOption("help") || !parse.hasOption("mode")) {
                return false;
            }

            //check mode
            boolean modeExsis = false;
            for (String mode : modes) {
                if (mode.equals(parse.getOptionValue("mode"))) {
                    modeExsis = true;
                    break;
                }
            }
            if (!modeExsis)
                return false;

            Usage.args.put("url", parse.getOptionValue("url"));
            Usage.args.put("commandArg", parse.getOptionValue("commandArg"));

            //check encode
            if ("classFile".equals(parse.getOptionValue("encode")))
                if(parse.hasOption("filePath"))
                    Usage.args.put("filePath", parse.getOptionValue("filePath"));
                else {
                    System.out.println("[-] classFile encode missing argument \"filePath\"");
                    return false;
                }

            Usage.args.put("encode", parse.getOptionValue("encode"));

            if (parse.hasOption("headerPassword")) {
                String headerPassword = parse.getOptionValue("headerPassword");
                if (!headerPassword.contains("=")){
                   return false;
                }
                String[] split = headerPassword.split("\\=");
                Usage.args.put("header", split[0]);
                Usage.args.put("headerValue", split[1]);
            }

            if (parse.getOptionValue("mode").contains("Jndi")){
                Usage.args.put("jndiServerIp", parse.getOptionValue("jndiServerIp"));
                Usage.args.put("jndiServerPort", parse.getOptionValue("jndiServerPort"));
                Usage.args.put("httpServerIp", parse.getOptionValue("httpServerIp"));
                Usage.args.put("httpServerPort", parse.getOptionValue("httpServerPort"));
                Usage.args.put("webPath", parse.getOptionValue("webPath"));
                Usage.args.put("className", parse.getOptionValue("className"));
            }


            //parse mode
            Usage.args.put("mode", parse.getOptionValue("mode"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void printUsage(){
        String usage = "java -jar GenerateNoHard.jar -mode [mode] -encode [encode] -url [URL] -commandArg [argName] -headerPassword [header=value] [additional]\n"
                .concat("eg:\n")
                .concat("java -jar GenerateNoHard.jar -mode SpringBoot.registerHandler -encode base64 -url /evil -commandArg cmd\n")
                .concat("java -jar GenerateNoHard.jar -mode Jndi.Ldap.URLClassLoader -webPath /web -commandArg cmd\n")
                .concat("java -jar GenerateNoHard.jar -mode SpringBoot.registerHandler -encode classFile -filePath /class -url /evil -commandArg cmd -headerPassword evil=attack\n")
                .concat("args:\n");
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp(usage, options);
    }

    public static HashMap<String, String> getArgs() {
        return args;
    }

    public static String getArg(String key) {
        return args.get(key);
    }
}
