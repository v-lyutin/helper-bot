package org.telegram.helperbot.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.telegram.helperbot.exception.ServiceException;
import java.io.IOException;
import java.util.Optional;

public class OpenWeatherClient {
    private final OkHttpClient client = new OkHttpClient();
    private final String token = System.getenv("OPEN_WEATHER_TOKEN");

    public Optional<String> getWeather(String city) throws ServiceException {
        Request request = new Request.Builder()
                .url("https://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + token + "&units=metric")
                .build();

        try (Response response = client.newCall(request).execute()){
            ResponseBody body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException exception) {
            throw new ServiceException("Ошибка в получении погоды", exception);
        }
    }

    public static void main(String[] args) throws ServiceException {
        OpenWeatherClient client1 = new OpenWeatherClient();
        System.out.println(client1.getWeather("djddjd"));
    }
}
