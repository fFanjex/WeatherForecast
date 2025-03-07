package ru.ffanjex.weatherforecast.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import ru.ffanjex.weatherforecast.model.City;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.model.WeatherForecastResponse;
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.service.CityService;
import ru.ffanjex.weatherforecast.service.UserService;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class WeatherController {

    private final UserService userService;
    private final CityService cityService;

    @Value("${api.key}")
    private String apiKey;

    private final String language = "ru";

    public WeatherController(UserService userService, CityService cityService) {
        this.userService = userService;
        this.cityService = cityService;
    }

    @GetMapping("/view-weather")
    public String searchWeather(Model model) {
        return "weather";
    }

    @GetMapping("/weather")
    public String getWeather(@RequestParam("city") String city, Model model) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&lang=" + language + "&units=metric";
        RestTemplate restTemplate = new RestTemplate();
        WeatherResponse weatherResponse = restTemplate.getForObject(url, WeatherResponse.class);

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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return "redirect:/login";
        }

        User user = optionalUser.get();
        City savedCity = cityService.findByName(city);

        if (savedCity == null) {
            savedCity = new City();
            savedCity.setName(city);
            savedCity = cityService.save(savedCity);
        }

        user.setCity(savedCity);
        userService.save(user);

        return "redirect:/view-weather";
    }

    @GetMapping("/weather/saved/data")
    public String getSavedCityWeatherData(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isEmpty() || optionalUser.get().getCity() == null) {
            return "redirect:/home";
        }

        String cityName = optionalUser.get().getCity().getName();
        model.addAttribute("savedCity", cityName);

        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + cityName + "&appid=" + apiKey + "&lang=" + language + "&units=metric";
        RestTemplate restTemplate = new RestTemplate();
        WeatherResponse weatherResponse = restTemplate.getForObject(url, WeatherResponse.class);

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

    @GetMapping("/weather/forecast")
    public String getFiveDayForecast(@RequestParam("city") String city,
                                     @RequestParam(value = "page", defaultValue = "1") int page,
                                     Model model) {
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&lang=" + language + "&units=metric";
        RestTemplate restTemplate = new RestTemplate();
        WeatherForecastResponse weatherForecastResponse = restTemplate.getForObject(url, WeatherForecastResponse.class);

        if (weatherForecastResponse != null && weatherForecastResponse.getList() != null) {

            Map<String, List<WeatherForecastResponse.Forecast>> dailyForecasts = weatherForecastResponse.getList().stream()
                    .collect(Collectors.groupingBy(forecast -> forecast.getDtTxt().substring(0, 10)));

            List<Map.Entry<String, List<WeatherForecastResponse.Forecast>>> daysList = new ArrayList<>(dailyForecasts.entrySet());
            daysList.sort(Comparator.comparing(Map.Entry::getKey));

            int totalPages = daysList.size();

            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;

            Map.Entry<String, List<WeatherForecastResponse.Forecast>> currentDay = daysList.get(page - 1);

            model.addAttribute("city", city);
            model.addAttribute("currentDate", currentDay.getKey());
            model.addAttribute("forecastList", currentDay.getValue());
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
        } else {
            model.addAttribute("error", "Прогноз для города не найден.");
        }

        return "weather-forecast";
    }


}
