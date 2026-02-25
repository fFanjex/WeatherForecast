package ru.ffanjex.weatherforecast.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.repository.AdviceRepository;
import ru.ffanjex.weatherforecast.repository.UserRepository;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdviceService {

    private final AdviceRepository adviceRepository;
    private final UserRepository userRepository;

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.proxyapi.ru/openai/v1/chat/completions";

    public record WeatherContext(
            double temp,
            double feelsLike,
            double humidity,
            double windSpeed,
            Double windGust,
            String description,
            Integer weatherId,
            Double rain1h,
            Double snow1h,
            Integer visibilityMeters
    ) {}

    public String generateAdvice(WeatherContext w) {
        try {
            String prompt = buildPrompt(w);

            JSONObject body = new JSONObject()
                    .put("model", "gpt-3.5-turbo")
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "system")
                                    .put("content",
                                            "Отвечай по-русски. Дай максимально практичный совет по одежде. " +
                                                    "Без воды, без общих фраз. Соблюдай формат. Не упоминай, что ты ИИ."))
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", prompt)))
                    .put("max_tokens", 800)
                    .put("temperature", 0.4);

            HttpURLConnection connection = (HttpURLConnection) new URL(OPENAI_URL).openConnection();
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
                return "Ошибка OpenAI: HTTP " + code + " — " + extractErrorMessage(responseText);
            }

            JSONObject json = new JSONObject(responseText);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return "Ошибка: пустой ответ от модели.";
            }

            String content = choices.getJSONObject(0)
                    .getJSONObject("message")
                    .optString("content", "")
                    .trim();

            if (content.isEmpty()) return "Ошибка: модель вернула пустой текст.";
            return content;

        } catch (Exception e) {
            return "Ошибка: " + e.getMessage();
        }
    }

    public String generateAdvice(double temperature, double humidity, double windSpeed) {
        WeatherContext ctx = new WeatherContext(
                temperature,
                temperature,
                humidity,
                windSpeed,
                null,
                null,
                null,
                null,
                null,
                null
        );
        return generateAdvice(ctx);
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

    private String buildPrompt(WeatherContext w) {
        String desc = (w.description == null || w.description.isBlank()) ? "нет данных" : w.description;
        String wid = (w.weatherId == null) ? "нет" : String.valueOf(w.weatherId);

        String gust = (w.windGust == null) ? "нет данных" : String.format(Locale.US, "%.1f", w.windGust);
        String rain = (w.rain1h == null) ? "0" : String.format(Locale.US, "%.2f", w.rain1h);
        String snow = (w.snow1h == null) ? "0" : String.format(Locale.US, "%.2f", w.snow1h);
        String vis  = (w.visibilityMeters == null) ? "нет данных" : (w.visibilityMeters + " м");

        return String.format(Locale.US,
                "Ты опытный стилист и метеоконсультант. Дай подробный, максимально практичный совет по одежде для улицы.\n" +
                        "Данные:\n" +
                        "Температура %.1f°C (ощущается %.1f°C), влажность %.0f%%, ветер %.1f м/с (порывы %s), осадки за 1ч: дождь %s мм, снег %s мм, видимость %s, погода: %s (код %s).\n\n" +
                        "Требования:\n" +
                        "1) Пиши конкретно: материалы и слои (например: термобельё/флис/пуховик/мембрана), без общих фраз.\n" +
                        "2) Учитывай «ощущается как», порывы и осадки: если осадки >0, добавь непромокаемость/капюшон/зонт и подходящую обувь.\n" +
                        "3) Если ветер >=6 м/с или порывы >=9 м/с, добавь защиту шеи/лица.\n" +
                        "4) Если влажность >=85%% при минусе, учти сырость и предложи слой, который не намокает.\n" +
                        "5) Дай 2 варианта: «Если прогулка 15–30 минут» и «Если на улице 1–2 часа».\n\n" +
                        "Формат строго:\n" +
                        "Коротко:\n" +
                        "- Прогулка 15–30 мин: ...\n" +
                        "- На 1–2 часа: ...\n" +
                        "Детально по пунктам:\n" +
                        "Верх: ...\n" +
                        "Низ: ...\n" +
                        "Обувь: ...\n" +
                        "Аксессуары: ...\n" +
                        "Чего избегать: ...\n" +
                        "Почему: 1–2 фразы.\n" +
                        "Объём: 600–900 символов.",
                w.temp, w.feelsLike, w.humidity, w.windSpeed, gust, rain, snow, vis, desc, wid
        );
    }

    private String readResponse(InputStream is) throws IOException {
        if (is == null) return "";
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
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

    public WeatherContext fromWeatherResponse(WeatherResponse wr) {
        if (wr == null || wr.getMain() == null || wr.getWind() == null) {
            return new WeatherContext(0, 0, 0, 0, null, null, null, null, null, null);
        }

        String description = null;
        Integer weatherId = null;
        if (wr.getWeather() != null && !wr.getWeather().isEmpty() && wr.getWeather().get(0) != null) {
            description = wr.getWeather().get(0).getDescription();
            weatherId = wr.getWeather().get(0).getId();
        }

        Double rain1h = null;
        Double snow1h = null;
        if (wr.getRain() != null) rain1h = wr.getRain().get1h();
        if (wr.getSnow() != null) snow1h = wr.getSnow().get1h();

        Double gust = null;
        if (wr.getWind() != null) gust = wr.getWind().getGust();

        Integer visibility = wr.getVisibility();

        return new WeatherContext(
                wr.getMain().getTemp(),
                wr.getMain().getFeelsLike(),
                wr.getMain().getHumidity(),
                wr.getWind().getSpeed(),
                gust,
                description,
                weatherId,
                rain1h,
                snow1h,
                visibility
        );
    }
}