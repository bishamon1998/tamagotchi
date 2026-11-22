package com.tamagotchi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Runs the Telegram bot with all game modules in one process. */
@SpringBootApplication
public class TamagotchiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TamagotchiApplication.class, args);
    }
}
