package com.bowei.community.controller;

import com.bowei.community.entity.DiscussPost;
import com.bowei.community.entity.Page;
import com.bowei.community.entity.SearchResult;
import com.bowei.community.service.ElasticSearchService;
import com.bowei.community.service.LikeService;
import com.bowei.community.service.UserService;
import com.bowei.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class SearchController implements CommunityConstant {
    @Autowired
    private ElasticSearchService elasticSearchService;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;

    // search?keyword=xxx
    @RequestMapping(path = "/search",method = RequestMethod.GET)
    public String search(String keyword, Page page, Model model){
        //搜索帖子
        try {
            SearchResult searchResult = elasticSearchService.searchDiscussPost(keyword, (page.getCurrent() - 1)*10, page.getLimit());
            List<Map<String,Object>> discussPosts = new ArrayList<>();
            List<DiscussPost> list = searchResult.getList();
            if(list != null) {
                for (DiscussPost post : list) {
                    Map<String,Object> map = new HashMap<>();
                    //帖子 和 作者
                    map.put("post",post);
                    map.put("user",userService.findUserById(post.getUserId()));
                    // 点赞数目
                    map.put("likeCount",likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId()));

                    discussPosts.add(map);
                }
            }
            model.addAttribute("discussPosts",discussPosts);
            model.addAttribute("keyword",keyword);
            //分页信息
            page.setPath("/search?keyword=" + keyword);
            page.setRows(searchResult.getTotal() == 0 ? 0 : (int) searchResult.getTotal());
        } catch (IOException e) {
            System.out.println("Search Error Occured!");
        }
        return "/site/search";
    }
}
