package com.tamagotchi.pet;

/** The pet after a care action, the balance after it and the coins the action earned. */
record CareOutcome(PetState pet, int coins, int earned) {
}
