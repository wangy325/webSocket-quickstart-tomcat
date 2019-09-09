[TOC]

### 基于Tomcate的websocket服务端客户端实例

#### Tomcat对websocket的支持
> Tomcat自7.0.5版本开始支持WebSocket，并且实现了Java WebSocket规范（JSR356），而在7.0.5版本之前（7.0.2版本之后）则采用自定义API，
即WebSocketServlet。
 >
 > 根据JSR356的规定，Java WebSocket应用由一系列的WebSocket **Endpoint**组成。Endpoint是一个Java对象，代表WebSocket链接的一端。
 >
 > 对于服务端，我们可以视为处理具体WebSocket消息的接口，就像Servlet之于HTTP请求一样（不同之处在于Endpoint一个链接就是一个实例）。
 
#### 声明Endpoint：
 
 > - 编程式，即继承类 `javax.websocket.Endpoint`并实现其方法
 > - 注解式，即定义一个POJO对象，为其添加**Endpoint相关的注解**
 > 
 > Endpoint实例在WebSocket握手时创建，并在客户端与服务端链接过程中有效，最后在链接关闭时结束。
Endpoint接口明确定义了与其生命周期相关的方法，规范实现者确保在生命周期的各个阶段调用实例的相关方法。

#### Endpoint的生命周期

 > - onOpen：当开启一个新的会话时调用。这是客户端与服务器握手成功后调用的方法。等同于注解`@OnOpen`
 > - onClose：当会话关闭时调用。等同于注解`@OnClose`
 > - onError：当链接过程中异常时调用。等同于注解`@OnError`
 >

#### Session
  > 当客户端链接到一个Endpoint时，服务器端会为其创建一个唯一的会话（`javax.websocket.Session`）。
  会话在WebSocket握手之后创建，并在链接关闭时结束。当生命周期中触发各个事件时，都会将当前会话传给Endpoint
  >
  > 一般情况下, 我们通过为Session添加`MessageHandler`消息处理器来接收消息。
  > 当采用**注解**方式定义Endpoint时，还可以通过`@OnMessage`指定接收消息的方法。
  > 发送消息则由`RemoteEndpoint`完成，其实例由Session维护，根据使用情况，
  可以通过`Session.getBasicRemote`获取**同步消息**发送的实例或者通过`Session.getAsyncRemote`获取
  **异步消息**发送的实例。
  >

#### WebSocket的加载

> Tomcat提供了一个`javax.servlet.ServletContainerInitializer`的实现类`org.apache.tomcat.websocket.server.WsSci`。
因此Tomcat的WebSocket加载是通过**SCI机制**完成的。
> WsSci可以处理的类型有三种：
> - 添加了注解@ServerEndpoint的类
> - Endpoint的子类
> - ServerApplicationConfig的实现类
>
> Web应用启动时，通过WsSci.onStartup方法完成WebSocket的初始化：
>
> - 构造WebSocketContainer实例，Tomcat提供的实现类为WsServerContainer。在WsServerContainer构造方法中，Tomcat除了初始化配置外，
还会为ServletContext添加一个过滤器`org.apache.tomcat.websocket.server.WsFilter`，它用于判断当前请求是否为WebSocket请求，
以便完成握手。
>
> - 对于扫描到的`Endpoint`子类和添加了注解`@ServerEndpoint`的类，如果当前应用存在ServerApplicationConfig实现，则通过
ServerApplicationConfig获取Endpoint子类的配置（ServerEndpointConfig实例，包含了请求路径等信息）和符合条件的注解类，将结果注册到
WebSocketContainer上，用于处理WebSocket请求。
>
> - 通过ServerApplicationConfig接口我们以**编程的方式**确定只有符合一定规则的Endpoint可以注册到WebSocketContainer，而非所有。
规范通过这种方式为我们提供了一种定制化机制。
>
> - 如果当前应用没有定义ServerApplicationConfig的实现类，那么WsSci默认将**所有**扫描到的注解式Endpoint注册到WebSocketContainer。
因此，如果采用可编程方式定义Endpoint，那么必须添加ServerApplicationConfig实现。

