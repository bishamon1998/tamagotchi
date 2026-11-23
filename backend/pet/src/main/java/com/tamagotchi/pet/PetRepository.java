package com.tamagotchi.pet;

import java.sql.Timestamp;
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

    /** Gives the user a newborn pet unless they already have one. */
    void createIfMissing(long userId, Instant now) {
        write(PetSql.CREATE_IF_MISSING, userId, PetNeeds.newborn(), now);
    }

    /** The user's pet, locked until the transaction ends. Call {@link #createIfMissing} first. */
    StoredPet findForUpdate(long userId) {
        return jdbc.sql(PetSql.FIND_FOR_UPDATE)
                .param("userId", userId)
                .query((row, rowNumber) -> new StoredPet(
                        new PetNeeds(row.getDouble("satiety"), row.getDouble("mood"), row.getDouble("energy")),
                        row.getTimestamp("updated_at").toInstant()))
                .single();
    }

    void save(long userId, PetNeeds needs, Instant updatedAt) {
        write(PetSql.SAVE, userId, needs, updatedAt);
    }

    private void write(String sql, long userId, PetNeeds needs, Instant updatedAt) {
        jdbc.sql(sql)
                .param("userId", userId)
                .param("satiety", needs.satiety())
                .param("mood", needs.mood())
                .param("energy", needs.energy())
                .param("updatedAt", Timestamp.from(updatedAt))
                .update();
    }
}
