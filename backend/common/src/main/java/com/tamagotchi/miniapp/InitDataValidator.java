package com.tamagotchi.miniapp;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.tamagotchi.telegram.TelegramProperties;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/**
 * Checks that Mini App launch data (initData) was signed by Telegram for this bot and is recent.
 * Algorithm: https://core.telegram.org/bots/webapps#validating-data-received-via-the-mini-app
 */
@Component
public class InitDataValidator {

    private static final String HMAC = "HmacSHA256";
    private static final byte[] WEB_APP_KEY = "WebAppData".getBytes(StandardCharsets.UTF_8);

    private final byte[] secret;
    private final TelegramProperties properties;
    private final JsonMapper json;
    private final Clock clock;

    InitDataValidator(TelegramProperties properties, JsonMapper json, Clock clock) {
        this.properties = properties;
        this.json = json;
        this.clock = clock;
        this.secret = hmac(WEB_APP_KEY, properties.botToken().getBytes(StandardCharsets.UTF_8));
    }

    /** The user from the launch data, or empty if the data is not signed by Telegram or too old. */
    public Optional<TelegramUser> validate(String initData) {
        Map<String, String> fields = parse(initData);
        String hash = fields.remove("hash");
        if (hash == null || !signatureMatches(fields, hash) || isExpired(fields.get("auth_date"))) {
            return Optional.empty();
        }
        return Optional.ofNullable(fields.get("user")).map(this::user);
    }

    private boolean signatureMatches(Map<String, String> fields, String hash) {
        String dataCheckString = fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + field.getValue())
                .collect(Collectors.joining("\n"));
        byte[] expected = hmac(secret, dataCheckString.getBytes(StandardCharsets.UTF_8));
        byte[] actual;
        try {
            actual = HexFormat.of().parseHex(hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return MessageDigest.isEqual(expected, actual);
    }

    private boolean isExpired(String authDate) {
        if (authDate == null || !authDate.chars().allMatch(Character::isDigit)) {
            return true;
        }
        Instant signedAt = Instant.ofEpochSecond(Long.parseLong(authDate));
        return signedAt.plus(properties.initDataMaxAge()).isBefore(clock.instant());
    }

    private TelegramUser user(String userJson) {
        JsonNode user = json.readTree(userJson);
        return new TelegramUser(user.path("id").asLong(), user.path("first_name").asString(),
                user.path("language_code").asString(null));
    }

    /** Sorted by key, as the data-check-string requires. */
    private static Map<String, String> parse(String initData) {
        Map<String, String> fields = new TreeMap<>();
        for (String pair : initData.split("&")) {
            int separator = pair.indexOf('=');
            if (separator > 0) {
                fields.put(decode(pair.substring(0, separator)), decode(pair.substring(separator + 1)));
            }
        }
        return fields;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private static byte[] hmac(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance(HMAC);
            mac.init(new SecretKeySpec(key, HMAC));
            return mac.doFinal(data);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC-SHA256 is not available", e);
        }
    }
}
