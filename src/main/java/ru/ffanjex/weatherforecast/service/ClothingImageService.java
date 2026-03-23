package ru.ffanjex.weatherforecast.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.dto.ClothingItemImageDto;
import ru.ffanjex.weatherforecast.dto.enums.Sex;

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
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClothingImageService {

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.image.model:gpt-image-1-mini}")
    private String imageModel;

    private static final String IMAGE_URL = "https://api.proxyapi.ru/openai/v1/images/generations";

    public List<ClothingItemImageDto> generateClothingImages(String shortClothingDescription, Sex sex) {
        List<String> clothingItems = extractClothingItems(shortClothingDescription);

        List<CompletableFuture<ClothingItemImageDto>> futures = clothingItems.stream()
                .map(item -> CompletableFuture.supplyAsync(() -> {
                    String prompt = buildItemImagePrompt(item, sex);
                    String image = generateSingleImage(prompt);
                    return new ClothingItemImageDto(item, prompt, image);
                }))
                .collect(Collectors.toList());

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    private List<String> extractClothingItems(String shortClothingDescription) {
        List<String> items = new ArrayList<>();

        if (shortClothingDescription == null || shortClothingDescription.isBlank()) {
            return items;
        }

        String[] parts = shortClothingDescription.split(",");
        for (String part : parts) {
            String item = part.trim();
            if (!item.isBlank()) {
                items.add(item);
            }
        }

        return items;
    }

    public String buildItemImagePrompt(String clothingItem, Sex sex) {
        String genderStyle = mapSexToPrompt(sex);

        return """
                Create a realistic catalog photo of a single %s clothing item.
                Item: %s.
                Requirements:
                - only this one item in the image
                - no person
                - plain clean background
                - realistic online store catalog style
                - highly detailed fabric and texture
                - centered composition
                - no text
                - no watermark
                - no collage
                """.formatted(genderStyle, clothingItem);
    }

    private String mapSexToPrompt(Sex sex) {
        if (sex == null) {
            return "adult";
        }
        return switch (sex) {
            case MALE -> "men's";
            case FEMALE -> "women's";
        };
    }

    private String generateSingleImage(String prompt) {
        try {
            JSONObject body = new JSONObject()
                    .put("model", imageModel)
                    .put("prompt", prompt)
                    .put("size", "1024x1024")
                    .put("quality", "low");

            HttpURLConnection connection = (HttpURLConnection) new URL(IMAGE_URL).openConnection();
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
                throw new RuntimeException("Ошибка генерации изображения: HTTP " + code + " — " + extractErrorMessage(responseText));
            }

            JSONObject json = new JSONObject(responseText);
            JSONArray data = json.optJSONArray("data");

            if (data == null || data.isEmpty()) {
                throw new RuntimeException("Пустой ответ от image model");
            }

            JSONObject first = data.getJSONObject(0);
            String b64 = first.optString("b64_json", "").trim();

            if (b64.isBlank()) {
                throw new RuntimeException("Модель не вернула b64_json");
            }

            return "data:image/png;base64," + b64;

        } catch (Exception e) {
            throw new RuntimeException("Не удалось сгенерировать изображение: " + e.getMessage(), e);
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
                String msg = err.optString("message", responseText);
                return msg.isBlank() ? responseText : msg;
            }
            return responseText;
        } catch (Exception ignore) {
            return responseText;
        }
    }
}