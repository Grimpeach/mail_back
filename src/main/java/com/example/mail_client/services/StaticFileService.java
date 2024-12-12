package com.example.mail_client.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class StaticFileService {

    @Value("${STATIC_UPLOAD_DIR:src/main/resources/static/uploads}")
    private String uploadDir; // Директория для загрузки файлов

    @Value("${SERVER_BASE_URL:http://localhost:8080}")
    private String serverBaseUrl; // Базовый URL сервера

    /**
     * Загружает файл в статическую папку сервера и возвращает URL.
     *
     * @param file Загружаемый файл
     * @return URL загруженного файла
     */
    public String uploadFile(MultipartFile file) {
        try {
            // Создаём папку для загрузки, если её нет
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Сохраняем файл
            String filename = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath.toFile());

            // Возвращаем URL для доступа к файлу
            return serverBaseUrl + "/uploads/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при сохранении файла: " + e.getMessage(), e);
        }
    }
}
