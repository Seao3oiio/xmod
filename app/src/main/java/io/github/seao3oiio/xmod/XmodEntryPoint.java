package io.github.seao3oiio.xmod;

import io.github.seao3oiio.xmod.features.globalsearch.GlobalSearchGoogleFeature;
import io.github.seao3oiio.xmod.features.guitartuna.GuitarTunaPopupFeature;
import io.github.seao3oiio.xmod.features.guitartuna.GuitarTunaRecoveryFeature;
import io.github.seao3oiio.xmod.features.guitartuna.GuitarTunaRestartHostFeature;
import io.github.seao3oiio.xmod.features.browser.ChromeRedirectFeature;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class XmodEntryPoint implements IXposedHookLoadPackage {
    private static final String TAG = "xmod";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        switch (loadPackageParam.packageName) {
            case "com.ovelin.guitartuna":
                install(loadPackageParam, "GuitarTuna popup", GuitarTunaPopupFeature::new);
                install(loadPackageParam, "GuitarTuna recovery", GuitarTunaRecoveryFeature::new);
                break;
            case "com.android.launcher":
                install(loadPackageParam, "GuitarTuna restart host",
                        GuitarTunaRestartHostFeature::new);
                break;
            case "com.heytap.quicksearchbox":
            case "com.oplus.quicksearchbox":
            case "com.oppo.quicksearchbox":
                install(loadPackageParam, "Global Search", GlobalSearchGoogleFeature::new);
                break;
            case "com.heytap.browser":
            case "com.oplus.browser":
            case "com.coloros.browser":
                install(loadPackageParam, "browser redirect", ChromeRedirectFeature::new);
                break;
            default:
                break;
        }
    }

    private static void install(
            XC_LoadPackage.LoadPackageParam loadPackageParam,
            String label,
            FeatureFactory factory
    ) {
        try {
            XmodFeature feature = factory.create();
            if (!feature.supports(loadPackageParam)) {
                return;
            }
            feature.install(loadPackageParam);
            XposedBridge.log(TAG + ": enabled " + feature.name()
                    + " for " + loadPackageParam.packageName
                    + " process=" + loadPackageParam.processName);
        } catch (Throwable throwable) {
            XposedBridge.log(TAG + ": failed to install " + label
                    + " for " + loadPackageParam.packageName);
            XposedBridge.log(throwable);
        }
    }

    private interface FeatureFactory {
        XmodFeature create();
    }
}
