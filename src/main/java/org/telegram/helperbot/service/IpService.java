package org.telegram.helperbot.service;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jvnet.hk2.annotations.Service;
import org.telegram.helperbot.client.IpClient;
import org.telegram.helperbot.exception.ServiceException;

@Service
public class IpService {
    private final IpClient ipClient = new IpClient();

    public String getIp() throws ParseException, ServiceException {
        String json = ipClient.getIp().orElseThrow(
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
