package com.tamagotchi.miniapp;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Telegram users who may use the tester tools (extra coins, skipping time, starting over), set by deployment
 * configuration. Nobody is a tester by default.
 *
 * @param ids Telegram user ids of the testers
 */
@ConfigurationProperties("testers")
public record Testers(@DefaultValue List<Long> ids) {

    public boolean includes(TelegramUser user) {
        return ids.contains(user.id());
    }
}
