package com.tamagotchi.pet;

import java.time.Duration;
import java.time.ZoneId;

/** Game balance: how fast needs drop, how much each action restores, and how coins are earned and spent. */
final class PetRules {

    static final double MAX = 100;
    static final double MIN = 0;

    /** A new pet is content but not full, so the first care action visibly helps. */
    static final double START = 80;

    // Per hour: a pet left alone gets hungry in about a day, bored a bit later, tired last.
    static final double SATIETY_DROP_PER_HOUR = 4;
    static final double MOOD_DROP_PER_HOUR = 3;
    static final double ENERGY_DROP_PER_HOUR = 2;

    static final double FEED_SATIETY = 30;
    static final double PLAY_MOOD = 25;
    /** Playing tires the pet, so play and sleep have to alternate. */
    static final double PLAY_ENERGY = -10;

    /**
     * A pet put to bed sleeps until its energy is full, gaining this much per hour, and cannot be cared for
     * meanwhile. Sleep takes hours, so play and sleep cannot be alternated to farm coins.
     */
    static final double SLEEP_ENERGY_PER_HOUR = 20;
    /** Even a rested pet naps at least this long once put to bed. */
    static final Duration SHORTEST_SLEEP = Duration.ofHours(1);

    /** A treat fills more than plain food and cheers the pet up. Plain food stays free so the pet never starves. */
    static final double TREAT_SATIETY = 40;
    static final double TREAT_MOOD = 15;
    static final int TREAT_PRICE = 5;

    /** Care pays only when its need was below this before the action, so looking after a full pet earns nothing. */
    static final double USEFUL_CARE_BELOW = 70;
    static final int CARE_REWARD = 2;
    /** Care earns at most this much per game day; the daily bonus comes on top. */
    static final int DAILY_CARE_CAP = 20;

    /** The first visit of a game day pays a bonus that grows by a step for each day in a row, up to a week. */
    static final int DAILY_BONUS = 5;
    static final int DAILY_BONUS_STEP = 2;
    static final int LONGEST_REWARDED_STREAK = 7;

    /** Game days (daily bonus, care cap) start at midnight in this zone, where most players live. */
    static final ZoneId DAY_ZONE = ZoneId.of("Europe/Moscow");

    // Decor prices: two weeks or so of daily care and visits buy the whole catalogue.
    static final int STRIPED_RUG_PRICE = 60;
    static final int DOG_BED_PRICE = 90;
    static final int CEILING_LAMP_PRICE = 70;
    static final int CORGI_PORTRAIT_PRICE = 120;
    static final int SEA_PICTURE_PRICE = 100;

    /** Stroking cheers the pet up a little, at most once per cooldown, and never pays coins. */
    static final double STROKE_MOOD = 2;
    static final Duration STROKE_COOLDOWN = Duration.ofSeconds(30);

    /** Long enough for a real name or a nickname, short enough to fit next to the coins on a phone. */
    static final int LONGEST_NAME = 20;

    /** Tester tools: a top-up that buys any decor item, and a skip long enough to see needs drop or a nap end. */
    static final int TESTER_COINS = 100;
    static final Duration TESTER_TIME_SKIP = Duration.ofHours(3);

    private PetRules() {
    }
}
