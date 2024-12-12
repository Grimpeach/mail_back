package com.example.mail_client.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    private String username;
    private String passwordHash;
    private String email;

    @Column(name = "created_at")
    private String createdAt;
    private Boolean isOnline;
    private LocalDateTime lastSeen;

    @Column(nullable = false)
    private Boolean isActive = true; // Статус активности пользователя

    private LocalDateTime deactivationDate; // Дата деактивации
    public User(Long senderId) {
        this.userId = senderId;
    }
    private String profilePictureUrl;

}