package org.telegram.helperbot.service;

import org.telegram.helperbot.exception.ServiceException;

public interface ExchangeRateService {
    String getUSDRateExchange() throws ServiceException;

    String getEURRateExchange() throws ServiceException;
}
