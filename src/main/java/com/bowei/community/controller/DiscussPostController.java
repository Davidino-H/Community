package com.bowei.community.controller;

import com.bowei.community.entity.Comment;
import com.bowei.community.entity.DiscussPost;
import com.bowei.community.entity.Page;
import com.bowei.community.entity.User;
import com.bowei.community.service.CommentService;
import com.bowei.community.service.DiscussPostService;
import com.bowei.community.service.UserService;
import com.bowei.community.util.CommunityConstant;
import com.bowei.community.util.CommunityUtil;
import com.bowei.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Array;
import java.util.*;

@Controller
@RequestMapping("/discuss")
public class DiscussPostController implements CommunityConstant {
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private UserService userService;
    @Autowired
    private CommentService commentService;
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public String addDiscussPost(String title, String content) {
        User user = hostHolder.getUser();
        if (user == null) {
            return CommunityUtil.getJSONString(403, "You didn't login");
        }
        DiscussPost post = new DiscussPost();
        post.setUserId(user.getId());
        post.setTitle(title);
        post.setContent(content);
        post.setCreateTime(new Date());
        discussPostService.addDiscussPost(post);

        // 报错的话，将来统一处理
        return CommunityUtil.getJSONString(0, "Post successfully!");
    }

    @RequestMapping(path = "/detail/{discussPostId}", method = RequestMethod.GET)
    public String getDiscussPost(@PathVariable("discussPostId") int discussPostId, Model model, Page page) {
        // Post
        DiscussPost post = discussPostService.findDiscussPostById(discussPostId);
        model.addAttribute("post", post);
        // Author
        User user = userService.findUserById(post.getUserId());
        model.addAttribute("user", user);

        // Comment paging
        page.setLimit(5);
        page.setPath("/discuss/detail/" + discussPostId);
        page.setRows(post.getCommentCount());

        // Comments: Comments for post
        // Reply: Comments for reviews
        // List of Comments
        List<Comment> commentList = commentService.findCommentByEntity(ENTITY_TYPE_POST, post.getId(), page.getOffset(), page.getLimit());
        // List of comments view object
        List<Map<String, Object>> commentVoList = new ArrayList<>();
        if (commentList != null) {
            for (Comment comment : commentList) {
                // One comment view object
                Map<String, Object> commentVo = new HashMap<>();
                // comment
                commentVo.put("comment", comment);
                // author
                commentVo.put("user", userService.findUserById(comment.getUserId()));

                // Reply to comment list
                List<Comment> replyList = commentService.findCommentByEntity(ENTITY_TYPE_COMMENT, comment.getId(), 0, Integer.MAX_VALUE);
                // List of reply view object
                List<Map<String, Object>> replyVoList = new ArrayList<>();
                if (replyList != null) {
                    for (Comment reply : replyList) {
                        Map<String, Object> replyVo = new HashMap<>();
                        // Reply
                        replyVo.put("reply", reply);
                        //author
                        replyVo.put("user", userService.findUserById(reply.getUserId()));
                        // Reply to target comment
                        User target = reply.getTargetId() == 0 ? null : userService.findUserById(reply.getTargetId());
                        replyVo.put("target", target);

                        replyVoList.add(replyVo);
                    }
                }

                commentVo.put("replies", replyVoList);
                // Number of replies
                int replyCount = commentService.findCommentCount(ENTITY_TYPE_COMMENT, comment.getId());
                commentVo.put("replyCount", replyCount);

                commentVoList.add(commentVo);

            }
        }

        model.addAttribute("comments", commentVoList);
        return "/site/discuss-detail";
    }
}