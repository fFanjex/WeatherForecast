package ru.ffanjex.weatherforecast.service;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.dto.AdviceResponseDto;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.model.User;
import ru.ffanjex.weatherforecast.model.WeatherResponse;
import ru.ffanjex.weatherforecast.repository.AdviceRepository;
import ru.ffanjex.weatherforecast.repository.UserRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Optional;

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

    public AdviceResponseDto generateAdvice(WeatherContext w) {
        try {
            String prompt = buildPrompt(w);

            JSONObject body = new JSONObject()
                    .put("model", "gpt-3.5-turbo")
                    .put("messages", new JSONArray()
                            .put(new JSONObject()
                                    .put("role", "system")
                                    .put("content",
                                            "Ты создаёшь ответ только в виде валидного JSON. " +
                                                    "Без markdown, без пояснений, без ```." +
                                                    "Поле adviceText — обычный человекочитаемый текст на русском языке. " +
                                                    "Поле shortClothingDescription — короткая строка со списком вещей на русском через запятую. " +
                                                    "Поле imagePrompt — короткий общий prompt на английском для стиля одежды."))
                            .put(new JSONObject()
                                    .put("role", "user")
                                    .put("content", prompt)))
                    .put("max_tokens", 1600)
                    .put("temperature", 0.5);

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
                return new AdviceResponseDto(
                        "Ошибка OpenAI: HTTP " + code + " — " + extractErrorMessage(responseText),
                        "",
                        ""
                );
            }

            JSONObject json = new JSONObject(responseText);
            JSONArray choices = json.optJSONArray("choices");
            if (choices == null || choices.isEmpty()) {
                return new AdviceResponseDto("Ошибка: пустой ответ от модели.", "", "");
            }

            String content = choices.getJSONObject(0)
                    .getJSONObject("message")
                    .optString("content", "")
                    .trim();

            if (content.isEmpty()) {
                return new AdviceResponseDto("Ошибка: модель вернула пустой текст.", "", "");
            }

            JSONObject result = extractJsonObject(content);

            String adviceText = normalizeAdviceText(result.optString("adviceText", ""));
            String shortClothingDescription = result.optString("shortClothingDescription", "").trim();
            String imagePrompt = result.optString("imagePrompt", "").trim();

            if (adviceText.isBlank()) {
                adviceText = "Ошибка: модель не вернула adviceText.";
            }

            return new AdviceResponseDto(adviceText, shortClothingDescription, imagePrompt);

        } catch (Exception e) {
            return new AdviceResponseDto("Ошибка: " + e.getMessage(), "", "");
        }
    }

    public AdviceResponseDto generateAdvice(double temperature, double humidity, double windSpeed) {
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
        if (userOpt.isEmpty()) {
            return false;
        }

        User user = userOpt.get();
        user.getAdviceList().add(advice);
        userRepository.save(user);
        return true;
    }

    public ArrayList<Advice> getUserAdvices(String username) {
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
        String vis = (w.visibilityMeters == null) ? "нет данных" : (w.visibilityMeters + " м");

        return String.format(Locale.US,
                """
                Ты опытный стилист и метеоконсультант.

                Верни строго один JSON-объект такого вида:
                {
                  "adviceText": "string",
                  "shortClothingDescription": "string",
                  "imagePrompt": "string"
                }

                ВАЖНО:
                1) Снаружи ответ должен быть JSON.
                2) Поле adviceText должно быть ОБЫЧНЫМ ЧЕЛОВЕЧЕСКИМ ТЕКСТОМ на русском языке.
                3) adviceText не должен быть JSON, объектом, массивом или набором ключей и значений.
                4) Внутри adviceText используй переносы строк \\n.
                5) adviceText должен быть развёрнутым, полезным и понятным.

                Формат adviceText строго такой:
                Коротко:
                - Прогулка 15–30 мин: ...
                - На 1–2 часа: ...

                Детально по пунктам:
                Верх: ...
                Низ: ...
                Обувь: ...
                Аксессуары: ...
                Чего избегать: ...
                Почему: ...

                Требования к adviceText:
                - пиши по-русски;
                - дай подробный и практичный совет;
                - объём примерно 900–1300 символов;
                - учитывай ощущаемую температуру, влажность, ветер, порывы и осадки;
                - отдельно уточняй, что лучше для короткой прогулки и что лучше для долгого пребывания на улице.

                Требования к shortClothingDescription:
                - одна короткая строка на русском;
                - только предметы одежды и аксессуары через запятую;
                - без пояснений.

                Требования к imagePrompt:
                - одна короткая строка на английском;
                - это общий стиль образа;
                - без погоды, температуры и объяснений.

                Погода:
                Температура %.1f°C (ощущается %.1f°C), влажность %.0f%%, ветер %.1f м/с (порывы %s), осадки за 1ч: дождь %s мм, снег %s мм, видимость %s, погода: %s (код %s).
                """,
                w.temp, w.feelsLike, w.humidity, w.windSpeed, gust, rain, snow, vis, desc, wid
        );
    }

    private String normalizeAdviceText(String adviceText) {
        if (adviceText == null || adviceText.isBlank()) {
            return "";
        }

        String trimmed = adviceText.trim();

        if (!trimmed.startsWith("{")) {
            return trimmed;
        }

        try {
            JSONObject obj = new JSONObject(trimmed);
            StringBuilder sb = new StringBuilder();

            JSONObject shortObj = obj.optJSONObject("Коротко");
            if (shortObj != null) {
                sb.append("Коротко:\n");

                String shortWalk = shortObj.optString("Прогулка 15–30 мин",
                        shortObj.optString("Прогулка 15-30 мин", ""));
                String longWalk = shortObj.optString("На 1–2 часа",
                        shortObj.optString("На 1-2 часа", ""));

                if (!shortWalk.isBlank()) {
                    sb.append("- Прогулка 15–30 мин: ").append(shortWalk).append("\n");
                }
                if (!longWalk.isBlank()) {
                    sb.append("- На 1–2 часа: ").append(longWalk).append("\n");
                }
                sb.append("\n");
            }

            JSONObject details = obj.optJSONObject("Детально по пунктам");
            if (details != null) {
                sb.append("Детально по пунктам:\n");

                String upper = details.optString("Верх", "");
                String lower = details.optString("Низ", "");
                String shoes = details.optString("Обувь", "");
                String accessories = details.optString("Аксессуары", "");
                String avoid = details.optString("Чего избегать", "");
                String why = details.optString("Почему", "");

                if (!upper.isBlank()) sb.append("Верх: ").append(upper).append("\n");
                if (!lower.isBlank()) sb.append("Низ: ").append(lower).append("\n");
                if (!shoes.isBlank()) sb.append("Обувь: ").append(shoes).append("\n");
                if (!accessories.isBlank()) sb.append("Аксессуары: ").append(accessories).append("\n");
                if (!avoid.isBlank()) sb.append("Чего избегать: ").append(avoid).append("\n");
                if (!why.isBlank()) sb.append("Почему: ").append(why).append("\n");
            }

            String result = sb.toString().trim();
            return result.isBlank() ? trimmed : result;

        } catch (Exception e) {
            return trimmed;
        }
    }

    private JSONObject extractJsonObject(String content) {
        String trimmed = content.trim();

        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            return new JSONObject(trimmed);
        }

        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return new JSONObject(trimmed.substring(start, end + 1));
        }

        throw new RuntimeException("Не удалось извлечь JSON из ответа модели");
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
        if (wr.getRain() != null) {
            rain1h = wr.getRain().get1h();
        }
        if (wr.getSnow() != null) {
            snow1h = wr.getSnow().get1h();
        }

        Double gust = null;
        if (wr.getWind() != null) {
            gust = wr.getWind().getGust();
        }

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