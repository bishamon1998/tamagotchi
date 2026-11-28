package com.tamagotchi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
 * The garden through the Mini App API, backed by a real PostgreSQL: the free daily seed, buying and planting seeds,
 * daily watering, wilting and dying, blooming into the collection, picking flowers into the basket, selling them at
 * the market and putting one in the vase. Days pass by moving the
 * stored days back. Each test uses its own user.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDatabase.class)
class GardenApiIT {

    private static final String BOT_TOKEN = "789:garden-test";
    private static final FakeBotApi telegram = createTelegram();
    private static final List<Integer> FLOWER_PRICES = List.of(8, 20, 60);

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
    void theFirstVisitOfTheDayGivesOneFreeSeed() throws IOException {
        JsonNode first = garden(6001);
        JsonNode second = garden(6001);

        assertThat(first.path("freeSeed").asBoolean()).isTrue();
        assertThat(first.path("seeds").path("common").asInt()).isEqualTo(1);
        assertThat(second.path("freeSeed").asBoolean()).isFalse();
        assertThat(second.path("seeds").path("common").asInt()).isEqualTo(1);
        assertThat(first.path("beds")).hasSize(6);
    }

    @Test
    void seedsCostCoins() throws IOException {
        setCoins(6002, 50);

        JsonNode garden = call(6002, HttpMethod.POST, "/api/v1/garden/seeds/rare/purchase");

        assertThat(garden.path("coins").asInt()).isEqualTo(10);
        assertThat(garden.path("seeds").path("rare").asInt()).isEqualTo(1);
        assertThat(status(6002, HttpMethod.POST, "/api/v1/garden/seeds/rare/purchase")).isEqualTo(409);
    }

    @Test
    void plantingUsesASeedAndKeepsTheFlowerSecret() throws IOException {
        garden(6003);

        JsonNode garden = call(6003, HttpMethod.POST, "/api/v1/garden/beds/0/plant/common");

        JsonNode plant = garden.path("beds").get(0).path("plant");
        assertThat(plant.path("stage").asText()).isEqualTo("seed");
        assertThat(plant.path("flower").isNull()).isTrue();
        assertThat(garden.path("seeds").path("common").asInt()).isZero();
        assertThat(status(6003, HttpMethod.POST, "/api/v1/garden/beds/1/plant/common")).isEqualTo(409);
    }

    @Test
    void aBedHoldsOnePlant() throws IOException {
        setCoins(6004, 50);
        call(6004, HttpMethod.POST, "/api/v1/garden/seeds/common/purchase");
        call(6004, HttpMethod.POST, "/api/v1/garden/beds/2/plant/common");

        assertThat(status(6004, HttpMethod.POST, "/api/v1/garden/beds/2/plant/common")).isEqualTo(409);
    }

    @Test
    void aPlantGrowsOneStagePerWateredDayAndBloomsIntoTheCollection() throws IOException {
        garden(6005);
        call(6005, HttpMethod.POST, "/api/v1/garden/beds/0/plant/common");

        JsonNode sprout = call(6005, HttpMethod.POST, "/api/v1/garden/beds/0/water");
        int secondWateringToday = status(6005, HttpMethod.POST, "/api/v1/garden/beds/0/water");
        passDays(6005, 1);
        JsonNode bud = call(6005, HttpMethod.POST, "/api/v1/garden/beds/0/water");
        passDays(6005, 1);
        JsonNode bloom = call(6005, HttpMethod.POST, "/api/v1/garden/beds/0/water");

        assertThat(plant(sprout, 0).path("stage").asText()).isEqualTo("sprout");
        assertThat(plant(sprout, 0).path("wateredToday").asBoolean()).isTrue();
        assertThat(secondWateringToday).isEqualTo(409);
        assertThat(plant(bud, 0).path("stage").asText()).isEqualTo("bud");
        assertThat(plant(bud, 0).path("flower").isNull()).isTrue();
        String flower = plant(bloom, 0).path("flower").asText();
        assertThat(plant(bloom, 0).path("stage").asText()).isEqualTo("bloom");
        assertThat(flower).isNotEmpty();
        assertThat(ids(bloom.path("discovered"))).containsExactly(flower);
        assertThat(status(6005, HttpMethod.POST, "/api/v1/garden/beds/0/water")).isEqualTo(409);
    }

    @Test
    void aMissedDayWiltsThePlantAndASecondKillsIt() throws IOException {
        garden(6006);
        call(6006, HttpMethod.POST, "/api/v1/garden/beds/3/plant/common");

        passDays(6006, 2);
        JsonNode wilting = garden(6006);
        passDays(6006, 1);
        JsonNode gone = garden(6006);

        assertThat(plant(wilting, 3).path("wilting").asBoolean()).isTrue();
        assertThat(plant(gone, 3).isNull()).isTrue();
    }

    @Test
    void wateringSavesAWiltingPlant() throws IOException {
        garden(6007);
        call(6007, HttpMethod.POST, "/api/v1/garden/beds/1/plant/common");
        passDays(6007, 2);

        JsonNode garden = call(6007, HttpMethod.POST, "/api/v1/garden/beds/1/water");

        assertThat(plant(garden, 1).path("wilting").asBoolean()).isFalse();
        assertThat(plant(garden, 1).path("stage").asText()).isEqualTo("sprout");
    }

