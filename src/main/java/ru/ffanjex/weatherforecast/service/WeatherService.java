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

    public Optional<WeatherResponse> getWeatherOpt(String city) {
        try {
            return Optional.ofNullable(getWeather(city));
        } catch (Exception e) {
            return Optional.empty();
        }
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

    public Map<String, Object> getWeatherModelAttributes(String city) {
        Map<String, Object> attributes = new HashMap<>();

        Optional<WeatherResponse> weatherOpt = getWeatherOpt(city);
        if (weatherOpt.isPresent()) {
            WeatherResponse weatherResponse = weatherOpt.get();
            attributes.put("city", weatherResponse.getName());
            attributes.put("country", weatherResponse.getSys().getCountry());
            attributes.put("weatherDescription", weatherResponse.getWeather().get(0).getDescription());
            attributes.put("temperature", weatherResponse.getMain().getTemp());
            attributes.put("humidity", weatherResponse.getMain().getHumidity());
            attributes.put("windSpeed", weatherResponse.getWind().getSpeed());
            attributes.put("weatherIcon", "wi wi-owm-" + weatherResponse.getWeather().get(0).getId());
        } else {
            attributes.put("error", "Город не найден.");
        }

        return attributes;
    }

    public Map<String, Object> getSavedCityWeatherModelAttributes() {
        Map<String, Object> attributes = new HashMap<>();

        getSavedCityForCurrentUser().ifPresentOrElse(city -> {
            attributes.put("savedCity", city);
            attributes.putAll(getWeatherModelAttributes(city));
        }, () -> attributes.put("error", "Город не найден."));

        return attributes;
    }

    public Map<String, Object> getForecastModelAttributes(String city, int page) {
        Map<String, Object> attributes = new HashMap<>();

        WeatherForecastResponse forecastResponse = getFiveDayForecast(city);
        Map.Entry<String, List<WeatherForecastResponse.Forecast>> currentDay = getForecastForPage(forecastResponse, page);

        if (currentDay != null) {
            attributes.put("city", city);
            attributes.put("currentDate", currentDay.getKey());
            attributes.put("forecastList", currentDay.getValue());
            attributes.put("currentPage", page);
            attributes.put("totalPages", forecastResponse.getList().size() / 8);
        } else {
            attributes.put("error", "Прогноз для города не найден.");
        }

        return attributes;
    }
}