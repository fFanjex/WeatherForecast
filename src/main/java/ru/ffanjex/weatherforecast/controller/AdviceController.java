package ru.ffanjex.weatherforecast.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ffanjex.weatherforecast.service.AdviceService;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdviceController {

    private final AdviceService adviceService;

    @GetMapping("/council-advice")
    public String showAdvicePage(@RequestParam double temperature,
                                 @RequestParam double humidity,
                                 @RequestParam double windSpeed,
                                 Model model) {
        String advice = adviceService.generateAdvice(temperature, humidity, windSpeed);
        model.addAttribute("advice", advice);
        return "council-advice";
    }

    @GetMapping("/clothing-advice")
    public String showUserAdvices(Model model, Principal principal) {
        List advices = adviceService.getUserAdvices(principal.getName());
        advices.sort(Comparator.comparing(a -> ((ru.ffanjex.weatherforecast.model.Advice) a).getCreatedAt(), Comparator.reverseOrder()));
        model.addAttribute("adviceList", advices);
        return "clothing-advice";
    }
}