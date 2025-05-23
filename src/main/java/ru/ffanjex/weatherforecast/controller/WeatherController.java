package ru.ffanjex.weatherforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.ffanjex.weatherforecast.service.WeatherService;

@Controller
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/view-weather")
    public String viewWeatherPage() {
        return "weather";
    }

    @GetMapping("/weather")
    public String showWeather(@RequestParam String city, Model model) {
        model.addAllAttributes(weatherService.getWeatherModelAttributes(city));
        return "weather-statistics";
    }

    @PostMapping("/save-city")
    public String saveCityAndRedirect(@RequestParam String city) {
        weatherService.saveCityForCurrentUser(city);
        return "redirect:/view-weather";
    }

    @GetMapping("/weather/saved/data")
    public String showSavedCityWeather(Model model) {
        model.addAllAttributes(weatherService.getSavedCityWeatherModelAttributes());
        return "weather-statistics";
    }

    @GetMapping("/weather/forecast")
    public String showForecast(@RequestParam String city,
                               @RequestParam(defaultValue = "1") int page,
                               Model model) {
        model.addAllAttributes(weatherService.getForecastModelAttributes(city, page));
        return "weather-forecast";
    }
}