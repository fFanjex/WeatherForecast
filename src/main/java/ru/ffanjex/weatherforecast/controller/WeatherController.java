package ru.ffanjex.weatherforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ffanjex.weatherforecast.service.WeatherService;

@Controller
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/view-weather")
    public String searchWeather(Model model) {
        return "weather";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam("city") String city, Model model) {
        model.addAllAttributes(weatherService.getWeatherModelAttributes(city));
        return "weather-statistics";
    }

    @PostMapping("/save-city")
    public String saveCity(@RequestParam("city") String city) {
        weatherService.saveCityForCurrentUser(city);
        return "redirect:/view-weather";
    }

    @GetMapping("/weather/saved/data")
    public String getSavedCityWeatherData(Model model) {
        model.addAllAttributes(weatherService.getSavedCityWeatherModelAttributes());
        return "weather-statistics";
    }

    @GetMapping("/weather/forecast")
    public String getFiveDayForecast(@RequestParam("city") String city,
                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                     Model model) {
        model.addAllAttributes(weatherService.getForecastModelAttributes(city, page));
        return "weather-forecast";
    }
}