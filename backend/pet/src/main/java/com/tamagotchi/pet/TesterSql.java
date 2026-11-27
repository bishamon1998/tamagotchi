package com.tamagotchi.pet;

/** SQL for the tester tools: moving a user's clock back and wiping their progress. */
final class TesterSql {

    /** Moves the pet's timestamps back, as if that much time had passed since the last visit. */
    static final String SKIP_PET_TIME = """
            UPDATE pets
            SET updated_at = updated_at - make_interval(secs => :seconds),
                asleep_until = asleep_until - make_interval(secs => :seconds)
            WHERE user_id = :userId
            """;

    /** Moves the last daily bonus back a day, so the next visit counts as the next day of the streak. */
    static final String SKIP_BONUS_DAY = """
            UPDATE wallets
            SET last_bonus_day = last_bonus_day - 1
            WHERE user_id = :userId
            """;

    /** Moves the ledger back a day, so coins earned today no longer count towards today's care cap. */
    static final String SKIP_LEDGER_DAY = """
            UPDATE coin_ledger
            SET created_at = created_at - INTERVAL '1 day'
            WHERE user_id = :userId
            """;

    static final String DELETE_PET = "DELETE FROM pets WHERE user_id = :userId";
    static final String DELETE_WALLET = "DELETE FROM wallets WHERE user_id = :userId";
    static final String DELETE_LEDGER = "DELETE FROM coin_ledger WHERE user_id = :userId";
    static final String DELETE_DECOR = "DELETE FROM owned_decor WHERE user_id = :userId";

    private TesterSql() {
    }
}
