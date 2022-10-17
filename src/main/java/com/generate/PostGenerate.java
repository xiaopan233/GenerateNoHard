package com.generate;

import com.Usage;
import com.dto.ClassDto;
import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;
import com.utils.Bcel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

public class PostGenerate{
    public static void dispatch(ClassDto classDto, String encode, HashMap<String, String> otherArgs){
        if (classDto == null) {
            Usage.printUsage();
            return;
        }
        try {
            switch (encode){
                case "classFile":
                    classFile(classDto, otherArgs);
                    break;
                case "base64":
                    base64(classDto);
                    break;
                case "hex":
                    hex(classDto);
                    break;
                case "bytes":
                    bytes(classDto);
                    break;
                case "bcel":
                    bcel(classDto);
                    break;
                default:
                    System.out.println("[-] Unknown encode");
                    Usage.printUsage();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void classFile(ClassDto classDto, HashMap<String, String> otherArgs){
        try {
            String rootPath = otherArgs.get("filePath");
            String className = classDto.getClassName();

            String[] directory = className.split("\\.");
            StringBuffer classFilePath = new StringBuffer();
            classFilePath.append(rootPath);
            //最后一个是类名不是目录
            for (int i = 0; i < directory.length-1; i++) {
                File file = new File(classFilePath.toString() + "/" + directory[i]);
                if (!file.exists())
                    file.mkdir();
                classFilePath.append("/" + directory[i]);
            }
            String classFileAbsolutePath = classFilePath + "/" + directory[directory.length-1] + ".class";
            File classFile = new File(classFileAbsolutePath);

            FileOutputStream fileOutputStream = new FileOutputStream(classFileAbsolutePath);
            fileOutputStream.write(classDto.getCbytes());
            fileOutputStream.close();
            System.out.println(" [+] byte length: " + classDto.getCbytes().length);
            System.out.println(" [+] Complete, file path: " + classFile.getCanonicalPath());
            System.out.println(" [+] \"Qualified Class Name\": " + className + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void base64(ClassDto classDto){
        String encode = new String(Base64.getEncoder().encode(classDto.getCbytes()));
        String result = "[+] ClassName: " + classDto.getClassName() + "\n"
                .concat("base64 class byte code:\n");
        System.out.println("[+] byte length: " + classDto.getCbytes().length);
        System.out.println("[+] string length: " + encode.length());
        System.out.println(result);
        System.out.println(encode);
    }

    private static void hex(ClassDto classDto){
        String encode = HexBin.encode(classDto.getCbytes());
        String result = "[+] ClassName: " + classDto.getClassName() + "\n"
                .concat("[+] hex decode: com.sun.org.apache.xerces.internal.impl.dv.util.HexBin.decode()\n")
                .concat("hex class byte code:\n");
        System.out.println("[+] byte length: " + classDto.getCbytes().length);
        System.out.println("[+] string length: " + encode.length());
        System.out.println(result);
        System.out.println(encode);
    }

    private static void bytes(ClassDto classDto){
        StringBuilder stringBuilder = new StringBuilder();
        for (byte cbyte : classDto.getCbytes()) {
            stringBuilder.append(cbyte + ",");
        }
        String encode = stringBuilder.substring(0, stringBuilder.length()-1);
        String result = "[+] ClassName: " + classDto.getClassName() + "\n"
                .concat("bytes class byte code:\n");
        String javaCode = "byte[] b = new byte[]{";
        System.out.println("[+] byte length: " + classDto.getCbytes().length);
        System.out.println("[+] string length: " + encode.length());
        System.out.println(result);
        System.out.println(javaCode + encode + "}");

    }

    private static void bcel(ClassDto classDto){
        try {
            String encode = Bcel.encode(classDto.getCbytes(), false);
            String result = "[+] ClassName: " + classDto.getClassName() + "\n"
                    .concat("bcel class byte code:\n");
            System.out.println("[+] byte length: " + classDto.getCbytes().length);
            System.out.println("[+] string length: " + encode.length());
            System.out.println(result);
            System.out.println(encode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
