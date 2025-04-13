package org.example.project.util;

import org.example.project.model.User;
import org.example.project.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    private UserRepository userRepository;

    public String loggedInEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user =userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getEmail();
    }

    public Long loggedInUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user =userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getUserId();

    }

    public User loggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user =userRepository.findByUserName(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user;

    }





}
