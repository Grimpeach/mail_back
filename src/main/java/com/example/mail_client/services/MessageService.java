package com.example.mail_client.services;

import com.example.mail_client.entity.Message;
import com.example.mail_client.entity.User;
import com.example.mail_client.repositories.MessageRepository;
import com.example.mail_client.repositories.UserRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    // Отправить сообщение и записать событие в Kafka
    public Message sendMessage(Message message) {
        // 1. Синхронное сохранение в базу данных (PostgreSQL)
        Message savedMessage = messageRepository.save(message);

        // 2. Асинхронная отправка события в брокер сообщений (Kafka)
        try {
            String eventPayload = String.format("{\"event\": \"MESSAGE_SENT\", \"messageId\": %d, \"sender\": \"%s\", \"recipient\": \"%s\"}",
                    savedMessage.getId(), savedMessage.getSender(), savedMessage.getRecipient());

            // Отправляем сообщение в топик "mail_events"
            kafkaTemplate.send("mail_events", eventPayload);
        } catch (Exception e) {
            // Если Kafka недоступна, логируем ошибку, но не прерываем работу метода,
            // так как сообщение уже сохранено в базе
            System.err.println("Failed to send event to Kafka: " + e.getMessage());
        }

        return savedMessage;
    }

    // Получить все сообщения от конкретного отправителя
    public List<Message> getMessagesFromSender(String sender) {
        return messageRepository.findAllBySender(sender);
    }

    // Удалить сообщение по ID
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    // Найти сообщение по ID
    public Optional<Message> getMessageById(Long id) {
        return messageRepository.findById(id);
    }
}