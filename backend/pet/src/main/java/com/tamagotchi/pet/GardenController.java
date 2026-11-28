package com.tamagotchi.pet;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tamagotchi.miniapp.TelegramUser;

/**
 * The signed-in user's garden. Every call answers with the whole garden, so the Mini App redraws the beds, seeds and
 * balance from one response. A plant's flower is sent only once it has bloomed: until then nobody knows what grows.
 */
@RestController
class GardenController {

    /**
     * {@code basket} maps each picked flower to how many are in it; {@code vase} is the flower in the vase at home or
     * null; {@code flowerPrices} is what the market pays by rarity.
     */
    record Garden(int coins, Seeds seeds, boolean freeSeed, Prices prices, Map<String, Integer> flowerPrices,
                  List<Bed> beds, Map<String, Integer> basket, String vase, List<String> discovered) {

        static Garden of(GardenView view) {
            List<Bed> beds = IntStream.range(0, GardenRules.BEDS)
                    .mapToObj(bed -> new Bed(bed, view.plants().stream()
                            .filter(plant -> plant.bed() == bed)
                            .findFirst()
                            .map(plant -> Growing.of(plant, view))
                            .orElse(null)))
                    .toList();
            Map<String, Integer> basket = new LinkedHashMap<>();
            view.basket().forEach((flower, count) -> basket.put(flower.id(), count));
            Map<String, Integer> flowerPrices = new LinkedHashMap<>();
            for (FlowerRarity rarity : FlowerRarity.values()) {
                flowerPrices.put(rarity.name().toLowerCase(Locale.ROOT), rarity.price());
            }
            return new Garden(view.coins(), new Seeds(view.seeds().common(), view.seeds().rare()), view.freeSeedGiven(),
                    new Prices(SeedKind.COMMON.price(), SeedKind.RARE.price()), flowerPrices, beds, basket,
                    view.vase() == null ? null : view.vase().id(),
                    view.discovered().stream().map(Flower::id).toList());
        }
    }

    record Seeds(int common, int rare) {
    }

    record Prices(int common, int rare) {
    }

    /** {@code plant} is null for an empty bed. */
    record Bed(int bed, Growing plant) {
    }

    /** {@code stage} is seed, sprout, bud or bloom; {@code flower} is null until it blooms. */
    record Growing(String stage, boolean wilting, boolean wateredToday, String flower) {

        static Growing of(Plant plant, GardenView view) {
            return new Growing(plant.stage().name().toLowerCase(Locale.ROOT), plant.wilting(view.today()),
                    plant.wateredOn(view.today()), plant.bloomed() ? plant.flower().id() : null);
        }
    }

    private final GardenService garden;

    GardenController(GardenService garden) {
        this.garden = garden;
    }

    /** The day's first visit also gives the free daily seed ({@code freeSeed} is true in that answer). */
    @GetMapping("/api/v1/garden")
    Garden garden(TelegramUser user) {
        return Garden.of(garden.visit(user.id()));
    }

    @PostMapping("/api/v1/garden/seeds/{kind}/purchase")
    Garden buySeed(TelegramUser user, @PathVariable String kind) {
        return Garden.of(garden.buySeed(user.id(), seedKind(kind)));
    }

    @PostMapping("/api/v1/garden/beds/{bed}/plant/{kind}")
    Garden plant(TelegramUser user, @PathVariable int bed, @PathVariable String kind) {
        return Garden.of(garden.plant(user.id(), bed, seedKind(kind)));
    }

    @PostMapping("/api/v1/garden/beds/{bed}/water")
    Garden water(TelegramUser user, @PathVariable int bed) {
        return Garden.of(garden.water(user.id(), bed));
    }

    @PostMapping("/api/v1/garden/beds/{bed}/pick")
    Garden pick(TelegramUser user, @PathVariable int bed) {
        return Garden.of(garden.pick(user.id(), bed));
    }

    /** Sells one picked flower at the market. */
    @PostMapping("/api/v1/garden/basket/{flower}/sale")
    Garden sell(TelegramUser user, @PathVariable String flower) {
        return Garden.of(garden.sell(user.id(), flower(flower)));
    }

    /** Puts one picked flower in the vase at home. */
    @PutMapping("/api/v1/garden/vase/{flower}")
    Garden putInVase(TelegramUser user, @PathVariable String flower) {
        return Garden.of(garden.putInVase(user.id(), flower(flower)));
    }

    private static Flower flower(String flower) {
        return Flower.byId(flower)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown flower: " + flower));
    }

    private static SeedKind seedKind(String kind) {
        return SeedKind.byId(kind)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown seed: " + kind));
    }
}
