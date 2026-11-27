package com.tamagotchi.miniapp;

import java.util.List;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registers Mini App sign-in for API controllers and the list of testers. */
@Configuration
@EnableConfigurationProperties(Testers.class)
class MiniAppWebConfig implements WebMvcConfigurer {

    private final TelegramUserResolver telegramUserResolver;

    MiniAppWebConfig(TelegramUserResolver telegramUserResolver) {
        this.telegramUserResolver = telegramUserResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(telegramUserResolver);
    }
}
