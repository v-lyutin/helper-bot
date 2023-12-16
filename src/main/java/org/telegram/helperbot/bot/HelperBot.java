package org.telegram.helperbot.bot;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.helperbot.exception.ServiceException;
import org.telegram.helperbot.service.WeatherService;
import org.telegram.helperbot.service.ExchangeRateService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import java.time.LocalDate;
import java.util.List;

@Component
public class HelperBot extends TelegramLongPollingBot {
    private static final Logger log = LoggerFactory.getLogger(HelperBot.class);
    private final ExchangeRateService exchangeRateService = new ExchangeRateService();
    private final WeatherService weatherService = new WeatherService();
    private static final String START = "/start";
    private static final String USD = "/usd";
    private static final String EUR = "/eur";
    private static final String WEATHER = "/weather";
    private static final String HELP = "/help";
    boolean startWait = false;

    public HelperBot() {
        super(System.getenv("BOT_TOKEN"));
        setBotCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String message = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        if (!startWait) {
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
                case WEATHER: {
                    sendMessage(chatId, "Напиши город, в котором хочешь узнать погоду");
                    startWait = true;
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
        } else {
            handleWeatherCommand(chatId, message);
            startWait = false;
        }
    }

    @Override
    public String getBotUsername() {
        return "SeregaPodruchniyBot";
    }

    private void setBotCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand(START, "приветственное сообщение"),
                new BotCommand(USD, "курс доллара"),
                new BotCommand(EUR, "курс евро"),
                new BotCommand(WEATHER, "прогноз погоды"),
                new BotCommand(HELP, "помощь")
        );

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException exception) {
            log.error("Error setting bot's command list: " + exception);
        }
    }

    private void handleStartCommand(Long chatId, String userName) {
        String text =
                "Серёга Подручный приветсвует, %s!%n%n" +
                        "Могу подсказать по валюте и не только, обращайся.%n%n" +
                        "Для этого используй команды:%n" +
                        "/usd - курс доллара%n" +
                        "/eur - курс евро%n" +
                        "/weather - прогноз погоды%n%n" +
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
        String text =
                "Справочная информация по мне\n\n" +
                        "Для получения текущих курсов валют воспользуйся командами:\n" +
                        "/usd - курс доллара\n" +
                        "/eur - курс евро\n" +
                        "/weather - прогноз погоды";
        sendMessage(chatId, text);
    }

    private void handleWeatherCommand(Long chatId, String city) {
        String formattedText;
        try {
            formattedText = weatherService.getWeather(city);
        } catch (ParseException | ServiceException exception) {
            log.error("Ошибка в получении данных о погоде");
            formattedText = "Не удалось получить данные о погоде. Попробуйте позже.";
        } catch (NullPointerException exception) {
            log.error("Введен некорректный город");
            formattedText = "Проверь название города";
        }
        sendMessage(chatId, formattedText);
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
