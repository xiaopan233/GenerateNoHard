package com.dto;

public class ClassDto {
    private String className;
    private String methodName;
    private byte[] cbytes;

    public ClassDto(String className, String methodName, byte[] cbytes) {
        this.className = className;
        this.methodName = methodName;
        this.cbytes = cbytes;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public byte[] getCbytes() {
        return cbytes;
    }

    public void setCbytes(byte[] cbytes) {
        this.cbytes = cbytes;
    }
}
