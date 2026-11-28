package com.tamagotchi.pet;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The garden as the user sees it now: their coins and seeds, whether this visit brought the free daily seed, the
 * living plants, the picked flowers in the basket, the flower in the vase at home ({@code null} if none) and the
 * flowers already discovered. {@code today} is the game day the plants are judged on.
 */
record GardenView(int coins, SeedStore seeds, boolean freeSeedGiven, List<Plant> plants, Map<Flower, Integer> basket,
                  Flower vase, Set<Flower> discovered, LocalDate today) {
}
