package com.babu.ms.controller;

import com.babu.ms.models.SportsMessage;
import com.babu.ms.service.MessageProducer;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {
    @Autowired
    private MessageProducer messageProducer;

    @PostMapping("/send")
    public ResponseEntity<String> sendMessage(@RequestBody SportsMessage message, @RequestParam String type) {
        if (message == null) {
            return ResponseEntity.badRequest().body("Message cannot be null");
        }
        if (type.equals("cricket")) {
            messageProducer.sendCricketMessage(message);
            return ResponseEntity.ok("Cricket message sent: " + message);
        } else if (type.equals("football")) {
            messageProducer.sendFootballMessage(message);
            return ResponseEntity.ok("Football message sent: " + message);
        } else {
            messageProducer.sendAllMessage(message);
            return ResponseEntity.ok("General message sent: " + message);
        }
    }
}
