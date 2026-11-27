package com.tamagotchi.pet;

/** SQL for the pets table. */
final class PetSql {

    static final String CREATE_IF_MISSING = """
            INSERT INTO pets (user_id, satiety, mood, energy, updated_at, asleep_until)
            VALUES (:userId, :satiety, :mood, :energy, :updatedAt, NULL)
            ON CONFLICT (user_id) DO NOTHING
            """;

    /** Locks the row so two quick taps cannot both apply their action to the same old state. */
    static final String FIND_FOR_UPDATE = """
            SELECT satiety, mood, energy, updated_at, asleep_until, name
            FROM pets
            WHERE user_id = :userId
            FOR UPDATE
            """;

    static final String SAVE = """
            UPDATE pets
            SET satiety = :satiety, mood = :mood, energy = :energy, updated_at = :updatedAt,
                asleep_until = :asleepUntil
            WHERE user_id = :userId
            """;

    static final String RENAME = """
            UPDATE pets
            SET name = :name
            WHERE user_id = :userId
            """;

    private PetSql() {
    }
}
