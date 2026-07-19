package io.github.seao3oiio.xmod.features.browser;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class BrowserRedirectPolicyTest {
    @Test
    public void recognizesSupportedOemBrowserPackages() {
        assertTrue(BrowserRedirectPolicy.supportsPackage("com.heytap.browser"));
        assertTrue(BrowserRedirectPolicy.supportsPackage("com.oplus.browser"));
        assertTrue(BrowserRedirectPolicy.supportsPackage("com.coloros.browser"));
        assertFalse(BrowserRedirectPolicy.supportsPackage("com.android.chrome"));
    }

    @Test
    public void redirectsOnlyHttpAndHttpsSchemes() {
        assertTrue(BrowserRedirectPolicy.isWebScheme("http"));
        assertTrue(BrowserRedirectPolicy.isWebScheme("HTTPS"));
        assertFalse(BrowserRedirectPolicy.isWebScheme("file"));
        assertFalse(BrowserRedirectPolicy.isWebScheme("intent"));
        assertFalse(BrowserRedirectPolicy.isWebScheme(null));
    }
}
