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

}
