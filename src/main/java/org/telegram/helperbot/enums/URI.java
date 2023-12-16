package org.telegram.helperbot.enums;

public enum URI {
    CBR_URL("http://www.cbr.ru/scripts/XML_daily.asp"),
    IP_URL("https://api.ipify.org/?format=json"),
    //https://api.openweathermap.org/data/2.5/weather?q= + + city + &appid= + token + &units=metric
    OPEN_WEATHER_URL("https://api.openweathermap.org/data/2.5/weather?q=");

    private final String url;

    URI(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
