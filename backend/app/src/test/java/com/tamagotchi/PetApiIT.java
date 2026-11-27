package com.tamagotchi;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/** Looks after a pet through the Mini App API, backed by a real PostgreSQL. Each test uses its own user. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestDatabase.class)
class PetApiIT {

    private static final String BOT_TOKEN = "789:pet-test";
    private static final FakeBotApi telegram = createTelegram();

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
    void hatchesAPetOnFirstVisit() throws IOException {
        JsonNode pet = get(3001);

        assertNeeds(pet, 80, 80, 80);
        assertThat(pet.path("name").isNull()).isTrue();
    }

    @Test
    void theUserNamesThePetAndCanRenameIt() throws IOException {
        get(3010);

        JsonNode named = name(3010, "  Булочка  ");
        JsonNode renamed = name(3010, "Bun");

        assertThat(named.path("name").asText()).isEqualTo("Булочка");
        assertThat(renamed.path("name").asText()).isEqualTo("Bun");
        assertThat(get(3010).path("name").asText()).isEqualTo("Bun");
        assertNeeds(renamed, 80, 80, 80);
    }

    @Test
    void rejectsBlankAndTooLongNames() {
        assertThat(nameStatus(3011, "   ")).isEqualTo(400);
        assertThat(nameStatus(3011, "x".repeat(21))).isEqualTo(400);
        assertThat(nameStatus(3011, "x".repeat(20))).isEqualTo(200);
    }

    @Test
    void feedingFillsSatietyUpToTheTop() throws IOException {
        JsonNode pet = act(3002, "feed");

        assertNeeds(pet, 100, 80, 80);
    }

    @Test
    void playingCheersUpButTires() throws IOException {
        JsonNode pet = act(3003, "play");

        assertNeeds(pet, 80, 100, 70);
    }

    @Test
    void aPetPutToBedSleepsUntilItIsRested() throws IOException {
        act(3004, "play");
        act(3004, "play");

        JsonNode pet = act(3004, "sleep");

        assertNeeds(pet, 80, 100, 60);
        assertThat(Duration.between(Instant.now(), Instant.parse(pet.path("asleepUntil").asText())))
                .isBetween(Duration.ofMinutes(119), Duration.ofMinutes(121));
    }

    @Test
    void aSleepingPetCannotBeCaredFor() throws IOException {
        act(3008, "sleep");

        int status = api().post().uri("/api/v1/pet/actions/play")
                .header("Authorization", signedIn(3008))
                .exchange((request, response) -> response.getStatusCode().value());

        assertThat(status).isEqualTo(409);
    }

    @Test
    void sleepRestoresEnergyAndThePetWakesUp() throws IOException {
        act(3009, "play");
        act(3009, "play");
        act(3009, "sleep");
        jdbc.sql("""
                UPDATE pets
                SET updated_at = updated_at - INTERVAL '3 hours', asleep_until = asleep_until - INTERVAL '3 hours'
                WHERE user_id = 3009
                """).update();

        JsonNode pet = get(3009);

        // Two hours asleep fill energy from 60 to 100, the third hour awake takes 2 off again.
        assertNeeds(pet, 68, 91, 98);
        assertThat(pet.path("asleepUntil").isNull()).isTrue();
    }

    @Test
    void needsDropWhileNobodyLooksAfterThePet() throws IOException {
        get(3005);
        jdbc.sql("UPDATE pets SET updated_at = :then WHERE user_id = 3005")
                .param("then", Timestamp.from(Instant.now().minus(Duration.ofHours(10))))
                .update();

        JsonNode pet = get(3005);

        assertNeeds(pet, 40, 50, 60);
    }

    @Test
    void keepsThePetBetweenVisits() throws IOException {
        act(3006, "feed");

        assertThat(get(3006).path("satiety").asInt()).isEqualTo(100);
    }

    @Test
    void rejectsUnknownActions() {
        int status = api().post().uri("/api/v1/pet/actions/dance")
                .header("Authorization", signedIn(3007))
                .exchange((request, response) -> response.getStatusCode().value());

        assertThat(status).isEqualTo(404);
    }

    @Test
    void rejectsRequestsOpenedOutsideTelegram() {
        int status = api().get().uri("/api/v1/pet")
                .exchange((request, response) -> response.getStatusCode().value());

        assertThat(status).isEqualTo(401);
    }

    @Test
    void strokingCheersThePetUpALittle() throws IOException {
        get(3012);

        JsonNode pet = stroke(3012);

        assertNeeds(pet, 80, 82, 80);
    }

    @Test
    void strokesRightAfterEachOtherCountOnce() throws IOException {
        get(3013);

        stroke(3013);
        JsonNode second = stroke(3013);

        assertThat(second.path("mood").asInt()).isEqualTo(82);
    }

    @Test
    void aStrokeCountsAgainAfterTheCooldown() throws IOException {
        get(3014);
        stroke(3014);
        jdbc.sql("UPDATE pets SET last_stroked_at = last_stroked_at - INTERVAL '31 seconds' WHERE user_id = 3014")
                .update();

        JsonNode pet = stroke(3014);

        assertThat(pet.path("mood").asInt()).isEqualTo(84);
    }

    @Test
    void aSleepingPetCannotBeStroked() throws IOException {
        act(3015, "sleep");

        int status = api().post().uri("/api/v1/pet/stroke")
                .header("Authorization", signedIn(3015))
                .exchange((request, response) -> response.getStatusCode().value());

        assertThat(status).isEqualTo(409);
    }

    private JsonNode stroke(long userId) throws IOException {
        return json(api().post().uri("/api/v1/pet/stroke")
                .header("Authorization", signedIn(userId))
                .retrieve().body(String.class));
    }

    private JsonNode get(long userId) throws IOException {
        return json(api().get().uri("/api/v1/pet")
                .header("Authorization", signedIn(userId))
                .retrieve().body(String.class));
    }

    private JsonNode act(long userId, String action) throws IOException {
        return json(api().post().uri("/api/v1/pet/actions/{action}", action)
                .header("Authorization", signedIn(userId))
                .retrieve().body(String.class));
    }

    private JsonNode name(long userId, String name) throws IOException {
        return json(naming(userId, name).retrieve().body(String.class));
    }

    private int nameStatus(long userId, String name) {
        return naming(userId, name).exchange((request, response) -> response.getStatusCode().value());
    }

    private RestClient.RequestBodySpec naming(long userId, String name) {
        return api().put().uri("/api/v1/pet/name")
                .header("Authorization", signedIn(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ObjectMapper().createObjectNode().put("name", name).toString());
    }

    private static void assertNeeds(JsonNode pet, int satiety, int mood, int energy) {
        assertThat(pet.path("satiety").asInt()).isEqualTo(satiety);
        assertThat(pet.path("mood").asInt()).isEqualTo(mood);
        assertThat(pet.path("energy").asInt()).isEqualTo(energy);
    }

    private static String signedIn(long userId) {
        String user = "{\"id\":" + userId + ",\"first_name\":\"Pet owner\",\"language_code\":\"en\"}";
        return "tma " + TelegramLaunchData.signed(BOT_TOKEN, user, Instant.now());
    }

    private static JsonNode json(String body) throws IOException {
        return new ObjectMapper().readTree(body);
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
