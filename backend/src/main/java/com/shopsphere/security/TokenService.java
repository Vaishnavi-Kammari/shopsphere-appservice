package com.shopsphere.security;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Issues and verifies simple HMAC-SHA256 signed tokens of the form  base64url(email|expiry).signature
 * Stateless: the server only needs the TOKEN_SECRET to validate them.
 */
@Component
public class TokenService {
    private final byte[] secret;
    private final long expirySeconds;

    public TokenService(@Value("${app.token-secret}") String secret,
                        @Value("${app.token-expiry-hours}") long expiryHours) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("TOKEN_SECRET must be at least 32 characters long");
        }
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirySeconds = expiryHours * 3600;
    }

    public String create(String email) {
        long expiresAt = Instant.now().getEpochSecond() + expirySeconds;
        String payload = Base64.getUrlEncoder().withoutPadding()
                .encodeToString((email + "|" + expiresAt).getBytes(StandardCharsets.UTF_8));
        return payload + "." + sign(payload);
    }

    /** Returns the email inside a valid, unexpired token; empty otherwise. */
    public Optional<String> parse(String token) {
        try {
            int dot = token.indexOf('.');
            if (dot < 1) return Optional.empty();
            String payload = token.substring(0, dot);
            String signature = token.substring(dot + 1);
            boolean ok = MessageDigest.isEqual(
                    sign(payload).getBytes(StandardCharsets.UTF_8),
                    signature.getBytes(StandardCharsets.UTF_8));
            if (!ok) return Optional.empty();
            String decoded = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            int bar = decoded.lastIndexOf('|');
            if (bar < 1) return Optional.empty();
            long expiresAt = Long.parseLong(decoded.substring(bar + 1));
            if (expiresAt < Instant.now().getEpochSecond()) return Optional.empty();
            return Optional.of(decoded.substring(0, bar));
        } catch (RuntimeException e) {
            return Optional.empty();
        }
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Unable to sign token", e);
        }
    }
}
