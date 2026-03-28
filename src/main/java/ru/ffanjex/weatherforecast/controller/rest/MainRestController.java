package ru.ffanjex.weatherforecast.controller.rest;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
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
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !"anonymousUser".equals(authentication.getName());
        Map<String, Object> response = new HashMap<>();
        if (!authenticated) {
            response.put("username", "Гость");
            response.put("savedCity", "");
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
        String email = authentication.getName();
        Optional<User> optionalUser = userService.findByEmail(email);
        if (optionalUser.isEmpty()) {
            response.put("username", "Гость");
            response.put("savedCity", "");
            response.put("authenticated", false);
            return ResponseEntity.ok(response);
        }
        User user = optionalUser.get();
        response.put("username", user.getUsername());
        response.put("savedCity", user.getCity() != null ? user.getCity().getName() : "");
        response.put("authenticated", true);

        return ResponseEntity.ok(response);
    }
}