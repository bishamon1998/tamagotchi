package com.tamagotchi.pet;

import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * The decor shop: items are bought once with coins and placed in the room, one per slot. Every change locks the
 * user's wallet first, so two quick taps cannot buy the same item twice or place two items in one slot.
 */
@Service
class DecorService {

    private final DecorRepository decor;
    private final WalletService wallets;
    private final Clock clock;

    DecorService(DecorRepository decor, WalletService wallets, Clock clock) {
        this.decor = decor;
        this.wallets = wallets;
        this.clock = clock;
    }

    @Transactional
    DecorShop shop(long userId) {
        int coins = wallets.coins(userId);
        Map<String, Boolean> owned = decor.findOwned(userId);
        List<DecorShop.Offer> offers = Arrays.stream(DecorItem.values())
                .map(item -> new DecorShop.Offer(item, owned.containsKey(item.id()), owned.getOrDefault(item.id(), false)))
                .toList();
        return new DecorShop(coins, offers);
    }

    /** Buys the item and puts it in the room right away, in place of whatever stood in its slot. */
    @Transactional
    DecorShop buy(long userId, DecorItem item) {
        Instant now = clock.instant();
        wallets.lock(userId);
        if (decor.findOwned(userId).containsKey(item.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already owned: " + item.id());
        }
        wallets.spend(userId, item.price(), CoinReason.DECOR, item.id(), now);
        decor.add(userId, item, now);
        putInRoom(userId, item);
        return shop(userId);
    }

    /** Puts an owned item in the room in place of whatever stood in its slot. */
    @Transactional
    DecorShop place(long userId, DecorItem item) {
        wallets.lock(userId);
        if (!decor.findOwned(userId).containsKey(item.id())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Not owned: " + item.id());
        }
        putInRoom(userId, item);
        return shop(userId);
    }

    /** Takes the item out of the room; it stays owned. */
    @Transactional
    DecorShop remove(long userId, DecorItem item) {
        wallets.lock(userId);
        decor.setPlaced(userId, List.of(item), false);
        return shop(userId);
    }

    private void putInRoom(long userId, DecorItem item) {
        List<DecorItem> sameSlot = Arrays.stream(DecorItem.values()).filter(other -> other.slot() == item.slot()).toList();
        decor.setPlaced(userId, sameSlot, false);
        decor.setPlaced(userId, List.of(item), true);
    }
}
