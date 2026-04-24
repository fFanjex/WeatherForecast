package ru.ffanjex.weatherforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PlacesAdviceController {

    @GetMapping("/places-advice")
    public String getPlacesAdvicePage(
            @RequestParam String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) String temperature,
            @RequestParam(required = false) String humidity,
            @RequestParam(required = false) String windSpeed,
            @RequestParam(required = false) String weatherDescription,
            Model model
    ) {
        model.addAttribute("city", city);
        model.addAttribute("country", country);
        model.addAttribute("temperature", temperature);
        model.addAttribute("humidity", humidity);
        model.addAttribute("windSpeed", windSpeed);
        model.addAttribute("weatherDescription", weatherDescription);

        return "places-advice";
    }
}