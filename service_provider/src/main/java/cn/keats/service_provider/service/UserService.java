package cn.keats.service_provider.service;

import cn.keats.service_provider.entity.User;

import java.util.List;

/**
 * @Author: keats_coder
 * @Date: 2020/2/13
 * @Version 1.0
 */
public interface UserService {
    User getUser(Integer age);

    List<User> getUsers();
}
