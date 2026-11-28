package com.tamagotchi.pet;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Stores each user's seeds, vase, beds, basket of picked flowers and the flowers they have discovered. */
@Repository
class GardenRepository {

    private final JdbcClient jdbc;

    GardenRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    /** The user's seeds, locked until the transaction ends; opens an empty garden on first use. */
    SeedStore lockSeeds(long userId) {
        jdbc.sql(GardenSql.CREATE_IF_MISSING).param("userId", userId).update();
        return jdbc.sql(GardenSql.FIND_FOR_UPDATE)
                .param("userId", userId)
                .query((row, rowNumber) -> new SeedStore(row.getInt("common_seeds"), row.getInt("rare_seeds"),
                        toDay(row.getDate("free_seed_day"))))
                .single();
    }

    void saveSeeds(long userId, SeedStore seeds) {
        jdbc.sql(GardenSql.SAVE)
                .param("userId", userId)
                .param("commonSeeds", seeds.common())
                .param("rareSeeds", seeds.rare())
                .param("freeSeedDay", toDate(seeds.freeSeedDay()), Types.DATE)
                .update();
    }

    List<Plant> plants(long userId) {
        return jdbc.sql(GardenSql.FIND_PLANTS).param("userId", userId).query(GardenRepository::plant).list();
    }

    void plant(long userId, Plant plant) {
        jdbc.sql(GardenSql.PLANT)
                .param("userId", userId)
                .param("bed", plant.bed())
                .param("flower", plant.flower().id())
                .param("seed", plant.seed().id())
                .param("plantedDay", Date.valueOf(plant.plantedDay()))
                .update();
    }

    void water(long userId, Plant plant) {
        jdbc.sql(GardenSql.WATER)
                .param("userId", userId)
                .param("bed", plant.bed())
                .param("waterings", plant.waterings())
                .param("lastWateredDay", Date.valueOf(plant.lastWateredDay()))
                .update();
    }

    void clearBed(long userId, int bed) {
        jdbc.sql(GardenSql.CLEAR_BED).param("userId", userId).param("bed", bed).update();
    }

    Set<Flower> discovered(long userId) {
        Set<Flower> flowers = EnumSet.noneOf(Flower.class);
        jdbc.sql(GardenSql.FIND_DISCOVERED)
                .param("userId", userId)
                .query((row, rowNumber) -> row.getString("flower"))
                .list()
                .forEach(id -> Flower.byId(id).ifPresent(flowers::add));
        return flowers;
    }

    void discover(long userId, Flower flower, Instant at) {
        jdbc.sql(GardenSql.DISCOVER)
                .param("userId", userId)
                .param("flower", flower.id())
                .param("discoveredAt", Timestamp.from(at))
                .update();
    }

    /** The flower in the vase at home, if any. */
    Optional<Flower> vase(long userId) {
        String id = jdbc.sql(GardenSql.FIND_VASE).param("userId", userId).query(String.class).optional().orElse(null);
        return id == null ? Optional.empty() : Flower.byId(id);
    }

    void putInVase(long userId, Flower flower) {
        jdbc.sql(GardenSql.SET_VASE).param("userId", userId).param("flower", flower.id()).update();
    }

    /** Picked flowers in the basket and how many of each. */
    Map<Flower, Integer> basket(long userId) {
        Map<Flower, Integer> basket = new EnumMap<>(Flower.class);
        jdbc.sql(GardenSql.FIND_BASKET)
                .param("userId", userId)
                .query((row, rowNumber) -> Map.entry(row.getString("flower"), row.getInt("count")))
                .list()
                .forEach(entry -> Flower.byId(entry.getKey())
                        .ifPresent(flower -> basket.put(flower, entry.getValue())));
        return basket;
    }

    void addToBasket(long userId, Flower flower) {
        jdbc.sql(GardenSql.ADD_TO_BASKET).param("userId", userId).param("flower", flower.id()).update();
    }

    /** Takes one of the flower out of the basket; false when there is none. */
    boolean takeFromBasket(long userId, Flower flower) {
        return jdbc.sql(GardenSql.TAKE_FROM_BASKET).param("userId", userId).param("flower", flower.id()).update() == 1;
    }

    private static Plant plant(ResultSet row, int rowNumber) throws SQLException {
        return new Plant(
                row.getInt("bed"),
                Flower.byId(row.getString("flower")).orElseThrow(),
                SeedKind.byId(row.getString("seed")).orElseThrow(),
                row.getDate("planted_day").toLocalDate(),
                row.getInt("waterings"),
                toDay(row.getDate("last_watered_day")));
    }

    private static LocalDate toDay(Date date) {
        return date == null ? null : date.toLocalDate();
    }

    private static Date toDate(LocalDate day) {
        return day == null ? null : Date.valueOf(day);
    }
}
