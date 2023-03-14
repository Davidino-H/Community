package com.bowei.community.quartz;

import com.bowei.community.entity.DiscussPost;
import com.bowei.community.service.DiscussPostService;
import com.bowei.community.service.ElasticSearchService;
import com.bowei.community.service.LikeService;
import com.bowei.community.util.CommunityConstant;
import com.bowei.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private RedisTemplate redisTemplate;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private DiscussPostService discussPostService;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private LikeService likeService;
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    private ElasticSearchService elasticSearchService;

    // The community epoch day
    private static final Date epoch;

    static {
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("Fail to initialize epoch day!", e);
        }
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if (operations.size() == 0) {
            logger.info("[Task cancelled] no more Posts Ranking needed to refresh.");
            return;
        }
        logger.info("[Task begin] refreshing the posts ranking: " + operations.size());
        while (operations.size() > 0) {
            this.refresh((Integer) operations.pop());
        }
        logger.info("[Task complete] posts ranking completed!");
    }

    private void refresh(int postId) {
        DiscussPost post  = discussPostService.findDiscussPostById(postId);

        if (post == null) {
            logger.error("Posts not exist: id = " + postId);
            return;
        }

        // Check whether the post is wonderful
        boolean wonderful =  post.getStatus() == 1;
        // Number of comments of post
        int commentCount = post.getCommentCount();
        //Number of like
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // Calculate weight
        double weight = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;
        // Score = posts weight + Number of days away
        double score = Math.log10(Math.max(weight, 1)) + (post.getCreateTime().getTime() - epoch.getTime())/(1000 * 3600 * 24);
        // Update the score of the post
        discussPostService.updateScore(postId, score);
        // Synchronization of searched data
        post.setScore(score);
        elasticSearchService.saveDiscussPost(post);
    }
}