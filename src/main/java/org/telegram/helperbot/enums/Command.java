package org.telegram.helperbot.enums;

public enum Command {
    START("/start"),
    USD("/usd"),
    EUR("/eur"),
    WEATHER("/weather"),
    IP("/ip"),
    SCAN_PORTS("/port_scan"),
    HELP("/help"),
    UNKNOWN("");

    private final String command;

    Command(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public static Command getCommand(String command) {
        switch (command) {
            case "/start": {
                return Command.START;
            }
            case "/usd": {
                return Command.USD;
            }
            case "/eur": {
                return Command.EUR;
            }
            case "/weather": {
                return Command.WEATHER;
            }
            case "/ip": {
                return Command.IP;
            }
            case "/port_scan": {
                return Command.SCAN_PORTS;
            }
            case "/help": {
                return Command.HELP;
            }
            default: {
                return Command.UNKNOWN;
            }
        }
    }
}
