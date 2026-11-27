package com.tamagotchi.pet;

import java.time.Instant;

/**
 * The pet at one moment: its needs, when it wakes up while it sleeps ({@code null} when awake) and the name the user
 * gave it ({@code null} until they do).
 */
record PetState(PetNeeds needs, Instant asleepUntil, String name) {

    boolean asleep() {
        return asleepUntil != null;
    }
}
