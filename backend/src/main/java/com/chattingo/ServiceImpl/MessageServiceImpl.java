
package com.chattingo.ServiceImpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.chattingo.Exception.ChatException;
import com.chattingo.Exception.MessageException;
import com.chattingo.Exception.UserException;
import com.chattingo.Model.Chat;
import com.chattingo.Model.Message;
import com.chattingo.Model.User;
import com.chattingo.Payload.SendMessageRequest;
import com.chattingo.Repository.MessageRepository;
import com.chattingo.Service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private ChatServiceImpl chatService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Override
    public Message sendMessage(SendMessageRequest req)
            throws UserException, ChatException {

        User user = userService.findUserById(req.getUserId());
        Chat chat = chatService.findChatById(req.getChatId());

        Message message = new Message();
        message.setChat(chat);
        message.setUser(user);
        message.setContent(req.getContent());
        message.setTimestamp(LocalDateTime.now());

        // Save the message to MySQL first
        message = messageRepository.save(message);

        // Broadcast to the appropriate chat destination
        if (chat.isGroup()) {
            messagingTemplate.convertAndSend(
                "/group/" + chat.getId(),
                message
            );
        } else {
            messagingTemplate.convertAndSend(
                "/chat/" + chat.getId(),
                message
            );
        }

        return message;
    }

    @Override
    public List<Message> getChatsMessages(
            Integer chatId, User reqUser)
            throws ChatException, UserException {

        Chat chat = chatService.findChatById(chatId);

        if (!chat.getUsers().contains(reqUser)) {
            throw new UserException(
                "You are not related to this chat"
            );
        }

        return messageRepository.findByChatId(chat.getId());
    }

    @Override
    public Message findMessageById(Integer messageId)
            throws MessageException {

        return messageRepository.findById(messageId)
            .orElseThrow(() ->
                new MessageException(
                    "The required message is not found"
                )
            );
    }

    @Override
    public void deleteMessage(Integer messageId, User reqUser)
            throws MessageException {

        Message message = messageRepository.findById(messageId)
            .orElseThrow(() ->
                new MessageException(
                    "The required message is not found"
                )
            );

        if (message.getUser().getId() == reqUser.getId()) {
            messageRepository.delete(message);
        } else {
            throw new MessageException(
                "You are not authorized for this task"
            );
        }
    }
}

