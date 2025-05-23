package ru.ffanjex.weatherforecast.controller.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.service.UserService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
@AllArgsConstructor
public class MainRestController {
    private final UserService userService;

    @GetMapping("/home")
    public ResponseEntity<?> getUserData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> optionalUser = userService.findByUsername(username);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(401).body("User not authenticated");
        }

        User user = optionalUser.get();

        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("savedCity", user.getCity() != null ? user.getCity().getName() : "");

        return ResponseEntity.ok(response);
    }
}