package com.tamagotchi.pet;

/** SQL for the owned_decor table. */
final class DecorSql {

    static final String FIND_OWNED = """
            SELECT item, placed
            FROM owned_decor
            WHERE user_id = :userId
            """;

    static final String ADD = """
            INSERT INTO owned_decor (user_id, item, placed, bought_at)
            VALUES (:userId, :item, FALSE, :boughtAt)
            """;

    static final String SET_PLACED = """
            UPDATE owned_decor
            SET placed = :placed
            WHERE user_id = :userId AND item IN (:items)
            """;

    private DecorSql() {
    }
}
