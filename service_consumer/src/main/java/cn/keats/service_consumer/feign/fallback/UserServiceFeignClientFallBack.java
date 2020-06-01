package cn.keats.service_consumer.feign.fallback;

import cn.keats.service_consumer.entity.User;
import cn.keats.service_consumer.feign.UserServiceFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/06/01
 * @Version 1.0
 */
@Slf4j
@Component
public class UserServiceFeignClientFallBack implements UserServiceFeignClient {

    @Override
    public User getUser(Integer age) {
        User user = new User();
        user.setName("默认用户");
        user.setAge(age);
        return user;
    }

    @Override
    public List<User> getUsers() {
        return null;
    }
}
