package cn.keats.service_consumer.controller;

import cn.keats.service_consumer.entity.User;
import cn.keats.service_consumer.exception.ExceptionEnum;
import cn.keats.service_consumer.exception.KeatsException;
import cn.keats.service_consumer.feign.UserServiceFeignClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/2/13
 * @Version 1.0
 */
@RestController
@RequestMapping("api/v1")
@Slf4j
public class TeacherController {
//    @Autowired
//    private RestTemplate restTemplate;
//    @Autowired
//    private DiscoveryClient discoveryClient;
    @Autowired
    private UserServiceFeignClient userServiceFeignClient;

    /**
     * 基于 Feign 的优雅的接口调用方式
     */
//    @HystrixCommand(fallbackMethod = "getUserByAgeFallBack", ignoreExceptions = {KeatsException.class})
    @GetMapping("teacher/user/{age}")
    public User getUserByAge(@PathVariable Integer age){

        if(age < 1){
            throw new KeatsException(ExceptionEnum.NUM_LESS_THAN_MIN);
        }

        return userServiceFeignClient.getUser(age);
    }

    @GetMapping("teacher/users")
    public List getAllUser(){
        return userServiceFeignClient.getUsers();
    }

    /**
     * Hystrix 回调
     * @param age
     * @return
     */
    public User getUserByAgeFallBack(Integer age, Throwable t){
        User user = new User();
        log.error("远程服务调用失败", t);
        user.setName("默认用户");
        user.setAge(age);
        return user;
    }

    /**
     * 基于 Ribbon 的调用。直接使用服务名称. 返回服务提供者的IP和端口
     */
//    @GetMapping("ip")
//    public String getUrl(){
//        return restTemplate.getForObject("http://SERVICE-PROVIDER/api/v1/ribbon", String.class);
//    }

    /**
     * 基于 Ribbon 的调用。直接使用服务名称
     * @param age
     * @return
     */
//    @GetMapping("teacher/user/{age}")
//    public User getAllUser(@PathVariable Integer age){
//        return restTemplate.getForObject("http://SERVICE-PROVIDER/api/v1/user/{age}", User.class, age);
//    }

//    @GetMapping("teacher/users")
//    public List getAllUser(){
//        return restTemplate.getForObject("http://SERVICE-PROVIDER/api/v1/users", List.class);
//    }

    /**
     * 使用 RestTemplate 调用不带惨的GET请求
     * @return
     */
//    @GetMapping("teacher/users")
//    public List<User> getAllUser(){
//        List<ServiceInstance> service_provider = discoveryClient.getInstances("SERVICE_PROVIDER");
//        ServiceInstance serviceInstance = service_provider.get(0);
//
//        return restTemplate.getForObject(serviceInstance.getUri() + "/api/v1/users", List.class);
//    }

    /**
     * 使用 RestTemplate 调用带url参数的GET请求
     */
//    @GetMapping("teacher/user/{age}")
//    public User getAllUser(@PathVariable Integer age){
//        List<ServiceInstance> service_provider = discoveryClient.getInstances("SERVICE_PROVIDER");
//        ServiceInstance serviceInstance = service_provider.get(0);
//
//        return restTemplate.getForObject(serviceInstance.getUri() + "/api/v1/user/{age}", User.class, age);
//    }
}
