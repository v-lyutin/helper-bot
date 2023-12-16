package org.telegram.helperbot.client;

import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import org.telegram.helperbot.client.enums.URI;
import org.telegram.helperbot.exception.ServiceException;
import java.io.IOException;
import java.util.Optional;

@Component
public class OpenWeatherClient extends AbstractClient {
    private final String token = System.getenv("OPEN_WEATHER_TOKEN");

    public Optional<String> getWeatherJSON(String city) throws ServiceException {
        Request request = new Request.Builder()
                .url(URI.OPEN_WEATHER_URL.getUrl() + city + "&appid=" + token + "&units=metric")
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException exception) {
            throw new ServiceException("Ошибка в получении погоды", exception);
        }
    }
}
