package org.telegram.helperbot.bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.helperbot.exception.ServiceException;
import org.telegram.helperbot.service.impl.ExchangeRateServiceImpl;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDate;

@Component
public class HelperBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(HelperBot.class);
    private final ExchangeRateServiceImpl exchangeRateService = new ExchangeRateServiceImpl();
    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String HELP = "/help";

    public HelperBot(@Value("${bot.token}") final String botToken) {
        super(botToken);
    }
    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        switch (message) {
            case START: {
                String userName = update.getMessage().getChat().getUserName();
                handleStartCommand(chatId, userName);
                break;
            }
            case USD: {
                handleUSDCommand(chatId);
                break;
            }
            case EUR: {
                handleEURCommand(chatId);
                break;
            }
            case HELP: {
                handleHelpCommand(chatId);
                break;
            }
            default: {
                handleUnknownCommand(chatId);
                break;
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "SeregaPodruchniyBot";
    }

    private void handleStartCommand(Long chatId, String userName) {
        String text =
                "Серёга Подручный приветсвует, %s!%n%n" +
                "Могу подсказать по валюте, обращайся.%n%n" +
                "Для этого используй команды:%n" +
                "/usd - курс доллара%n" +
                "/eur - курс евро%n%n" +
                "Дополнительные команды:%n" +
                "/help - получение справки%n";
        String formattedText = String.format(text, userName);
        sendMessage(chatId, formattedText);
    }

    private void handleUSDCommand(Long chatId) {
        String formattedText;
        try {
            String usd = exchangeRateService.getUSDRateExchange();
            String text = "Курс доллара на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), usd);
        } catch (ServiceException exception) {
            log.error("Ошибка получения курса доллара", exception);
            formattedText = "Не удалось получить текущий курс доллара. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void handleEURCommand(Long chatId) {
        String formattedText;
        try {
            String eur = exchangeRateService.getEURRateExchange();
            String text = "Курс евро на %s составляет %s рублей";
            formattedText = String.format(text, LocalDate.now(), eur);
        } catch (ServiceException exception) {
            log.error("Ошибка получения курса евро", exception);
            formattedText = "Не удалось получить текущий курс евро. Попробуйте позже.";
        }
        sendMessage(chatId, formattedText);
    }

    private void handleHelpCommand(Long chatId) {
        var text =
                "Справочная информация по мне\n\n" +
                "Для получения текущих курсов валют воспользуйся командами:\n" +
                "/usd - курс доллара\n" +
                "/eur - курс евро";
        sendMessage(chatId, text);
    }

    private void handleUnknownCommand(Long chatId) {
        String text = "Не удалось распознать команду!";
        sendMessage(chatId, text);
    }

    private void sendMessage(Long chatId, String message) {
        SendMessage sendMessage = new SendMessage(String.valueOf(chatId), message);

        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Произошла ошибка при отправке сообщения");
        }
    }
}
