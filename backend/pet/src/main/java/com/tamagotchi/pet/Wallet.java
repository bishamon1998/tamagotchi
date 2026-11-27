package com.tamagotchi.pet;

import java.time.LocalDate;

/** A wallet row: the balance and the daily-visit streak. {@code lastBonusDay} is null before the first bonus. */
record Wallet(int coins, int streak, LocalDate lastBonusDay) {

    /** Days in a row including {@code today}: one more if the last bonus was yesterday, else a fresh start. */
    int streakOn(LocalDate today) {
        return today.minusDays(1).equals(lastBonusDay) ? streak + 1 : 1;
    }

    boolean bonusClaimedOn(LocalDate today) {
        return today.equals(lastBonusDay);
    }

    Wallet withCoins(int newCoins) {
        return new Wallet(newCoins, streak, lastBonusDay);
    }
}
