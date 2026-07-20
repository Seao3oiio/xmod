package io.github.seao3oiio.xmod.features.guitartuna;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class GuitarTunaRecoveryPolicyTest {
    @Test
    public void supportsVerifiedAndNewerVersions() {
        assertTrue(GuitarTunaRecoveryPolicy.supports(150_917L));
        assertTrue(GuitarTunaRecoveryPolicy.supports(150_918L));
        assertTrue(GuitarTunaRecoveryPolicy.supports(200_000L));
        assertFalse(GuitarTunaRecoveryPolicy.supports(150_916L));
    }

    @Test
    public void limitsRecoveryToOncePerDay() {
        long now = 2L * GuitarTunaRecoveryPolicy.COOLDOWN_MILLIS;
        assertTrue(GuitarTunaRecoveryPolicy.canRecover(0L, now));
        assertFalse(GuitarTunaRecoveryPolicy.canRecover(now - 1_000L, now));
        assertTrue(GuitarTunaRecoveryPolicy.canRecover(
                now - GuitarTunaRecoveryPolicy.COOLDOWN_MILLIS,
                now
        ));
        assertFalse(GuitarTunaRecoveryPolicy.canRecover(now + 1_000L, now));
    }

    @Test
    public void splashMatchUsesNormalizedRgbDistance() {
        byte[] template = {0, 64, (byte) 255, 32, 48, 64};
        byte[] close = {4, 60, (byte) 250, 35, 45, 68};
        byte[] different = {(byte) 255, 0, 0, (byte) 255, 0, 0};
        assertTrue(GuitarTunaRecoveryPolicy.matchesSplash(close, template));
        assertFalse(GuitarTunaRecoveryPolicy.matchesSplash(different, template));
        assertFalse(GuitarTunaRecoveryPolicy.matchesSplash(new byte[1], template));
    }
}
