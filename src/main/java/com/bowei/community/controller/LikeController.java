package com.bowei.community.controller;

import com.bowei.community.Event.EventProducer;
import com.bowei.community.entity.Event;
import com.bowei.community.entity.User;
import com.bowei.community.service.LikeService;
import com.bowei.community.util.CommunityConstant;
import com.bowei.community.util.CommunityUtil;
import com.bowei.community.util.HostHolder;
import com.bowei.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController implements CommunityConstant {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private EventProducer eventProducer;
    @Autowired
    private RedisTemplate redisTemplate;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId, int entityUserId, int postId) {
        User user = hostHolder.getUser();

        // Like
        likeService.like(user.getId(), entityType, entityId, entityUserId);
        // Number
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // States
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // Return results
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        // Trigger like event
        if (likeStatus == 1) {
            Event event = new Event()
                    .setTopic(TOPIC_LIKE)
                    .setUserid(hostHolder.getUser().getId())
                    .setEntityType(entityType)
                    .setEntityId(entityId)
                    .setEntityUserId(entityUserId)
                    .setData("postId", postId);
            eventProducer.fireEvent(event);
        }
        if (entityType == ENTITY_TYPE_POST) {
            // Prepare for calculating post score
            String redisKey = RedisKeyUtil.getPostScoreKey();
            redisTemplate.opsForSet().add(redisKey, postId);
        }
        return CommunityUtil.getJSONString(0, null, map);
    }
}
