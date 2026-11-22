package com.tamagotchi.telegram;

import java.net.URI;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Bot connection settings. The API URL is configurable so integration tests can point the bot at a local Bot API.
 *
 * @param miniAppUrl     public HTTPS address of the Mini App; without it the bot sends no "open" button
 * @param initDataMaxAge how long a Mini App sign-in from Telegram stays valid
 */
@ConfigurationProperties("telegram")
public record TelegramProperties(String botToken, URI apiUrl, URI miniAppUrl,
                                 @DefaultValue("24h") Duration initDataMaxAge) {

    public TelegramProperties {
        if (botToken == null || botToken.isBlank()) {
            throw new IllegalStateException("Set TELEGRAM_BOT_TOKEN");
        }
        if (miniAppUrl != null && !"https".equals(miniAppUrl.getScheme())) {
            throw new IllegalStateException("MINI_APP_URL must be an https address");
        }
    }
}
