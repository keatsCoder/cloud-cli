package cn.keats.service_provider.controller;

import cn.keats.service_provider.entity.User;
import cn.keats.service_provider.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/2/13
 * @Version 1.0
 */
@RestController
@RequestMapping("/api/v1")
public class UserController {
    @Autowired
    private UserService userService;

    /**
     * 单个用户
     * @return
     */
    @GetMapping("user/{age}")
    public User getUser(@PathVariable("age") Integer age) {
        return userService.getUser(age);
    }

    /**
     * 用户列表
     * @return
     */
    @GetMapping("users")
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @Value("${server.port}")
    private String port;
    @Value("${spring.cloud.client.ip-address}")
    private String ip;

    @GetMapping("ribbon")
    public String ribbonTest() {
        return "本次访问的地址 " + ip + ":" + port;
    }
}
