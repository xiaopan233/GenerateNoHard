import sun.rmi.server.UnicastRef;
import sun.rmi.transport.*;
import sun.rmi.transport.tcp.TCPChannel;
import sun.rmi.transport.tcp.TCPConnection;
import sun.rmi.transport.tcp.TCPEndpoint;
import sun.rmi.transport.tcp.TCPTransport;

import javax.management.ObjectName;
import javax.management.remote.rmi.RMIConnection;
import javax.naming.InitialContext;
import javax.security.auth.Subject;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.rmi.MarshalledObject;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.util.LinkedList;

public class TestRmiClient {
    public static void main(String[] args) throws Exception{
        Object[] params = new Object[]{
                null,
                "id",
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

//        Socket socket = new Socket("192.168.122.1", 12333);
        Socket socket = new Socket("192.168.122.1", 3344);

        LinkedList<TCPEndpoint> tcpEndpoints = new LinkedList<>();
//        TCPEndpoint tcpEndpoint = new TCPEndpoint("192.168.122.1", 12333);
        TCPEndpoint tcpEndpoint = new TCPEndpoint("192.168.122.1", 3344);
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


//        ConnectionInputStream.class.getDeclaredConstructor(new Class[]{
//
//        });

//        Constructor<TCPConnection> tcpConnectionConstructor = TCPConnection.class.getDeclaredConstructor(new Class[]{
//                TCPChannel.class,
//                Socket.class,
//                InputStream.class,
//                OutputStream.class
//        });
//        tcpConnectionConstructor.setAccessible(true);
//        TCPConnection tcpConnection = tcpConnectionConstructor.newInstance(new Object[]{
//                tcpChannel,
//                socket,
//                socket.getInputStream(),
//                socket.getOutputStream()
//        });
//
        //ObjectName name,
        //                         String operationName,
        //                         MarshalledObject params,
        //                         String signature[],
        //                         Subject delegationSubject
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
        System.out.println(streamRemoteCall);
//
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
//        streamRemoteCall.getOutputStream()

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
        System.out.println(invoke);
    }
}
