package cn.keats.service_consumer.feign;

import cn.keats.service_consumer.configuration.FeignDisableHystrixConfiguration;
import cn.keats.service_consumer.entity.User;
import cn.keats.service_consumer.feign.fallback.UserServiceFallbackFactory;
import cn.keats.service_consumer.feign.fallback.UserServiceFeignClientFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/3/7
 * @Version 1.0
 */
// fallback 和 fallbackFactory 同时存在时，fallback 的优先级更高
//@FeignClient(name = "SERVICE-PROVIDER", fallback = UserServiceFeignClientFallBack.class, fallbackFactory = UserServiceFallbackFactory.class)
//@FeignClient(name = "SERVICE-PROVIDER", fallbackFactory = UserServiceFallbackFactory.class)
// feign 不使用 hystrix
@FeignClient(name = "SERVICE-PROVIDER", configuration = {FeignDisableHystrixConfiguration.class})
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
