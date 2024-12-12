package com.example.mail_client.services;

import com.example.mail_client.dto.UserRegistrationDto;
import com.example.mail_client.entity.User;
import com.example.mail_client.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private StaticFileService staticFileService;


    public User registerUser(UserRegistrationDto registrationDto) {
        User newUser = new User();
        newUser.setUsername(registrationDto.getUsername());
        newUser.setPasswordHash(passwordEncoder.encode(registrationDto.getPasswordHash()));
        newUser.setEmail(registrationDto.getEmail());
        User savedUser = userRepository.save(newUser);
        return savedUser;
    }


    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException("The password you entered was incorrect.");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void changeUsername(Long userId, String newUsername) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRepository.existsByUsername(newUsername)) {
            throw new IllegalArgumentException("Username already taken");
        }

        user.setUsername(newUsername);
        userRepository.save(user);
    }

    @Transactional
    public void changeEmail(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already in use");
        }

        user.setEmail(newEmail);
        userRepository.save(user);
    }

    @Transactional
    public void deactivateUser(Long userId, String token) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.getIsActive()) {
            throw new IllegalStateException("User is already deactivated");
        }

        user.setIsActive(false);
        user.setDeactivationDate(LocalDateTime.now());


        userRepository.save(user);
    }

    @Transactional
    public void restoreUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getIsActive()) {
            throw new IllegalStateException("User is already active");
        }

        if (user.getDeactivationDate() != null && user.getDeactivationDate().plusDays(30).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("The restoration period has expired");
        }

        user.setIsActive(true);
        user.setDeactivationDate(null); // Очистить дату деактивации
        userRepository.save(user);
    }

    public String uploadProfileImage(MultipartFile file, Long userId, String element) throws IOException {
        // Проверка, что файл не пустой
        if (file.isEmpty()) {
            throw new IOException("Файл пуст");
        }

        // Генерация имени файла с текущим временем
        String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        // Создание нового MultipartFile с обновлённым именем
        MultipartFile renamedFile = new MockMultipartFile(
                filename,
                filename,
                file.getContentType(),
                file.getInputStream()
        );

        // Загрузка файла через StaticFileService
        String fileUrl = staticFileService.uploadFile(renamedFile);

        // Обновление профиля пользователя в базе данных
        Optional<User> userOptional = userRepository.findById(userId);
        userOptional.ifPresent(user -> {
            user.setProfilePictureUrl(fileUrl);
            userRepository.save(user);
        });

        // Возвращение URL файла
        return fileUrl;
    }


}