#### 补充说明

  > WebSocket通过`javax.websocket.WebSocketContainer`接口维护应用中定义的所有Endpoint。
  它在每个Web应用中只有一个实例，类似于传统Web应用中的ServletContext。
  >
  >最后，WebSocket规范提供了一个接口`javax.websocket.server.ServerApplicationConfig`，
  通过它，可以为*编程式的Endpoint创建配置*（如指定请求地址），还可以过滤只有符合条件的Endpoint提供服务。
  >
  >在本示例中，通过编程式和注解式分别实现了websocket服务端，需要注意的是，在同一个项目中，如果实现了ServerApplicationConfig，不管是通过何种方式
  实现的客户端，都应该在ServerApplicationConfig中配置EndPoint，正如`MyEndpointConfig`中配置的那样，否则，webSocket会握手失败
  
  
以上内容参考文章：

- [Tomcat的WebSocket实现](https://www.cnblogs.com/duanxz/p/5041110.html)

### Maven项目log4j日志系统异常的问题

#### 异常信息
```
log4j:WARN No appenders could be found for logger (com.zc.playground.server.MyWebSocketServer).
log4j:WARN Please initialize the log4j system properly.
log4j:WARN See http://logging.apache.org/log4j/1.2/faq.html#noconfig for more info.
```
关于异常信息，Apache官网给出的解释：
> [Why do I see a warning about "No appenders found for logger" and "Please configure log4j properly"?](http://logging.apache.org/log4j/1.2/faq.html#noconfig)

实际上就是`log4j.properties`文件不在项目的classpath目录下，编译的时候，没有编译进`${basedir}\target\class`中，一般maven项目在IDEA中正常配置（在项目下将resources目录mark
as resources root），
是不会出现这个问题的，如果出现了，并且配置正常的话，可以尝试在`pom.xml`的`<build>`模块中配置`<resources>`尝试解决：

```xml
<resources>
    <resource>
        <!--描述了资源目标路径，该路径相对target/classes-->
        <!--事实上，log4j的配置文件只需要在classes目录下就可以了，所以 target可以配空或者不配置-->
        <!--<targetPath></targetPath>-->
        <filtering>false</filtering>
        <!--资源目录的位置-->
        <directory>${basedir}/src/main/resources</directory>
        <!--以下配置了编译过程中（不）需要copy的配置文件-->
        <includes>
            <include>**/*.properties</include>
        </includes>
        <excludes>
            <exclude>jdbc.properties</exclude>
        </excludes>
    </resource>
</resources>
```
此外，`pom.xml`的<plugins>中`compiler plugin`也可以配置对resources执行编译的操作，一个可以参考的配置如下：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-resources-plugin</artifactId>
    <version>3.0.2</version>
    <executions>
        <!--编译时copy libs资源文件以及配置文件到打包目录下-->
        <execution>
            <id>copy-libs</id>
            <phase>prepare-package</phase>
            <goals>
                <goal>copy-resources</goal>
            </goals>
            <configuration>
                <encoding>UTF-8</encoding>
                <outputDirectory>${basedir}/target/resources</outputDirectory>
                <resources>
                    <resource>
                        <directory>${basedir}/libs</directory>
                        <includes>
                            <include>*.properties</include>
                            <include>*.jar</include>
                        </includes>
                    </resource>
                </resources>
            </configuration>
        </execution>
    </executions>
</plugin>
```

有关maven pom.xml 更多详细配置信息，可参考

- [pom.xml配置文件详解](https://blog.csdn.net/u012152619/article/details/51485297)
- [pom.xml文件中的Build配置](https://blog.csdn.net/u010010606/article/details/79727438)
- [pom.xml文件结构之Build](https://blog.csdn.net/taiyangdao/article/details/52374125)