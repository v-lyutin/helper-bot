package org.telegram.helperbot.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import org.telegram.helperbot.exception.ServiceException;
import java.io.IOException;
import java.util.Optional;

@Component
public class IpClient {
    private final OkHttpClient client = new OkHttpClient();
    private final String url = "https://api.ipify.org/?format=json";

    public Optional<String> getIp() throws ServiceException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException exception) {
            throw new ServiceException("Ошибка в получении IP");
        }
    }
}
