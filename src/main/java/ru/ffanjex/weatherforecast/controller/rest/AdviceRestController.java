package ru.ffanjex.weatherforecast.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.service.AdviceService;

@RestController
@RequestMapping("/api/advice")
@RequiredArgsConstructor
public class AdviceRestController {

    private final AdviceService adviceService;

    @GetMapping("/generate")
    public ResponseEntity<String> generate(@RequestParam double temperature,
                                           @RequestParam double humidity,
                                           @RequestParam double windSpeed) {
        String result = adviceService.generateAdvice(temperature, humidity, windSpeed);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestParam String adviceText) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Advice advice = adviceService.saveAdvice(adviceText);
        boolean success = adviceService.attachAdviceToUser(username, advice);
        return success ? ResponseEntity.ok("Совет сохранён") : ResponseEntity.badRequest().body("Ошибка при сохранении");
    }
}
