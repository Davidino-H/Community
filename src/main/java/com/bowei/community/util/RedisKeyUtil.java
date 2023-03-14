package com.bowei.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    /**
     * 某个实体的赞
     * like:entity:entityType:entityId->set(userId)
     *
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType, int entityId) {
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户的赞
     * like:user:userId -> int
     *
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId) {
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

    /**
     * 某一个用户关注的实体
     * followee:userId:entityType -> zset(entityId, now)
     */
    public static String getFolloweeKey(int userId, int entityType) {
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * The fans of the one user
     * follower:entityType:entityId -> zset(userId, now)
     */

    public static String getFollowerKey(int entityType, int entityId) {
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * Login kaptcha
     */
    public static String getKaptchaKey(String owner) {
        return PREFIX_KAPTCHA + SPLIT + owner;
    }
    /**
     * Login ticket
     */
    public static String getTicketKey(String ticket) {
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * User
     */
    public static String getUserKey(int userId) {
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * Daily UV
     */
    public static String getUVKey(String date) {
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * Region UV
     */
    public static String getUVKey(String startDate, String endDate) {
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * Daily active user
     */
    public static String getDAUKey(String date) {
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * Region active user
     */
    public static String getDAUKey(String startdate, String endDate) {
        return PREFIX_DAU + SPLIT + startdate + SPLIT + endDate;
    }

    /**
     * Post scores
     */
    public static String getPostScoreKey() {
        return PREFIX_POST + SPLIT + "score";
    }
}
