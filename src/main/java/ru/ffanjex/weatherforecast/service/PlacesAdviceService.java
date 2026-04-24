package ru.ffanjex.weatherforecast.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.dto.PlacesAdviceDto;
import ru.ffanjex.weatherforecast.dto.PlacesAdviceRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlacesAdviceService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.text.model:gpt-4o-mini}")
    private String textModel;

    private static final String CHAT_URL = "https://api.proxyapi.ru/openai/v1/chat/completions";

    public List<PlacesAdviceDto> generatePlaces(PlacesAdviceRequest request) {
        String prompt = buildPrompt(request);
        String content = sendPrompt(prompt);
        return parsePlaces(content);
    }

    private String buildPrompt(PlacesAdviceRequest request) {
        return """
Ты помощник для туристических рекомендаций.

Нужно предложить ТОП-5 мест, куда можно сходить погулять в городе с учётом текущей погоды.

Данные:
Город: %s
Страна: %s
Температура: %s °C
Влажность: %s %%
Скорость ветра: %s м/с
Описание погоды: %s

Верни строго JSON без markdown, без ```json и без пояснений.

Формат ответа:
{
  "places": [
    {
      "name": "Название места",
      "description": "Подробное описание места",
      "reason": "Подробное объяснение, почему туда стоит пойти"
    }
  ]
}

Требования:
- ровно 5 мест
- текст только на русском языке
- не добавляй лишние поля

- description:
  минимум 2-3 предложения
  подробно описывать атмосферу, особенности, чем можно заняться
  не менее 200 символов

- reason:
  минимум 2-3 предложения
  обязательно учитывать погоду (температура, ветер, описание)
  объяснять, почему именно сейчас туда лучше пойти
  не менее 200 символов

- если погода хорошая:
  советуй парки, набережные, площади, смотровые

- если плохая:
  музеи, галереи, торговые центры, крытые места
""".formatted(
                safe(request.getCity()),
                safe(request.getCountry()),
                safe(request.getTemperature()),
                safe(request.getHumidity()),
                safe(request.getWindSpeed()),
                safe(request.getWeatherDescription())
        );
    }

    private String sendPrompt(String prompt) {
        try {
            JSONObject body = new JSONObject()
                    .put("model", textModel)
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", prompt)
                            )
                    )
                    .put("temperature", 0.7);

            HttpURLConnection connection = (HttpURLConnection) new URL(CHAT_URL).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "Bearer " + apiKey);
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(body.toString().getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int code = connection.getResponseCode();
            String responseText = readResponse(code < 400 ? connection.getInputStream() : connection.getErrorStream());

            if (code >= 400) {
                throw new RuntimeException("Ошибка генерации мест: HTTP " + code + " — " + extractErrorMessage(responseText));
            }

            JSONObject json = new JSONObject(responseText);

            return json
                    .getJSONArray("choices")
                    .getJSONObject(0)
                    .getJSONObject("message")
                    .getString("content");

        } catch (Exception e) {
            throw new RuntimeException("Не удалось получить рекомендации мест: " + e.getMessage(), e);
        }
    }

    private List<PlacesAdviceDto> parsePlaces(String content) {
        try {
            String cleaned = content
                    .replace("```json", "")
                    .replace("```", "")
                    .trim();

            JSONObject json = new JSONObject(cleaned);
            JSONArray placesArray = json.getJSONArray("places");

            List<PlacesAdviceDto> places = new ArrayList<>();

            for (int i = 0; i < placesArray.length(); i++) {
                JSONObject item = placesArray.getJSONObject(i);

                places.add(new PlacesAdviceDto(
                        item.optString("name"),
                        item.optString("description"),
                        item.optString("reason")
                ));
            }

            return places;

        } catch (Exception e) {
            throw new RuntimeException("Не удалось разобрать JSON с местами: " + e.getMessage(), e);
        }
    }

    private String readResponse(InputStream is) throws IOException {
        if (is == null) {
            return "";
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            return sb.toString();
        }
    }

    private String extractErrorMessage(String responseText) {
        try {
            JSONObject json = new JSONObject(responseText);
            JSONObject err = json.optJSONObject("error");

            if (err != null) {
                String message = err.optString("message", responseText);
                return message.isBlank() ? responseText : message;
            }

            return responseText;
        } catch (Exception ignored) {
            return responseText;
        }
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "не указано" : value;
    }
}