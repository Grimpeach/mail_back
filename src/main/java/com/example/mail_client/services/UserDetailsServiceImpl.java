package com.example.mail_client.services;

import com.example.mail_client.entity.User;
import com.example.mail_client.repositories.UserRepository;
import com.example.mail_client.utils.CustomUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));



        return new CustomUser(user.getUsername(), user.getPasswordHash(), user.getEmail(), new ArrayList<>(), user.getUserId());
    }

}