package com.tamagotchi.pet;

import java.util.random.RandomGenerator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Randomness for the garden: which flower a seed grows into. A bean, so it can be swapped where needed. */
@Configuration
class GardenConfig {

    @Bean
    RandomGenerator gardenRandom() {
        return RandomGenerator.getDefault();
    }
}
