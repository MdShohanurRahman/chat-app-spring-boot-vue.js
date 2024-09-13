package com.example.chatapp.service;

import com.example.chatapp.dto.ChatMessageDTO;
import com.example.chatapp.paylaod.MarkSeenPayload;
import com.example.chatapp.entity.User;
import com.example.chatapp.paylaod.ResetUnreadPayload;
import com.example.chatapp.utils.MapperUtils;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class MessagingService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendMessageToRecipient(User recipient, ChatMessageDTO chatMessage) {
        messagingTemplate.convertAndSendToUser(recipient.getUsername(), "/queue/messages", chatMessage);
    }

    public void notifySenderMessage(User sender, ChatMessageDTO chatMessage) {
        messagingTemplate.convertAndSend("/topic/messages/" + sender.getUsername(), chatMessage);
    }

    public void notifyMessageStatus(MarkSeenPayload markSeenPayload) {
        messagingTemplate.convertAndSend("/topic/messagesSeen/" + markSeenPayload.getRecipientUsername(), markSeenPayload);
    }

    public void notifyResetUnread(String senderUsername, String recipientUsername) {
        ResetUnreadPayload payload = new ResetUnreadPayload();
        payload.setSenderUsername(senderUsername);
        payload.setRecipientUsername(recipientUsername);
        payload.setReset(true);
        messagingTemplate.convertAndSend("/topic/resetUnread/" + senderUsername, payload);
    }

    public void notifyUserStatus(User user) {
        messagingTemplate.convertAndSend("/topic/userStatus", MapperUtils.mapToDto(user));
    }
}
