package com.tamagotchi.pet;

/** The wallet after a visit; {@code bonus} is 0 when the daily bonus was already paid today. */
record DailyCheckIn(Wallet wallet, int bonus) {
}
