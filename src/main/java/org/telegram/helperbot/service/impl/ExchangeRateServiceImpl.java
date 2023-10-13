package org.telegram.helperbot.service.impl;

import org.jvnet.hk2.annotations.Service;
import org.telegram.helperbot.client.CbrClient;
import org.telegram.helperbot.exception.ServiceException;
import org.telegram.helperbot.service.ExchangeRateService;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;

@Service
public class ExchangeRateServiceImpl implements ExchangeRateService {
    private static final String USD_XPATH = "/ValCurs//Valute[@ID='R01235']/Value";
    private static final String EUR_XPATH = "/ValCurs//Valute[@ID='R01239']/Value";
    private final CbrClient client = new CbrClient();

    @Override
    public String getUSDRateExchange() throws ServiceException {
        String xml = client.getCurrencyRateXML().orElseThrow(
                () -> new ServiceException("Не удалось получить XML")
        );
        return getCurrencyValueFromXML(xml, USD_XPATH);
    }

    @Override
    public String getEURRateExchange() throws ServiceException {
        String xml = client.getCurrencyRateXML().orElseThrow(
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
