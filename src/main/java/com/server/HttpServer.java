package com.server;

import java.io.*;
import java.net.*;

public class HttpServer extends Thread{
    private InputStream inputStream;
    private OutputStream outputStream;
    private String basePath;

    private Socket socket;

    public static void runServer(String serverIp, int serverPort, String basePath){
        try {
            SocketAddress socketAddress = new InetSocketAddress(serverIp, serverPort);
            ServerSocket serverSocket = new ServerSocket();
            serverSocket.bind(socketAddress);
            System.out.println("[+] HTTP server running at " + serverIp + ":" + serverPort
                                + "\nWeb path: " + basePath
                                + "\n"
                    );
            while (true){
                Socket accept = serverSocket.accept();
                HttpServer httpServer = new HttpServer(accept, serverIp, serverPort, basePath);
                httpServer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HttpServer(Socket socket, String serverIp, int serverPort, String basePath){
        try {
            SocketAddress remoteSocketAddress = socket.getRemoteSocketAddress();
            System.out.println("[HTTP Server] " + "Recive from " + remoteSocketAddress.toString());
            this.socket = socket;
            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();
            this.basePath = basePath;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public byte[] socketInRequest(){
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(this.inputStream));
        try {
            //只看请求头，确定请求哪个就够了
            String header = bufferedReader.readLine();

            System.out.println("[HTTP Server] Request Header: " + header);
            //一般格式 GET /xxx HTTP/1.1。直接取路径，也就是第二个
            String[] strings = header.split(" ");
            String path = strings[1];

            if ("/".equals(path)){
                return "Http Server Running!!!".getBytes();
            }

            File file = new File(basePath + path);
            if (!file.exists()) {
                return null;
            }

            FileInputStream fileInputStream = new FileInputStream(file);
            int size = fileInputStream.available();
            byte[] content = new byte[size];
            fileInputStream.read(content);

            return content;
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void socketOutResponse(byte[] contentBytes){
        /*
        * HTTP/1.1 200 OK
            Server: SimpleHTTP/0.6
            Content-Type: text/plain
        * */
        String bodyLength = "";
        String code = "";
        String notFoundContent = "Not Found";
        byte[] response;

        if (contentBytes == null){
            code = "HTTP/1.1 404 Not Found\r\n";
            bodyLength = "Content-Length: " + notFoundContent.getBytes().length;
        }else {
            code = "HTTP/1.1 200 OK\r\n";
            bodyLength = "Content-Length: " + contentBytes.length;
        }
        String header = code
                .concat("Server: SimpleHTTP/0.6\r\n")
                .concat("Content-Type: text/plain\r\n")
                .concat(bodyLength + "\r\n\r\n");

        if (contentBytes != null){
            int responseLength = header.getBytes().length + contentBytes.length;
            response = new byte[responseLength];
            System.arraycopy(header.getBytes(), 0, response, 0, header.getBytes().length);
            System.arraycopy(contentBytes, 0, response, header.getBytes().length, contentBytes.length);
        }else{
            int responseLength = header.getBytes().length + notFoundContent.getBytes().length;
            response = new byte[responseLength];
            System.arraycopy(header.getBytes(), 0, response, 0, header.getBytes().length);
            System.arraycopy(notFoundContent.getBytes(), 0, response, header.getBytes().length, notFoundContent.getBytes().length);
        }

        try {
            this.outputStream.write(response);
            this.outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //get
        byte[] bytes = socketInRequest();
        socketOutResponse(bytes);
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
