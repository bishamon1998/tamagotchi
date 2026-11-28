package com.tamagotchi.pet;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/** Every flower that can grow in the garden. A new one needs only a line here, its art and its name in the Mini App. */
enum Flower {
    DAISY(FlowerRarity.COMMON),
    TULIP(FlowerRarity.COMMON),
    POPPY(FlowerRarity.COMMON),
    CORNFLOWER(FlowerRarity.COMMON),
    SUNFLOWER(FlowerRarity.COMMON),
    BLUEBELL(FlowerRarity.COMMON),
    ROSE(FlowerRarity.UNCOMMON),
    IRIS(FlowerRarity.UNCOMMON),
    LILY(FlowerRarity.UNCOMMON),
    PEONY(FlowerRarity.UNCOMMON),
    LOTUS(FlowerRarity.RARE),
    EDELWEISS(FlowerRarity.RARE);

    private final FlowerRarity rarity;

    Flower(FlowerRarity rarity) {
        this.rarity = rarity;
    }

    FlowerRarity rarity() {
        return rarity;
    }

    /** The name in the API and the database. */
    String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    static List<Flower> of(FlowerRarity rarity) {
        return Arrays.stream(values()).filter(flower -> flower.rarity == rarity).toList();
    }

    static Optional<Flower> byId(String id) {
        return Arrays.stream(values()).filter(flower -> flower.id().equals(id)).findFirst();
    }
}
