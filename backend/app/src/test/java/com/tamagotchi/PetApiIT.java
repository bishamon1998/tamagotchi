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
    void sleepingRestoresEnergy() throws IOException {
        act(3004, "play");
        act(3004, "play");

        JsonNode pet = act(3004, "sleep");

        assertNeeds(pet, 80, 100, 100);
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
