package com.tamagotchi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
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
 * Earns and spends coins through the Mini App API, backed by a real PostgreSQL: the daily bonus and streak, rewards
 * for useful care with a daily cap, treats and the decor shop. Each test uses its own user.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDatabase.class)
class CoinsApiIT {

    private static final String BOT_TOKEN = "789:coins-test";
    private static final FakeBotApi telegram = createTelegram();
    /** Game days start at midnight in this zone (PetRules.DAY_ZONE). */
    private static final ZoneId DAY_ZONE = ZoneId.of("Europe/Moscow");

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcClient jdbc;

    @DynamicPropertySource
    static void telegramProperties(DynamicPropertyRegistry registry) {
        registry.add("telegram.bot-token", () -> BOT_TOKEN);
        registry.add("telegram.api-url", telegram::url);
    }

    @Test
    void firstVisitOfTheDayPaysTheDailyBonusOnce() throws IOException {
        JsonNode first = call(4001, HttpMethod.GET, "/api/v1/wallet");
        JsonNode second = call(4001, HttpMethod.GET, "/api/v1/wallet");

        assertThat(first.path("dailyBonus").asInt()).isEqualTo(5);
        assertThat(first.path("coins").asInt()).isEqualTo(5);
        assertThat(first.path("streak").asInt()).isEqualTo(1);
        assertThat(second.path("dailyBonus").asInt()).isZero();
        assertThat(second.path("coins").asInt()).isEqualTo(5);
    }

    @Test
    void comingBackTheNextDayGrowsTheStreakAndTheBonus() throws IOException {
        call(4002, HttpMethod.GET, "/api/v1/wallet");
        setLastBonusDay(4002, today().minusDays(1));

        JsonNode wallet = call(4002, HttpMethod.GET, "/api/v1/wallet");

        assertThat(wallet.path("streak").asInt()).isEqualTo(2);
        assertThat(wallet.path("dailyBonus").asInt()).isEqualTo(7);
        assertThat(wallet.path("coins").asInt()).isEqualTo(12);
    }

    @Test
    void theBonusStopsGrowingAfterAWeek() throws IOException {
        call(4003, HttpMethod.GET, "/api/v1/wallet");
        jdbc.sql("UPDATE wallets SET streak = 30 WHERE user_id = 4003").update();
        setLastBonusDay(4003, today().minusDays(1));

        JsonNode wallet = call(4003, HttpMethod.GET, "/api/v1/wallet");

        assertThat(wallet.path("streak").asInt()).isEqualTo(31);
        assertThat(wallet.path("dailyBonus").asInt()).isEqualTo(17);
    }

    @Test
    void aMissedDayStartsTheStreakOver() throws IOException {
        call(4004, HttpMethod.GET, "/api/v1/wallet");
        jdbc.sql("UPDATE wallets SET streak = 5 WHERE user_id = 4004").update();
        setLastBonusDay(4004, today().minusDays(2));

        JsonNode wallet = call(4004, HttpMethod.GET, "/api/v1/wallet");

        assertThat(wallet.path("streak").asInt()).isEqualTo(1);
        assertThat(wallet.path("dailyBonus").asInt()).isEqualTo(5);
    }

    @Test
    void careThePetNeededEarnsCoins() throws IOException {
        setNeeds(4005, 20, 80, 80);

        JsonNode care = call(4005, HttpMethod.POST, "/api/v1/pet/actions/feed");

        assertThat(care.path("satiety").asInt()).isEqualTo(50);
        assertThat(care.path("earned").asInt()).isEqualTo(2);
        assertThat(care.path("coins").asInt()).isEqualTo(2);
    }

    @Test
    void careThePetDidNotNeedEarnsNothing() throws IOException {
        JsonNode care = call(4006, HttpMethod.POST, "/api/v1/pet/actions/feed");

        assertThat(care.path("earned").asInt()).isZero();
        assertThat(care.path("coins").asInt()).isZero();
    }

