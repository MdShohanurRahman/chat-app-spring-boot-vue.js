package com.example.chatapp.dto;

import lombok.*;

import java.util.UUID;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessageDTO {
    private UUID id;
    private UserDTO sender;
    private UserDTO recipient;
    private String content;
    private String timestamp;
    private Boolean delivered = false;
    private Boolean seen = false;
}
