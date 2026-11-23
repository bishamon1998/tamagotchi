package com.tamagotchi.pet;

import java.time.Instant;

/** A pet row: needs as of {@code updatedAt}. */
record StoredPet(PetNeeds needs, Instant updatedAt) {
}
