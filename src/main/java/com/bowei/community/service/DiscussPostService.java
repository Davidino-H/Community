package com.bowei.community.service;

import com.bowei.community.dao.DiscussPostMapper;
import com.bowei.community.entity.DiscussPost;
import com.bowei.community.util.OffensiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;
    @Autowired
    private OffensiveFilter offensiveFilter;
    public List<DiscussPost> findDiscussPosts(int userId, int offset, int limit) {
        return discussPostMapper.selectDiscussPosts(userId, offset, limit);
    }

    public int findDiscussPostRows(int userId) {
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post) {
        if (post == null) {
            throw new IllegalArgumentException("参数不能为空");
        }

        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));
        // filter offensive words
        post.setTitle(offensiveFilter.filter(post.getTitle()));
        post.setContent(offensiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }
}
