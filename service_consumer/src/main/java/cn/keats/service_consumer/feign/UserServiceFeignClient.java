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
