package com.example.mail_client.controllers;

import com.example.mail_client.dto.LoginDto;
import com.example.mail_client.dto.UserRegistrationDto;
import com.example.mail_client.entity.User;
import com.example.mail_client.repositories.UserRepository;
import com.example.mail_client.services.JwtTokenService;
import com.example.mail_client.services.UserService;
import com.example.mail_client.utils.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "${FRONTEND_URL}")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenService jwtTokenService;
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody UserRegistrationDto registrationDto) {
        User registeredUser = userService.registerUser(registrationDto);
        String token = jwtTokenService.createToken(registeredUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("user", registeredUser);
        response.put("token", token);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }


    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginDto loginDto) {
        Authentication authentication = authenticateUser(loginDto.getUsername(), loginDto.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenService.createToken(loginDto.getUsername());
        CustomUser loggedInUser = (CustomUser) authentication.getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("user", loggedInUser);
        response.put("token", token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(Principal principal) {
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user != null) {
            user.setIsOnline(false);
            user.setLastSeen(LocalDateTime.now());
            userRepository.save(user);
            return ResponseEntity.ok("successful logout");
        }
        return ResponseEntity.ok("user not found");
    }

    @PostMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestBody String token) {
        System.out.println("token=" + token);
        try {
            if (jwtTokenService.validateToken(token))
                return ResponseEntity.ok().build();
            else
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    private Authentication authenticateUser(String username, String password) {
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }


    @PutMapping("/{userId}/change-password")
    public ResponseEntity<String> changePassword(@PathVariable Long userId,
                                                 @RequestParam String oldPassword,
                                                 @RequestParam String newPassword) {
        userService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok("Password updated successfully");
    }

    @PutMapping("/{userId}/change-username")
    public ResponseEntity<String> changeUsername(@PathVariable Long userId,
                                                 @RequestParam String newUsername) {
        userService.changeUsername(userId, newUsername);
        return ResponseEntity.ok("Username updated successfully");
    }

    @PutMapping("/{userId}/change-email")
    public ResponseEntity<String> changeEmail(@PathVariable Long userId,
                                              @RequestParam String newEmail) {
        userService.changeEmail(userId, newEmail);
        return ResponseEntity.ok("Email updated successfully");
    }

    @PutMapping("/{userId}/deactivate")
    public ResponseEntity<String> deactivateUser(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authorizationHeader // Получаем токен из заголовка
    ) {
        // Извлекаем токен из заголовка
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().body("Invalid or missing Authorization header");
        }

        String token = authorizationHeader.substring(7); // Убираем "Bearer "

        userService.deactivateUser(userId, token);

        return ResponseEntity.ok("User deactivated successfully");
    }

    @PutMapping("/{userId}/restore")
    public ResponseEntity<String> restoreUser(@PathVariable Long userId) {
        userService.restoreUser(userId);
        return ResponseEntity.ok("User restored successfully");
    }


    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUserProfile() {
        System.out.println("get mapping starts");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> user = userRepository.findByUsername(username);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @PostMapping("/upload-icon/{userId}")
    public ResponseEntity<?> uploadProfileIcon(@PathVariable Long userId, @RequestParam("image") MultipartFile file) {
        try {
            String imageUrl = userService.uploadProfileImage(file, userId, "icon");
            return ResponseEntity.ok().body(imageUrl);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка при загрузке изображения: " + e.getMessage());
        }
    }

    @GetMapping("/other/{userId}")
    public ResponseEntity<User> getUserProfile(@PathVariable Long userId) {
        Optional<User> user = userRepository.findById(userId);

        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
