package ru.ffanjex.weatherforecast.controller.rest;

import lombok.RequiredArgsConstructor;
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

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
public class LocationRestController {

    @GetMapping
    public ResponseEntity<?> reverseGeocode(@RequestParam double lat, @RequestParam double lon) {
        try {
            String urlStr = String.format(
                    "https://nominatim.openstreetmap.org/reverse?format=json&lat=%f&lon=%f",
                    lat, lon
            );
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
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
            String city = null;
            if (address != null) {
                city = address.optString("city",
                        address.optString("town",
                                address.optString("village", "Неизвестно")));
            }
            return ResponseEntity.ok(new JSONObject()
                    .put("city", city)
                    .toString());
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("Ошибка определения города: " + e.getMessage());
        }
    }
}
