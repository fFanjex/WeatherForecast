package ru.ffanjex.weatherforecast.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.service.AdviceService;
import ru.ffanjex.weatherforecast.service.WeatherService;

import java.security.Principal;
import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AdviceController {

    private final AdviceService adviceService;
    private final WeatherService weatherService;

    @GetMapping("/council-advice")
    public String showAdvicePage(@RequestParam String city, Model model) {
        WeatherResponse wr = weatherService.getWeather(city);
        AdviceService.WeatherContext ctx = adviceService.fromWeatherResponse(wr);

        model.addAttribute("city", city);
        model.addAttribute("advice", adviceService.generateAdvice(ctx));
        model.addAttribute("weather", wr);

        return "council-advice";
    }

    @GetMapping("/clothing-advice")
    public String showUserAdvices(Model model, Principal principal) {
        List<ru.ffanjex.weatherforecast.model.Advice> advices =
                adviceService.getUserAdvices(principal.getName());

        advices.sort(Comparator.comparing(
                ru.ffanjex.weatherforecast.model.Advice::getCreatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())
        ));

        model.addAttribute("adviceList", advices);
        return "clothing-advice";
    }
}