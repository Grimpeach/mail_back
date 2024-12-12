package com.example.mail_client.services;

import com.example.mail_client.entity.Message;
import com.example.mail_client.entity.User;
import com.example.mail_client.repositories.MessageRepository;
import com.example.mail_client.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    // Отправить сообщение
    public Message sendMessage(Message message) {
        return messageRepository.save(message);
    }

    // Получить все сообщения для конкретного получателя


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
