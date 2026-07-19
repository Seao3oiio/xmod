package io.github.seao3oiio.xmod;

import io.github.seao3oiio.xmod.features.globalsearch.GlobalSearchGoogleFeature;
import io.github.seao3oiio.xmod.features.guitartuna.GuitarTunaPopupFeature;
import io.github.seao3oiio.xmod.features.browser.ChromeRedirectFeature;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public final class XmodEntryPoint implements IXposedHookLoadPackage {
    private static final String TAG = "xmod";
    private static final XmodFeature[] FEATURES = {
            new GuitarTunaPopupFeature(),
            new GlobalSearchGoogleFeature(),
            new ChromeRedirectFeature()
    };

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        for (XmodFeature feature : FEATURES) {
            if (!feature.supports(loadPackageParam)) {
                continue;
            }

            try {
                feature.install(loadPackageParam);
                XposedBridge.log(TAG + ": enabled " + feature.name()
                        + " for " + loadPackageParam.packageName
                        + " process=" + loadPackageParam.processName);
            } catch (Throwable throwable) {
                XposedBridge.log(TAG + ": failed to install " + feature.name()
                        + " for " + loadPackageParam.packageName);
                XposedBridge.log(throwable);
            }
        }
    }
}
