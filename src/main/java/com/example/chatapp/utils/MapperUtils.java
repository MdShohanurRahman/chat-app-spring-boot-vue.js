package com.example.chatapp.utils;

import com.example.chatapp.dto.ChatMessageDTO;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.ChatMessage;
import com.example.chatapp.entity.User;
import org.modelmapper.ModelMapper;

import java.time.format.DateTimeFormatter;

public final class MapperUtils {

    private final static ModelMapper mapper = new ModelMapper();

    private MapperUtils() {}

    public static UserDTO mapToDto(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .lastLogin(user.getLastLogin().format(DateTimeFormatter.ofPattern("h:mm a")))
                .isOnline(user.getIsOnline())
                .build();
    }

    public static ChatMessageDTO mapToDto(ChatMessage chatMessage) {
        return ChatMessageDTO.builder()
                .id(chatMessage.getId())
                .sender(mapToDto(chatMessage.getSender()))
                .recipient(mapToDto(chatMessage.getRecipient()))
                .content(chatMessage.getContent())
                .timestamp(chatMessage.getTimestamp().format(DateTimeFormatter.ofPattern("h:mm a")))
                .delivered(chatMessage.getDelivered())
                .seen(chatMessage.getSeen())
                .build();
    }
}
