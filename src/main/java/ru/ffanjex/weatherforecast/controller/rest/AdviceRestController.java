package ru.ffanjex.weatherforecast.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.service.AdviceService;
import ru.ffanjex.weatherforecast.service.WeatherService;

@RestController
@RequestMapping("/api/advice")
@RequiredArgsConstructor
public class AdviceRestController {

    private final AdviceService adviceService;
    private final WeatherService weatherService;

    @GetMapping("/generate")
    public ResponseEntity<String> generate(@RequestParam String city) {
        WeatherResponse wr = weatherService.getWeather(city);
        AdviceService.WeatherContext ctx = adviceService.fromWeatherResponse(wr);
        return ResponseEntity.ok(adviceService.generateAdvice(ctx));
    }

    @PostMapping("/save")
    public ResponseEntity<String> save(@RequestParam String adviceText) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Advice advice = adviceService.saveAdvice(adviceText);
        boolean success = adviceService.attachAdviceToUser(username, advice);
        return success
                ? ResponseEntity.ok("Совет сохранён")
                : ResponseEntity.badRequest().body("Ошибка при сохранении");
    }
}