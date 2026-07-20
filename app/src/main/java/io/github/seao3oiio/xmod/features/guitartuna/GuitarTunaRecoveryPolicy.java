package io.github.seao3oiio.xmod.features.guitartuna;

final class GuitarTunaRecoveryPolicy {
    static final long MIN_VERSION_CODE = 150_917L;
    static final long COOLDOWN_MILLIS = 24L * 60L * 60L * 1_000L;
    private static final double MAX_NORMALIZED_RMSE = 0.02;

    private GuitarTunaRecoveryPolicy() {
    }

    static boolean supports(long versionCode) {
        return versionCode >= MIN_VERSION_CODE;
    }

    static boolean canRecover(long lastRecoveryAt, long now) {
        if (lastRecoveryAt <= 0L) {
            return true;
        }
        return now >= lastRecoveryAt
                && now - lastRecoveryAt >= COOLDOWN_MILLIS;
    }

    static boolean matchesSplash(byte[] observedRgb, byte[] templateRgb) {
        return normalizedRmse(observedRgb, templateRgb) <= MAX_NORMALIZED_RMSE;
    }

    static double normalizedRmse(byte[] observedRgb, byte[] templateRgb) {
        if (observedRgb == null
                || templateRgb == null
                || observedRgb.length == 0
                || observedRgb.length != templateRgb.length) {
            return Double.POSITIVE_INFINITY;
        }

        double squaredError = 0.0;
        for (int index = 0; index < observedRgb.length; index++) {
            int observed = observedRgb[index] & 0xff;
            int expected = templateRgb[index] & 0xff;
            double difference = observed - expected;
            squaredError += difference * difference;
        }
        double rmse = Math.sqrt(squaredError / observedRgb.length) / 255.0;
        return rmse;
    }
}
