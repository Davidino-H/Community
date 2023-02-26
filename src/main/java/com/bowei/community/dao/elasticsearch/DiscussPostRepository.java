package com.bowei.community.dao.elasticsearch;

import com.bowei.community.entity.DiscussPost;
//import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost,Integer> {
}