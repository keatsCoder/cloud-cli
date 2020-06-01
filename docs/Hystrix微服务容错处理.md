## 前言

在 SpringCloud 微服务项目中，我们有了 Eureka 做服务的注册中心，进行服务的注册于发现和服务治理。使得我们可以摒弃硬编码式的 ip:端口 + 映射路径 来发送请求。我们有了 Feign 作为声明式服务调用组件，可以像调用本地服务一样来调用远程服务。基于 Ribbon 我们又实现了客户端负载均衡，轻松的在集群环境下选取合适的服务提供者。这样看来我们的微服务貌似很完善了。是这样的吗？

并非如此，想想我们在编码过程中进行的健壮性检查。类比一下服务与服务调用是否也应该更加健壮一些呢？我们目前的微服务在正常运行的时候是没有问题的，但若是某个偏下游的服务提供者不可用，造成服务积压，接连引起上游的服务消费者宕机，引法雪崩效应。是不是就显得我们的微服务不堪一击呢？因此我们需要一个组件来解决这样的问题，前辈们参考生活中保险丝的原理做出了微服务中的保险丝-Hystrix熔断器。下面让我们来一起使用一下

<font color=red>声明：本文首发于博客园，作者：后青春期的Keats；地址：https://www.cnblogs.com/keatsCoder/ 转载请注明，谢谢！</font>

## Hystrix简介

Hystrix主要实现了下面的功能：

* 包裹请求：使用 HystrixCommand(或 HystrixObservableCommand) 包裹对依赖的调用逻辑。每个命令在独立的线程中执行，使用了设计模式中的‘命令模式’
* 跳闸机制：当某微服务的错误率超过一定阈值时，可以自动跳闸，停止请求该服务一段时间
* 资源隔离：Hystrix 为每个微服务都维护了一个小型的线程池(或信号量)如果该线程池已满，发往该依赖的请求就会被立即拒绝
* 监控：Hystrix 可以近乎实时的监控运行指标和配置的变化，例如成功、失败、超时和被拒绝的请求等
* 回退机制：当请求成功、失败、超时和被拒绝或者断路器打开时，执行回退逻辑。回退逻辑可由开发人员自行提供
* 自我修复：断路器打开一段时间后，会进入‘半开’状态，允许一个请求访问服务提供方，如果成功。则关闭断路器

## 使用 Hystrix

### 引入依赖

```xml
        <!-- 熔断器 hystrix -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-hystrix</artifactId>
        </dependency>
```

### 在启动类上添加 @EnableHystrix

### 两种情况下的回退方法

#### 非 Feign 调用下的回退方法

##### 编写回退方法

```java
/**
 * getUserByAge 方法 Hystrix 回退方法
 * @param age
 * @return
 */
public User getUserByAgeFallBack(Integer age){
    User user = new User();
    user.setName("默认用户");
    user.setAge(age);
    return user;
}
```

##### 在客户端的方法上声明

```java
@HystrixCommand(fallbackMethod = "getUserByAgeFallBack")
```

测试：将服务提供方的代码打断点。调用服务消费方，会发现返回了默认用户

需要注意：

