package com.tamagotchi.pet;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Stores the decor items each user bought and whether each one is placed in the room. */
@Repository
class DecorRepository {

    private final JdbcClient jdbc;

    DecorRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * The user's items by id, true for the placed ones. Ids are kept as text: a row for an item that left the
     * catalogue must not break the shop.
     */
    Map<String, Boolean> findOwned(long userId) {
        return jdbc.sql(DecorSql.FIND_OWNED)
                .param("userId", userId)
                .query((row, rowNumber) -> Map.entry(row.getString("item"), row.getBoolean("placed")))
                .list()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /** Adds a bought item, not placed yet. */
    void add(long userId, DecorItem item, Instant boughtAt) {
        jdbc.sql(DecorSql.ADD)
                .param("userId", userId)
                .param("item", item.id())
                .param("boughtAt", Timestamp.from(boughtAt))
                .update();
    }

    void setPlaced(long userId, Collection<DecorItem> items, boolean placed) {
        jdbc.sql(DecorSql.SET_PLACED)
                .param("userId", userId)
                .param("items", items.stream().map(DecorItem::id).toList())
                .param("placed", placed)
                .update();
    }
}
