package ru.ffanjex.weatherforecast.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class WeatherResponse {

    private String name;
    private Sys sys;
    private Main main;
    private List<Weather> weather;
    private Wind wind;
    private Rain rain;
    private Snow snow;
    private Integer visibility;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Sys getSys() {
        return sys;
    }

    public void setSys(Sys sys) {
        this.sys = sys;
    }

    public Main getMain() {
        return main;
    }

    public void setMain(Main main) {
        this.main = main;
    }

    public List<Weather> getWeather() {
        return weather;
    }

    public void setWeather(List<Weather> weather) {
        this.weather = weather;
    }

    public Wind getWind() {
        return wind;
    }

    public void setWind(Wind wind) {
        this.wind = wind;
    }

    public Rain getRain() {
        return rain;
    }

    public void setRain(Rain rain) {
        this.rain = rain;
    }

    public Snow getSnow() {
        return snow;
    }

    public void setSnow(Snow snow) {
        this.snow = snow;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public static class Sys {
        private String country;

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }

    public static class Main {
        private double temp;

        @JsonProperty("feels_like")
        private double feelsLike;

        private int humidity;

        public double getTemp() {
            return temp;
        }

        public void setTemp(double temp) {
            this.temp = temp;
        }

        public double getFeelsLike() {
            return feelsLike;
        }

        public void setFeelsLike(double feelsLike) {
            this.feelsLike = feelsLike;
        }

        public int getHumidity() {
            return humidity;
        }

        public void setHumidity(int humidity) {
            this.humidity = humidity;
        }
    }

    public static class Weather {
        private int id;
        private String description;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    public static class Wind {
        private double speed;
        private Double gust;

        public double getSpeed() {
            return speed;
        }

        public void setSpeed(double speed) {
            this.speed = speed;
        }

        public Double getGust() {
            return gust;
        }

        public void setGust(Double gust) {
            this.gust = gust;
        }
    }

    public static class Rain {
        @JsonProperty("1h")
        private Double oneHour;

        public Double get1h() {
            return oneHour;
        }

        public void set1h(Double oneHour) {
            this.oneHour = oneHour;
        }
    }

    public static class Snow {
        @JsonProperty("1h")
        private Double oneHour;

        public Double get1h() {
            return oneHour;
        }

        public void set1h(Double oneHour) {
            this.oneHour = oneHour;
        }
    }
}