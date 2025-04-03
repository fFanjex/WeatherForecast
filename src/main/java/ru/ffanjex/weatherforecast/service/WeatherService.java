package ru.ffanjex.weatherforecast.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.ffanjex.weatherforecast.model.City;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.model.WeatherForecastResponse;
import ru.ffanjex.weatherforecast.model.WeatherResponse;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class WeatherService {
    private final UserService userService;
    private final CityService cityService;
    private final RestTemplate restTemplate;

    @Value("${api.key}")
    private String apiKey;

    private final String language = "ru";

    public WeatherService(UserService userService, CityService cityService) {
        this.userService = userService;
        this.cityService = cityService;
        this.restTemplate = new RestTemplate();
    }

    public WeatherResponse getWeather(String city) {
        String url = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + apiKey + "&lang=" + language + "&units=metric";
        return restTemplate.getForObject(url, WeatherResponse.class);
    }

    public WeatherForecastResponse getFiveDayForecast(String city) {
        String url = "http://api.openweathermap.org/data/2.5/forecast?q=" + city + "&appid=" + apiKey + "&lang=" + language + "&units=metric";
        return restTemplate.getForObject(url, WeatherForecastResponse.class);
    }

    public Optional<String> getSavedCityForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findByUsername(username)
                .map(User::getCity)
                .map(City::getName);
    }

    public void saveCityForCurrentUser(String city) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<User> optionalUser = userService.findByUsername(username);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            City savedCity = cityService.findByName(city);

            if (savedCity == null) {
                savedCity = new City();
                savedCity.setName(city);
                savedCity = cityService.save(savedCity);
            }

            user.setCity(savedCity);
            userService.save(user);
        }
    }

    public Map.Entry<String, List<WeatherForecastResponse.Forecast>> getForecastForPage(WeatherForecastResponse forecastResponse, int page) {
        if (forecastResponse == null || forecastResponse.getList() == null) {
            return null;
        }

        Map<String, List<WeatherForecastResponse.Forecast>> dailyForecasts = forecastResponse.getList().stream()
                .collect(Collectors.groupingBy(forecast -> forecast.getDtTxt().substring(0, 10)));

        List<Map.Entry<String, List<WeatherForecastResponse.Forecast>>> daysList = new ArrayList<>(dailyForecasts.entrySet());
        daysList.sort(Comparator.comparing(Map.Entry::getKey));

        int totalPages = daysList.size();
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        return daysList.get(page - 1);
    }
}
