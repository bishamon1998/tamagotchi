package com.tamagotchi.pet;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tamagotchi.miniapp.TelegramUser;

/**
 * The decor shop for the signed-in user. Every call answers with the whole shop, so the Mini App redraws the room
 * and the balance from one response. Buying without enough coins, buying twice or placing an item the user does
 * not own is refused with 409.
 */
@RestController
class DecorController {

    record Shop(int coins, List<Item> items) {

        static Shop of(DecorShop shop) {
            return new Shop(shop.coins(), shop.items().stream().map(Item::of).toList());
        }
    }

    record Item(String id, String slot, int price, boolean owned, boolean placed) {

        static Item of(DecorShop.Offer offer) {
            DecorItem item = offer.item();
            return new Item(item.id(), item.slot().name().toLowerCase(Locale.ROOT), item.price(), offer.owned(), offer.placed());
        }
    }

    private final DecorService decor;

    DecorController(DecorService decor) {
        this.decor = decor;
    }

    @GetMapping("/api/v1/decor")
    Shop shop(TelegramUser user) {
        return Shop.of(decor.shop(user.id()));
    }

    @PostMapping("/api/v1/decor/{item}/purchase")
    Shop buy(TelegramUser user, @PathVariable String item) {
        return Shop.of(decor.buy(user.id(), parse(item)));
    }

    @PutMapping("/api/v1/decor/{item}/placement")
    Shop place(TelegramUser user, @PathVariable String item) {
        return Shop.of(decor.place(user.id(), parse(item)));
    }

    @DeleteMapping("/api/v1/decor/{item}/placement")
    Shop remove(TelegramUser user, @PathVariable String item) {
        return Shop.of(decor.remove(user.id(), parse(item)));
    }

    private static DecorItem parse(String item) {
        return DecorItem.byId(item)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown decor item: " + item));
    }
}
