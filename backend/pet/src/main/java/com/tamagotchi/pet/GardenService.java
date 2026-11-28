package com.tamagotchi.pet;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * The garden: seeds (one free a day, more for coins), planting, daily watering, picking bloomed flowers into the
 * basket, and selling them at the market or putting one in the vase at home. Plants that stayed dry too long die and
 * free their bed. Every change locks the wallet first and the garden second, always
 * in that order, so two requests cannot deadlock. Refusals are 409 Conflict; a bed out of range is 404.
 */
@Service
class GardenService {

    private final GardenRepository garden;
    private final WalletService wallets;
    private final RandomGenerator random;
    private final Clock clock;

    GardenService(GardenRepository garden, WalletService wallets, RandomGenerator gardenRandom, Clock clock) {
        this.garden = garden;
        this.wallets = wallets;
        this.random = gardenRandom;
        this.clock = clock;
    }

    @Transactional
    GardenView visit(long userId) {
        wallets.lock(userId);
        return view(userId, clock.instant());
    }

    @Transactional
    GardenView buySeed(long userId, SeedKind kind) {
        Instant now = clock.instant();
        wallets.spend(userId, kind.price(), CoinReason.SEEDS, kind.id(), now);
        SeedStore seeds = garden.lockSeeds(userId);
        garden.saveSeeds(userId, seeds.plus(kind, 1));
        return view(userId, now);
    }

    /** Plants a seed of the kind in an empty bed; which flower it becomes is decided now and shown only at bloom. */
    @Transactional
    GardenView plant(long userId, int bed, SeedKind kind) {
        Instant now = clock.instant();
        wallets.lock(userId);
        requireBed(bed);
        SeedStore seeds = garden.lockSeeds(userId);
        if (seeds.count(kind) == 0) {
            throw conflict("No seeds of this kind");
        }
        if (livingPlant(userId, bed, gameDay(now)).isPresent()) {
            throw conflict("The bed is taken");
        }
        garden.saveSeeds(userId, seeds.plus(kind, -1));
        garden.plant(userId, new Plant(bed, kind.grow(random), kind, gameDay(now), 0, null));
        return view(userId, now);
    }

    /** Waters a growing plant once a game day; the watering that completes its growth makes it bloom. */
    @Transactional
    GardenView water(long userId, int bed) {
        Instant now = clock.instant();
        LocalDate today = gameDay(now);
        wallets.lock(userId);
        requireBed(bed);
        garden.lockSeeds(userId);
        Plant plant = livingPlant(userId, bed, today).orElseThrow(() -> conflict("Nothing grows in this bed"));
        if (plant.bloomed()) {
            throw conflict("A bloomed flower needs no water");
        }
        if (plant.wateredOn(today)) {
            throw conflict("Already watered today");
        }
        Plant watered = plant.wateredToday(today);
        garden.water(userId, watered);
        if (watered.bloomed()) {
            garden.discover(userId, watered.flower(), now);
        }
        return view(userId, now);
    }

    /** Picks a bloomed flower into the basket and frees the bed. */
    @Transactional
    GardenView pick(long userId, int bed) {
        Instant now = clock.instant();
        wallets.lock(userId);
        requireBed(bed);
        garden.lockSeeds(userId);
        Plant plant = livingPlant(userId, bed, gameDay(now))
                .filter(Plant::bloomed)
                .orElseThrow(() -> conflict("Nothing to pick in this bed"));
        garden.addToBasket(userId, plant.flower());
        garden.clearBed(userId, bed);
        return view(userId, now);
    }

    /** Sells one flower from the basket at the market for coins by its rarity. */
    @Transactional
    GardenView sell(long userId, Flower flower) {
        Instant now = clock.instant();
        wallets.lock(userId);
        garden.lockSeeds(userId);
        if (!garden.takeFromBasket(userId, flower)) {
            throw conflict("No such flower in the basket");
        }
        wallets.grant(userId, flower.rarity().price(), CoinReason.FLOWERS, now);
        return view(userId, now);
    }

    /** Puts one flower from the basket in the vase at home, in place of the one there before. */
    @Transactional
    GardenView putInVase(long userId, Flower flower) {
        Instant now = clock.instant();
        wallets.lock(userId);
        garden.lockSeeds(userId);
        if (!garden.takeFromBasket(userId, flower)) {
            throw conflict("No such flower in the basket");
        }
        garden.putInVase(userId, flower);
        return view(userId, now);
    }

    /** Hands out the free daily seed, clears dead plants and reads the garden. Call with the wallet locked. */
    private GardenView view(long userId, Instant now) {
        LocalDate today = gameDay(now);
        SeedStore seeds = garden.lockSeeds(userId);
        boolean freeSeed = !seeds.freeSeedTakenOn(today);
        if (freeSeed) {
            seeds = seeds.withFreeSeed(today);
            garden.saveSeeds(userId, seeds);
        }
        List<Plant> living = new ArrayList<>();
        for (Plant plant : garden.plants(userId)) {
            if (plant.dead(today)) {
                garden.clearBed(userId, plant.bed());
            } else {
                living.add(plant);
            }
        }
        return new GardenView(wallets.coins(userId), seeds, freeSeed, living, garden.basket(userId),
                garden.vase(userId).orElse(null), garden.discovered(userId), today);
    }

    private Optional<Plant> livingPlant(long userId, int bed, LocalDate today) {
        return garden.plants(userId).stream()
                .filter(plant -> plant.bed() == bed && !plant.dead(today))
                .findFirst();
    }

    private static void requireBed(int bed) {
        if (bed < 0 || bed >= GardenRules.BEDS) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No such bed: " + bed);
        }
    }

    private static ResponseStatusException conflict(String reason) {
        return new ResponseStatusException(HttpStatus.CONFLICT, reason);
    }

    private static LocalDate gameDay(Instant instant) {
        return LocalDate.ofInstant(instant, PetRules.DAY_ZONE);
    }
}
