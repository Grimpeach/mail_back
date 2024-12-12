package com.example.mail_client.controllers;

import com.example.mail_client.entity.Message;
import com.example.mail_client.entity.User;
import com.example.mail_client.repositories.MessageRepository;
import com.example.mail_client.repositories.UserRepository;
import com.example.mail_client.services.MessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    public MessageController(MessageService messageService, UserRepository userRepository, MessageRepository messageRepository) {
        this.messageService = messageService;
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
    }

    // Отправить сообщение
    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message message) {
        Message savedMessage = messageService.sendMessage(message);
        return ResponseEntity.ok(savedMessage);
    }

    @PostMapping("/markDelete")
    public ResponseEntity<Message> markDeleteMessage(@RequestBody Message message) {
        Message full_message = messageRepository.findById(message.getId()).get();
        if (!full_message.getDeleted()) {
            message.setDeleted(true);
            return ResponseEntity.ok(messageRepository.save(message));
        }
        else {
            messageRepository.delete(full_message);
            return ResponseEntity.ok(message);
        }
    }

    // Получить сообщения для конкретного пользователя
    @GetMapping("/recipient/{recipient}")
    public ResponseEntity<List<Message>> getMessagesForRecipient(@PathVariable String recipient) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).get();
        List<Message> messages = messageRepository.findAllByRecipient(user.getEmail()).stream().filter(message -> !message.getDeleted()).toList();
        return ResponseEntity.ok(messages);
    }

    // Получить сообщения от конкретного отправителя
    @GetMapping("/sender/{sender}")
    public ResponseEntity<List<Message>> getMessagesFromSender(@PathVariable String sender) {
        User user = userRepository.findById(Long.valueOf(sender)).get();
        List<Message> messages = messageRepository.findAllBySender(user.getEmail()).stream().filter(message -> !message.getDeleted()).toList();
        return ResponseEntity.ok(messages);
    }

    // Удалить сообщение
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/deleted")
    public ResponseEntity<List<Message>> getDeletedMessages() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).get();
        List<Message> messages = messageRepository.findAllByDeletedTrueAndRecipientOrDeletedTrueAndSender(user.getEmail(), user.getEmail());
        return ResponseEntity.ok(messages);
    }

    // Получить сообщение по ID
    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<Message> searchChats(@RequestParam String query) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).get();
        String user_email = user.getEmail();
        return messageRepository.findChatsByMessageContentContaining(query, user_email);
    }
}
