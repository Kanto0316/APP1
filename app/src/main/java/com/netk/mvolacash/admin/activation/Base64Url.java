package com.netk.mvolacash.admin.activation;

import java.io.ByteArrayOutputStream;

/** Minimal RFC 4648 Base64URL codec, kept API-23 compatible and padding-free. */
final class Base64Url {
    private static final char[] ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_".toCharArray();

    private Base64Url() {}

    static String encode(byte[] input) {
        StringBuilder out = new StringBuilder((input.length * 4 + 2) / 3);
        for (int i = 0; i < input.length; i += 3) {
            int value = (input[i] & 255) << 16;
            int remaining = input.length - i;
            if (remaining > 1) value |= (input[i + 1] & 255) << 8;
            if (remaining > 2) value |= input[i + 2] & 255;
            out.append(ALPHABET[(value >>> 18) & 63]);
            out.append(ALPHABET[(value >>> 12) & 63]);
            if (remaining > 1) out.append(ALPHABET[(value >>> 6) & 63]);
            if (remaining > 2) out.append(ALPHABET[value & 63]);
        }
        return out.toString();
    }

    static byte[] decode(String input) {
        if (input.isEmpty() || input.length() % 4 == 1) {
            throw new IllegalArgumentException("Base64URL incorrect");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(input.length() * 3 / 4);
        int value = 0;
        int bits = 0;
        for (int i = 0; i < input.length(); i++) {
            int digit = digit(input.charAt(i));
            if (digit < 0) throw new IllegalArgumentException("Base64URL incorrect");
            value = (value << 6) | digit;
            bits += 6;
            if (bits >= 8) {
                bits -= 8;
                out.write((value >>> bits) & 255);
            }
        }
        if (bits > 0 && (value & ((1 << bits) - 1)) != 0) {
            throw new IllegalArgumentException("Base64URL non canonique");
        }
        return out.toByteArray();
    }

    private static int digit(char c) {
        if (c >= 'A' && c <= 'Z') return c - 'A';
        if (c >= 'a' && c <= 'z') return c - 'a' + 26;
        if (c >= '0' && c <= '9') return c - '0' + 52;
        if (c == '-') return 62;
        if (c == '_') return 63;
        return -1;
    }
}
