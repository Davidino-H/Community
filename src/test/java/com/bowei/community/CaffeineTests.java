package com.bowei.community;

import com.bowei.community.entity.DiscussPost;
import com.bowei.community.service.DiscussPostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {
    @Autowired
    private DiscussPostService discussPostService;

    @Test
    public void initDataForTest() {
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("Test: The following is the falsified data.");
            post.setContent("ChatGPT is a language model developed by OpenAI, designed to respond to text-based queries and generate natural language responses.");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            discussPostService.addDiscussPost(post);
        }
    }

    @Test
    public void testCache() {
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 1));
        System.out.println(discussPostService.findDiscussPosts(0, 0, 10, 0));
    }
}
