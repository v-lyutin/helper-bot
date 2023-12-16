package org.telegram.helperbot.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.helperbot.client.AbstractClient;
import org.telegram.helperbot.enums.URI;
import org.telegram.helperbot.exception.ServiceException;

@Service
public class IpService {
    private final AbstractClient client;

    @Autowired
    public IpService(@Qualifier("abstractClient") AbstractClient client) {
        this.client = client;
    }

    public String getIp() throws ParseException, ServiceException {
        String json = client.getStringRepresentationOfData(URI.IP_URL.getUrl()).orElseThrow(
                () -> new ServiceException("Не удалось получить JSON")
        );
        return parseIpFromJson(json);
    }

    private String parseIpFromJson(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject jsonObject = (JSONObject) parser.parse(json);
        return jsonObject.get("ip").toString();
    }
}
