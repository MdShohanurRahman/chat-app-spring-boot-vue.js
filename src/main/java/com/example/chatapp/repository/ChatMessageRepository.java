package com.example.chatapp.repository;

import com.example.chatapp.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    @Query("SELECT c FROM ChatMessage c WHERE (c.sender.username = :senderUsername AND c.recipient.username = :recipientUsername) " +
            "OR (c.sender.username = :recipientUsername AND c.recipient.username = :senderUsername) " +
            "ORDER BY c.timestamp ASC")
    Optional<List<ChatMessage>> findChatHistory(String senderUsername, String recipientUsername);

    @Query("SELECT c FROM ChatMessage c WHERE c.sender.username = :senderUsername and c.recipient.username = :recipientUserName and c.seen = false")
    Optional<List<ChatMessage>> findAllUnseenMessages(String senderUsername, String recipientUserName);

}