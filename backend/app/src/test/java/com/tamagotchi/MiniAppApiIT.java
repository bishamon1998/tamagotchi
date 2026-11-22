package com.tamagotchi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Calls the Mini App API over HTTP with launch data signed the way Telegram signs it. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
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
                .header("Authorization", "tma " + signed(ANNA, Instant.now()))
                .retrieve().body(String.class);

        JsonNode me = new ObjectMapper().readTree(body);
        assertThat(me.path("id").asLong()).isEqualTo(2001);
        assertThat(me.path("firstName").asText()).isEqualTo("Аня");
        assertThat(me.path("language").asText()).isEqualTo("ru");
    }

    @Test
    void rejectsChangedLaunchData() {
        String forged = signed(ANNA, Instant.now()).replace("2001", "2002");

        assertThat(statusOf("tma " + forged)).isEqualTo(401);
    }

    @Test
    void rejectsLaunchDataOlderThanADay() {
        String old = signed(ANNA, Instant.now().minus(Duration.ofHours(25)));

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

    /** Builds initData as Telegram does: HMAC of sorted fields, with a key derived from the bot token. */
    private static String signed(String userJson, Instant authDate) {
        Map<String, String> fields = new TreeMap<>(Map.of(
                "auth_date", String.valueOf(authDate.getEpochSecond()),
                "query_id", "AAH-test",
                "signature", "third-party-signature",
                "user", userJson));
        String dataCheckString = fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + field.getValue())
                .collect(Collectors.joining("\n"));
        byte[] secret = hmac("WebAppData".getBytes(StandardCharsets.UTF_8), BOT_TOKEN);
        fields.put("hash", HexFormat.of().formatHex(hmac(secret, dataCheckString)));
        return fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static byte[] hmac(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static FakeBotApi createTelegram() {
        try {
            return new FakeBotApi();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
