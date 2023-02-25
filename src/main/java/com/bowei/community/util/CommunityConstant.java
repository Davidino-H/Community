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

    /**
     * Entity type: Posts
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * Entity type: Comments
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * Entity type: User
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * Topic: comment
     */
    String TOPIC_COMMENT = "comment";

    /**
     * Topic: like
     */
    String TOPIC_LIKE = "like";

    /**
     * Topic: follow
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * System id
     */
    int SYSTEM_USER_ID = 1;
}
