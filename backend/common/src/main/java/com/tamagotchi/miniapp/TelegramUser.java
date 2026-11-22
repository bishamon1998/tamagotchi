package com.tamagotchi.miniapp;

/** The Telegram user who opened the Mini App, taken from verified launch data. */
public record TelegramUser(long id, String firstName, String languageCode) {
}
