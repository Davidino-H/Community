package com.bowei.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

public class CommunityUtil {
    // Create random string
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "-");
    }

    // MD5 encrypt
    // world -> abcdef1234 Not safe
    // world + 3e4a7(Random string) -> abcdef1234 Safe
    public static String md5(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }
}
