package org.telegram.helperbot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.telegram.helperbot.client.AbstractClient;
import org.telegram.helperbot.enums.URI;
import org.telegram.helperbot.exception.ServiceException;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Service
public class ExchangeRateService {
    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";
    private final AbstractClient client;

    @Autowired
    public ExchangeRateService(@Qualifier("abstractClient") AbstractClient client) {
        this.client = client;
    }

    public String getUSDRateExchange() throws ServiceException {
        String xml = client.getStringRepresentationOfData(URI.CBR_URL.getUrl()).orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return getCurrencyValueFromXML(xml, USD_XPATH);
    }

    public String getEURRateExchange() throws ServiceException {
        String xml = client.getStringRepresentationOfData(URI.CBR_URL.getUrl()).orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return getCurrencyValueFromXML(xml, EUR_XPATH);
    }

    private static String getCurrencyValueFromXML(String xml, String xPathExpression) throws ServiceException {
        InputSource source = new InputSource(new StringReader(xml));

        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            Document document = (Document) xPath.evaluate("/", source, XPathConstants.NODE);

            return xPath.evaluate(xPathExpression, document);
        } catch (XPathExpressionException exception) {
            throw new ServiceException("Не удалось распарсить XML", exception);
        }
    }
}
