package com.tamagotchi.pet;

import java.util.List;

/** The catalogue as one user sees it, with their balance. */
record DecorShop(int coins, List<Offer> items) {

    /** One catalogue item: what it costs and whether the user owns it and has it in the room. */
    record Offer(DecorItem item, boolean owned, boolean placed) {
    }
}
