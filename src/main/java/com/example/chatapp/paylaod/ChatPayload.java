package com.example.chatapp.paylaod;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatPayload {
    private UUID id;
    private String senderUsername;
    private String recipientUsername;
    private String content;
    private String timestamp;
}