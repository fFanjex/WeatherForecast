package ru.ffanjex.weatherforecast.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.service.AdviceService;
import ru.ffanjex.weatherforecast.service.UserService;

import java.util.*;

@Controller
@AllArgsConstructor
public class AdviceController {
    private final AdviceService adviceService;
    private final UserService userService;

    @GetMapping("/council-advice")
    public String getAdvice(@RequestParam("temperature") double temperature,
                            @RequestParam("humidity") double humidity,
                            @RequestParam("windSpeed") double windSpeed,
                            Model model) {
        String advice = adviceService.getClothingAdvice(temperature, humidity, windSpeed);
        model.addAttribute("advice", advice);
        return "council-advice";
    }

    @PostMapping("/save-advice")
    @ResponseBody
    public ResponseEntity<String> saveUserAdvice(@RequestParam("adviceText") String adviceText) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Optional<User> userOptional = userService.findByUsername(authentication.getName());

        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().body("Ошибка: Пользователь не найден.");
        }

        User user = userOptional.get();
        Advice advice = adviceService.saveAdvice(adviceText);
        userService.addAdviceToUser(user.getId(), advice.getId());

        return ResponseEntity.ok("Совет сохранен!");
    }

    @GetMapping("/clothing-advice")
    public String viewUserAdvice(Model model) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String username;

        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }

        User user = userService.findByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("adviceList", List.of());
            return "clothing-advice";
        }

        Set<Advice> adviceSet = user.getAdviceList();
        List<Advice> adviceList = new ArrayList<>(adviceSet);
        adviceList.sort(Comparator.comparing(Advice::getCreatedAt).reversed());
        model.addAttribute("adviceList", adviceList);

        return "clothing-advice";
    }
}
