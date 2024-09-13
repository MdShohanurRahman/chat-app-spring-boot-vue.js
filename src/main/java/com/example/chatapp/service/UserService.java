package com.example.chatapp.service;

import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.repository.ChatMessageRepository;
import com.example.chatapp.repository.UserRepository;
import com.example.chatapp.utils.MapperUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ChatMessageRepository chatMessageRepository;

    public User login(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        User user = userOptional.orElseGet(() -> {
            User newUser = new User();
            newUser.setUsername(username);
            newUser.setLastLogin(LocalDateTime.now());
            return newUser;
        });

        user.setIsOnline(true);
        return userRepository.save(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> userList = userRepository.findAll();
        return userList.stream().map(MapperUtils::mapToDto).toList();
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow();
    }

    public User logout(UUID userId) {
        User user = userRepository.findById(userId).orElseThrow();
        user.setIsOnline(false);
        return userRepository.save(user);
    }
}
