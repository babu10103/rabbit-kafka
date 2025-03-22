package com.babu.ms.controller;

import com.babu.ms.service.MessageProducer;
import jakarta.websocket.server.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class MessageController {
    @Autowired
    private MessageProducer messageProducer;

    @PostMapping("/send")
    public String sendMessage(@RequestBody String message, @RequestParam String type) {
        if (type.equals("cricket")) {
            messageProducer.sendCricketMessage(message);
        } else if (type.equals("football")) {
            messageProducer.sendFootballMessage(message);
        } else {
            messageProducer.sendAllMessage(message);
        }
        return "Message sent: " + message;
    }
}
