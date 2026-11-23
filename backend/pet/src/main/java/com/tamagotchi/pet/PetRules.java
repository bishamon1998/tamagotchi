package com.tamagotchi.pet;

/** Game balance: how fast needs drop and how much each action restores. Needs are percentages. */
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
    static final double SLEEP_ENERGY = 40;

    private PetRules() {
    }
}
