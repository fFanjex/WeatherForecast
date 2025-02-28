package ru.ffanjex.weatherforecast.controller;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.service.CityService;
import ru.ffanjex.weatherforecast.service.UserService;

import java.util.Optional;

@Controller
public class WeatherController {
    @Autowired
    private final UserService userService;
    @Autowired
    private final CityService cityService;

    public WeatherController(UserService userService, CityService cityService) {
        this.userService = userService;
        this.cityService = cityService;
    }

    @Value("${api.key}")
    private String apiKey;

    private String language = "ru";

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
            String weatherIcon = "wi wi-owm-" + weatherResponse.getWeather().get(0).getId();
            model.addAttribute("weatherIcon", weatherIcon);
        } else {
            model.addAttribute("error", "Город не найден.");
        }

        return "weather-statistics";
    }

    @PostMapping("save-city")
    public String saveCity(@RequestParam("city") String city, Model model) {
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
}
