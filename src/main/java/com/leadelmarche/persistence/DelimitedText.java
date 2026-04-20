package com.leadelmarche.persistence;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public final class DelimitedText {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private DelimitedText() {
    }

    public static String encode(List<String> fields) {
        List<String> encoded = new ArrayList<>(fields.size());
        for (String field : fields) {
            String safe = field == null ? "" : field;
            encoded.add(ENCODER.encodeToString(safe.getBytes(StandardCharsets.UTF_8)));
        }
        return String.join("|", encoded);
    }

    public static List<String> decode(String line) {
        String[] parts = line.split("\\|", -1);
        List<String> decoded = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isBlank()) {
                decoded.add("");
            } else {
                decoded.add(new String(DECODER.decode(part), StandardCharsets.UTF_8));
            }
        }
        return decoded;
    }
}

