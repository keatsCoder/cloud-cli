package cn.keats.service_consumer.controller;

import cn.keats.service_consumer.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
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
public class TeacherController {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private DiscoveryClient discoveryClient;

    @GetMapping("teacher/users")
    public List<User> getAllUser(){
        List<ServiceInstance> service_provider = discoveryClient.getInstances("SERVICE_PROVIDER");
        ServiceInstance serviceInstance = service_provider.get(0);

        return restTemplate.getForObject(serviceInstance.getUri() + "/api/v1/users", List.class);
    }

    @GetMapping("teacher/user/{age}")
    public User getAllUser(@PathVariable Integer age){
        List<ServiceInstance> service_provider = discoveryClient.getInstances("SERVICE_PROVIDER");
        ServiceInstance serviceInstance = service_provider.get(0);

        return restTemplate.getForObject(serviceInstance.getUri() + "/api/user/{age}", User.class, age);
    }
}
