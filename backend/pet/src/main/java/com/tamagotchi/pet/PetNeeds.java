package com.tamagotchi.pet;

import java.time.Duration;

/** The pet's needs in percent at one moment. */
record PetNeeds(double satiety, double mood, double energy) {

    private static final double SECONDS_PER_HOUR = Duration.ofHours(1).toSeconds();

    static PetNeeds newborn() {
        return new PetNeeds(PetRules.START, PetRules.START, PetRules.START);
    }

    /** The needs after the pet spent this long without care. */
    PetNeeds after(Duration elapsed) {
        double hours = Math.max(0, elapsed.toSeconds()) / SECONDS_PER_HOUR;
        return plus(-PetRules.SATIETY_DROP_PER_HOUR * hours,
                -PetRules.MOOD_DROP_PER_HOUR * hours,
                -PetRules.ENERGY_DROP_PER_HOUR * hours);
    }

    PetNeeds plus(double satietyChange, double moodChange, double energyChange) {
        return new PetNeeds(clamp(satiety + satietyChange), clamp(mood + moodChange), clamp(energy + energyChange));
    }

    private static double clamp(double value) {
        return Math.clamp(value, PetRules.MIN, PetRules.MAX);
    }
}
