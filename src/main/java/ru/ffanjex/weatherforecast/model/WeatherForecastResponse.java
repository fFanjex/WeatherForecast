package ru.ffanjex.weatherforecast.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class WeatherForecastResponse {
    private List<Forecast> list;

    public List<Forecast> getList() {
        return list;
    }

    public void setList(List<Forecast> list) {
        this.list = list;
    }

    public static class Forecast {
        private Main main;
        private List<WeatherResponse.Weather> weather;
        private WeatherResponse.Wind wind;

        @JsonProperty("dt_txt")
        private String dtTxt;

        public Main getMain() {
            return main;
        }

        public void setMain(Main main) {
            this.main = main;
        }

        public List<WeatherResponse.Weather> getWeather() {
            return weather;
        }

        public void setWeather(List<WeatherResponse.Weather> weather) {
            this.weather = weather;
        }

        public WeatherResponse.Wind getWind() {
            return wind;
        }

        public void setWind(WeatherResponse.Wind wind) {
            this.wind = wind;
        }

        public String getDtTxt() {
            return dtTxt;
        }

        public void setDtTxt(String dtTxt) {
            this.dtTxt = dtTxt;
        }
    }

    public static class Main {
        private double temp;
        private int humidity;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
    }
}
