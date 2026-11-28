package com.tamagotchi.pet;

/** Garden balance: beds, growth, how forgetting to water hurts, seed prices and what the market pays for flowers. */
final class GardenRules {

    static final int BEDS = 6;

    /** A plant grows one stage per day it is watered: seed, sprout, bud, then it blooms on the third watering. */
    static final int WATERINGS_TO_BLOOM = 3;

    /** A plant that missed this many whole days of water wilts; watering it saves it. */
    static final int DRY_DAYS_TO_WILT = 1;
    /** A plant that missed this many whole days in a row dies, and its bed is free again. */
    static final int DRY_DAYS_TO_DIE = 2;

    /** Common seeds are cheap and mostly grow common flowers; rare ones cost more and bloom rarer. */
    static final int COMMON_SEED_PRICE = 10;
    static final int RARE_SEED_PRICE = 40;

    // Chances (out of 100) of each rarity per seed kind.
    static final int[] COMMON_SEED_ODDS = {80, 18, 2};
    static final int[] RARE_SEED_ODDS = {30, 50, 20};

    // What the market pays for a flower, by rarity: common seeds pay back a little, rare seeds are bought to collect.
    static final int COMMON_FLOWER_PRICE = 8;
    static final int UNCOMMON_FLOWER_PRICE = 20;
    static final int RARE_FLOWER_PRICE = 60;

    /** Tester tools: seeds to plant at once. */
    static final int TESTER_COMMON_SEEDS = 5;
    static final int TESTER_RARE_SEEDS = 2;

    private GardenRules() {
    }
}
