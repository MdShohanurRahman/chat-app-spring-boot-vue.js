package com.example.chatapp.controller;

import com.example.chatapp.dto.ChatMessageDTO;
import com.example.chatapp.paylaod.ChatPayload;
import com.example.chatapp.paylaod.MarkSeenPayload;
import com.example.chatapp.entity.User;
import com.example.chatapp.service.ChatMessageService;
import com.example.chatapp.service.MessagingService;
import com.example.chatapp.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final UserService userService;
    private final ChatMessageService chatMessageService;
    private final MessagingService messagingService;

    /**
     * Fetch chat history between sender and recipient.
     *
     * @param senderUsername the username of the sender
     * @param recipientUserName the username of the recipient
     * @return chat history list between the two users
     */
    @GetMapping("/history/sender/{senderUsername}/recipient{recipientUserName}")
    public ResponseEntity<List<ChatMessageDTO>> getChatHistory(@PathVariable String senderUsername, @PathVariable String recipientUserName) {
        return ResponseEntity.ok(chatMessageService.getChatHistory(senderUsername, recipientUserName));
    }


    /**
     * Send a message from sender to recipient via WebSocket.
     *
     * @param payload the chat message payload
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatPayload payload) {
        User sender = userService.findByUsername(payload.getSenderUsername());
        User recipient = userService.findByUsername(payload.getRecipientUsername());

        // Store the message in the database
        ChatMessageDTO chatMessage = chatMessageService.saveChatMessage(sender, recipient, payload.getContent());

        // broadcast sender message
        messagingService.notifySenderMessage(sender, chatMessage);

        // Send message to recipient through WebSocket (real-time messaging)
        messagingService.sendMessageToRecipient(recipient, chatMessage);
    }

    /**
     * Mark a message as seen by the recipient.
     *
     * @param payload payload containing sender and recipient information
     */
    @MessageMapping("/chat.markSeen")
    public void markMessageAsSeen(@Payload MarkSeenPayload payload) {
        // for mark as seen sender and recipient will be alternate
        boolean markedSeen = chatMessageService.markAsSeen(payload.getRecipientUsername(), payload.getSenderUsername());
        if (markedSeen) {
            messagingService.notifyMessageStatus(payload);
            messagingService.notifyResetUnread(payload.getSenderUsername(), payload.getRecipientUsername());
        }
    }
}