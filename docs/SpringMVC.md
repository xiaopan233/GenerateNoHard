在打入一个内存马之前，需要先了解中间件从接受请求到调用Servlet中间的流程。

调用栈：

```java
a:12, AController (com.pan) - 最终调用Controller
...Java反射
doInvoke:205, InvocableHandlerMethod (org.springframework.web.method.support)
...根据url找对应的控制器(url -> handler)
doDispatch:1071, DispatcherServlet (org.springframework.web.servlet)
...解析Request
service:779, HttpServlet (javax.servlet.http)
...Filter chain
doFilter:162, ApplicationFilterChain (org.apache.catalina.core)
....Valve 
invoke:78, StandardEngineValve (org.apache.catalina.core)
...Tomcat流程
doRun:1789, NioEndpoint$SocketProcessor (org.apache.tomcat.util.net)
```

明确了Servlet流程之后，若要注入Filter马，就去Filter chain看；要注入Handler（Controller）马，就去url -> handler的流程看



# 获取Context

如果用过Spring开发会知道，Spring有一个Bean管理池（IOC），Spring的所有对象都会放在这个IOC容器中进行存取。前文说的`mappingRegistry`也是同理。

获取Context可以通过 https://www.anquanke.com/post/id/198886 中介绍的方法，通过getAttribute获取`RequestContextHolder`类的`Attributes`，然后获取到`GenericWebApplicationContext`类。有了`GenericWebApplicationContext`，就可以通过`getBean()`方法获取Spring中任意对象了。

```java
ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
HttpServletRequest httpServletRequest = requestAttributes.getRequest();
GenericWebApplicationContext webApplicationContext = (GenericWebApplicationContext) httpServletRequest.getAttribute("org.springframework.web.servlet.DispatcherServlet.THEME_SOURCE");
```



# Controller注册

SpringMVC中的Controller，实际上就是一个Handler。所以需要在url -> handler的流程看。找到**`org.springframework.web.servlet.DispatcherServlet#doDispatch`**

其中关键的部分是变量`mappedHandler`。他是确定Request被哪个Handler处理的关键

```java
mappedHandler = this.getHandler(processedRequest);
mv = ha.handle(processedRequest, response, mappedHandler.getHandler());
```



在方法`this.getHandler()`中，将本类的`handlerMappings`进行循环，并验证路由是否匹配。路由匹配则返回赋值给变量`mappedHandler`

**`org.springframework.web.servlet.DispatcherServlet#getHandler`**

```java
protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
    for (HandlerMapping mapping : this.handlerMappings) {
        HandlerExecutionChain handler = mapping.getHandler(request);
        return handler;
    }
}
```



最终对于一个普通的Request请求，最终进行路由匹配的是**`org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#lookupHandlerMethod`**

```java
List<T> directPathMatches = this.mappingRegistry.getMappingsByDirectPath(lookupPath);
if (directPathMatches != null) {
    addMatchingMappings(directPathMatches, matches, request);
}
```

掌管路由信息的是类成员`mappingRegistry`。若要往该成员添加新路由，可以调用**`org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#registerMapping`**

本项目中`SpringBoot.registerHandler`生成的内存马Payload就是采用这个方式



当然在`AbstractHandlerMethodMapping`类中，还有另一个方法也可以添加新路由，但是构造成本和体积比`registerMapping()`大很多：**`org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#registerMapping`**

本项目中`SpringBoot.registerMapping`生成的内存马Payload就是采用这个方式

# Reference

[基于内存 Webshell 的无文件攻击技术研究](https://www.anquanke.com/post/id/198886)
