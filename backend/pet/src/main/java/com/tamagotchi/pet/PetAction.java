package com.tamagotchi.pet;

import java.util.function.UnaryOperator;

/** Care actions from the Mini App buttons. */
enum PetAction {
    FEED(needs -> needs.plus(PetRules.FEED_SATIETY, 0, 0)),
    PLAY(needs -> needs.plus(0, PetRules.PLAY_MOOD, PetRules.PLAY_ENERGY)),
    SLEEP(needs -> needs.plus(0, 0, PetRules.SLEEP_ENERGY));

    private final UnaryOperator<PetNeeds> effect;

    PetAction(UnaryOperator<PetNeeds> effect) {
        this.effect = effect;
    }

    PetNeeds applyTo(PetNeeds needs) {
        return effect.apply(needs);
    }
}
