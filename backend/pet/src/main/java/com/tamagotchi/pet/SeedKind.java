package com.tamagotchi.pet;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** Seed packets: what they cost and the chance of each flower rarity they grow into. */
enum SeedKind {
    COMMON(GardenRules.COMMON_SEED_PRICE, GardenRules.COMMON_SEED_ODDS),
    RARE(GardenRules.RARE_SEED_PRICE, GardenRules.RARE_SEED_ODDS);

    private final int price;
    private final int[] odds;

    SeedKind(int price, int[] odds) {
        this.price = price;
        this.odds = odds;
    }

    int price() {
        return price;
    }

    /** Picks the flower this seed will grow into: first a rarity by the seed's odds, then a flower of that rarity. */
    Flower grow(RandomGenerator random) {
        int roll = random.nextInt(100);
        FlowerRarity[] rarities = FlowerRarity.values();
        int index = 0;
        while (index < rarities.length - 1 && roll >= odds[index]) {
            roll -= odds[index];
            index++;
        }
        var flowers = Flower.of(rarities[index]);
        return flowers.get(random.nextInt(flowers.size()));
    }

    String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    static Optional<SeedKind> byId(String id) {
        return Arrays.stream(values()).filter(kind -> kind.id().equals(id)).findFirst();
    }
}
