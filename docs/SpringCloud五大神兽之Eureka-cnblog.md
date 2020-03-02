## 注册中心概述

### 什么是注册中心？

相当于服务之间的‘通讯录’，记录了服务和服务地址之间的映射关系。在分布式架构中服务会注册到这里。当服务需要调用其他服务时，就在注册中心找到其他服务的地址，进行调用

### 注册中心的主要作用？

注册中心一般有以下的功能：

1. 服务发现
    - 服务注册/反注册：保存服务提供者和调用者的关系
    - 服务订阅/取消订阅：服务调用者订阅服务提供者的信息
    - 服务路由：筛选整合服务提供者
2. 服务配置
    - 配置订阅：服务提供者和消费者订阅微服务相关的配置
    - 配置下发：主动将配置推送给提供者和消费者
3. 服务健康检测
    - 检测服务提供者的健康情况

## Eureka

Eureka是SpringCloud微服务架构中常用的注册中心，其架构图如下：

![1581421880584](https://img2018.cnblogs.com/blog/1654189/202002/1654189-20200218200846218-1860032827.png)

由其架构图可以看出，Eureka可以分为三部分：Eureka服务端，Eureka服务提供者，Eureka服务消费者。

其中Eureka服务端需要作为独立的服务运行，而服务提供者、消费者则是需要使用EurekaClient嵌入我们自己的服务中。

其运行原理是这样的：

1. 当服务提供者启动时，会向注册中心发送请求，在注册中心注册实例。并且每隔一段时间向注册中心发送心跳，注册中心会保存实例和地址的映射关系

2. 当服务消费者启动时，会从注册中心拉去所有的注册信息，并缓存起来。当需要调用某一个服务时。根据缓存的注册信息直接调用服务

聪明的你这个时候会察觉到。既然消费者使用的是缓存的注册信息。那么一定会存在一种情况就是服务提供者这边已经宕机。这个时候消费者根据缓存的信息没有及时更新就会导致调用失败，Eureka是怎么解决这个问题的呢？且往下看。

### 服务端的搭建

1. 创建工程，导入坐标

这里推荐使用 Spring Initializer 直接创建。选择 web 和 euraka 即可。主要引入的依赖如下：

```xml
       <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
        </dependency>
```

2. 配置 application.yml ，说明见注释

```yml
server:
  port: 9000
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false # 是否将自己注册到注册中心
    fetch-registry: false # 是否从Eureka中获取注册信息
    service-url: # Eureka Client 的请求地址
      defaultZone: http://#{eureka.instance.hostname}:#{server.port}/eureka/
```

3. 配置启动类

启动类除了常规的 @SpringBootApplication 外，还要添加 @EnableEurekaServer 表示开启 Eureka 服务

4. 启动项目，浏览器访问 localhost:9000 出现以下页面即表示服务搭建成功

![1581517629277](https://img2018.cnblogs.com/blog/1654189/202002/1654189-20200218200845711-1434710181.png)

### 注册服务到Eureka

1. 在要注册的工程中导入Eureka Client的坐标

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

2. 在 application.yml 中配置Eureka服务端的地址：

这里需要特别注意：**Eureka客户端配置的服务端地址使用的key是 defaultZone,不是 default_zone** 如果写错会导致客户端无法注册服务，进而导致客户端无法启动

```yml
server:
  port: 9001
spring:
  application:
    name: SERVICE_PROVIDER # 服务名称
  datasource:
    url: jdbc:mysql://192.168.25.128:3306/mysql?characterEncoding=UTF-8&useSSL=false&serverTimezone=CTT
    driver-class-name: com.mysql.cj.jdbc.Driver
    password: 521
    username: keats
eureka:
  client:
    service-url:
      defaultZone: http://localhost:9000/eureka/ # 刚刚在Eureka Server 配置的请求地址
    register-with-eureka: true # 注册到注册中心
    fetch-registry: false # 作为服务提供者，可以不用从Eureka获取注册信息。视实际情况而定
  instance:
    prefer-ip-address: true # 使用IP地址注册
```

3. 在启动类加上 @EnableEurekaClient 注解(SpringCloud Finchley.RELEASE 版本及之后的版本会在项目中引入EurekaClient依赖后自动开启，我们使用的是最新版。因此也可以不用添加)

**Euraka 客户端默认每隔 30 S向服务端发送一次心跳请求，如果服务端 90 S没有收到某客户端发送的请求将视为客户端宕机。会将其从服务列表剔除**

### 消费者获取和使用服务

搭建服务消费者和提供者的步骤类似，首先是添加 eureka-client 依赖

之后配置 application.yml 如下：

```yml
server:
  port: 9002
spring:
  application:
    name: SERVICE_CONSUMER # 服务名称
eureka:
  client:
    service-url: # 刚刚在Eureka Server 配置的请求地址
      defaultZone: http://localhost:9000/eureka/
    fetch-registry: true
    register-with-eureka: false
```

这样该服务就具备了从Eureka获取服务的能力，那具体怎么使用呢？

Spring 为我们提供了一个Bean：DiscoveryClient (注意需要导入：org.springframework.cloud.client.discovery 包下的DiscoveryClient 而不是 netflix 包下的类)

我们在需要获取服务的类里面注入该类

```java
@Autowired
private DiscoveryClient discoveryClient;
```

接着调用其 getInstances(String instancdName) 方法，通过服务的名称获取服务列表。我们这里只注册了一个服务提供方没有搭集群所以直接使用列表第0位的服务实体。而实体提供了 getUri() 方法用于获取服务提供者的 url。接着我们用该方法替换硬编码的 url 即可完成 Eureka 的使用。核心代码如下：

网上的很多其他教程在这里使用的都是 getHost() + ":" + getPort() 拼接，推测可能是版本比较老旧。新API既然已经提供了 getUri() 方法我们就要积极使用。**这里建议读者们在使用某Bean的方法时通过打点的方式阅读一下其开放的API，大概了解一下**

```java
@GetMapping("teacher/users")
public List<User> getAllUser(){
    List<ServiceInstance> service_provider = discoveryClient.getInstances("SERVICE_PROVIDER");
    ServiceInstance serviceInstance = service_provider.get(0);

    return restTemplate.getForObject(serviceInstance.getUri() + "/api/v1/users", List.class);
}
```

## Eureka的自我保护

![1582026485017](https://img2018.cnblogs.com/blog/1654189/202002/1654189-20200218200844969-747307277.png)

如上图提示，表示Eureka进入了自我保护模式。自我保护模式的介绍如下：

Eureka Server 在运行期间会去统计心跳失败比例在 15 分钟之内是否高于 85%，如果高于 85%，Eureka Server 会将这些实例保护起来，让这些实例不会过期。在我看来可以用一句古成语来形容这种模式---“三人成虎” 即当越来越多的服务提供者心跳不能到达时。Eureka开始不在怀疑是提供者GG，而怀疑自己了！

在开发环境中，我们往往启动一个Eureka服务 + 一个 Eureka 提供者，如果此时提供者正好出了问题。90S未发送心跳。但由于满足自我保护条件(这段时间失败比例为100%),Eureka不会将服务剔除。会直接导致服务消费者无法正确获取服务。**因此开发环境中建议关闭其自我保护机制，而在生产环境打开之**, yml 配置如下：

```yml
eureka:
  server:
    enable-self-preservation: false # 关闭自我保护
```
## Eureka集群

我给项目搭建了Eureka注册中心，和配套的服务提供方与服务消费方。完成了服务的灵活调用。从此我们可以将项目中的各个模块都拆分成微服务，注册到注册中心相互调用。但此做法在带来便利性的同时，也为我们带来的隐患：**如果Eureka宕掉，怎么办？**

解决办法就是**搭建Eureka集群**，一个Eureka倒下了，还有另一个Eureka兄弟顶着。那如何搭建Eureka集群呢？

这个问题Spring已经帮我们解决了。通过 Eureka **服务端之间相互注册**的方式，Eureka 服务端就会互相同步注册信息，好兄弟嘛，你的就是我的，我的也是你的。

我在项目中用 SpringProfile 来模拟 Eureka 集群的运行，具体的操作方法见[SpringProfile轻松切换多环境配置文件](https://www.cnblogs.com/keatsCoder/p/12347382.html)：

1. 首先在修改两个 yml 文件的端口，避免端口冲突
2. 打开 Eureka 服务将自己注册到服务的开关、打开 Eureka 服务从服务获取注册信息的开关
3. 配置对方的服务地址
4. 启动两个 Eureka 服务端

这样就完成了两个Eureka服务端的搭建。此时有任何一个服务端收到服务注册，都会同步到另一个。所以理论上我们可以将服务端注册到其中任何一个，但是为了保险起见。我们还是**将服务注册到每个Eureka上**。实现方法就是**将 defaultZone 的值填写多个Eureka的地址，用逗号隔开**

### 其他优化项目

#### 在控制台显示服务的IP

```yml
eureka:
  instance:
    prefer-ip-address: true # 使用IP地址注册
    instance-id: ${spring.cloud.client.ip-address}:${server.port} # 自定义注册ID为ip:端口的格式
```

#### 修改服务剔除时间

默认情况下，服务提供者每隔30S会发送一次心跳到Eureka服务端，且在90S内未接收心跳才会剔除服务。开发阶段来说这个优点长了。可通过以下配置修改

```yml
eureka:
  instance:
    prefer-ip-address: true # 使用IP地址注册
    instance-id: ${spring.cloud.client.ip-address}:${server.port} # 自定义注册ID为ip:端口的格式
    lease-renewal-interval-in-seconds: 5 #发送心跳的间隔
    lease-expiration-duration-in-seconds: 10 #续约到期时间
```


