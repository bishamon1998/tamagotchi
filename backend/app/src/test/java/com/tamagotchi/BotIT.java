package com.tamagotchi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

/** Runs the whole bot against a local Bot API and checks what users get back. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BotIT {

    private static final String MINI_APP_URL = "https://app.example.test/";

    // Lives until the JVM exits so it outlasts the cached Spring context that polls it.
    private static final FakeBotApi telegram = createTelegram();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void telegramProperties(DynamicPropertyRegistry registry) {
        registry.add("telegram.bot-token", () -> "123:test");
        registry.add("telegram.api-url", telegram::url);
        registry.add("telegram.mini-app-url", () -> MINI_APP_URL);
    }

    @Test
    void greetsRussianSpeakerInRussian() throws InterruptedException {
        telegram.userSends(1001, "Аня", "ru", "/start");

        JsonNode reply = telegram.nextSentMessage();

        assertThat(reply.path("chat_id").asText()).isEqualTo("1001");
        assertThat(reply.path("text").asText()).startsWith("Привет, Аня!");
        JsonNode button = reply.path("reply_markup").path("inline_keyboard").path(0).path(0);
        assertThat(button.path("text").asText()).isEqualTo("Открыть приложение");
        assertThat(button.path("web_app").path("url").asText()).isEqualTo(MINI_APP_URL);
    }

    @Test
    void greetsOtherUsersInEnglish() throws InterruptedException {
        telegram.userSends(1002, "Nick", "el", "/start@TamagotchiBot");

        JsonNode reply = telegram.nextSentMessage();

        assertThat(reply.path("chat_id").asText()).isEqualTo("1002");
        assertThat(reply.path("text").asText()).startsWith("Hi, Nick!");
    }

    @Test
    void reportsReadinessForTheHostingHealthCheck() {
        HttpStatusCode status = RestClient.create("http://localhost:" + port)
                .get().uri("/actuator/health/readiness")
                .retrieve().toBodilessEntity().getStatusCode();

        assertThat(status.value()).isEqualTo(200);
    }

    private static FakeBotApi createTelegram() {
        try {
            return new FakeBotApi();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
