package com.tamagotchi.pet;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tamagotchi.miniapp.TelegramUser;

/** The signed-in user's coins. Loading the wallet counts as the daily visit and pays the daily bonus. */
@RestController
class WalletController {

    /** {@code dailyBonus} is what this visit paid: 0 after the first visit of the day. */
    record WalletView(int coins, int streak, int dailyBonus, int treatPrice) {
    }

    private final WalletService wallets;

    WalletController(WalletService wallets) {
        this.wallets = wallets;
    }

    @GetMapping("/api/v1/wallet")
    WalletView wallet(TelegramUser user) {
        DailyCheckIn checkIn = wallets.checkIn(user.id());
        Wallet wallet = checkIn.wallet();
        return new WalletView(wallet.coins(), wallet.streak(), checkIn.bonus(), PetRules.TREAT_PRICE);
    }
}
