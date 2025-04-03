package ru.ffanjex.weatherforecast.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.ffanjex.weatherforecast.model.WeatherForecastResponse;
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.service.WeatherService;

import java.util.List;
import java.util.Map;

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
        WeatherResponse weatherResponse = weatherService.getWeather(city);

        if (weatherResponse != null) {
            model.addAttribute("city", weatherResponse.getName());
            model.addAttribute("country", weatherResponse.getSys().getCountry());
            model.addAttribute("weatherDescription", weatherResponse.getWeather().get(0).getDescription());
            model.addAttribute("temperature", weatherResponse.getMain().getTemp());
            model.addAttribute("humidity", weatherResponse.getMain().getHumidity());
            model.addAttribute("windSpeed", weatherResponse.getWind().getSpeed());
            model.addAttribute("weatherIcon", "wi wi-owm-" + weatherResponse.getWeather().get(0).getId());
        } else {
            model.addAttribute("error", "Город не найден.");
        }

        return "weather-statistics";
    }

    @PostMapping("/save-city")
    public String saveCity(@RequestParam("city") String city) {
        weatherService.saveCityForCurrentUser(city);
        return "redirect:/view-weather";
    }

    @GetMapping("/weather/saved/data")
    public String getSavedCityWeatherData(Model model) {
        weatherService.getSavedCityForCurrentUser().ifPresentOrElse(city -> {
            model.addAttribute("savedCity", city);
            WeatherResponse weatherResponse = weatherService.getWeather(city);

            if (weatherResponse != null) {
                model.addAttribute("city", weatherResponse.getName());
                model.addAttribute("country", weatherResponse.getSys().getCountry());
                model.addAttribute("weatherDescription", weatherResponse.getWeather().get(0).getDescription());
                model.addAttribute("temperature", weatherResponse.getMain().getTemp());
                model.addAttribute("humidity", weatherResponse.getMain().getHumidity());
                model.addAttribute("windSpeed", weatherResponse.getWind().getSpeed());
                model.addAttribute("weatherIcon", "wi wi-owm-" + weatherResponse.getWeather().get(0).getId());
            } else {
                model.addAttribute("error", "Город не найден.");
            }
        }, () -> model.addAttribute("error", "Город не найден."));

        return "weather-statistics";
    }

    @GetMapping("/weather/forecast")
    public String getFiveDayForecast(@RequestParam("city") String city,
                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                     Model model) {
        WeatherForecastResponse forecastResponse = weatherService.getFiveDayForecast(city);
        Map.Entry<String, List<WeatherForecastResponse.Forecast>> currentDay = weatherService.getForecastForPage(forecastResponse, page);

        if (currentDay != null) {
            model.addAttribute("city", city);
            model.addAttribute("currentDate", currentDay.getKey());
            model.addAttribute("forecastList", currentDay.getValue());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", forecastResponse.getList().size() / 8);
        } else {
            model.addAttribute("error", "Прогноз для города не найден.");
        }

        return "weather-forecast";
    }
}