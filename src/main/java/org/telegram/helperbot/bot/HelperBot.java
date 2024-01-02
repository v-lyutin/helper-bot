package org.telegram.helperbot.bot;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.helperbot.enums.Command;
import org.telegram.helperbot.exception.ServiceException;
import org.telegram.helperbot.service.IpService;
import org.telegram.helperbot.service.PortScanService;
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
    private final ExchangeRateService exchangeRateService;
    private final WeatherService weatherService;
    private final IpService ipService;
    private final PortScanService portScanService;
    boolean startWait = false;
    private static Command breakpointCommand = Command.UNKNOWN;

    @Autowired
    public HelperBot(ExchangeRateService exchangeRateService, WeatherService weatherService, IpService ipService,
                     PortScanService portScanService) {
        super(System.getenv("BOT_TOKEN"));
        this.exchangeRateService = exchangeRateService;
        this.weatherService = weatherService;
        this.ipService = ipService;
        this.portScanService = portScanService;
        setBotCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        Long chatId = update.getMessage().getChatId();
        String message = update.getMessage().getText();
        Command command = Command.getCommand(message);

        if (!startWait) {
            switch (command) {
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
                    breakpointCommand = Command.WEATHER;
                    break;
                }
                case IP: {
                    handleIpCommand(chatId);
                    break;
                }
                case SCAN_PORTS: {
                    sendMessage(chatId, "Напиши имя хоста, который хочешь просканировать");
                    startWait = true;
                    breakpointCommand = Command.SCAN_PORTS;
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
        } else if (breakpointCommand == Command.WEATHER) {
            handleWeatherCommand(chatId, message);
            startWait = false;
        } else if (breakpointCommand == Command.SCAN_PORTS) {
            sendMessage(chatId, "Начинаю сканирование... Ожидай ответа в течение одной минуты!");
            handlePortScanCommand(chatId, message);
            startWait = false;
        }
    }

    @Override
    public String getBotUsername() {
        return System.getenv("BOT_NAME");
    }

    private void setBotCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand(Command.START.getCommand(), "приветственное сообщение"),
                new BotCommand(Command.USD.getCommand(), "курс доллара"),
                new BotCommand(Command.EUR.getCommand(), "курс евро"),
                new BotCommand(Command.WEATHER.getCommand(), "прогноз погоды"),
                new BotCommand(Command.IP.getCommand(), "узнать свой ip"),
                new BotCommand(Command.SCAN_PORTS.getCommand(), "просканировать открыте порты"),
                new BotCommand(Command.HELP.getCommand(), "помощь")
        );

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException exception) {
            log.error("Error setting bot's command list: " + exception);
        }
    }

    private void handleStartCommand(Long chatId, String userName) {
        String text =
                "Серёга Подручный приветствует, %s!%n%n" +
                "Могу подсказать по валюте и не только, обращайся.%n" +
                "Для получения справки по командам используй /help!";
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
                "/ip - узнать свой IP\n" +
                "/port_scan - узнать открыте порты хоста\n" +
                "/weather - прогноз погоды (город на латинице вводи)";
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

    private void handlePortScanCommand(Long chatId, String host) {
        String formattedText;
        List<Integer> ports = portScanService.scan(host);
        if (ports.isEmpty()) {
            formattedText = "Проверь название хоста, мне не удалось обнауржить открытые порты...";
        } else {
            formattedText = String.format("Открытые порты на %s:%n%s", host, portScanService.getInfo(ports));
        }
        sendMessage(chatId, formattedText);
    }

    private void handleIpCommand(Long chatId) {
        String formattedText;
        try {
            formattedText = ipService.getIp();
        } catch (ParseException | ServiceException exception) {
            formattedText = "Не удалось получить IP";
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
