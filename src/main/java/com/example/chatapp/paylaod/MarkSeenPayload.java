package com.example.chatapp.paylaod;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MarkSeenPayload {
    private boolean seen;
    private String senderUsername;
    private String recipientUsername;
}
