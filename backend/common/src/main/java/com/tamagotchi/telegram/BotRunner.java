package com.tamagotchi.telegram;

import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.util.DefaultGetUpdatesGenerator;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/** Starts polling Telegram once the context is ready and stops it on shutdown. */
@Component
class BotRunner implements SmartLifecycle {

    private final TelegramBotsLongPollingApplication application;
    private final TelegramProperties properties;
    private final TelegramUrl telegramUrl;
    private final UpdateRouter router;
    private volatile boolean running;

    BotRunner(TelegramBotsLongPollingApplication application, TelegramProperties properties,
              TelegramUrl telegramUrl, UpdateRouter router) {
        this.application = application;
        this.properties = properties;
        this.telegramUrl = telegramUrl;
        this.router = router;
    }

    @Override
    public void start() {
        try {
            application.registerBot(properties.botToken(), () -> telegramUrl,
                    new DefaultGetUpdatesGenerator(), router);
            running = true;
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Could not start Telegram polling", e);
        }
    }

    @Override
    public void stop() {
        try {
            application.unregisterBot(properties.botToken());
            running = false;
        } catch (TelegramApiException e) {
            throw new IllegalStateException("Could not stop Telegram polling", e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