    @Test
    void careStopsEarningAtTheDailyCap() throws IOException {
        setNeeds(4007, 20, 80, 80);
        jdbc.sql("""
                INSERT INTO coin_ledger (user_id, amount, reason, detail, created_at)
                VALUES (4007, 19, 'CARE', 'play', :now)
                """).param("now", Timestamp.from(Instant.now())).update();

        JsonNode first = call(4007, HttpMethod.POST, "/api/v1/pet/actions/feed");
        setNeeds(4007, 20, 80, 80);
        JsonNode second = call(4007, HttpMethod.POST, "/api/v1/pet/actions/feed");

        assertThat(first.path("earned").asInt()).isEqualTo(1);
        assertThat(second.path("earned").asInt()).isZero();
    }

    @Test
    void aTreatCostsCoinsAndFillsMoreThanFood() throws IOException {
        call(4008, HttpMethod.GET, "/api/v1/wallet");
        setNeeds(4008, 20, 50, 80);

        JsonNode care = call(4008, HttpMethod.POST, "/api/v1/pet/actions/treat");

        assertThat(care.path("satiety").asInt()).isEqualTo(60);
        assertThat(care.path("mood").asInt()).isEqualTo(65);
        assertThat(care.path("coins").asInt()).isZero();
        assertThat(care.path("earned").asInt()).isZero();
    }

    @Test
    void aTreatWithoutCoinsIsRefusedAndThePetStaysHungry() throws IOException {
        setNeeds(4009, 20, 80, 80);

        int status = status(4009, HttpMethod.POST, "/api/v1/pet/actions/treat");

        assertThat(status).isEqualTo(409);
        assertThat(call(4009, HttpMethod.GET, "/api/v1/pet").path("satiety").asInt()).isEqualTo(20);
    }

    @Test
    void theShopListsTheCatalogueWithNothingOwned() throws IOException {
        JsonNode shop = call(4010, HttpMethod.GET, "/api/v1/decor");

        assertThat(shop.path("items").size()).isEqualTo(5);
        assertThat(shop.path("items").findValues("owned")).allMatch(owned -> !owned.asBoolean());
        assertThat(item(shop, "striped-rug").path("slot").asText()).isEqualTo("rug");
    }

    @Test
    void buyingDecorTakesItsPriceAndPutsItInTheRoom() throws IOException {
        setCoins(4011, 200);
        int price = item(call(4011, HttpMethod.GET, "/api/v1/decor"), "striped-rug").path("price").asInt();

        JsonNode shop = call(4011, HttpMethod.POST, "/api/v1/decor/striped-rug/purchase");

        assertThat(shop.path("coins").asInt()).isEqualTo(200 - price);
        assertThat(item(shop, "striped-rug").path("owned").asBoolean()).isTrue();
        assertThat(item(shop, "striped-rug").path("placed").asBoolean()).isTrue();
        assertThat(ledgerTotal(4011, "DECOR")).isEqualTo(-price);
    }

    @Test
    void anItemIsBoughtOnlyOnce() throws IOException {
        setCoins(4012, 500);
        call(4012, HttpMethod.POST, "/api/v1/decor/dog-bed/purchase");

        int status = status(4012, HttpMethod.POST, "/api/v1/decor/dog-bed/purchase");

        assertThat(status).isEqualTo(409);
    }

    @Test
    void decorWithoutEnoughCoinsIsRefused() throws IOException {
        int status = status(4013, HttpMethod.POST, "/api/v1/decor/dog-bed/purchase");

        assertThat(status).isEqualTo(409);
        assertThat(item(call(4013, HttpMethod.GET, "/api/v1/decor"), "dog-bed").path("owned").asBoolean()).isFalse();
    }

