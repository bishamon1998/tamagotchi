package com.tamagotchi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Calls the Mini App API over HTTP with launch data signed the way Telegram signs it. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDatabase.class)
class MiniAppApiIT {

    private static final String BOT_TOKEN = "456:mini-app-test";
    private static final String ANNA = "{\"id\":2001,\"first_name\":\"Аня\",\"language_code\":\"ru\"}";
    private static final FakeBotApi telegram = createTelegram();

    @LocalServerPort
    private int port;

    @DynamicPropertySource
    static void telegramProperties(DynamicPropertyRegistry registry) {
        registry.add("telegram.bot-token", () -> BOT_TOKEN);
        registry.add("telegram.api-url", telegram::url);
    }

    @Test
    void returnsTheSignedInUser() throws IOException {
        String body = api().get().uri("/api/v1/me")
                .header("Authorization", "tma " + TelegramLaunchData.signed(BOT_TOKEN, ANNA, Instant.now()))
                .retrieve().body(String.class);

        JsonNode me = new ObjectMapper().readTree(body);
        assertThat(me.path("id").asLong()).isEqualTo(2001);
        assertThat(me.path("firstName").asText()).isEqualTo("Аня");
        assertThat(me.path("language").asText()).isEqualTo("ru");
    }

    @Test
    void rejectsChangedLaunchData() {
        String forged = TelegramLaunchData.signed(BOT_TOKEN, ANNA, Instant.now()).replace("2001", "2002");

        assertThat(statusOf("tma " + forged)).isEqualTo(401);
    }

    @Test
    void rejectsLaunchDataOlderThanADay() {
        String old = TelegramLaunchData.signed(BOT_TOKEN, ANNA, Instant.now().minus(Duration.ofHours(25)));

        assertThat(statusOf("tma " + old)).isEqualTo(401);
    }

    @Test
    void rejectsRequestsOpenedOutsideTelegram() {
        assertThat(statusOf(null)).isEqualTo(401);
    }

    private int statusOf(String authorization) {
        return api().get().uri("/api/v1/me")
                .headers(headers -> {
                    if (authorization != null) {
                        headers.set("Authorization", authorization);
                    }
                })
                .exchange((request, response) -> response.getStatusCode())
                .value();
    }

    private RestClient api() {
        return RestClient.create("http://localhost:" + port);
    }

    private static FakeBotApi createTelegram() {
        try {
            return new FakeBotApi();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
