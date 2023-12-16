package org.telegram.helperbot.client;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.springframework.stereotype.Component;
import org.telegram.helperbot.client.enums.URI;
import org.telegram.helperbot.exception.ServiceException;
import java.io.IOException;
import java.util.Optional;

@Component
public class IpClient {
    private final OkHttpClient client = new OkHttpClient();

    public Optional<String> getIp() throws ServiceException {
        Request request = new Request.Builder()
                .url(URI.IP_URL.getUrl())
                .build();

        try (Response response = client.newCall(request).execute()) {
            ResponseBody body = response.body();
            return body == null ? Optional.empty() : Optional.of(body.string());
        } catch (IOException exception) {
            throw new ServiceException("Ошибка в получении IP");
        }
    }
}
