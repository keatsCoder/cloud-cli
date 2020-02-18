package cn.keats.service_provider.service.impl;

import cn.keats.service_provider.entity.User;
import cn.keats.service_provider.service.UserService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/2/13
 * @Version 1.0
 */
@Service
public class UserServiceImpl implements UserService {
    @Override
    public User getUser(Integer age) {
        User user = new User();
        if(age == 18){
            user.setName("后青春期的Keats");
            user.setAge(18);
        } else if(age == 20){
            user.setName("五月天");
            user.setAge(20);
        }
        return user;
    }

    @Override
    public List<User> getUsers() {
        List<User> userList = new ArrayList<>();
        User user = new User();
        user.setName("后青春期的Keats");
        user.setAge(18);
        userList.add(user);

        User user2 = new User();
        user2.setName("五月天");
        user2.setAge(20);
        userList.add(user2);
        return userList;
    }
}
