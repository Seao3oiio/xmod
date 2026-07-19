package io.github.seao3oiio.xmod.features.guitartuna;

import android.content.SharedPreferences;

import io.github.seao3oiio.xmod.XmodFeature;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class GuitarTunaPopupFeature implements XmodFeature {
    private static final String TAG = "xmod/GuitarTuna";
    private static final String TARGET_PACKAGE = "com.ovelin.guitartuna";
    private static final String LAST_DISPLAYED_KEY =
            "LastSessionIndexPopupConversionDisplayed";
    private static final String CURRENT_SESSION_KEY =
            "PreviousAppStartupSessionIndex";

    @Override
    public String name() {
        return "GuitarTuna popup suppressor";
    }

    @Override
    public boolean supports(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        return TARGET_PACKAGE.equals(loadPackageParam.packageName)
                && TARGET_PACKAGE.equals(loadPackageParam.processName);
    }

    @Override
    public void install(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        hookPopupMarkerReads();
    }

    private static void hookPopupMarkerReads() {
        XposedHelpers.findAndHookMethod(
                "android.app.SharedPreferencesImpl",
                null,
                "getInt",
                String.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) {
                        if (!LAST_DISPLAYED_KEY.equals(param.args[0])) {
                            return;
                        }

                        try {
                            returnCurrentSession(param);
                        } catch (Throwable throwable) {
                            XposedBridge.log(TAG + ": failed to intercept popup marker read");
                            XposedBridge.log(throwable);
                        }
                    }
                }
        );
    }

    private static void returnCurrentSession(XC_MethodHook.MethodHookParam param) {
        SharedPreferences preferences = (SharedPreferences) param.thisObject;
        if (!preferences.contains(CURRENT_SESSION_KEY)) {
            return;
        }

        int storedLastDisplayed = param.getResult() instanceof Integer
                ? (Integer) param.getResult()
                : (Integer) param.args[1];
        int currentSession = preferences.getInt(
                CURRENT_SESSION_KEY,
                storedLastDisplayed
        );

        param.setResult(currentSession);
        XposedBridge.log(
                TAG + ": suppressed popup marker read; currentSession=" + currentSession
        );
    }
}
