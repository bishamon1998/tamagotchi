package com.tamagotchi.pet;

/** How rare a flower is: it decides the chance to grow it and the coins the market pays for it. */
enum FlowerRarity {
    COMMON(GardenRules.COMMON_FLOWER_PRICE),
    UNCOMMON(GardenRules.UNCOMMON_FLOWER_PRICE),
    RARE(GardenRules.RARE_FLOWER_PRICE);

    private final int price;

    FlowerRarity(int price) {
        this.price = price;
    }

    int price() {
        return price;
    }
}
