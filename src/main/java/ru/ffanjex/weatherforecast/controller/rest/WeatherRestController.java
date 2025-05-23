package ru.ffanjex.weatherforecast.controller.rest;

import org.springframework.web.bind.annotation.*;
import ru.ffanjex.weatherforecast.model.WeatherForecastResponse;
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.service.WeatherService;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/weather")
public class WeatherRestController {

    private final WeatherService weatherService;

    public WeatherRestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public Optional<WeatherResponse> getCurrentWeather(@RequestParam String city) {
        return weatherService.getWeatherOpt(city);
    }

    @GetMapping("/forecast")
    public WeatherForecastResponse getForecast(@RequestParam String city) {
        return weatherService.getFiveDayForecast(city);
    }

    @PostMapping("/save-city")
    public void saveCity(@RequestParam String city) {
        weatherService.saveCityForCurrentUser(city);
    }

    @GetMapping("/saved")
    public Map<String, Object> getSavedWeather() {
        return weatherService.getSavedCityWeatherModelAttributes();
    }
}
