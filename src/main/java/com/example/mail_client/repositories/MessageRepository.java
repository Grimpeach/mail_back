package com.example.mail_client.repositories;

import com.example.mail_client.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Найти все сообщения для получателя
    List<Message> findAllByRecipient(String recipient);

    // Найти все сообщения, отправленные пользователем
    List<Message> findAllBySender(String sender);

    List<Message> findAllByDeletedTrueAndRecipientOrDeletedTrueAndSender(String recipient, String sender);


    @Query("SELECT DISTINCT m FROM Message m WHERE (m.recipient = :user_email or m.sender = :user_email) AND (LOWER(m.subject) LIKE LOWER(CONCAT('%', :substring, '%')) or LOWER(m.body) LIKE LOWER(CONCAT('%', :substring, '%')) or LOWER(m.sender) LIKE LOWER(CONCAT('%', :substring, '%')) or LOWER(m.recipient) LIKE LOWER(CONCAT('%', :substring, '%')))")
    List<Message> findChatsByMessageContentContaining(@Param("substring") String substring, @Param("user_email") String user_email);
}
