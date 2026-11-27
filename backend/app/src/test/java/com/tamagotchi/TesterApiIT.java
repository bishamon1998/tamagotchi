package com.tamagotchi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tester tools through the Mini App API: only users listed in the configuration may use them, and they change only
 * the tester's own game. Each test uses its own user; users 5001 to 5005 are testers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDatabase.class)
class TesterApiIT {

    private static final String BOT_TOKEN = "789:tester-test";
    private static final FakeBotApi telegram = createTelegram();
    private static final ZoneId DAY_ZONE = ZoneId.of("Europe/Moscow");
    private static final long NOT_A_TESTER = 5100;

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcClient jdbc;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("telegram.bot-token", () -> BOT_TOKEN);
        registry.add("telegram.api-url", telegram::url);
        registry.add("testers.ids", () -> "5001,5002,5003,5004,5005");
    }

    @Test
    void theAppKnowsWhoIsATester() throws IOException {
        assertThat(json(5001, HttpMethod.GET, "/api/v1/me").path("tester").asBoolean()).isTrue();
        assertThat(json(NOT_A_TESTER, HttpMethod.GET, "/api/v1/me").path("tester").asBoolean()).isFalse();
    }

    @Test
    void otherUsersCannotUseTheTools() {
        assertThat(status(NOT_A_TESTER, HttpMethod.POST, "/api/v1/tester/coins")).isEqualTo(403);
        assertThat(status(NOT_A_TESTER, HttpMethod.POST, "/api/v1/tester/skip-hours")).isEqualTo(403);
        assertThat(status(NOT_A_TESTER, HttpMethod.POST, "/api/v1/tester/skip-day")).isEqualTo(403);
        assertThat(status(NOT_A_TESTER, HttpMethod.DELETE, "/api/v1/tester/progress")).isEqualTo(403);
    }

    @Test
    void aTesterCanTopUpCoins() throws IOException {
        int before = json(5002, HttpMethod.GET, "/api/v1/wallet").path("coins").asInt();

        assertThat(status(5002, HttpMethod.POST, "/api/v1/tester/coins")).isEqualTo(204);

        assertThat(json(5002, HttpMethod.GET, "/api/v1/wallet").path("coins").asInt()).isEqualTo(before + 100);
    }

    @Test
    void skippingHoursWakesASleepingPet() throws IOException {
        json(5003, HttpMethod.POST, "/api/v1/pet/actions/play");
        json(5003, HttpMethod.POST, "/api/v1/pet/actions/play");
        json(5003, HttpMethod.POST, "/api/v1/pet/actions/sleep");

        status(5003, HttpMethod.POST, "/api/v1/tester/skip-hours");
        JsonNode pet = json(5003, HttpMethod.GET, "/api/v1/pet");

        assertThat(pet.path("asleepUntil").isNull()).isTrue();
        assertThat(pet.path("energy").asInt()).isEqualTo(98);
    }

    @Test
    void skippingADayPaysTheNextDailyBonus() throws IOException {
        json(5004, HttpMethod.GET, "/api/v1/wallet");

        status(5004, HttpMethod.POST, "/api/v1/tester/skip-day");
        JsonNode wallet = json(5004, HttpMethod.GET, "/api/v1/wallet");

        assertThat(wallet.path("streak").asInt()).isEqualTo(2);
        assertThat(wallet.path("dailyBonus").asInt()).isEqualTo(7);
        LocalDate lastBonusDay = jdbc.sql("SELECT last_bonus_day FROM wallets WHERE user_id = 5004")
                .query(Date.class).single().toLocalDate();
        assertThat(lastBonusDay).isEqualTo(LocalDate.now(DAY_ZONE));
    }

    @Test
    void startingOverHatchesANewPetWithAnEmptyWallet() throws IOException {
        json(5005, HttpMethod.POST, "/api/v1/pet/actions/feed");
        status(5005, HttpMethod.POST, "/api/v1/tester/coins");

        assertThat(status(5005, HttpMethod.DELETE, "/api/v1/tester/progress")).isEqualTo(204);

        JsonNode pet = json(5005, HttpMethod.GET, "/api/v1/pet");
        assertThat(pet.path("satiety").asInt()).isEqualTo(80);
        JsonNode wallet = json(5005, HttpMethod.GET, "/api/v1/wallet");
        assertThat(wallet.path("coins").asInt()).isEqualTo(5);
        assertThat(wallet.path("streak").asInt()).isEqualTo(1);
    }

    private JsonNode json(long userId, HttpMethod method, String path) throws IOException {
        String body = api().method(method).uri(path)
                .header("Authorization", signedIn(userId))
                .retrieve().body(String.class);
        return new ObjectMapper().readTree(body);
    }

    private int status(long userId, HttpMethod method, String path) {
        return api().method(method).uri(path)
                .header("Authorization", signedIn(userId))
                .exchange((request, response) -> response.getStatusCode().value());
    }

    private static String signedIn(long userId) {
        String user = "{\"id\":" + userId + ",\"first_name\":\"Tester\",\"language_code\":\"en\"}";
        return "tma " + TelegramLaunchData.signed(BOT_TOKEN, user, Instant.now());
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
