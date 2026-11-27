package com.tamagotchi.pet;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Stores one pet per Telegram user. */
@Repository
class PetRepository {

    private final JdbcClient jdbc;

    PetRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** Gives the user a newborn, awake pet unless they already have one. */
    void createIfMissing(long userId, Instant now) {
        PetNeeds needs = PetNeeds.newborn();
        jdbc.sql(PetSql.CREATE_IF_MISSING)
                .param("userId", userId)
                .param("satiety", needs.satiety())
                .param("mood", needs.mood())
                .param("energy", needs.energy())
                .param("updatedAt", Timestamp.from(now))
                .update();
    }

    /** The user's pet, locked until the transaction ends. Call {@link #createIfMissing} first. */
    StoredPet findForUpdate(long userId) {
        return jdbc.sql(PetSql.FIND_FOR_UPDATE)
                .param("userId", userId)
                .query((row, rowNumber) -> {
                    Timestamp asleepUntil = row.getTimestamp("asleep_until");
                    return new StoredPet(
                            new PetNeeds(row.getDouble("satiety"), row.getDouble("mood"), row.getDouble("energy")),
                            row.getTimestamp("updated_at").toInstant(),
                            asleepUntil == null ? null : asleepUntil.toInstant(),
                            row.getString("name"));
                })
                .single();
    }

    void rename(long userId, String name) {
        jdbc.sql(PetSql.RENAME).param("userId", userId).param("name", name).update();
    }

    /** Stores the pet as it is at {@code updatedAt}. */
    void save(long userId, PetState pet, Instant updatedAt) {
        PetNeeds needs = pet.needs();
        jdbc.sql(PetSql.SAVE)
                .param("userId", userId)
                .param("satiety", needs.satiety())
                .param("mood", needs.mood())
                .param("energy", needs.energy())
                .param("updatedAt", Timestamp.from(updatedAt))
                .param("asleepUntil", pet.asleep() ? Timestamp.from(pet.asleepUntil()) : null, Types.TIMESTAMP)
                .update();
    }
}
