package io.github.seao3oiio.xmod.features.globalsearch;

import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

final class SearchUrlRewriter {
    private static final String GOOGLE_SEARCH = "https://www.google.com/search?q=";
    private static final String[] QUERY_KEYS = {
            "wd", "word", "q", "query", "keyword", "key"
    };

    private SearchUrlRewriter() {
    }

    static String rewrite(String originalUrl) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }

        final URI uri;
        try {
            uri = URI.create(originalUrl);
        } catch (IllegalArgumentException ignored) {
            return originalUrl;
        }

        String scheme = lower(uri.getScheme());
        String host = lower(uri.getHost());
        String path = uri.getPath() == null ? "" : uri.getPath();
        if (!("http".equals(scheme) || "https".equals(scheme)) || host == null) {
            return originalUrl;
        }

        if (!(isBaiduSearch(host, path) || isSogouSearch(host, path))) {
            return originalUrl;
        }

        String query = findQuery(uri.getRawQuery());
        if (query == null || query.trim().isEmpty()) {
            return originalUrl;
        }

        String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return GOOGLE_SEARCH + encoded;
    }

    private static boolean isBaiduSearch(String host, String path) {
        if (!(host.equals("baidu.com")
                || host.equals("www.baidu.com")
                || host.equals("m.baidu.com")
                || host.equals("wap.baidu.com"))) {
            return false;
        }
        return path.equals("/s")
                || path.endsWith("/s")
                || path.startsWith("/search/");
    }

    private static boolean isSogouSearch(String host, String path) {
        if (!(host.equals("sogou.com")
                || host.equals("www.sogou.com")
                || host.equals("m.sogou.com")
                || host.equals("wap.sogou.com"))) {
            return false;
        }
        String loweredPath = path.toLowerCase(Locale.ROOT);
        return loweredPath.equals("/web")
                || loweredPath.startsWith("/web/")
                || loweredPath.contains("searchlist");
    }

    private static String findQuery(String rawQuery) {
        if (rawQuery == null || rawQuery.isEmpty()) {
            return null;
        }

        String[] fields = rawQuery.split("&");
        for (String wantedKey : QUERY_KEYS) {
            for (String field : fields) {
                int separator = field.indexOf('=');
                String rawKey = separator >= 0 ? field.substring(0, separator) : field;
                String key = decode(rawKey);
                if (!wantedKey.equalsIgnoreCase(key)) {
                    continue;
                }

                String rawValue = separator >= 0 ? field.substring(separator + 1) : "";
                return decode(rawValue);
            }
        }
        return null;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ignored) {
            return value;
        }
    }

    private static String lower(String value) {
        return value == null ? null : value.toLowerCase(Locale.ROOT);
    }
}
