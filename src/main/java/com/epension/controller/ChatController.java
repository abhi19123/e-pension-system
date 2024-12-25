package com.epension.controller;

import com.epension.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/message")
    public ResponseEntity<Map<String, String>> handleMessage(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        String response = chatService.generateResponse(userMessage);
        return ResponseEntity.ok(Map.of("response", response));
    }
}
