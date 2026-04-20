package com.leadelmarche.common;

import java.text.Normalizer;
import java.util.Locale;

public final class SearchNormalizer {
    private SearchNormalizer() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String nfd = Normalizer.normalize(text, Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT).trim();
    }

    public static boolean containsNormalized(String source, String partial) {
        String normalizedSource = normalize(source);
        String normalizedPartial = normalize(partial);
        if (normalizedPartial.isBlank()) {
            return true;
        }
        return normalizedSource.contains(normalizedPartial);
    }
}

