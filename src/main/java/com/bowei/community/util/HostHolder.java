package com.bowei.community.util;

import com.bowei.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，代替session对象
 */
@Component
public class HostHolder {
    private ThreadLocal<User> users = new ThreadLocal<>();
    public void setUsers(User user) {
        users.set(user);
    }

    public User getUser() {
        return users.get();
    }

    public void clear() {
        users.remove();
    }
}
