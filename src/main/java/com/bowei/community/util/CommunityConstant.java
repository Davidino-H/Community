package com.bowei.community.util;

public interface CommunityConstant {
    /**
     * Activate success
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * Duplicate avtivation
     */
    int ACTIVATION_REPEAT = 1;
    /**
     * Activate failed
     */
    int ACTIVATION_FAILURE = 2;
    /**
     * default login ticket expired time
     */
    int DEFAULT_EXPIRED_SECODES = 3600 * 12;
    /**
     * Remember status login ticket expired time
     */
    int REMEMBER_EXPIRED_SECODES = 3600 * 24 * 100;
}
