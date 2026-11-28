package com.tamagotchi.pet;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * A plant in a bed: the flower it will be (hidden until it blooms), the seed it came from, the game day it was
 * planted, how many days it was watered and the last of them ({@code null} if never).
 */
record Plant(int bed, Flower flower, SeedKind seed, LocalDate plantedDay, int waterings, LocalDate lastWateredDay) {

    enum Stage { SEED, SPROUT, BUD, BLOOM }

    Stage stage() {
        return Stage.values()[Math.min(waterings, GardenRules.WATERINGS_TO_BLOOM)];
    }

    boolean bloomed() {
        return waterings >= GardenRules.WATERINGS_TO_BLOOM;
    }

    boolean wateredOn(LocalDate day) {
        return day.equals(lastWateredDay);
    }

    /** Whole days without water before {@code today}; today itself does not count until it is over. */
    int dryDays(LocalDate today) {
        LocalDate since = lastWateredDay == null ? plantedDay : lastWateredDay;
        return (int) Math.max(0, ChronoUnit.DAYS.between(since, today) - 1);
    }

    /** A bloomed flower needs no more water; anything else wilts and then dies when it stays dry. */
    boolean wilting(LocalDate today) {
        return !bloomed() && dryDays(today) >= GardenRules.DRY_DAYS_TO_WILT;
    }

    boolean dead(LocalDate today) {
        return !bloomed() && dryDays(today) >= GardenRules.DRY_DAYS_TO_DIE;
    }

    Plant wateredToday(LocalDate today) {
        return new Plant(bed, flower, seed, plantedDay, waterings + 1, today);
    }
}