1. 回退方法的返回值类型需要和原来方法返回值类型相同(否则会报 FallbackDefinitionException: Incompatible return types)
2. 回退方法的参数列表也要和原来方法相同(否则会报 FallbackDefinitionException: fallback method wasn't found: getUserByAgeFallBack([class java.lang.Integer]))
3. **当我写下第二句时，发现书中下一节介绍说可以通过在回退方法中添加第二个参数：ThrowEable 来捕获异常，分析调用失败的原因，我就知道我错了。**为了避免继续得到错误的结论，我决定读一读 Hystrix 处理回退方法的源码

##### 加点料：Hystrix 对回退方法的封装的源码如下：

```java
com.netflix.hystrix.contrib.javanica.utils.MethodProvider
public FallbackMethod find(Class<?> enclosingType, Method commandMethod, boolean extended) {
	// 首先判断该方法的 HystrixCommand 注解上有没有 defaultFallback / fallbackMethod 配置回退方法名称
    if (this.canHandle(enclosingType, commandMethod)) {
    	// 调用 doFind 方法
        return this.doFind(enclosingType, commandMethod, extended);
    } else {
    	// 没有配置的化就接着下一个判断
        return this.next != null ? this.next.find(enclosingType, commandMethod, extended) : FallbackMethod.ABSENT;
    }
}
```

find 方法在用户所请求的方法的 HystrixCommand 注解上有用 defaultFallback / fallbackMethod 配置回退方法名称的时候，会调用 doFind 方法来寻找回退方法。该方法的参数有两个，enclosingType 是用户所请求的方法的类字节码文件，commandMethod 是用户所请求的方法

首先通过 this.getFallbackName 获取回退方法名称，接着通过获取 commandMethod 的参数类型们

接着分两种情况：

1. 回调方法继承于 commandMethod 且最后一个参数类型是 Throwable，则去掉回退方法参数列表中的 Throwable 类型进行匹配
2. 回调方法不继承于 commandMethod ，则存在两个可能的参数类型列表： fallbackParameterTypes 和 extendedFallbackParameterTypes 前者是 commandMethod 是参数列表，后者是前者 + Throwable。然后两个都进行匹配。接着使用 Java8 Optional API，按顺序选取前者匹配到的方法 / 后者 / 空返回

```java
private FallbackMethod doFind(Class<?> enclosingType, Method commandMethod, boolean extended) {
    String name = this.getFallbackName(enclosingType, commandMethod);
    Class<?>[] fallbackParameterTypes = null;
    if (this.isDefault()) {
        fallbackParameterTypes = new Class[0];
    } else {
        fallbackParameterTypes = commandMethod.getParameterTypes();
    }

    if (extended && fallbackParameterTypes[fallbackParameterTypes.length - 1] == Throwable.class) {
        fallbackParameterTypes = (Class[])ArrayUtils.remove(fallbackParameterTypes, fallbackParameterTypes.length - 1);
    }

    Class<?>[] extendedFallbackParameterTypes = (Class[])Arrays.copyOf(fallbackParameterTypes, fallbackParameterTypes.length + 1);
    extendedFallbackParameterTypes[fallbackParameterTypes.length] = Throwable.class;
    Optional<Method> exFallbackMethod = MethodProvider.getMethod(enclosingType, name, extendedFallbackParameterTypes);
    Optional<Method> fMethod = MethodProvider.getMethod(enclosingType, name, fallbackParameterTypes);
    Method method = (Method)exFallbackMethod.or(fMethod).orNull();
    if (method == null) {
        throw new FallbackDefinitionException("fallback method wasn't found: " + name + "(" + Arrays.toString(fallbackParameterTypes) + ")");
    } else {
        return new FallbackMethod(method, exFallbackMethod.isPresent(), this.isDefault());
    }
}
```

**由源码可以得到结论：回退方法要么参数列表和原始方法相同，要么加且仅加一个类型为 Throwable 的参数。其他的都不行**

#### Feign 客户端下的回退方法

1. 设置：feign.hystrix.enabled: true

2. Feign 客户端接口上的 @FeignClient 添加 fallback 属性，指向回退类
3. 回退类实现客户端接口

```yml
# feign的配置
feign:
  hystrix:
    enabled: true # 打开 feign 的 hystrix 支持
```

注意回退类加上 @Component 接口，避免因为 Spring 容器找不到该类而启动报错

```java
// Feign 客户端接口上的 @FeignClient 添加 fallback 属性，指向回退类
@FeignClient(name = "SERVICE-PROVIDER", fallback = UserServiceFeignClientFallBack.class)
public interface UserServiceFeignClient {

    @GetMapping("/api/v1/user/{age}")
    User getUser(@PathVariable("age") Integer age);

    /**
     * 用户列表
     * @return
     */
    @GetMapping("/api/v1/users")
    List<User> getUsers();
}
// 回退类实现客户端接口
@Component 
public class UserServiceFeignClientFallBack implements UserServiceFeignClient {
    @Override
    public User getUser(Integer age) {
        return null;
    }

    @Override
    public List<User> getUsers() {
        return null;
    }
}
```

当采用 Feign 客户端来实现回退的时候，前面的捕捉异常方法就不起作用了，那我们应该如何来处理异常呢？可以使用 @FeignClient 的 fallbackFactory 属性

```java
@FeignClient(name = "SERVICE-PROVIDER", fallbackFactory = UserServiceFallbackFactory.class)

@Component
@Slf4j
public class UserServiceFallbackFactory implements FallbackFactory<UserServiceFeignClient> {
    @Override
    public UserServiceFeignClient create(Throwable t) {
        // 日志最好写在各个 fallback 方法中，而不要直接卸载 create方法中
        // 否则引用启动时就会打印该日志

        return new UserServiceFeignClient() {
            @Override
            public User getUser(Integer age) {
                log.info("调用User服务提供者失败", t);
                User user = new User();
                user.setName("默认用户");
                user.setAge(age);
                return user;
            }

            @Override
            public List<User> getUsers() {
                return null;
            }
        };
    }
}
```

注意： **fallback 和 fallbackFactory 属性同时存在时，fallback 的优先级更高。因此开发中如果需要处理异常，只需配置 fallbackFactory 属性即可 **

### 避免业务异常走进回退方法

在某些场景下，当发生业务异常时，我们并不想触发 fallback。例如业务中判断年龄 age 不能小于 1，否则抛出异常

```java
if(age < 1){
    throw new KeatsException(ExceptionEnum.NUM_LESS_THAN_MIN);
}
```

这时 Hystrix 会捕捉到异常然后执行 fallback 方法，我们可以通过下面两个方法来避免：

1. 继承 HystrixBadRequestException 该类继承自 RunntimeException
2. 在 @HystrixCommand 添加属性 ignoreExceptions = {KeatsException.class}

## 为 Feign 禁用 Hystrix

只要打开 feign 的 hystrix 支持开关，feign 就会使用断路器包裹 feign 客户端的所有方法，但很多场景并不需要这样。该如何禁用呢？

- 为指定客户端禁用。需要借助 Feign 的自定义配置。首先添加一个自定义配置类，然后配置到 @FeignClient 的 configuration 属性中

```java
@Configuration
public class FeignDisableHystrixConfiguration {
    @Bean
    @Scope("prototype")
    public Feign.Builder feignBuilder(){
        return Feign.builder();
    }
}

@FeignClient(name = "SERVICE-PROVIDER", configuration = {FeignDisableHystrixConfiguration.class})
```

- 全局禁用： feign.hystrix.enabled: false



本博客中所有示例代码都已上传至 github仓库： 

参考文献：《Spring Cloud与Docker 微服务架构实战》 --- 周立



<font size=5 color=blue>码字不易，如果你觉得读完以后有收获，不妨点个推荐让更多的人看到吧！</font>