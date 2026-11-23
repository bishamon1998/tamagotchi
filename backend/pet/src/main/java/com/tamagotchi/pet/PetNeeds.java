package com.tamagotchi.pet;

import java.time.Duration;

/** The pet's needs in percent at one moment. */
record PetNeeds(double satiety, double mood, double energy) {

    private static final double SECONDS_PER_HOUR = Duration.ofHours(1).toSeconds();

    static PetNeeds newborn() {
        return new PetNeeds(PetRules.START, PetRules.START, PetRules.START);
    }

    /** The needs after the pet spent this long awake without care. */
    PetNeeds after(Duration elapsed) {
        return over(elapsed, -PetRules.ENERGY_DROP_PER_HOUR);
    }

    /** The needs after the pet slept this long: it still gets hungry and bored, but regains energy. */
    PetNeeds afterSleeping(Duration elapsed) {
        return over(elapsed, PetRules.SLEEP_ENERGY_PER_HOUR);
    }

    /** How long the pet sleeps once put to bed: until its energy is full, but never less than a short nap. */
    Duration sleepLength() {
        double hours = (PetRules.MAX - energy) / PetRules.SLEEP_ENERGY_PER_HOUR;
        Duration untilRested = Duration.ofSeconds(Math.round(hours * SECONDS_PER_HOUR));
        return untilRested.compareTo(PetRules.SHORTEST_SLEEP) < 0 ? PetRules.SHORTEST_SLEEP : untilRested;
    }

    PetNeeds plus(double satietyChange, double moodChange, double energyChange) {
        return new PetNeeds(clamp(satiety + satietyChange), clamp(mood + moodChange), clamp(energy + energyChange));
    }

    private PetNeeds over(Duration elapsed, double energyPerHour) {
        double hours = Math.max(0, elapsed.toSeconds()) / SECONDS_PER_HOUR;
        return plus(-PetRules.SATIETY_DROP_PER_HOUR * hours, -PetRules.MOOD_DROP_PER_HOUR * hours, energyPerHour * hours);
    }

    private static double clamp(double value) {
        return Math.clamp(value, PetRules.MIN, PetRules.MAX);
    }
}
