package com.bowei.community.dao;

import com.bowei.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MessageMapper {

    // Query the current user's session list, returning only the latest private message for each painting.
    List<Message> selectConversations(int userId, int offset, int limit);

    // Query the number of sessions of the current user.
    int selectConversationCount(int userId);

    // Query the list of private messages contained in a session.
    List<Message> selectLetters(String conversationId, int offset, int limit);

    // Query the number of private messages contained in a session.
    int selectLetterCount(String conversationId);

    // Query the number of unread letters;
    int selectLetterUnreadCount(int userId, String conversationId);

    // Add new message
    int insertMessage(Message message);

    //Update message status
    int updateStatus(List<Integer> ids, int status);

    // Check the latest notifications under a topic
    Message selectLatestNotice(int userId, String topic);

    //查询某个主题包含通知数量
    int selectNoticeCount(int userId, String topic);

    // Check the number of unread notifications
    int selectNoticeUnreadCount(int userId, String topic);

    // Query the list of notifications contained in a topic
    List<Message> selectNotices(int userId, String topic, int offset, int limit);
}
