package io.github.seao3oiio.xmod.features.guitartuna;

final class GuitarTunaRestartContract {
    static final String ACTION =
            "io.github.seao3oiio.xmod.action.RESTART_GUITARTUNA";
    static final String ACTION_INPUT_TAP =
            "io.github.seao3oiio.xmod.action.GUITARTUNA_INPUT_TAP";
    static final String EXTRA_X_RATIO = "x_ratio";
    static final String EXTRA_Y_RATIO = "y_ratio";
    static final String HOST_PACKAGE = "com.android.launcher";
    static final String TARGET_PACKAGE = "com.ovelin.guitartuna";
    static final String SENDER_PERMISSION =
            "com.ovelin.guitartuna.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION";
    static final String TARGET_ACTIVITY =
            "com.yousician.yousiciannative.GuitarTunaActivity";
    static final long RESTART_DELAY_MILLIS = 1_000L;

    private GuitarTunaRestartContract() {
    }
}
