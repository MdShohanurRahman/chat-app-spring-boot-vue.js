package com.example.chatapp.paylaod;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResetUnreadPayload {
    private boolean reset;
    private String senderUsername;
    private String recipientUsername;
}
