package ru.ffanjex.weatherforecast.controller.rest;

import lombok.AllArgsConstructor;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/location")
public class LocationRestController {


    @GetMapping
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        try {
            String urlStr = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f",
                    lat, lon
            );

            HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "WeatherForecastApp");
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            JSONObject json = new JSONObject(response.toString());
            JSONObject address = json.optJSONObject("address");

            String city = "Неизвестно";
            if (address != null) {
                city = address.optString("city",
                        address.optString("town",
                                address.optString("village", "Неизвестно")));
            }

            // ВАЖНО: возвращаем нормальный JSON-объект, а не строку
            return ResponseEntity.ok(Map.of("city", city));

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ошибка определения города: " + e.getMessage()));
        }
    }

}