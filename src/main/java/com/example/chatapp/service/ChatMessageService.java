package com.example.chatapp.service;

import com.example.chatapp.dto.ChatMessageDTO;
import com.example.chatapp.entity.ChatMessage;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.utils.MapperUtils;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;

    public ChatMessageDTO saveChatMessage(User sender, User recipient, String content) {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSender(sender);
        chatMessage.setRecipient(recipient);
        chatMessage.setContent(content);
        chatMessage.setDelivered(recipient.getIsOnline());
        chatMessage.setTimestamp(LocalDateTime.now());
        chatMessageRepository.save(chatMessage);
        return MapperUtils.mapToDto(chatMessage);
    }

    public List<ChatMessageDTO> getChatHistory(String senderUsername, String recipientUserName) {
        List<ChatMessage> chatMessageList = chatMessageRepository.findChatHistory(senderUsername, recipientUserName).orElse(new ArrayList<>());
        if (chatMessageList.isEmpty()) {
            return new ArrayList<>();
        }
        return chatMessageList.stream().map(MapperUtils::mapToDto).toList();
    }

    public boolean markAsSeen(String senderUsername, String recipientUserName) {
        boolean markSeen = false;
        List<ChatMessage> chatMessageList = chatMessageRepository.findAllUnseenMessages(senderUsername, recipientUserName).orElse(new ArrayList<>());
        if (!chatMessageList.isEmpty()) {
            markSeen = true;
            chatMessageList.forEach(chatMessage -> {
                chatMessage.setDelivered(true);
                chatMessage.setSeen(true);
            });
            chatMessageRepository.saveAll(chatMessageList);
        }
        return markSeen;
    }
}