    @Test
    void pickingABloomPutsItInTheBasketAndFreesTheBed() throws IOException {
        String flower = growAFlower(6008, 4);
        int coins = garden(6008).path("coins").asInt();

        JsonNode picked = call(6008, HttpMethod.POST, "/api/v1/garden/beds/4/pick");

        assertThat(picked.path("basket").path(flower).asInt()).isEqualTo(1);
        assertThat(picked.path("coins").asInt()).isEqualTo(coins);
        assertThat(plant(picked, 4).isNull()).isTrue();
        assertThat(picked.path("discovered")).hasSize(1);
    }

    @Test
    void theMarketBuysPickedFlowersByRarity() throws IOException {
        String flower = growAFlower(6011, 0);
        int coins = call(6011, HttpMethod.POST, "/api/v1/garden/beds/0/pick").path("coins").asInt();

        JsonNode sold = call(6011, HttpMethod.POST, "/api/v1/garden/basket/" + flower + "/sale");

        assertThat(FLOWER_PRICES).contains(sold.path("coins").asInt() - coins);
        assertThat(sold.path("basket").has(flower)).isFalse();
        assertThat(status(6011, HttpMethod.POST, "/api/v1/garden/basket/" + flower + "/sale")).isEqualTo(409);
    }

    @Test
    void aPickedFlowerCanGoInTheVaseAtHome() throws IOException {
        String flower = growAFlower(6012, 2);
        call(6012, HttpMethod.POST, "/api/v1/garden/beds/2/pick");

        JsonNode garden = call(6012, HttpMethod.PUT, "/api/v1/garden/vase/" + flower);

        assertThat(garden.path("vase").asText()).isEqualTo(flower);
        assertThat(garden.path("basket").has(flower)).isFalse();
        assertThat(status(6012, HttpMethod.PUT, "/api/v1/garden/vase/" + flower)).isEqualTo(409);
    }

    @Test
    void onlyABloomCanBePicked() throws IOException {
        garden(6009);
        call(6009, HttpMethod.POST, "/api/v1/garden/beds/0/plant/common");

        assertThat(status(6009, HttpMethod.POST, "/api/v1/garden/beds/0/pick")).isEqualTo(409);
        assertThat(status(6009, HttpMethod.POST, "/api/v1/garden/beds/5/pick")).isEqualTo(409);
    }

    @Test
    void rejectsUnknownBedsAndSeeds() {
        assertThat(status(6010, HttpMethod.POST, "/api/v1/garden/beds/6/water")).isEqualTo(404);
        assertThat(status(6010, HttpMethod.POST, "/api/v1/garden/seeds/golden/purchase")).isEqualTo(404);
        assertThat(status(6010, HttpMethod.POST, "/api/v1/garden/basket/dandelion/sale")).isEqualTo(404);
    }

    /** Plants the free seed in the bed and waters it over three days until it blooms; returns the flower. */
    private String growAFlower(long userId, int bed) throws IOException {
        garden(userId);
        call(userId, HttpMethod.POST, "/api/v1/garden/beds/" + bed + "/plant/common");
        call(userId, HttpMethod.POST, "/api/v1/garden/beds/" + bed + "/water");
        passDays(userId, 1);
        call(userId, HttpMethod.POST, "/api/v1/garden/beds/" + bed + "/water");
        passDays(userId, 1);
        JsonNode bloom = call(userId, HttpMethod.POST, "/api/v1/garden/beds/" + bed + "/water");
        return plant(bloom, bed).path("flower").asText();
    }

    private JsonNode garden(long userId) throws IOException {
        return call(userId, HttpMethod.GET, "/api/v1/garden");
    }

    private static JsonNode plant(JsonNode garden, int bed) {
        return garden.path("beds").get(bed).path("plant");
    }

    private static List<String> ids(JsonNode array) {
        List<String> ids = new ArrayList<>();
        array.forEach(id -> ids.add(id.asText()));
        return ids;
    }

    /** Moves every stored garden day back, as if that many game days had passed. */
    private void passDays(long userId, int days) {
        jdbc.sql("""
                UPDATE garden_plants
                SET planted_day = planted_day - :days, last_watered_day = last_watered_day - :days
                WHERE user_id = :userId
                """).param("days", days).param("userId", userId).update();
    }

    /** Sets the balance directly, after the daily bonus was paid, so it does not add up. */
    private void setCoins(long userId, int coins) throws IOException {
        call(userId, HttpMethod.GET, "/api/v1/wallet");
        jdbc.sql("UPDATE wallets SET coins = :coins WHERE user_id = :userId")
                .param("coins", coins).param("userId", userId)
                .update();
    }

    private JsonNode call(long userId, HttpMethod method, String path) throws IOException {
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
        String user = "{\"id\":" + userId + ",\"first_name\":\"Gardener\",\"language_code\":\"en\"}";
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
