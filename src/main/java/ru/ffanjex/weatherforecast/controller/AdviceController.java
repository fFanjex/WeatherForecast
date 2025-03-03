package ru.ffanjex.weatherforecast.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ffanjex.weatherforecast.service.AdviceService;

@Controller
@AllArgsConstructor
public class AdviceController {
    private final AdviceService adviceService;

    @GetMapping("/council-advice")
    public String getAdvice(@RequestParam("temperature") double temperature,
                            @RequestParam("humidity") double humidity,
                            @RequestParam("windSpeed") double windSpeed,
                            Model model) {
        String advice = adviceService.getClothingAdvice(temperature, humidity, windSpeed);
        model.addAttribute("advice", advice);
        return "council-advice";
    }
}
