package com.bowei.community.controller;

import com.bowei.community.entity.User;
import com.bowei.community.service.LikeService;
import com.bowei.community.util.CommunityUtil;
import com.bowei.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;

    @RequestMapping(path = "/like", method = RequestMethod.POST)
    @ResponseBody
    public String like(int entityType, int entityId) {
        User user = hostHolder.getUser();

        // Like
        likeService.like(user.getId(), entityType, entityId);
        // Number
        long likeCount = likeService.findEntityLikeCount(entityType, entityId);
        // States
        int likeStatus = likeService.findEntityLikeStatus(user.getId(), entityType, entityId);
        // Return results
        Map<String, Object> map = new HashMap<>();
        map.put("likeCount", likeCount);
        map.put("likeStatus", likeStatus);

        return CommunityUtil.getJSONString(0, null, map);
    }
}
