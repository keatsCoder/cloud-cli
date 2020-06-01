package cn.keats.service_consumer.controller;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author: keats_coder
 * @Date: 2020/05/28
 * @Version 1.0
 */
@RestController
@RequestMapping("/api")
@Data
public class GateWayTestController {
    @Value("${spring.application.name}")
    private String name;


    @GetMapping("/name")
    public String getServiceName(){
        System.out.println("我还以为，没人会选我了呢");
        return  "欢迎来到：" + name;
    }
}