    @Test
    void aSlotHoldsOneItemAtATime() throws IOException {
        setCoins(4014, 500);
        call(4014, HttpMethod.POST, "/api/v1/decor/corgi-portrait/purchase");
        JsonNode bothBought = call(4014, HttpMethod.POST, "/api/v1/decor/sea-picture/purchase");

        JsonNode portraitBack = call(4014, HttpMethod.PUT, "/api/v1/decor/corgi-portrait/placement");

        assertThat(item(bothBought, "sea-picture").path("placed").asBoolean()).isTrue();
        assertThat(item(bothBought, "corgi-portrait").path("placed").asBoolean()).isFalse();
        assertThat(item(portraitBack, "corgi-portrait").path("placed").asBoolean()).isTrue();
        assertThat(item(portraitBack, "sea-picture").path("placed").asBoolean()).isFalse();
    }

    @Test
    void removedDecorStaysOwned() throws IOException {
        setCoins(4015, 500);
        call(4015, HttpMethod.POST, "/api/v1/decor/ceiling-lamp/purchase");

        JsonNode shop = call(4015, HttpMethod.DELETE, "/api/v1/decor/ceiling-lamp/placement");

        assertThat(item(shop, "ceiling-lamp").path("owned").asBoolean()).isTrue();
        assertThat(item(shop, "ceiling-lamp").path("placed").asBoolean()).isFalse();
    }

    @Test
    void decorNotOwnedCannotBePlaced() {
        int status = status(4016, HttpMethod.PUT, "/api/v1/decor/ceiling-lamp/placement");

        assertThat(status).isEqualTo(409);
    }

    @Test
    void unknownDecorIsNotFound() {
        int status = status(4017, HttpMethod.POST, "/api/v1/decor/piano/purchase");

        assertThat(status).isEqualTo(404);
    }

    private void setNeeds(long userId, int satiety, int mood, int energy) throws IOException {
        call(userId, HttpMethod.GET, "/api/v1/pet");
        jdbc.sql("UPDATE pets SET satiety = :satiety, mood = :mood, energy = :energy WHERE user_id = :userId")
                .param("satiety", satiety).param("mood", mood).param("energy", energy).param("userId", userId)
                .update();
    }

    /** Sets the balance directly, as if the user had saved up; the daily bonus is paid first so it does not add up. */
    private void setCoins(long userId, int coins) throws IOException {
        call(userId, HttpMethod.GET, "/api/v1/wallet");
        jdbc.sql("UPDATE wallets SET coins = :coins WHERE user_id = :userId")
                .param("coins", coins).param("userId", userId)
                .update();
    }

    private void setLastBonusDay(long userId, LocalDate day) {
        jdbc.sql("UPDATE wallets SET last_bonus_day = :day WHERE user_id = :userId")
                .param("day", Date.valueOf(day)).param("userId", userId)
                .update();
    }

    private int ledgerTotal(long userId, String reason) {
        return jdbc.sql("SELECT COALESCE(SUM(amount), 0) FROM coin_ledger WHERE user_id = :userId AND reason = :reason")
                .param("userId", userId).param("reason", reason)
                .query(Integer.class).single();
    }

    private static LocalDate today() {
        return LocalDate.now(DAY_ZONE);
    }

    private static JsonNode item(JsonNode shop, String id) {
        for (JsonNode item : shop.path("items")) {
            if (item.path("id").asText().equals(id)) {
                return item;
            }
        }
        throw new AssertionError("No item " + id + " in " + shop);
    }

    private JsonNode call(long userId, HttpMethod method, String path) throws IOException {
        return new ObjectMapper().readTree(api().method(method).uri(path)
                .header("Authorization", signedIn(userId))
                .retrieve().body(String.class));
    }

    private int status(long userId, HttpMethod method, String path) {
        return api().method(method).uri(path)
                .header("Authorization", signedIn(userId))
                .exchange((request, response) -> response.getStatusCode().value());
    }

    private static String signedIn(long userId) {
        String user = "{\"id\":" + userId + ",\"first_name\":\"Coin owner\",\"language_code\":\"en\"}";
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
