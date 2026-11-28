package com.tamagotchi.pet;

/** SQL for the garden: seeds and the vase per user, the plants in the beds, the basket and the flowers discovered. */
final class GardenSql {

    static final String CREATE_IF_MISSING = """
            INSERT INTO gardens (user_id, common_seeds, rare_seeds, free_seed_day)
            VALUES (:userId, 0, 0, NULL)
            ON CONFLICT (user_id) DO NOTHING
            """;

    /** Locks the garden so two quick taps cannot plant the same seed or water the same bed twice. */
    static final String FIND_FOR_UPDATE = """
            SELECT common_seeds, rare_seeds, free_seed_day
            FROM gardens
            WHERE user_id = :userId
            FOR UPDATE
            """;

    static final String SAVE = """
            UPDATE gardens
            SET common_seeds = :commonSeeds, rare_seeds = :rareSeeds, free_seed_day = :freeSeedDay
            WHERE user_id = :userId
            """;

    static final String FIND_PLANTS = """
            SELECT bed, flower, seed, planted_day, waterings, last_watered_day
            FROM garden_plants
            WHERE user_id = :userId
            ORDER BY bed
            """;

    static final String PLANT = """
            INSERT INTO garden_plants (user_id, bed, flower, seed, planted_day, waterings, last_watered_day)
            VALUES (:userId, :bed, :flower, :seed, :plantedDay, 0, NULL)
            """;

    static final String WATER = """
            UPDATE garden_plants
            SET waterings = :waterings, last_watered_day = :lastWateredDay
            WHERE user_id = :userId AND bed = :bed
            """;

    static final String CLEAR_BED = "DELETE FROM garden_plants WHERE user_id = :userId AND bed = :bed";

    static final String FIND_DISCOVERED = "SELECT flower FROM discovered_flowers WHERE user_id = :userId";

    static final String DISCOVER = """
            INSERT INTO discovered_flowers (user_id, flower, discovered_at)
            VALUES (:userId, :flower, :discoveredAt)
            ON CONFLICT (user_id, flower) DO NOTHING
            """;

    static final String FIND_VASE = "SELECT vase_flower FROM gardens WHERE user_id = :userId";

    static final String SET_VASE = "UPDATE gardens SET vase_flower = :flower WHERE user_id = :userId";

    static final String FIND_BASKET = """
            SELECT flower, count
            FROM flower_basket
            WHERE user_id = :userId AND count > 0
            """;

    static final String ADD_TO_BASKET = """
            INSERT INTO flower_basket (user_id, flower, count)
            VALUES (:userId, :flower, 1)
            ON CONFLICT (user_id, flower) DO UPDATE SET count = flower_basket.count + 1
            """;

    /** Takes one flower out of the basket if there is one; the update count says whether it was there. */
    static final String TAKE_FROM_BASKET = """
            UPDATE flower_basket
            SET count = count - 1
            WHERE user_id = :userId AND flower = :flower AND count > 0
            """;

    private GardenSql() {
    }
}
