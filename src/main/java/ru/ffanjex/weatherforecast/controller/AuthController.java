package ru.ffanjex.weatherforecast.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ffanjex.weatherforecast.service.UserService;

@Controller
@AllArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/login")
    public String getLoginPage() {
        return "login";
    }

    @GetMapping("/register")
    public String getRegisterPage() {
        return "register";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password, Model model) {
        var user = userService.findByUsername(username);

        if (user.isPresent() && user.get().getPassword().equals(password)) {
            return "redirect:/";
        }

        model.addAttribute("error", "Неверное имя пользователя или пароль");
        return "login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String email,
                           Model model) {
        try {
            userService.registerUser(username, email, password);
            return "redirect:/login";
        } catch (IllegalAccessException e) {
            model.addAttribute("error", e.getMessage());
        }

        return "register";
    }
}
