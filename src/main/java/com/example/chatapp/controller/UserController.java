package com.example.chatapp.controller;

import com.example.chatapp.paylaod.LoginRequest;
import com.example.chatapp.dto.UserDTO;
import com.example.chatapp.entity.User;
import com.example.chatapp.service.MessagingService;
import com.example.chatapp.service.UserService;
import com.example.chatapp.utils.MapperUtils;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final MessagingService messagingService;

    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getUsername());
        messagingService.notifyUserStatus(user);
        return ResponseEntity.ok(MapperUtils.mapToDto(user));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<Void> logout(@PathVariable UUID userId) {
        User user = userService.logout(userId);
        messagingService.notifyUserStatus(user);
        return ResponseEntity.ok().build();
    }
}