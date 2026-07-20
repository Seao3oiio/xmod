package io.github.seao3oiio.xmod.features.globalsearch;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class GoogleSearchUrl {
    private static final String SEARCH_PREFIX = "https://www.google.com/search?q=";

    private GoogleSearchUrl() {
    }

    static String build(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8.name())
                    .replace("+", "%20");
            return SEARCH_PREFIX + encoded;
        } catch (UnsupportedEncodingException impossible) {
            throw new AssertionError(impossible);
        }
    }
}
