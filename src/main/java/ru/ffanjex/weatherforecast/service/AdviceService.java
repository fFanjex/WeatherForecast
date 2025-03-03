package ru.ffanjex.weatherforecast.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ffanjex.weatherforecast.model.Advice;
import ru.ffanjex.weatherforecast.repository.AdviceRepository;


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

@Service
public class AdviceService {

    @Autowired
    private final AdviceRepository adviceRepository;

    public AdviceService(AdviceRepository adviceRepository) {
        this.adviceRepository = adviceRepository;
    }

    @Value("${openai.api.key}")
    private String apiKey;

    private static final String OPENAI_URL = "https://api.proxyapi.ru/openai/v1/chat/completions";

    public String getClothingAdvice(double temperature, double humidity, double windSpeed) {
        try {
            String prompt = "Дай подробный и полезный совет по одежде, если температура "
                    + temperature + "°C, влажность " + humidity + "%, и скорость ветра " + windSpeed + " м/с. "
                    + "Подробно опиши, что стоит надеть, учитывая все эти условия, и объясни, почему. Уложись в 350 токенов.";

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", "gpt-3.5-turbo");
            requestBody.put("messages", new JSONArray()
                    .put(new JSONObject().put("role", "user").put("content", prompt)));
            requestBody.put("max_tokens", 350);
            requestBody.put("temperature", 0.7);

            URL url = new URL(OPENAI_URL);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setDoOutput(true);

            try (OutputStream os = con.getOutputStream()) {
                os.write(requestBody.toString().getBytes());
                os.flush();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray choices = jsonResponse.getJSONArray("choices");

            if (choices.length() > 0) {
                return choices.getJSONObject(0).getJSONObject("message").getString("content");
            } else {
                return "Не удалось получить совет, попробуйте позже.";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Ошибка при получении совета: " + e.getMessage();
        }
    }



    public Advice save(Advice advice) {
        return adviceRepository.save(advice);
    }

    public List<Advice> getAllAdvice() {
        return adviceRepository.findAll();
    }
}
