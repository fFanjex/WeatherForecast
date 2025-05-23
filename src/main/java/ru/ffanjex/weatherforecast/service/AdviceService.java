package ru.ffanjex.weatherforecast.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.repository.AdviceRepository;
import ru.ffanjex.weatherforecast.repository.UserRepository;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdviceService {

    private final AdviceRepository adviceRepository;
    private final UserRepository userRepository;

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.proxyapi.ru/openai/v1/chat/completions";

    public String generateAdvice(double temperature, double humidity, double windSpeed) {
        try {
            String prompt = String.format("Дай совет по одежде при %.1f°C, %.1f%% влажности и ветре %.1f м/с. Уложись в 250 символов.",
                    temperature, humidity, windSpeed);

            JSONObject body = new JSONObject()
                    .put("model", "gpt-3.5-turbo")
                    .put("messages", new JSONArray()
                            .put(new JSONObject().put("role", "user").put("content", prompt)))
                    .put("max_tokens", 250)
                    .put("temperature", 0.7);

            HttpURLConnection connection = (HttpURLConnection) new URL(OPENAI_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.toString().getBytes());
                os.flush();
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) response.append(line);

                JSONArray choices = new JSONObject(response.toString()).getJSONArray("choices");
                return choices.getJSONObject(0).getJSONObject("message").getString("content");
            }

        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    public Advice saveAdvice(String adviceText) {
        Advice advice = new Advice();
        advice.setCouncil(adviceText);
        return adviceRepository.save(advice);
    }

    public boolean attachAdviceToUser(String username, Advice advice) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return false;

        User user = userOpt.get();
        user.getAdviceList().add(advice);
        userRepository.save(user);
        return true;
    }

    public List<Advice> getUserAdvices(String username) {
        return userRepository.findByUsername(username)
                .map(User::getAdviceList)
                .map(ArrayList::new)
                .orElseGet(ArrayList::new);
    }
}