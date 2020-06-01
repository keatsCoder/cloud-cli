package cn.keats.service_consumer.feign.fallback;

import cn.keats.service_consumer.entity.User;
import cn.keats.service_consumer.feign.UserServiceFeignClient;
import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/06/01
 * @Version 1.0
 */
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
