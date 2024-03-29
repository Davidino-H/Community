package com.bowei.community.Event;


import com.alibaba.fastjson.JSONObject;
import com.bowei.community.entity.DiscussPost;
import com.bowei.community.entity.Event;
import com.bowei.community.entity.Message;
import com.bowei.community.service.DiscussPostService;
import com.bowei.community.service.ElasticSearchService;
import com.bowei.community.service.MessageService;
import com.bowei.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventConsumer implements CommunityConstant {
    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if (record == null || record.value() == null) {
            System.out.println("Record is empty!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            System.out.println("Event error!");
            return;
        }

        // Send in-station notifications
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserid());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty()) {
            for (Map.Entry<String, Object> entry : event.getData().entrySet()) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
    // 消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            System.out.println("Record is empty!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            System.out.println("Event error!");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticSearchService.saveDiscussPost(post);
    }

    // 消费删除事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if (record == null || record.value() == null) {
            System.out.println("Record is empty!");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null) {
            System.out.println("Event error!");
            return;
        }
        elasticSearchService.deleteDiscussPost(event.getEntityId());
    }
}
