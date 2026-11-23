package com.tamagotchi.pet;

import java.time.Instant;

/** The pet at one moment: its needs and, while it sleeps, when it wakes up ({@code null} when awake). */
record PetState(PetNeeds needs, Instant asleepUntil) {

    boolean asleep() {
        return asleepUntil != null;
    }
}
