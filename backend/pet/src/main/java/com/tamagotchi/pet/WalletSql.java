package com.tamagotchi.pet;

/** SQL for the wallets and coin_ledger tables. */
final class WalletSql {

    static final String CREATE_IF_MISSING = """
            INSERT INTO wallets (user_id, coins, streak, last_bonus_day)
            VALUES (:userId, 0, 0, NULL)
            ON CONFLICT (user_id) DO NOTHING
            """;

    /** Locks the row so two requests cannot both spend the same coins. */
    static final String FIND_FOR_UPDATE = """
            SELECT coins, streak, last_bonus_day
            FROM wallets
            WHERE user_id = :userId
            FOR UPDATE
            """;

    static final String SAVE = """
            UPDATE wallets
            SET coins = :coins, streak = :streak, last_bonus_day = :lastBonusDay
            WHERE user_id = :userId
            """;

    static final String RECORD = """
            INSERT INTO coin_ledger (user_id, amount, reason, detail, created_at)
            VALUES (:userId, :amount, :reason, :detail, :createdAt)
            """;

    static final String EARNED_SINCE = """
            SELECT COALESCE(SUM(amount), 0)
            FROM coin_ledger
            WHERE user_id = :userId AND reason = :reason AND created_at >= :since
            """;

    private WalletSql() {
    }
}
