package com.tamagotchi.pet;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** The decor catalogue, in shop order. Ids are stored in the database, so an item must never be renamed. */
enum DecorItem {
    STRIPED_RUG(DecorSlot.RUG, PetRules.STRIPED_RUG_PRICE),
    DOG_BED(DecorSlot.BED, PetRules.DOG_BED_PRICE),
    CEILING_LAMP(DecorSlot.LAMP, PetRules.CEILING_LAMP_PRICE),
    CORGI_PORTRAIT(DecorSlot.WALL, PetRules.CORGI_PORTRAIT_PRICE),
    SEA_PICTURE(DecorSlot.WALL, PetRules.SEA_PICTURE_PRICE);

    private final DecorSlot slot;
    private final int price;

    DecorItem(DecorSlot slot, int price) {
        this.slot = slot;
        this.price = price;
    }

    DecorSlot slot() {
        return slot;
    }

    int price() {
        return price;
    }

    /** The name in URLs, API responses and the database, such as {@code striped-rug}. */
    String id() {
        return name().toLowerCase(Locale.ROOT).replace('_', '-');
    }

    static Optional<DecorItem> byId(String id) {
        return Arrays.stream(values()).filter(item -> item.id().equals(id)).findFirst();
    }
}
