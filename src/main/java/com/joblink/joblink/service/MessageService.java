
package com.joblink.joblink.service;

import com.joblink.joblink.dao.ConversationDao;
import com.joblink.joblink.dao.MessageBlockDao;
import com.joblink.joblink.dao.MessageDao;
import com.joblink.joblink.model.Conversation;
import com.joblink.joblink.model.Message;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageService {

    private final MessageDao messageDao;
    private final ConversationDao conversationDao;
    private final MessageBlockDao messageBlockDao;

    public MessageService(MessageDao messageDao, ConversationDao conversationDao, MessageBlockDao messageBlockDao) {
        this.messageDao = messageDao;
        this.conversationDao = conversationDao;
        this.messageBlockDao = messageBlockDao;
    }

    public List<Conversation> getUserConversations(int userId) {
        return conversationDao.findByUserId(userId);
    }

    public List<Message> getConversationMessages(int conversationId) {
        return messageDao.findByConversationId(conversationId);
    }

    @Transactional
    public Message sendMessage(int senderUserId, int receiverUserId, String content, String messageType,
                               int seekerId, int employerId) {
        // Check if sender is blocked by receiver
        if (messageBlockDao.isBlocked(receiverUserId, senderUserId)) {
            throw new IllegalStateException("You are blocked by this user");
        }

        // Get or create conversation
        Conversation conversation = conversationDao.findByParticipants(seekerId, employerId);
        if (conversation == null) {
            conversation = new Conversation();
            conversation.setSeekerId(seekerId);
            conversation.setEmployerId(employerId);
            int convId = conversationDao.create(conversation);
            conversation.setConversationId(convId);
        }

        // Create message
        Message message = new Message();
        message.setConversationId(conversation.getConversationId());
        message.setSenderUserId(senderUserId);
        message.setReceiverUserId(receiverUserId);
        message.setMessageContent(content);
        message.setMessageType(messageType != null ? messageType : "text");
        message.setIsRead(false);
        message.setIsRecalled(false);

        int messageId = messageDao.create(message);
        message.setMessageId(messageId);

        return message;
    }

    @Transactional
    public void markMessagesAsRead(int conversationId, int userId) {
        messageDao.markAsRead(conversationId, userId);
    }

    @Transactional
    public void recallMessage(int messageId, int userId) {
        Message message = messageDao.findById(messageId);
        if (message == null) {
            throw new IllegalArgumentException("Message not found");
        }
        if (!message.getSenderUserId().equals(userId)) {
            throw new IllegalStateException("You can only recall your own messages");
        }
        messageDao.recallMessage(messageId, userId);
    }

    public void blockUser(int blockerUserId, int blockedUserId, String reason) {
        if (blockerUserId == blockedUserId) {
            throw new IllegalArgumentException("Cannot block yourself");
        }
        messageBlockDao.blockUser(blockerUserId, blockedUserId, reason);
    }

    public void unblockUser(int blockerUserId, int blockedUserId) {
        messageBlockDao.unblockUser(blockerUserId, blockedUserId);
    }

    public boolean isBlocked(int blockerUserId, int blockedUserId) {
        return messageBlockDao.isBlocked(blockerUserId, blockedUserId);
    }

    public int getUnreadMessageCount(int userId) {
        return messageDao.countUnreadMessages(userId);
    }

    public Conversation getConversation(int conversationId) {
        return conversationDao.findById(conversationId);
    }
}
