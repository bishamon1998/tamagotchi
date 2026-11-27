package com.tamagotchi.pet;

import java.util.Locale;
import java.util.function.ToDoubleFunction;
import java.util.function.UnaryOperator;

/** Care actions from the Mini App buttons. Each looks after one need; a treat costs coins. */
enum PetAction {
    FEED(PetNeeds::satiety, 0, needs -> needs.plus(PetRules.FEED_SATIETY, 0, 0)),
    TREAT(PetNeeds::satiety, PetRules.TREAT_PRICE, needs -> needs.plus(PetRules.TREAT_SATIETY, PetRules.TREAT_MOOD, 0)),
    PLAY(PetNeeds::mood, 0, needs -> needs.plus(0, PetRules.PLAY_MOOD, PetRules.PLAY_ENERGY)),
    // Sleep changes nothing at once: the pet regains energy while it sleeps (see PetNeeds#afterSleeping).
    SLEEP(PetNeeds::energy, 0, UnaryOperator.identity());

    private final ToDoubleFunction<PetNeeds> need;
    private final int price;
    private final UnaryOperator<PetNeeds> effect;

    PetAction(ToDoubleFunction<PetNeeds> need, int price, UnaryOperator<PetNeeds> effect) {
        this.need = need;
        this.price = price;
        this.effect = effect;
    }

    PetNeeds applyTo(PetNeeds needs) {
        return effect.apply(needs);
    }

    int price() {
        return price;
    }

    /** Whether the action puts the pet to bed, so it sleeps for a while and cannot be cared for meanwhile. */
    boolean putsToBed() {
        return this == SLEEP;
    }

    /** Free care pays when the pet really needed it; bought care never pays, or coins would breed coins. */
    boolean earnsCoins(PetNeeds before) {
        return price == 0 && need.applyAsDouble(before) < PetRules.USEFUL_CARE_BELOW;
    }

    /** The name in URLs and in the coin ledger. */
    String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}
