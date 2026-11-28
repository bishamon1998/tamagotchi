package com.tamagotchi.pet;

import java.time.LocalDate;

/** The seeds a user has in hand and the game day they last got the free daily seed. */
record SeedStore(int common, int rare, LocalDate freeSeedDay) {

    int count(SeedKind kind) {
        return kind == SeedKind.COMMON ? common : rare;
    }

    SeedStore plus(SeedKind kind, int change) {
        return kind == SeedKind.COMMON
                ? new SeedStore(common + change, rare, freeSeedDay)
                : new SeedStore(common, rare + change, freeSeedDay);
    }

    boolean freeSeedTakenOn(LocalDate day) {
        return day.equals(freeSeedDay);
    }

    /** One common seed for the day's first garden visit. */
    SeedStore withFreeSeed(LocalDate day) {
        return new SeedStore(common + 1, rare, day);
    }
}
