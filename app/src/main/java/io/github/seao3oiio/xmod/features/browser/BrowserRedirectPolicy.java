package io.github.seao3oiio.xmod.features.browser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

final class BrowserRedirectPolicy {
    private static final Set<String> OEM_BROWSER_PACKAGES = new HashSet<>(Arrays.asList(
            "com.heytap.browser",
            "com.oplus.browser",
            "com.coloros.browser"
    ));

    private BrowserRedirectPolicy() {
    }

    static boolean supportsPackage(String packageName) {
        return OEM_BROWSER_PACKAGES.contains(packageName);
    }

    static boolean isWebScheme(String scheme) {
        if (scheme == null) {
            return false;
        }
        String normalized = scheme.toLowerCase(Locale.ROOT);
        return "http".equals(normalized) || "https".equals(normalized);
    }
}
