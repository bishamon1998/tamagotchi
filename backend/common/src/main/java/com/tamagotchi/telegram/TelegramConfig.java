package com.tamagotchi.telegram;

import java.net.URI;
import java.time.Clock;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.TelegramUrl;
import org.telegram.telegrambots.meta.generics.TelegramClient;

/** Creates the Bot API client and the long polling application. */
@Configuration
@EnableConfigurationProperties(TelegramProperties.class)
class TelegramConfig {

    @Bean
    TelegramUrl telegramUrl(TelegramProperties properties) {
        URI url = properties.apiUrl();
        return new TelegramUrl(url.getScheme(), url.getHost(), url.getPort() == -1 ? 443 : url.getPort(), false);
    }

    @Bean
    TelegramClient telegramClient(TelegramProperties properties, TelegramUrl telegramUrl) {
        return new OkHttpTelegramClient(properties.botToken(), telegramUrl);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean(destroyMethod = "close")
    TelegramBotsLongPollingApplication telegramBotsApplication() {
        return new TelegramBotsLongPollingApplication();
    }
}
