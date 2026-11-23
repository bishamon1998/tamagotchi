package com.tamagotchi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Builds Mini App launch data (initData) signed the way Telegram signs it. */
final class TelegramLaunchData {

    private TelegramLaunchData() {
    }

    /** HMAC of the sorted fields, with a key derived from the bot token. */
    static String signed(String botToken, String userJson, Instant authDate) {
        Map<String, String> fields = new TreeMap<>(Map.of(
                "auth_date", String.valueOf(authDate.getEpochSecond()),
                "query_id", "AAH-test",
                "signature", "third-party-signature",
                "user", userJson));
        String dataCheckString = fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + field.getValue())
                .collect(Collectors.joining("\n"));
        byte[] secret = hmac("WebAppData".getBytes(StandardCharsets.UTF_8), botToken);
        fields.put("hash", HexFormat.of().formatHex(hmac(secret, dataCheckString)));
        return fields.entrySet().stream()
                .map(field -> field.getKey() + "=" + URLEncoder.encode(field.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    private static byte[] hmac(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
