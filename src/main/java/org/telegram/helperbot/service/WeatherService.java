package org.telegram.helperbot.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.helperbot.client.OpenWeatherClient;
import org.telegram.helperbot.exception.ServiceException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

@Service
public class WeatherService {
    private final OpenWeatherClient client;
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    public WeatherService(OpenWeatherClient client) {
        this.client = client;
    }

    private final Map<String, String> emoji = Map.of(
            "Clear", "Ясно",
            "Clouds", "Облачно",
            "Rain", "Дождь",
            "Drizzle", "Дождь",
            "Thunderstorm", "Гроза",
            "Snow", "Снег",
            "Mist", "Туман"
    );

    public String getWeather(String city) throws ParseException, ServiceException {
        String json = client.getWeatherJSON(city).orElseThrow(
                () -> new ServiceException("Не удалось получить JSON")
        );
        return getWeatherFromJSON(json);
    }

    private String getWeatherFromJSON(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);

        JSONArray weatherDescription =  (JSONArray) jsonObject.get("weather");
        String weatherCode = weatherDescription.toString().split(",")[2].split(":")[1];
        String weatherMessage = emoji.getOrDefault(
                weatherCode.substring(1, weatherCode.length() - 1),
                "Посмотри в окно");

        String city = (String) jsonObject.get("name");

        JSONObject weather = (JSONObject) jsonObject.get("main");
        Double temperature = (Double) weather.get("temp");
        Long humidity = (Long) weather.get("humidity");
        Long pressure = (Long) weather.get("pressure");

        JSONObject wind = (JSONObject) jsonObject.get("wind");
        Double speed = (Double) wind.get("speed");

        JSONObject sys = (JSONObject) jsonObject.get("sys");
        Long sunriseTimestamp = (Long) sys.get("sunrise");
        Long sunsetTimestamp = (Long) sys.get("sunset");

        return "***" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) + "***" + "\n" +
                "Погода в городе: " + city + "\n" +
                "Температура: " + temperature + "°C " + weatherMessage + "\n" +
                "Влажность: " + humidity + "%\n" +
                "Давление: " + pressure + " мм.рт.ст\n" +
                "Ветер: " + speed + " м/с\n" +
                "Восход солнца: " + simpleDateFormat.format(new Date(sunriseTimestamp * 1000L)) + "\n" +
                "Закат солнца: " + simpleDateFormat.format(new Date(sunsetTimestamp * 1000L));
    }
}
