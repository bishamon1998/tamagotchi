package com.tamagotchi.pet;

/** Why coins were credited or debited; stored in the coin ledger. */
enum CoinReason {
    CARE,
    DAILY_BONUS,
    TREAT,
    DECOR,
    /** Seeds bought for the garden. */
    SEEDS,
    /** A bloomed flower picked in the garden. */
    FLOWERS,
    /** Coins a tester added to their own wallet with the tester tools. */
    TESTER
}
