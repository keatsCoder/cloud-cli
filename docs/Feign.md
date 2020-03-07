Fegin 是由NetFlix开发的声明式、模板化HTTP客户端，可用于SpringCloud 的服务调用。提供了一套更优雅、便捷的HTTP调用API，并且SpringCloud整合了Fegin、Eureka和Ribbon。使其使用更加简便。替换了之前使用RestTemplate进行硬编码方式的服务调用。

## Feign组件入门

### 导入依赖

```xml
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### 开启服务

在启动类上添加 @EnableFeignClients 开启

```java
@SpringBootApplication
@EnableFeignClients
public class ServiceConsumerApplication {}
```

### 写服务接口

1. 在项目中创建一个文件夹用来管理Fegin的所有接口
2. 可以按照服务提供者的名字进行二级分类
3. 创建一个接口。这里我根据服务提供者的名称创建了一个 **UserServiceFeignClient** 的接口
4. 接口上添加注解 @FeignClient("SERVICE-PROVIDER")，注解的默认值填写服务提供者的名字
5. 复制提供者的Controller方法，**注意不要遗漏类名上的地址**



```java
package cn.keats.service_consumer.feign;

import cn.keats.service_consumer.entity.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/3/7
 * @Version 1.0
 */
@FeignClient("SERVICE-PROVIDER")
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

```

### 调用

在需要调用服务的地方使用 @AutoWired 注入该接口。调用其对应的方法

```java
@Autowired
private UserServiceFeignClient userServiceFeignClient;

/**
 * 基于 Feign 的优雅的接口调用方式
 */
@GetMapping("teacher/user/{age}")
public User getAllUser(@PathVariable Integer age){
    return userServiceFeignClient.getUser(age);
}
```

## Feign负载均衡

Feign中已经集成了Ribbon的负载均衡功能，默认情况下使用轮询的规则，若想要自定义可以参考：[Ribbon进行服务调用/负载均衡以及请求重试配置](https://www.cnblogs.com/keatsCoder/p/12398650.html)

## Feign的配置

新版本的Feign支持配置文件配置，常用的配置有日志级别的配置。开发环境下可以配置成最细腻级别的日志方便检查问题

```yml
# feign的配置
feign:
  client:
    config:
      SERVICE-PROVIDER:
        loggerLevel: FULL # NONE(默认选项), BASIC, HEADERS, FULL; 内容依次丰富、性能影响依次增大
logging:
  level:
    cn.keats.service_consumer.feign.UserServiceFeignClient: debug
```

## Feign的执行原理

1. 在Spring容器启动时，扫描到 @EnableFeignClients 注解。而该注解又导入了 FeignClientsRegistrar.class 这个类。该类定义了扫描所有添加 @FeignClient 注解的接口
2. FeignClientsRegistrar 实现了 Spring ImportBeanDefinitionRegistrar 类，用来导入自定义Bean。其中该方法主要用来导入Bean.

```java
public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
    this.registerDefaultConfiguration(metadata, registry); // 注册配置
    this.registerFeignClients(metadata, registry);// 注册Feign客户端
}
```

3. 之后通过 registerFeignClient 方法，以**动态代理**的形式生成接口的实现。让我们得以调用



